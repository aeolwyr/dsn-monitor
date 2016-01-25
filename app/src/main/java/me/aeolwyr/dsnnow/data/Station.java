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

import java.util.HashSet;
import java.util.Set;

/**
 * Data structure to hold the persistent information about a station; usually
 * Goldstone, Madrid or Canberra. This object is created from a <code>config.xml</code> file.
 */
public class Station {
    private String name;
    private long longitude;
    private long latitude;
    private Set<String> dishes = new HashSet<>();

    /**
     * Create a new station from given values.
     * @param name the name of the station, e.g. <code>gdscc</code>
     * @param longitude the longitude of the station, in ten millionth of degrees
     * @param latitude the latitude of the station, in ten millionth of degrees
     */
    public Station(String name, long longitude, long latitude) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /**
     * Return the name of this station, e.g. <code>cdscc</code>.
     * @return the name of the station
     */
    public String getName() {
        return name;
    }

    /**
     * Return the latitude of this station, in the units of ten millionth of a degree
     * (i.e. multiply of 10,000,000 to get the value in degrees).
     * @return the latitude of the station
     */
    public long getLatitude() {
        return latitude;
    }

    /**
     * Return the longitude of this station, in the units of ten millionth of a degree
     * (i.e. multiply of 10,000,000 to get the value in degrees).
     * @return the longitude of the station
     */
    public long getLongitude() {
        return longitude;
    }

    /**
     * Return the dishes located in this station, in no particular order.
     * @return the dishes located in this station
     */
    public Set<String> getDishes() {
        return dishes;
    }

    // XML tag/attribute names
    public static final String SITE = "site";
    public static final String NAME = "name";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
}
