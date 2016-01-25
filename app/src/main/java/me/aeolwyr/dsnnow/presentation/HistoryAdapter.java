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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.aeolwyr.dsnnow.R;

import static me.aeolwyr.dsnnow.presentation.MainActivity.STATE_FILE_FILTER;

/**
 * Adapter to list the cached and pinned state files.
 */
public class HistoryAdapter extends BaseAdapter {
    private Context context;
    /** all the cached and pinned files combined **/
    private List<File> files = new ArrayList<>();
    /** pinned files only **/
    private Set<File> pinnedFiles = new HashSet<>();

    // for formatting purposes
    private static final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
    private static final DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);

    /**
     * Create a new history adapter on the given context. <br />
     * This constructor takes a snapshot of the current status of the cached and pinned files.
     * @param context the context of the activity
     */
    public HistoryAdapter(Context context) {
        this.context = context;

        // add cached files
        Collections.addAll(files, context.getCacheDir().listFiles(STATE_FILE_FILTER));

        // add pinned files to both data structures
        File[] pinned = context.getFilesDir().listFiles(STATE_FILE_FILTER);
        Collections.addAll(files, pinned);
        Collections.addAll(pinnedFiles, pinned);

        // combined data structure should be sorted according to the file name
        // which should be the timestamp (e.g. 1452507050.xml)
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
    }

    @Override
    public int getCount() {
        return files.size();
    }

    @Override
    public File getItem(int position) {
        return files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // inflate the view if necessary
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.history_item, parent, false);
        }

        File file = getItem(position);
        long timestamp = getTimestamp(file);

        // update date/time
        TextView date = (TextView) convertView.findViewById(R.id.date);
        TextView time = (TextView) convertView.findViewById(R.id.time);
        date.setText(dateFormat.format(timestamp));
        time.setText(timeFormat.format(timestamp));

        // update the button
        ImageButton pin = (ImageButton) convertView.findViewById(R.id.pin);
        pin.setImageResource(pinnedFiles.contains(file)
                ? R.drawable.ic_star
                : R.drawable.ic_star_outline);
        pin.setTag(position);
        pin.setOnClickListener(pinOnClickListener);

        return convertView;
    }

    /**
     * on click listener to pin/unpin the files
     */
    private View.OnClickListener pinOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag();
            File file = getItem(position);
            if(pinnedFiles.contains(file)) {
                // currently pinned, need to unpin
                File cachedFile = new File(context.getCacheDir(), file.getName());
                if (file.renameTo(cachedFile)) {
                    pinnedFiles.remove(file);
                    files.set(position, cachedFile);
                    notifyDataSetChanged();
                } else {
                    Toast.makeText(context, R.string.unpin_error, Toast.LENGTH_SHORT).show();
                }
            } else {
                // currently unpinned, need to pin
                File pinnedFile = new File(context.getFilesDir(), file.getName());
                if (file.renameTo(pinnedFile)) {
                    pinnedFiles.add(pinnedFile);
                    files.set(position, pinnedFile);
                    notifyDataSetChanged();
                } else {
                    Toast.makeText(context, R.string.pin_error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    /**
     * Extract timestamp from a file name.
     * @param file the file to read from
     * @return the timestamp of the file
     */
    private static long getTimestamp(File file) {
        // file name should be the timestamp (e.g. 1452507050.xml)
        int index = file.getName().lastIndexOf('.');
        return Long.valueOf(file.getName().substring(0, index));
    }
}