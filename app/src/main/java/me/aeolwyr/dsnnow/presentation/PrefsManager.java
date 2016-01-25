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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Helper class to access the shared preferences easily.
 */
public class PrefsManager {
    /**
     * Return the user set capture interval preference (minimum 5), in seconds.
     * @param context context to read values from
     * @return the capture interval
     */
    public static int getCaptureInterval(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return Math.max(Integer.parseInt(sharedPref.getString("capture_interval", "5")), 5);
    }

    /**
     * Return the user set history size preference (minimum 1).
     * @param context context to read values from
     * @return the history size
     */
    public static int getHistorySize(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return Math.max(Integer.parseInt(sharedPref.getString("history_size", "100")), 1);
    }

    /**
     * Return the user set acronym help preference.
     * @param context context to read values from
     * @return true if the acronym help is enabled, false otherwise
     */
    public static boolean isAcronymHelpEnabled(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean("show_acronym_help", false);
    }

    /**
     * Return the user set wind speed unit. One of these values are returned:
     * <ol>
     * <li>kilometers per hour</li>
     * <li>miles per hour</li>
     * <li>meters per second</li>
     * </ol>
     * @param context context to read values from
     * @return the wind speed unit
     */
    public static String getWindSpeedUnit(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString("wind_speed_unit", "1");
    }

    /**
     * Return the user set data rate unit. One of these values are returned:
     * <ol>
     * <li>bits per second</li>
     * <li>bytes per second</li>
     * </ol>
     * @param context context to read values from
     * @return the data rate unit
     */
    public static String getDataRateUnit(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString("data_rate_unit", "1");
    }

    /**
     * Return the user set download power unit. One of these values are returned:
     * <ol>
     * <li>decibel-milliwatts</li>
     * <li>watts</li>
     * </ol>
     * @param context context to read values from
     * @return the download power unit
     */
    public static String getDownloadPowerUnit(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString("download_power_unit", "1");
    }

    /**
     * Return the user set upload power unit. One of these values are returned:
     * <ol>
     * <li>decibel-milliwatts</li>
     * <li>watts</li>
     * </ol>
     * @param context context to read values from
     * @return the upload power unit
     */
    public static String getUploadPowerUnit(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString("upload_power_unit", "2");
    }

    /**
     * Return the user set range unit. One of these values are returned:
     * <ol>
     * <li>kilometers</li>
     * <li>miles</li>
     * <li>astronomical units</li>
     * </ol>
     * @param context context to read values from
     * @return the range unit
     */
    public static String getRangeUnit(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString("range_unit", "1");
    }
}
