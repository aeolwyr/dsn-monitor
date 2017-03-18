/*
 * DSN Monitor is an app to monitor the NASA Deep Space Network in real time.
 * Copyright (c) 2016 Kaan Karaagacli
 *
 * This file is part of DSN Monitor.
 *
 * DSN Monitor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DSN Monitor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DSN Monitor.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.aeolwyr.dsnnow.presentation;

import android.app.Activity;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import me.aeolwyr.dsnnow.R;
import me.aeolwyr.dsnnow.data.NetworkConfig;
import me.aeolwyr.dsnnow.data.NetworkState;
import me.aeolwyr.dsnnow.logic.ConfigParser;
import me.aeolwyr.dsnnow.logic.StateParserTask;

/**
 * Activity that presents the information inside a state file to the user.
 * In addition, it may continuously download the latest network state.
 */
public class MainActivity extends Activity {
    private NetworkConfig config;
    /** adapter of the list view, null if the list view is not ready yet **/
    private NetworkAdapter adapter;
    /** timer for continuous downloading **/
    private Timer timer;
    private Menu menu;
    private static final DateFormat dateTimeFormat =
            DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.getDefault());

    /** true if continuous download is active, false if paused **/
    private boolean monitoring = true;
    /** used to make sure the monitor error is shown once **/
    private boolean monitorErrorShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_loading);

        // it could be better to download this file
        // however, it is never changed, therefore it is cached inside the app
        try (XmlResourceParser xml = getResources().getXml(R.xml.config)) {
            config = ConfigParser.parse(xml);
        } catch (XmlPullParserException | IOException e) {
            // the application cannot continue without a NetworkConfig
            throw new RuntimeException(e);
        }

        // load the previous state if available
        if (savedInstanceState != null) {
            long timestamp = savedInstanceState.getLong("timestamp");
            if (timestamp > 0) {
                File inputFile = findFileByTimestamp(timestamp);
                if (inputFile != null) {
                    openFile(inputFile);
                }
            }

            monitoring = savedInstanceState.getBoolean("monitoring");
            monitorErrorShown = savedInstanceState.getBoolean("monitorErrorShown");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (adapter != null) {
            outState.putLong("timestamp", adapter.getNetworkState().getTimestamp());
        }
        outState.putBoolean("monitoring", monitoring);
        outState.putBoolean("monitorErrorShown", monitorErrorShown);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // continue monitoring if it was active before the app has paused
        if (monitoring) {
            startTimer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // stop downloads, but do not reset the monitoring status
        stopTimer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // keep a reference to use later
        this.menu = menu;
        updateMenuButtons();
        return true;
    }

    // request codes for intent results
    private static final int OPEN_FILE_REQUEST_CODE = 1;
    private static final int SAVE_FILE_REQUEST_CODE = 2;
    private static final int OPEN_HISTORY_REQUEST_CODE = 3;
    private static final int OPEN_PREFS_REQUEST_CODE = 4;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_monitor: {
                // start monitoring
                monitoring = true;
                monitorErrorShown = false;
                startTimer();
                return true;
            }
            case R.id.action_pause: {
                // stop monitoring
                monitoring = false;
                stopTimer();
                return true;
            }
            case R.id.action_open: {
                // show file chooser to open
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("text/xml");
                startActivityForResult(intent, OPEN_FILE_REQUEST_CODE);
                return true;
            }
            case R.id.action_save: {
                // show file chooser to save
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.setType("text/xml");
                startActivityForResult(intent, SAVE_FILE_REQUEST_CODE);
                return true;
            }
            case R.id.action_history: {
                // show history activity
                Intent intent = new Intent(this, HistoryActivity.class);
                startActivityForResult(intent, OPEN_HISTORY_REQUEST_CODE);
                return true;
            }
            case R.id.action_settings: {
                // show settings activity
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, OPEN_PREFS_REQUEST_CODE);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // this function is called when another activity is comes back to this activity
        // with a result
        if (resultCode == Activity.RESULT_OK && resultData != null) {
            switch (requestCode) {
                case OPEN_FILE_REQUEST_CODE: {
                    // file to open is chosen successfully
                    Uri uri = resultData.getData();
                    openFile(uri);
                    break;
                }
                case SAVE_FILE_REQUEST_CODE: {
                    // file to save is chosen successfully
                    Uri uri = resultData.getData();
                    saveFile(adapter.getNetworkState().getTimestamp(), uri);
                    break;
                }
                case OPEN_HISTORY_REQUEST_CODE: {
                    // history file to open is chosen successfully
                    String path = resultData.getStringExtra(HistoryActivity.CHOSEN_FILE);
                    openFile(new File(path));
                    break;
                }
            }
        }
        if (requestCode == OPEN_PREFS_REQUEST_CODE && adapter != null) {
            // if the previous activity was settings, refresh the views
            // so that they they use the newly chosen units
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Set the network state to a new one. This function initializes the activity if necessary.
     * @param networkState network state to use in the network adapter
     */
    private void setNetworkState(NetworkState networkState) {
        if (adapter == null) {
            // discard the "loading" message, and replace it with the main layout
            setContentView(R.layout.activity_main);
            adapter = new NetworkAdapter(this, config);

            ExpandableListView expandableListView =
                    (ExpandableListView) findViewById(R.id.expandable_list_view);
            expandableListView.setAdapter(adapter);

            // enable save as there is now data to save
            menu.findItem(R.id.action_save).setEnabled(true);
        }
        adapter.setNetworkState(networkState);
        // also set the timestamp indicator
        TextView timestamp = (TextView) findViewById(R.id.timestamp);
        timestamp.setText(dateTimeFormat.format(networkState.getTimestamp()));
        // a new file is successfully loaded, which means the older errors are now invalid
        monitorErrorShown = false;
    }

    /**
     * Start the continuous downloads.
     */
    private void startTimer() {
        updateMenuButtons();

        int interval = PrefsManager.getCaptureInterval(this); // in seconds
        // create a new timer and run it
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                new StateParserTask(config, getCacheDir(), getFilesDir()) {
                    @Override
                    protected void onPostExecute(NetworkState networkState) {
                        if (monitoring) {
                            if (networkState != null) {
                                // state downloaded successfully
                                setNetworkState(networkState);
                                // this action resulted in one more cached file,
                                // make sure we are not over the limit
                                deleteOldFiles();
                            } else {
                                // state download unsuccessful
                                showMonitorError();
                            }
                        }
                    }
                }.execute();
            }
        }, 0, interval * 1000);
    }

    /**
     * Stop the continuous downloads and cancel the current one. <br />
     * It might be necessary to set the <code>monitoring</code> field to
     * false to make sure an ongoing download does not replace the current state
     * when finished.
     */
    private void stopTimer() {
        updateMenuButtons();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Show or hide the monitor and pause menu buttons according to the
     * <code>monitoring</code> field.
     */
    private void updateMenuButtons() {
        if (menu != null) {
            menu.findItem(R.id.action_monitor).setVisible(!monitoring);
            menu.findItem(R.id.action_pause).setVisible(monitoring);
        }
    }

    /**
     * Open the state file with the given URI and show it in the adapter.
     * @param uri the URI of the state file
     */
    private void openFile(Uri uri) {
        try {
            openFile(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            showOpenError();
        }
    }

    /**
     * Open the given state file and show it in the adapter.
     * @param file the state file
     */
    private void openFile(File file) {
        try {
            openFile(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            showOpenError();
        }
    }

    /**
     * Load a network state from the given stream, and show it in the adapter.
     * @param inputStream the input stream that contains a network state
     */
    private void openFile(InputStream inputStream) {
        // a file is manually opened, therefore stop the continuous downloads
        monitoring = false;
        stopTimer();
        new StateParserTask(config, getCacheDir(), getFilesDir()) {
            @Override
            protected void onPostExecute(NetworkState networkState) {
                if (networkState != null) {
                    setNetworkState(networkState);
                    // don't remove the old files here.
                    // the opened file is cached, and if the it is very old, it could be deleted
                    // which would prevent the save file function to work
                } else {
                    showOpenError();
                }
            }
        }.execute(inputStream);
    }

    /**
     * Save the network state with the given timestamp to another location.
     * @param timestamp the timestamp of the state file
     * @param destination the destination location to save to
     */
    private void saveFile(long timestamp, Uri destination) {
        try {
            // locate the file
            File inputFile = findFileByTimestamp(timestamp);

            OutputStream outputStream = getContentResolver().openOutputStream(destination);
            // continue if the file is located successfully, and the output stream is ready
            if (inputFile != null && outputStream != null) {
                WritableByteChannel outputChannel = Channels.newChannel(outputStream);

                // copy the file from the cache to the destination
                try (FileInputStream inputStream = new FileInputStream(inputFile)) {
                    FileChannel inputChannel = inputStream.getChannel();
                    inputChannel.transferTo(0, inputChannel.size(), outputChannel);
                }

                outputStream.close();
                Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (IOException ignored) { }
        // if an exception has occurred, or the input file is not found, show an error message
        Toast.makeText(this, R.string.save_error, Toast.LENGTH_SHORT).show();
    }

    /**
     * Locate a file with the given timestamp across the cached and the pinned files.
     * @param timestamp the timestamp of the wanted file
     * @return the file if found, or null if not found
     */
    private File findFileByTimestamp(long timestamp) {
        String filename = timestamp + ".xml";
        File cachedFile = new File(getCacheDir(), filename);
        File pinnedFile = new File(getFilesDir(), filename);

        if (cachedFile.exists()) {
            return cachedFile;
        } else if (pinnedFile.exists()) {
            return pinnedFile;
        } else {
            return null;
        }
    }

    /**
     * Purge the old files from the cache.
     */
    private void deleteOldFiles() {
        File[] files = getCacheDir().listFiles(STATE_FILE_FILTER);
        int keep = PrefsManager.getHistorySize(this);

        // use tree set so that they are sorted according to their names,
        // which should be timestamps (e.g. 1452507050.xml)
        Collection<File> filesSet = new TreeSet<>();
        Collections.addAll(filesSet, files);

        int numberOfFilesToRemove = filesSet.size() - keep;

        // first files in the iterator are the oldest files
        Iterator<File> iterator = filesSet.iterator();
        for (int i = 0; i < numberOfFilesToRemove; i++) {
            // these files are created by this application,
            // and they reside in a private folder.
            // there shouldn't be a problem deleting them
            // noinspection ResultOfMethodCallIgnored
            iterator.next().delete();
        }
    }

    /** filter that matches the cached/pinned files **/
    static final FilenameFilter STATE_FILE_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String filename) {
            return filename.matches("^\\d+\\.xml$");
        }
    };

    /**
     * Open error, to be used for open from file chooser and open from history actions.
     */
    private void showOpenError() {
        Toast.makeText(this, R.string.open_error, Toast.LENGTH_SHORT).show();
    }

    /**
     * Monitor error, to be used if a download is unsuccessful.
     */
    private void showMonitorError() {
        // show once, as if the connection is down the error message would be created continuously
        if (!monitorErrorShown) {
            Toast.makeText(this, R.string.monitor_error, Toast.LENGTH_SHORT).show();
            monitorErrorShown = true;
        }
    }

    /**
     * On click help to be used if the "show acronym help" is enabled.
     * @param view the view to show help about
     */
    public void showOnClickHelp(View view) {
        // show help according to their IDs
        switch (view.getId()) {
            case R.id.name:
                Toast.makeText(this, getString(R.string.help_DSCC), Toast.LENGTH_SHORT).show();
                return;
            case R.id.friendly_name:
                Toast.makeText(this, getString(R.string.help_DSS), Toast.LENGTH_SHORT).show();
                return;
            case R.id.type:
                TextView textView = (TextView) view;
                switch (textView.getText().toString()) {
                    case "34M":
                        Toast.makeText(this, R.string.help_34M, Toast.LENGTH_SHORT).show();
                        return;
                    case "34MHEF":
                        Toast.makeText(this, R.string.help_34MHEF, Toast.LENGTH_SHORT).show();
                        return;
                    case "70M":
                        Toast.makeText(this, R.string.help_70M, Toast.LENGTH_SHORT).show();
                        return;
                }
                return;
            case R.id.mspa:
                Toast.makeText(this, R.string.help_MSPA, Toast.LENGTH_SHORT).show();
                return;
            case R.id.ddor:
                Toast.makeText(this, R.string.help_DDOR, Toast.LENGTH_SHORT).show();
                return;
            case R.id.rtlt:
                Toast.makeText(this, R.string.help_RTLT, Toast.LENGTH_SHORT).show();
        }
    }
}