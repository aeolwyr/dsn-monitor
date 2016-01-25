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
 * Data structure to hold the persistent info about a dish, <br />
 * parsed from a <code>config.xml</code> file.
 */
public class Dish {
    private String name;
    private String type;

    /**
     * Create a new dish with the given values.
     * @param name name of the dish (e.g. <code>DSS63</code>)
     * @param type type of the dish (e.g. <code>34M</code>)
     */
    public Dish(String name, String type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Return the name of this dish (e.g. <code>DSS15</code>).
     * @return the name of this dish
     */
    public String getName() {
        return name;
    }

    /**
     * Return the type of this dish, usually one of these:
     * 70M / 34MHEF / 34M
     * @return the type of this dish
     */
    public String getType() {
        return type;
    }


    // XML tag/attribute names
    public static final String DISH = "dish";
    public static final String NAME = "name";
    public static final String TYPE = "type";
}
