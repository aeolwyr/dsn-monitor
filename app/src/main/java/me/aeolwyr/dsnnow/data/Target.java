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

/**
 * Data structure to hold a temporary information about a signal target,
 * parsed from a <code>dsn.xml</code> file, and held by a <code>DishState</code>.
 */
public class Target {
    private String name;
    /** in meters **/
    private long uplegRange;
    /** in meters **/
    private long downlegRange;
    /** round-trip light time, in microseconds **/
    private long rtlt;

    /**
     * Create a new target with the given values. <br />
     * Fields with no data should be -1.
     * @param name the name of the target, e.g. <code>MSL</code>
     * @param uplegRange the upleg range of the target, in meters
     * @param downlegRange the downleg range of the target, in meters
     * @param rtlt the round-trip light time of the target, in microseconds
     */
    public Target(String name, long uplegRange, long downlegRange, long rtlt) {
        this.name = name;
        this.uplegRange = uplegRange;
        this.downlegRange = downlegRange;
        this.rtlt = rtlt;
    }

    /**
     * Return the name of this target spacecraft, e.g. <code>NHPC</code>.
     * @return the name of the target
     */
    public String getName() {
        return name;
    }

    /**
     * Return the upleg range of this target spacecraft, in meters. <br />
     * If no data is available, -1 is returned.
     * @return the upleg range of the target
     */
    public long getUplegRange() {
        return uplegRange;
    }

    /**
     * Return the downleg range of this target spacecraft, in meters.
     * If no data is available, -1 is returned.
     * @return the downleg range of the target
     */
    public long getDownlegRange() {
        return downlegRange;
    }

    /**
     * Return the round-trip light time of this target spacecraft, in microseconds.
     * If no data is available, -1 is returned.
     * @return the round-trip light time of the target
     */
    public long getRTLT() {
        return rtlt;
    }

    // XML tag/attribute names
    public static final String TARGET = "target";
    public static final String NAME = "name";
    public static final String UPLEG_RANGE = "uplegRange";
    public static final String DOWNLEG_RANGE = "downlegRange";
    public static final String RTLT = "rtlt";
}
