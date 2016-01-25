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

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

import me.aeolwyr.dsnnow.data.DishState;
import me.aeolwyr.dsnnow.data.NetworkConfig;
import me.aeolwyr.dsnnow.data.NetworkState;
import me.aeolwyr.dsnnow.data.Signal;
import me.aeolwyr.dsnnow.data.Station;
import me.aeolwyr.dsnnow.data.StationState;
import me.aeolwyr.dsnnow.data.Target;

/**
 * Parser class to be used with a <code>dsn.xml</code> file,
 * creates a <code>NetworkState</code> object that holds the information in the XML.
 */
public class StateParser {
    public static NetworkState parse(NetworkConfig config, InputStream inputStream)
            throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        // namespaces are not used in these files
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
        parser.setInput(inputStream, null);
        try {
            NetworkState networkState = new NetworkState();
            // should be at the BEGIN_DOCUMENT
            parser.nextTag();
            // now at <dsn>
            parser.nextTag();
            // now at the beginning of the station
            while (parser.getName().equals(StationState.STATION)) {
                StationState stationState = processStation(parser);
                parser.nextTag();
                // now at the end of the station
                parser.nextTag();
                // now at the first dish
                while (parser.getName().equals(DishState.DISH)) {
                    DishState dishState = processDish(parser);
                    parser.nextTag();
                    // now at the first signal
                    while (parser.getName().endsWith(Signal.SIGNAL)) {
                        Signal signal = processSignal(parser);

                        switch (parser.getName()) {
                            case Signal.DOWN_SIGNAL:
                                dishState.getDownSignals().add(signal);
                                break;
                            case Signal.UP_SIGNAL:
                                dishState.getUpSignals().add(signal);
                                break;
                            default:
                                throw new XmlPullParserException("Invalid signal");
                        }

                        parser.nextTag();
                        // now at the end of this signal
                        parser.nextTag();
                        // now at the next signal
                        // or at the first target
                    }

                    while (parser.getName().equals(Target.TARGET)) {
                        Target target = processTarget(parser);
                        dishState.getTargets().add(target);

                        parser.nextTag();
                        // now at the end of this target
                        parser.nextTag();
                        // now at the next target
                        // or at the end of the dish
                    }

                    // dish state is ready
                    String dishStateName = dishState.getName();
                    stationState.getDishStates().add(dishStateName);
                    networkState.getDishStates().put(dishStateName, dishState);

                    parser.nextTag();
                    // now at the next dish
                    // or at the next the station
                    // or at the timestamp
                }

                // station state is ready
                networkState.getStationStates().put(stationState.getName(), stationState);
            }

            String timestamp = parser.nextText();
            networkState.setTimestamp(Long.parseLong(timestamp));

            // unused dishes do not appear in the dsn.xml files
            // add the missing dish names so that it is easier to process later on
            for (Station station : config.getStations().values()) {
                StationState stationState = networkState.getStationStates().get(station.getName());
                if (stationState != null) {
                    for (String dishName : station.getDishes()) {
                        // if this dish contained in this station
                        // does not exist in the corresponding station state,
                        // add it with a null value
                        if (!stationState.getDishStates().contains(dishName)) {
                            stationState.getDishStates().add(dishName);
                            networkState.getDishStates().put(dishName, null);
                        }
                    }
                }
            }

            return networkState;
        } catch (NumberFormatException e) {
            // a problem occurred while parsing a number
            throw new XmlPullParserException(null, parser, e);
        }
    }

    /**
     * Parse and return a station state. The parser must be at a station.
     * @param parser the parser of the XML file
     * @return the parsed station state
     */
    private static StationState processStation(XmlPullParser parser) {
        String name = parser.getAttributeValue(null, StationState.NAME);
        String friendlyName = parser.getAttributeValue(null, StationState.FRIENDLY_NAME);
        String timeZoneOffset = parser.getAttributeValue(null, StationState.TIME_ZONE_OFFSET);

        return new StationState(name, friendlyName, Integer.parseInt(timeZoneOffset));
    }

    /**
     * Parse and return a dish state. The parser must be at a dish.
     * @param parser the parser of the XML file
     * @return the parsed dish state
     */
    private static DishState processDish(XmlPullParser parser) {
        String dishName = parser.getAttributeValue(null, DishState.NAME);
        String azimuthAngleString = parser.getAttributeValue(null, DishState.AZIMUTH_ANGLE);
        String elevationAngleString = parser.getAttributeValue(null, DishState.ELEVATION_ANGLE);
        String windSpeedString = parser.getAttributeValue(null, DishState.WIND_SPEED);
        String isMSPAString = parser.getAttributeValue(null, DishState.IS_MSPA);
        String isArrayString = parser.getAttributeValue(null, DishState.IS_ARRAY);
        String isDDORString = parser.getAttributeValue(null, DishState.IS_DDOR);

        int azimuthAngle = stringToFixedPoint(azimuthAngleString);
        int elevationAngle = stringToFixedPoint(elevationAngleString);
        int windSpeed = stringToFixedPoint(windSpeedString);
        boolean isMSPA = Boolean.parseBoolean(isMSPAString);
        boolean isArray = Boolean.parseBoolean(isArrayString);
        boolean isDDOR = Boolean.parseBoolean(isDDORString);

        return new DishState(dishName, azimuthAngle, elevationAngle,
                windSpeed, isMSPA, isArray, isDDOR);
    }

    /**
     * Parse and return a signal. The parser must be at a signal.
     * @param parser the parser of the XML file
     * @return the parsed signal
     */
    private static Signal processSignal(XmlPullParser parser) {
        String type = parser.getAttributeValue(null, Signal.SIGNAL_TYPE);
        String typeDebug = parser.getAttributeValue(null, Signal.SIGNAL_TYPE_DEBUG);
        String dataRateString = parser.getAttributeValue(null, Signal.DATA_RATE);
        String frequencyString = parser.getAttributeValue(null, Signal.FREQUENCY);
        String powerString = parser.getAttributeValue(null, Signal.POWER);
        String spacecraft = parser.getAttributeValue(null, Signal.SPACECRAFT);

        long dataRate = parseDataRate(dataRateString);
        long frequency = parseFrequency(frequencyString, parser.getName());
        int power = parsePower(powerString);

        return new Signal(type, typeDebug, dataRate, frequency, power, spacecraft);
    }

    /**
     * Parse and return a target. The parser must be at a target.
     * @param parser the parser of the XML file
     * @return the parsed target
     */
    private static Target processTarget(XmlPullParser parser) {
        String name = parser.getAttributeValue(null, Target.NAME);
        String uplegRangeString = parser.getAttributeValue(null, Target.UPLEG_RANGE);
        String downlegRangeString = parser.getAttributeValue(null, Target.DOWNLEG_RANGE);
        String rtltString = parser.getAttributeValue(null, Target.RTLT);

        long uplegRange = parseRange(uplegRangeString);
        long downlegRange = parseRange(downlegRangeString);
        long rtlt = parseRTLT(rtltString);

        return new Target(name, uplegRange, downlegRange, rtlt);
    }

    // used for number parsing
    private static final BigDecimal HUNDRED = new BigDecimal(100);
    private static final BigDecimal THOUSAND = new BigDecimal(1000);
    private static final BigDecimal MILLION = new BigDecimal(1_000_000);
    private static final BigDecimal TRILLION = new BigDecimal(1_000_000_000_000l);


    /**
     * Parse a number, preserving up to 2 fractional digits. The resulting number is therefore
     * the original number multiplied by 100. <br />
     * Empty strings are parsed as -1.
     * @param string the number string
     * @return the parsed fixed point number
     * @throws NumberFormatException if the number is invalid or has more than 2 fractional digits
     */
    private static int stringToFixedPoint(String string) {
        if (!string.isEmpty()) {
            return new BigDecimal(string).multiply(HUNDRED).intValueExact();
        } else {
            return -1;
        }
    }

    /**
     * Parse a range value, preserving up to 3 fractional digits. The resulting number
     * is therefore the original number multiplied by 1000.
     * @param range the range string
     * @return the parsed fixed point number
     * @throws NumberFormatException if the number is invalid or has more than 3 fractional digits
     */
    private static long parseRange(String range) {
        if (!range.equals("-1.0")) {
            return new BigDecimal(range).multiply(THOUSAND).longValueExact();
        } else {
            // means no range value
            return -1;
        }
    }

    /**
     * Parse a round-trip light time value, preserving up to 6 fractional digits. The resulting
     * number is therefore the original number multiplied by 1,000,000.
     * @param rtlt the RTLT string
     * @return the parsed fixed point number
     * @throws NumberFormatException if the number is invalid or has more than 6 fractional digits
     */
    private static long parseRTLT(String rtlt) {
        if (!rtlt.equals("-1.0")) {
            return new BigDecimal(rtlt).multiply(MILLION).longValueExact();
        } else {
            return -1;
        }
    }

    /**
     * Parse a data rate value, preserving up to 6 fractional digits. The resulting number is
     * therefore the original number multiplied by 1,000,000. <br />
     * Empty or null values are parsed as -1.
     * @param dataRate the data rate string
     * @return the parsed fixed point number
     * @throws NumberFormatException if the number is invalid or has more than 6 fractional digits
     */
    private static long parseDataRate(String dataRate) {
        if (!dataRate.isEmpty() && !dataRate.equals("null")) {
            return new BigDecimal(dataRate).multiply(MILLION).longValueExact();
        } else {
            return -1;
        }
    }

    /**
     * Parse a frequency value, preserving up to 6 fractional digits in case of a down signal,
     * and up to 12 fractional digits in case of a up signal. The resulting number is therefore
     * the original number multiplied by 1,000,000 or 1,000,000,000,000 depending on the
     * direction. <br />
     * Empty, none and null values are parsed as -1.
     * @param frequency the frequency string
     * @param direction the direction of the signal, either <code>Signal.DOWN_SIGNAL</code>
     *                  or <code>Signal.UP_SIGNAL</code>
     * @return the parsed fixed point number
     * @throws NumberFormatException if the number is invalid or has more than 6
     * or 12 fractional digits
     */
    private static long parseFrequency(String frequency, String direction) {
        if (!frequency.isEmpty() && !frequency.equals("none") && !frequency.equals("null")) {
            switch (direction) {
                case Signal.DOWN_SIGNAL:
                    return new BigDecimal(frequency).multiply(MILLION).longValueExact();
                case Signal.UP_SIGNAL:
                    return new BigDecimal(frequency).multiply(TRILLION).longValueExact();
                default:
                    return -1;
            }
        } else {
            return -1;
        }
    }

    /**
     * Parse a power value, preserving up to 6 fractional digits. The resulting number is therefore
     * the original number multiplied by 1,000,000. <br />
     * Empty or null values are parsed as 0.
     * @param power the power string
     * @return the parsed fixed point number
     * @throws NumberFormatException if the number is invalid or has more than 6 fractional digits
     */
    private static int parsePower(String power) {
        if (!power.isEmpty() && !power.equals("null")) {
            return new BigDecimal(power).multiply(MILLION).intValueExact();
        } else {
            return 0;
        }
    }
}
