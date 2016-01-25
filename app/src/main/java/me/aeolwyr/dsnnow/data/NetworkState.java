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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Data structure to hold the temporary info about the whole network at a time instant,
 * parsed from a <code>dsn.xml</code> file.
 */
public class NetworkState {
    private Map<String, StationState> stationStates = new LinkedHashMap<>();
    private Map<String, DishState> dishStates = new HashMap<>();
    private long timestamp;

    /**
     * Set the time this state object represents.
     * @param timestamp timestamp, in milliseconds
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Return the timestamp this state object represents.
     * @return timestamp, in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Return the station states stored in this object, ordered same as they
     * appear in the xml file, and mapped by their names (e.g. <code>gdscc</code>).
     * @return the station states map, iterable
     */
    public Map<String, StationState> getStationStates() {
        return stationStates;
    }

    /**
     * Return the dish states stored in this object, in no particular order,
     * and mapped by their names (e.g. <code>DSS63</code>).
     * @return the dish states map
     */
    public Map<String, DishState> getDishStates() {
        return dishStates;
    }
}
