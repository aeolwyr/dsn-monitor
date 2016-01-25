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
import java.util.Map;

/**
 * Data structure to hold the persistent info about the whole network,
 * parsed from a <code>config.xml</code> file.
 */
public class NetworkConfig {
    private Map<String, Station> stations = new HashMap<>();
    private Map<String, Dish> dishes = new HashMap<>();
    private Map<String, Spacecraft> spacecrafts = new HashMap<>();

    /**
     * Return the stations in this config object, mapped by their names.
     * @return the stations map
     */
    public Map<String, Station> getStations() {
        return stations;
    }

    /**
     * Return all the dishes in this config object, in no particular order,
     * and mapped by their names (e.g. <code>DSS63</code>). <br />
     * @return the dishes map
     */
    public Map<String, Dish> getDishes() {
        return dishes;
    }

    /**
     * Return the spacecrafts in this config object, mapped by their names
     * (e.g. <code>NHPC</code>).
     * @return the spacecrafts map
     */
    public Map<String, Spacecraft> getSpacecrafts() {
        return spacecrafts;
    }
}
