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
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import me.aeolwyr.dsnnow.R;

/**
 * Activity to show the cached and pinned state files, and cache/pin them if the user requests.
 */
public class HistoryActivity extends Activity {
    /**
     * intent extra name for the chosen history item, refers to a path string
     */
    public static final String CHOSEN_FILE = "me.aeolwyr.dsnnow.CHOSEN_FILE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(new HistoryAdapter(this));

        listView.setOnItemClickListener(onItemClickListener);
    }

    /**
     * on click listener to go back to the main activity with the chosen item
     */
    private AdapterView.OnItemClickListener onItemClickListener =
            new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String path = parent.getItemAtPosition(position).toString();
            Intent intent = new Intent();
            intent.putExtra(CHOSEN_FILE, path);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };
}
