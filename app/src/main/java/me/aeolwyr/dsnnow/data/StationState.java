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

package me.aeolwyr.dsnnow.data;

import java.util.Set;
import java.util.TreeSet;

/**
 * Data structure to hold the temporary information about a station,
 * parsed from a <code>dsn.xml</code> file.
 */
public class StationState {
    private String name;
    private String friendlyName;
    /** in milliseconds **/
    private int timeZoneOffset;
    private Set<String> dishStates = new TreeSet<>();

    /**
     * Create a new station state with the given values.
     * @param name the name of the station, e.g. <code>gdscc</code>
     * @param friendlyName the friendly (full) name of the station, e.g. <code>Goldstone</code>
     * @param timeZoneOffset the current time zone offset of the station state, in milliseconds
     */
    public StationState(String name, String friendlyName, int timeZoneOffset) {
        this.name = name;
        this.friendlyName = friendlyName;
        this.timeZoneOffset = timeZoneOffset;
    }

    /**
     * Return the name of this station, e.g. <code>cdscc</code>.
     * @return the name of the station
     */
    public String getName() {
        return name;
    }

    /**
     * Return the friendly (full) name of this station, e.g. <code>Canberra</code>
     * @return the friendly name of the station
     */
    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * Return the current time zone offset of this station, in milliseconds.
     * @return the time zone offset of the station
     */
    public int getTimeZoneOffset() {
        return timeZoneOffset;
    }

    /**
     * Return the states of the dished located in this station,
     * ordered alphabetically.
     * @return the states of the dishes located in the station
     */
    public Set<String> getDishStates() {
        return dishStates;
    }

    // XML tag/attribute names
    public static final String STATION = "station";
    public static final String NAME = "name";
    public static final String FRIENDLY_NAME = "friendlyName";
    public static final String TIME_ZONE_OFFSET = "timeZoneOffset";
}
