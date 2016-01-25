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

import java.util.ArrayList;
import java.util.List;

/**
 * Data structure to hold the temporary info about a dish, <br />
 * parsed from a <code>dsn.xml</code> file.
 */
public class DishState {
    private String name;
    /** in centidegrees **/
    private int azimuthAngle;
    /** in centidegrees **/
    private int elevationAngle;
    /** in decameters per hour **/
    private int windSpeed;
    /** Multiple Spacecraft Per Aperture **/
    private boolean isMSPA;
    private boolean isArray;
    /** Delta-Differential One-Way Ranging **/
    private boolean isDDOR;

    private List<Signal> downSignals = new ArrayList<>();
    private List<Signal> upSignals = new ArrayList<>();
    private List<Target> targets = new ArrayList<>();

    /**
     * Create a new dish state.
     * @param name the name of the dish (e.g. <code>DSS63</code>)
     * @param azimuthAngle the azimuth angle of the dish, in centidegrees
     * @param elevationAngle the elevation angle of the dish, in centidegrees
     * @param windSpeed the wind speed, in decameters per hour
     * @param isMSPA is this dish working in MSPA mode
     * @param isArray is this dish working as a part of an array
     * @param isDDOR is this dish working in DDOR mode
     */
    public DishState(String name, int azimuthAngle, int elevationAngle, int windSpeed,
                     boolean isMSPA, boolean isArray, boolean isDDOR) {
        this.name = name;
        this.azimuthAngle = azimuthAngle;
        this.elevationAngle = elevationAngle;
        this.windSpeed = windSpeed;
        this.isMSPA = isMSPA;
        this.isArray = isArray;
        this.isDDOR = isDDOR;
    }

    /**
     * Return the name of this dish (e.g. <code>DSS15</code>).
     * @return the name of the dish
     */
    public String getName() {
        return name;
    }

    /**
     * Return the azimuth angle of this dish, in centidegrees. <br />
     * This is a part of the spherical coordinates of the direction the dish is facing.
     * @return the azimuth angle of the dish
     */
    public int getAzimuthAngle() {
        return azimuthAngle;
    }

    /**
     * Return the elevation angle of this dish, in centidegrees.
     * This is a part of the spherical coordinates of the direction the dish is facing.
     * @return the elevation angle of the dish
     */
    public int getElevationAngle() {
        return elevationAngle;
    }

    /**
     * Return the wind speed around the dish, in decameters per hour.
     * @return the wind speed around the dish
     */
    public int getWindSpeed() {
        return windSpeed;
    }

    /**
     * Return whether this dish is in MSPA (multiple spacecraft per aperture) mode or not.
     * @return true if the dish is working in MSPA mode, false otherwise
     */
    public boolean isMSPA() {
        return isMSPA;
    }

    /**
     * Return whether this dish is working as a part of a dish array or not.
     * @return true if the dish is working as a part of an array, false otherwise
     */
    public boolean isArray() {
        return isArray;
    }

    /**
     * Return whether the DDOR (delta-differential one-way ranging) is used or not.
     * @return true if the dish is working in DDOR mode, false otherwise
     */
    public boolean isDDOR() {
        return isDDOR;
    }

    /**
     * Return a list of signals currently received by this dish.
     * @return the download signals of the dish
     */
    public List<Signal> getDownSignals() {
        return downSignals;
    }

    /**
     * Return a list of signals currently transmitted by this dish.
     * @return the upload signals of the dish
     */
    public List<Signal> getUpSignals() {
        return upSignals;
    }

    /**
     * Return a list of spacecraft targets this dish is facing.
     * @return the targets of this dish
     */
    public List<Target> getTargets() {
        return targets;
    }
    
    // XML tag/attribute names
    public static final String DISH = "dish";
    public static final String NAME = "name";
    public static final String AZIMUTH_ANGLE = "azimuthAngle";
    public static final String ELEVATION_ANGLE = "elevationAngle";
    public static final String WIND_SPEED = "windSpeed";
    public static final String IS_MSPA = "isMSPA";
    public static final String IS_ARRAY = "isArray";
    public static final String IS_DDOR = "isDDOR";
}
