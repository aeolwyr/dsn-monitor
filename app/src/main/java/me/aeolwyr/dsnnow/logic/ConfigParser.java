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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.math.BigDecimal;

import me.aeolwyr.dsnnow.data.Dish;
import me.aeolwyr.dsnnow.data.NetworkConfig;
import me.aeolwyr.dsnnow.data.Spacecraft;
import me.aeolwyr.dsnnow.data.Station;

/**
 * Parser class to be used with a <code>config.xml</code> file, creates a <code>NetworkConfig</code>
 * object that holds the information in this XML file.
 */
public class ConfigParser {
    /**
     * Parse the given XML file, usually acquired via a <code>getResources().getXml()</code> call.
     * @param parser the parser of the XML file
     * @return a network config object if the parsing is successful
     * @throws XmlPullParserException if the XML file is malformed
     * @throws IOException if there is an input/output error
     */
    public static NetworkConfig parse(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        try {
            NetworkConfig config = new NetworkConfig();

            parser.next();
            // now at the BEGIN_DOCUMENT
            parser.nextTag();
            // not at the START_TAG of config
            parser.nextTag();
            // now at the START_TAG of sites
            parser.nextTag();
            // now at the first site
            while (parser.getName().equals(Station.SITE)) {
                Station station = processStation(parser);
                parser.nextTag();
                // now at the first dish
                while (parser.getName().equals(Dish.DISH)) {
                    Dish dish = processDish(parser);
                    station.getDishes().add(dish.getName());
                    config.getDishes().put(dish.getName(), dish);

                    parser.nextTag();
                    // now at the end of this dish
                    parser.nextTag();
                    // now at the next dish
                    // or at the end of the dishes
                }

                // station is ready
                config.getStations().put(station.getName(), station);

                parser.nextTag();
                // now at the START of the next site
                // or at the END of the sites
            }
            // stations are complete
            parser.nextTag();
            // now at the START of spacecraftMap
            parser.nextTag();
            // now at the first spacecraft
            while (parser.getName().equals(Spacecraft.SPACECRAFT)) {
                Spacecraft spacecraft = processSpacecraft(parser);
                config.getSpacecrafts().put(spacecraft.getName(), spacecraft);
                parser.nextTag();
                // now at the END
                parser.nextTag();
                // now at the START of the next spacecraft
                // or at the END of spacecraftMap
            }

            return config;
        } catch (NumberFormatException e) {
            // a problem occurred while parsing a number
            throw new XmlPullParserException(null, parser, e);
        }
    }

    /**
     * Parse and return a station. The parser must be at a station (site).
     * @param parser the parser of the XML file
     * @return the parsed station
     */
    private static Station processStation(XmlPullParser parser) {
        String name = parser.getAttributeValue(null, Station.NAME);
        String longitudeString = parser.getAttributeValue(null, Station.LONGITUDE);
        String latitudeString = parser.getAttributeValue(null, Station.LATITUDE);

        long longitude = stringToFixedPoint(longitudeString);
        long latitude = stringToFixedPoint(latitudeString);

        return new Station(name, longitude, latitude);
    }

    /**
     * Parse and return a dish. The parser must be at a dish.
     * @param parser the parser of the XML file
     * @return the parsed dish
     */
    private static Dish processDish(XmlPullParser parser) {
        String name = parser.getAttributeValue(null, Dish.NAME);
        String type = parser.getAttributeValue(null, Dish.TYPE);

        return new Dish(name, type);
    }

    /**
     * Parse and return a spacecraft. The parser must be at a spacecraft.
     * @param parser the parser of the XML file
     * @return the parsed spacecraft
     */
    private static Spacecraft processSpacecraft(XmlPullParser parser) {
        String name = parser.getAttributeValue(null, Spacecraft.NAME).toUpperCase();
        String friendlyName = parser.getAttributeValue(null, Spacecraft.FRIENDLY_NAME);

        return new Spacecraft(name, friendlyName);
    }

    private static final BigDecimal TEN_MILLION = new BigDecimal(10_000_000);

    /**
     * Parse a number, preserving up to 7 fractional digits. The resulting number is therefore
     * the original number multiplied by 10,000,000.
     * @param string the number string
     * @return the parsed fixed point number
     * @throws NumberFormatException if the number is invalid or has more than 7 fractional digits
     */
    private static long stringToFixedPoint(String string) {
        return new BigDecimal(string).multiply(TEN_MILLION).longValueExact();
    }
}
