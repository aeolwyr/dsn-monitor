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
 * Data structure to hold a temporary signal information,
 * parsed from a <code>dsn.xml</code> file, and held by a <code>DishState</code>.
 */
public class Signal {
    private String type;
    private String typeDebug;
    /** in microbits per second **/
    private long dataRate;
    /** in microhertz **/
    private long frequency;
    /** in milliwatts for up, micro-dBm for down **/
    private int power;
    private String spacecraft;

    /**
     * Create a new signal with the given values.
     * @param type type of this signal, e.g. <code>data</code> or <code>carrier</code>
     * @param typeDebug detailed info about this signal
     * @param dataRate the data rate of the signal, in microbits per second
     * @param frequency the frequency of the signal, in microhertz
     * @param power the communication power, either in milliwatts or micro-dBm
     * @param spacecraft the name of the target spacecraft, e.g. <code>VGR2</code>
     */
    public Signal(String type, String typeDebug, long dataRate,
                  long frequency, int power, String spacecraft) {
        this.type = type;
        this.typeDebug = typeDebug;
        this.dataRate = dataRate;
        this.frequency = frequency;
        this.power = power;
        this.spacecraft = spacecraft;
    }

    /**
     * Return the type of this signal, usually one of these:
     * <code>none</code> / <code>carrier</code> / <code>data</code>
     * @return the type of the signal
     */
    public String getSignalType() {
        return type;
    }

    /**
     * Return the detailed type debug string of this signal,
     * what it represents is currently not fully understood.
     * @return the type debug string of the signal
     */
    public String getSignalTypeDebug() {
        return typeDebug;
    }

    /**
     * Return the data rate of this signal, in microbits per second.
     * @return the data rate of the signal
     */
    public long getDataRate() {
        return dataRate;
    }

    /**
     * Return the frequency of this signal, in microhertz. <br />
     * Note that in case of a upload signal, the digits up until the
     * gigahertz point are always zero and can be discarded. <br />
     * In addition, a value of zero will be returned if the data is not available.
     * @return the frequency of the signal
     */
    public long getFrequency() {
        return frequency;
    }

    /**
     * Return the power of this signal, in milliwatts if positive
     * or in micro-dBm if negative. <br />
     * Positive values are used in uploads, whereas the negative
     * values are used in downloads.
     * @return the power of the signal
     */
    public int getPower() {
        return power;
    }

    /**
     * Return the target spacecraft of this signal, i.e. <code>NHPC</code>.
     * @return the target of the signal
     */
    public String getSpacecraft() {
        return spacecraft;
    }

    // XML tag/attribute names
    public static final String SIGNAL = "Signal";
    public static final String DOWN_SIGNAL = "downSignal";
    public static final String UP_SIGNAL = "upSignal";
    public static final String SIGNAL_TYPE = "signalType";
    public static final String SIGNAL_TYPE_DEBUG = "signalTypeDebug";
    public static final String DATA_RATE = "dataRate";
    public static final String FREQUENCY = "frequency";
    public static final String POWER = "power";
    public static final String SPACECRAFT = "spacecraft";
}
