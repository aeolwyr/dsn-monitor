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

package me.aeolwyr.dsnnow.logic;

import android.os.AsyncTask;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.net.ssl.HttpsURLConnection;

import me.aeolwyr.dsnnow.data.NetworkConfig;
import me.aeolwyr.dsnnow.data.NetworkState;

/**
 * Async task that parses a network state. It is also capable of downloading the state
 * if necessary.
 */
public class StateParserTask extends AsyncTask<InputStream, Void, NetworkState> {
    /** maximum file size allowed, in bytes **/
    private static final int MAX_FILE_SIZE = 15360;
    /** download location **/
    private static final String FILE_LOCATION = "https://eyes.nasa.gov/dsn/data/dsn.xml?r=";

    private NetworkConfig config;
    private File cacheDir;
    private File filesDir;

    /**
     * Create a new parser task. <br />
     * Call the <code>execute()</code> function with a <code>InputStream</code> to parse an
     * available stream, or call it without arguments to download and parse the latest state. <br />
     * Note that if an input stream is given, it will be closed after reading.
     * @param config config file to use when parsing
     * @param cacheDir cache folder to cache the files if necessary
     * @param filesDir persistent storage folder to check when caching
     */
    public StateParserTask(NetworkConfig config, File cacheDir, File filesDir) {
        this.config = config;
        this.cacheDir = cacheDir;
        this.filesDir = filesDir;
    }

    /**
     * Main execution function of this class. Usually the <code>execute()</code> function
     * should be called instead.
     * @param params optional, input stream to parse
     * @return the parsed network state
     */
    @Override
    protected NetworkState doInBackground(InputStream... params) {
        try {
            // download if there is no input stream given
            InputStream inputStream = (params.length == 0) ? downloadState() : params[0];

            ByteBuffer byteBuffer = ByteBuffer.allocate(MAX_FILE_SIZE);

            // read file to buffer
            try (ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream)) {
                while (readableByteChannel.read(byteBuffer) >= 0) {
                    // make sure the input file is not very large
                    if (!byteBuffer.hasRemaining()) return null;
                }
                // inputStream is closed by the channel
            }

            // store the file size as the limit
            // this also makes sure the unused part of the buffer is inaccessible
            byteBuffer.limit(byteBuffer.position());

            // parse the buffer
            NetworkState networkState;
            try (InputStream parserInputStream =
                         new ByteArrayInputStream(byteBuffer.array(), 0, byteBuffer.limit())) {
                networkState = StateParser.parse(config, parserInputStream);
            }

            // if the file is already cached, it is in one of the locations below
            String filename = networkState.getTimestamp() + ".xml";
            File cachedFile = new File(cacheDir, filename);
            File pinnedFile = new File(filesDir, filename);

            // cache the file if not already cached
            if (!cachedFile.exists() && !pinnedFile.exists()) {
                byteBuffer.rewind();
                try (FileOutputStream fos = new FileOutputStream(cachedFile)) {
                    fos.getChannel().write(byteBuffer);
                }
            }

            return networkState;
        } catch (IOException | XmlPullParserException e) {
            return null;
        }
    }

    /**
     * Download the latest state.
     * @return input stream of the downloaded state
     * @throws IOException if a connection error has occurred
     */
    private static InputStream downloadState() throws IOException {
        // create a new URL each time to prevent hitting the CDN cache
        URL url = new URL(FILE_LOCATION + (System.currentTimeMillis() / 5000));
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setReadTimeout(5000);
        connection.setConnectTimeout(5000);
        connection.connect();
        return connection.getInputStream();
    }
}