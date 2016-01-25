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

package me.aeolwyr.dsnnow.presentation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.aeolwyr.dsnnow.R;
import me.aeolwyr.dsnnow.data.Dish;
import me.aeolwyr.dsnnow.data.DishState;
import me.aeolwyr.dsnnow.data.NetworkConfig;
import me.aeolwyr.dsnnow.data.NetworkState;
import me.aeolwyr.dsnnow.data.Signal;
import me.aeolwyr.dsnnow.data.Spacecraft;
import me.aeolwyr.dsnnow.data.Station;
import me.aeolwyr.dsnnow.data.StationState;
import me.aeolwyr.dsnnow.data.Target;

/**
 * Adapter that shows the contents of a NetworkState in an expandable list.
 */
public class NetworkAdapter extends BaseExpandableListAdapter {
    private Context context;
    private NetworkConfig config;
    private NetworkState state;

    /** list items, includes both sites and dishes **/
    private List<Item> items = new ArrayList<>();

    /**
     * Create a new <code>NetworkAdapter</code> with empty contents.
     * @param context the current activity context
     * @param config the persistent network config to use as a base
     */
    public NetworkAdapter(Context context, NetworkConfig config) {
        this.context = context;
        this.config = config;
    }

    /**
     * Set the network state to a new one. The initialization process is repeated.
     * @param state the state to read
     */
    public void setNetworkState(NetworkState state) {
        this.state = state;

        items.clear();
        for (String stationName : state.getStationStates().keySet()) {
            // put all the stations and the dishes into the items list
            items.add(new Item(stationName, true));

            for (String dishStateName : state.getStationStates().get(stationName).getDishStates()) {
                items.add(new Item(dishStateName, false));
            }
        }
        // notify the list view
        notifyDataSetChanged();
    }

    /**
     * Return the network state currently shown in this adapter.
     * @return the network state shown in the adapter
     */
    public NetworkState getNetworkState() {
        return state;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {
        Item item = items.get(groupPosition);

        if (item.isStation) {
            // inflate the view if necessary
            if (convertView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                convertView = layoutInflater.inflate(R.layout.station_overview, parent, false);
            }

            // need to set this manually, as this is a custom list item view
            convertView.setActivated(isExpanded);

            // station item
            String stationName = item.name;
            StationState stationState = state.getStationStates().get(stationName);

            // friendly name (e.g. "Goldstone")
            TextView friendlyName = (TextView) convertView.findViewById(R.id.friendly_name);
            friendlyName.setText(stationState.getFriendlyName());

            // name (e.g. "gdscc")
            TextView name = (TextView) convertView.findViewById(R.id.name);
            name.setText(stationName);

        } else {
            // inflate the view if necessary
            if (convertView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                convertView = layoutInflater.inflate(R.layout.dish_overview, parent, false);
            }

            // need to set this manually, as this is a custom list item view
            convertView.setActivated(isExpanded);

            // dish item
            String dishName = item.name;
            Dish dish = config.getDishes().get(dishName);
            DishState dishState = state.getDishStates().get(dishName);

            // friendly name (e.g. "DSS15")
            TextView name = (TextView) convertView.findViewById(R.id.name);
            name.setText(dishName);

            // type (e.g. "35MHEF")
            TextView type = (TextView) convertView.findViewById(R.id.type);
            type.setText(dish != null ? dish.getType() : "");
            type.setClickable(PrefsManager.isAcronymHelpEnabled(context));

            // show targets overview
            final int[] targetRows = {R.id.target_1, R.id.target_2, R.id.target_3};

            if (dishState != null && !dishState.getTargets().isEmpty()) {
                // number of targets to show
                int numberOfTargets = Math.min(targetRows.length, dishState.getTargets().size());
                for (int i = 0; i < numberOfTargets; i++) {
                    View targetRow = convertView.findViewById(targetRows[i]);
                    Target target = dishState.getTargets().get(i);

                    showTargetOverview(targetRow, target);
                }
                // hide unused rows
                for (int i = numberOfTargets; i < targetRows.length; i++) {
                    convertView.findViewById(targetRows[i]).setVisibility(View.GONE);
                }
            } else {
                // no dish state means this dish is not used
                // show 1 row for consistency
                View firstRow = convertView.findViewById(targetRows[0]);
                TextView nameView = (TextView) firstRow.findViewById(R.id.friendly_name);
                nameView.setText(context.getString(R.string.no_target));

                // hide the rest
                for (int i = 1; i < targetRows.length; i++) {
                    convertView.findViewById(targetRows[i]).setVisibility(View.GONE);
                }
            }
        }
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        Item item = items.get(groupPosition);
        if (item.isStation) {
            // inflate the view if necessary
            if (convertView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                convertView = layoutInflater.inflate(R.layout.station_details, parent, false);
            }

            // station details
            String stationName = item.name;
            Station station = config.getStations().get(stationName);
            StationState stationState = state.getStationStates().get(stationName);

            // coordinate (e.g. "12.34 N 56.78 W")
            TextView coordinate = (TextView) convertView.findViewById(R.id.coordinate);
            coordinate.setText(station != null ?
                    getCoordinateString(station.getLatitude(), station.getLongitude()) : "");
            // time zone offset (e.g. "+ 01:00")
            TextView timeZoneOffset = (TextView) convertView.findViewById(R.id.time_zone_offset);
            timeZoneOffset.setText(getTimeZoneOffsetString(stationState.getTimeZoneOffset()));

        } else {
            // inflate the view if necessary
            if (convertView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                convertView = layoutInflater.inflate(R.layout.dish_details, parent, false);
            }

            // dish details
            DishState dishState = state.getDishStates().get(item.name);

            if (dishState != null) {
                setVisibilityForAllChildren(convertView, View.VISIBLE);
                convertView.findViewById(R.id.dish_not_in_use_row).setVisibility(View.GONE);

                showDishDetails(convertView, dishState);

                // view IDs
                final int[][] downSignalRows = {
                        {R.id.down_signal_1_1, R.id.down_signal_1_2, R.id.down_signal_1_3},
                        {R.id.down_signal_2_1, R.id.down_signal_2_2, R.id.down_signal_2_3},
                        {R.id.down_signal_3_1, R.id.down_signal_3_2, R.id.down_signal_3_3}
                };
                final int[] upSignalRow = {R.id.up_signal_1, R.id.up_signal_2, R.id.up_signal_3};
                final int[][] targetRows = {
                        {R.id.target_1_1, R.id.target_1_2},
                        {R.id.target_2_1, R.id.target_2_2},
                        {R.id.target_3_1, R.id.target_3_2}
                };

                // down signals
                int numberOfDownSignals = Math.min(
                        downSignalRows.length,
                        dishState.getDownSignals().size());

                for (int i = 0; i < numberOfDownSignals; i++) {
                    showSignal(convertView, downSignalRows[i],
                            dishState.getDownSignals().get(i), true);
                }

                for (int i = numberOfDownSignals; i < downSignalRows.length; i++) {
                    hideSignal(convertView, downSignalRows[i]);
                }

                // up signal
                if (!dishState.getUpSignals().isEmpty()) {
                    showSignal(convertView, upSignalRow, dishState.getUpSignals().get(0), false);
                } else {
                    hideSignal(convertView, upSignalRow);
                }

                // targets
                int numberOfTargets = Math.min(targetRows.length, dishState.getTargets().size());
                for (int i = 0; i < numberOfTargets; i++) {
                    showTarget(convertView, targetRows[i], dishState.getTargets().get(i));
                }
                for (int i = numberOfTargets; i < targetRows.length; i++) {
                    hideTarget(convertView, targetRows[i]);
                }
            } else {
                // no dish state means the dish is not in use
                setVisibilityForAllChildren(convertView, View.GONE);
                convertView.findViewById(R.id.dish_not_in_use_row).setVisibility(View.VISIBLE);
            }
        }
        return convertView;
    }

    /**
     * Set visibility of all the first-level children of a <code>ViewGroup</code>.
     * @param view the parent view
     * @param visibility a <code>View</code> visibility state
     */
    private static void setVisibilityForAllChildren(View view, int visibility) {
        ViewGroup viewGroup = (ViewGroup) view;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            viewGroup.getChildAt(i).setVisibility(visibility);
        }
    }

    /**
     * Update a target overview row (resides inside the dish overview).
     * @param targetRow the row to update
     * @param target the target to read data from
     */
    private void showTargetOverview(View targetRow, Target target) {
        targetRow.setVisibility(View.VISIBLE);

        // friendly name (e.g. "New Horizons")
        TextView name = (TextView) targetRow.findViewById(R.id.friendly_name);
        Spacecraft spacecraft = config.getSpacecrafts().get(target.getName());
        // show spacecraft name if it exists in the config.xml
        if (spacecraft != null) {
            name.setText(context.getString(R.string.target_name,
                    spacecraft.getFriendlyName(), spacecraft.getName()));
        } else {
            name.setText(target.getName());
        }
    }

    /**
     * Update the details of a dish state.
     * @param convertView the parent view of the dish details
     * @param dishState the dish state to read data from
     */
    private void showDishDetails(View convertView, DishState dishState) {
        boolean helpEnabled = PrefsManager.isAcronymHelpEnabled(context);

        TextView azimuth = (TextView) convertView.findViewById(R.id.azimuth);
        TextView elevation = (TextView) convertView.findViewById(R.id.elevation);
        TextView wind = (TextView) convertView.findViewById(R.id.wind);
        TextView mspa = (TextView) convertView.findViewById(R.id.mspa);
        TextView array = (TextView) convertView.findViewById(R.id.array);
        TextView ddor = (TextView) convertView.findViewById(R.id.ddor);

        String yes = context.getString(R.string.yes);
        String no = context.getString(R.string.no);

        azimuth.setText(context.getString(R.string.azimuth_angle,
                (float) dishState.getAzimuthAngle() / 100));
        elevation.setText(context.getString(R.string.elevation_angle,
                (float) dishState.getElevationAngle() / 100));
        wind.setText(getWindSpeedString(dishState.getWindSpeed()));
        mspa.setText(context.getString(R.string.mspa, (dishState.isMSPA() ? yes : no)));
        array.setText(context.getString(R.string.array, (dishState.isArray() ? yes : no)));
        ddor.setText(context.getString(R.string.ddor, (dishState.isDDOR() ? yes : no)));

        mspa.setClickable(helpEnabled);
        ddor.setClickable(helpEnabled);
    }

    /**
     * Make a signal item visible and update its contents.
     * @param convertView the parent view of the signal
     * @param signalRow the rows of this signal
     * @param signal the signal to read data from
     * @param isDown true if the signal is a download signal, false if it is a upload signal
     */
    private void showSignal(View convertView, int[] signalRow, Signal signal, boolean isDown) {
        View signalRow1 = convertView.findViewById(signalRow[0]);
        signalRow1.setVisibility(View.VISIBLE);
        // type (e.g. "data")
        TextView type = (TextView) signalRow1.findViewById(R.id.type);
        type.setText(context.getString(R.string.signal_type,
                signal.getSpacecraft(), signal.getSignalType()));
        // icon
        int icon = isDown ? R.drawable.ic_file_download : R.drawable.ic_file_upload;
        type.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, 0, 0, 0);
        // details
        TextView details = (TextView) signalRow1.findViewById(R.id.details);
        details.setText(signal.getSignalTypeDebug());

        View signalRow2 = convertView.findViewById(signalRow[1]);
        signalRow2.setVisibility(View.VISIBLE);
        // data rate (e.g. "12.34 kb/s")
        TextView dataRate = (TextView) signalRow2.findViewById(R.id.data_rate);
        dataRate.setText(getDataRateString(signal.getDataRate()));
        // frequency (e.g. "1234 GHz")
        TextView frequency = (TextView) signalRow2.findViewById(R.id.frequency);
        frequency.setText(getFrequencyString(signal.getFrequency()));

        View signalRow3 = convertView.findViewById(signalRow[2]);
        signalRow3.setVisibility(View.VISIBLE);
        // power (e.g. "1.23 kW")
        TextView power = (TextView) signalRow3.findViewById(R.id.power);
        power.setText(getPowerString(signal.getPower(), isDown));
    }

    /**
     * Hide a signal item, to be used when this signal row is not used.
     * @param convertView the parent view of the signal row
     * @param signalRow the rows of this signal
     */
    private void hideSignal(View convertView, int[] signalRow) {
        convertView.findViewById(signalRow[0]).setVisibility(View.GONE);
        convertView.findViewById(signalRow[1]).setVisibility(View.GONE);
        convertView.findViewById(signalRow[2]).setVisibility(View.GONE);
    }

    /**
     * Make a target item visible and update its contents.
     * @param convertView the parent view of the target row
     * @param targetRow the rows of this target
     * @param target the target to read data from
     */
    private void showTarget(View convertView, int[] targetRow, Target target) {
        View targetRow1 = convertView.findViewById(targetRow[0]);
        targetRow1.setVisibility(View.VISIBLE);
        // name (e.g. "NHPC")
        TextView name = (TextView) targetRow1.findViewById(R.id.name);
        name.setText(target.getName());
        // rtlt (e.g. "12.34 min")
        TextView rtlt = (TextView) targetRow1.findViewById(R.id.rtlt);
        rtlt.setText(getRTLTString(target.getRTLT()));
        rtlt.setClickable(PrefsManager.isAcronymHelpEnabled(context));

        View targetRow2 = convertView.findViewById(targetRow[1]);
        targetRow2.setVisibility(View.VISIBLE);
        // ranges (e.g. "13.57M km")
        TextView uplegRange = (TextView) targetRow2.findViewById(R.id.upleg_range);
        uplegRange.setText(getRangeString(target.getUplegRange(), true));
        TextView downlegRange = (TextView) targetRow2.findViewById(R.id.downleg_range);
        downlegRange.setText(getRangeString(target.getDownlegRange(), false));
    }

    /**
     * Hide a target item, to be used when this signal row is not used.
     * @param convertView the parent view of this target
     * @param targetRow the rows of this target
     */
    private void hideTarget(View convertView, int[] targetRow) {
        convertView.findViewById(targetRow[0]).setVisibility(View.GONE);
        convertView.findViewById(targetRow[1]).setVisibility(View.GONE);
    }

    @Override
    public int getGroupCount() { return items.size(); }

    @Override
    public int getChildrenCount(int groupPosition) { return 1; }

    @Override
    public boolean hasStableIds() { return false; }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) { return false; }

    @Override
    public int getGroupTypeCount() { return 2; }

    @Override
    public int getChildTypeCount() { return 2; }

    @Override
    public Object getGroup(int groupPosition) { return null; }

    @Override
    public Object getChild(int groupPosition, int childPosition) { return null; }

    @Override
    public long getGroupId(int groupPosition) { return 0; }

    @Override
    public long getChildId(int groupPosition, int childPosition) { return 0; }

    /**
     * Get the type of a item in the list.
     * @param position the position of the item
     * @return 0 if the item is a station, 1 if the item is a dish
     */
    private int getType(int position) { return items.get(position).isStation ? 0 : 1; }

    @Override
    public int getGroupType(int groupPosition) { return getType(groupPosition); }

    @Override
    public int getChildType(int groupPosition, int childPosition) { return getType(groupPosition); }

    /**
     * Internal data type to easily calculate the positions of the dishes and the stations.
     * An item represents either a station or a dish (but not both).
     */
    private static class Item {
        public String name;
        public boolean isStation;

        /**
         * Create a new network adapter item
         * @param name the name of the station/dish
         * @param isStation true if this is a station, false if this is a dish
         */
        public Item(String name, boolean isStation) {
            this.name = name;
            this.isStation = isStation;
        }
    }

    /**
     * Convert latitude/longitude values to string.
     * @param latitude the latitude value of the station
     * @param longitude the longitude value of the station
     * @return the coordinate string, e.g. "12.34 N 56.78 W"
     */
    private String getCoordinateString(long latitude, long longitude) {
        double latitudeValue = (double) Math.abs(latitude) / 10000000;
        double longitudeValue = (double) Math.abs(longitude) / 10000000;

        String latitudeDirection = (latitude > 0) ?
                context.getString(R.string.north) : context.getString(R.string.south);
        String longitudeDirection = (longitude > 0) ?
                context.getString(R.string.east) : context.getString(R.string.west);

        return context.getString(R.string.coordinate,
                latitudeValue, latitudeDirection, longitudeValue, longitudeDirection);
    }

    /**
     * Convert a time zone offset to string.
     * @param timeZoneOffset the time zone offset of the station
     * @return the time zone offset string, e.g. "+ 01:00"
     */
    private String getTimeZoneOffsetString(int timeZoneOffset) {
        int hours = Math.abs(timeZoneOffset) / 3600000;
        int minutes = (Math.abs(timeZoneOffset) / 60000) % 60;

        String sign = timeZoneOffset >= 0 ? "+" : "-";
        return context.getString(R.string.time_zone_offset, sign, hours, minutes);
    }

    /**
     * Convert a wind speed value to string. The units are read from the context's preferences.
     * @param windSpeed the wind speed around this dish, in decameters per hour
     * @return the wind speed string, e.g. "12.34 km/h"
     */
    private String getWindSpeedString(int windSpeed) {
        String windSpeedString;
        switch (PrefsManager.getWindSpeedUnit(context)) {
            default:
            case "1":
                windSpeedString = context.getString(R.string.value_with_unit,
                        (double) windSpeed / 100,
                        context.getString(R.string.kilometers_per_hour));
                break;
            case "2":
                windSpeedString = context.getString(R.string.value_with_unit,
                        (double) windSpeed / 160.9344,
                        context.getString(R.string.miles_per_hour));
                break;
            case "3":
                windSpeedString = context.getString(R.string.value_with_unit,
                        (double) windSpeed / 360,
                        context.getString(R.string.meters_per_second));
                break;
        }
        return context.getString(R.string.wind_speed, windSpeedString);
    }

    /**
     * Convert a data rate value to string. The units are read from the context's preferences.
     * @param dataRate the data rate of the signal, in microbits per second
     * @return the data rate string, e.g. "12.34 kB/s"
     */
    private String getDataRateString(long dataRate) {
        String unit;
        double dataRateValue;
        switch (PrefsManager.getDataRateUnit(context)) {
            default:
            case "1":
                unit = context.getString(R.string.bits_per_second);
                dataRateValue = dataRate;
                break;
            case "2":
                unit = context.getString(R.string.bytes_per_second);
                dataRateValue = dataRate / 8;
                break;
        }

        String dataRateString;
        if (dataRateValue < 0) {
            dataRateString = context.getString(R.string.no_data);
        } else if (dataRateValue < 1000000000) {
            dataRateString = context.getString(R.string.value_with_unit,
                    dataRateValue / 1000000,
                    unit);
        } else if (dataRateValue < 1000000000000l) {
            dataRateString = context.getString(R.string.value_with_prefixed_unit,
                    dataRateValue / 1000000000,
                    context.getString(R.string.kilo), unit);
        } else {
            dataRateString = context.getString(R.string.value_with_prefixed_unit,
                    dataRateValue / 1000000000000l,
                    context.getString(R.string.mega), unit);
        }
        return context.getString(R.string.data_rate, dataRateString);
    }

    /**
     * Convert a frequency value to string. The units are read from the context's preferences.
     * @param frequency the frequency of the signal, in microhertz
     * @return the frequency string, e.g. "12.34 GHz"
     */
    private String getFrequencyString(long frequency) {
        String frequencyString;
        if (frequency < 0) {
            frequencyString = context.getString(R.string.no_data);
        } else {
            frequencyString = context.getString(R.string.value_with_unit,
                    (double) frequency / 1000000000000000l,
                    context.getString(R.string.gigahertz));
        }
        return context.getString(R.string.frequency, frequencyString);
    }

    /**
     * Convert a power value to string. The units are read from the context's preferences. <br />
     * Download signal powers are assumed to be in micro-dBm, whereas the upload signal powers
     * are assumed to be in milliwatts.
     * @param power the power of the signal, either in milliwatts or micro-dBm
     * @param isDown true if this belongs to a down signal, false otherwise
     * @return the power string, e.g. "12.34 kW" or "-123.45 dBm"
     */
    private String getPowerString(int power, boolean isDown) {
        String powerString;
        if (isDown) {
            switch (PrefsManager.getDownloadPowerUnit(context)) {
                default:
                case "1":
                    powerString = context.getString(R.string.value_with_unit,
                            (double) power / 1000000,
                            context.getString(R.string.decibel_milliwatts));
                    break;
                case "2":
                    // convert from micro-dBm to aW/zW/fW
                    double zeptoPower = Math.pow(10, (double) power / 10000000 + 18);
                    if (zeptoPower < 1000) {
                        powerString = context.getString(R.string.value_with_prefixed_unit,
                                zeptoPower,
                                context.getString(R.string.zepto),
                                context.getString(R.string.watts));
                    } else if (zeptoPower < 1000000){
                        powerString = context.getString(R.string.value_with_prefixed_unit,
                                zeptoPower / 1000,
                                context.getString(R.string.atto),
                                context.getString(R.string.watts));
                    } else {
                        powerString = context.getString(R.string.value_with_prefixed_unit,
                                zeptoPower / 1000000,
                                context.getString(R.string.femto),
                                context.getString(R.string.watts));
                    }
                    break;
            }
        } else {
            switch (PrefsManager.getUploadPowerUnit(context)) {
                case "1":
                    // convert from milliwatts to dbm
                    if (power == 0) {
                        powerString = "-∞ " + context.getString(R.string.decibel_milliwatts);
                    } else {
                        powerString = context.getString(R.string.value_with_unit,
                                10 * Math.log10(power),
                                context.getString(R.string.decibel_milliwatts));
                    }
                    break;
                default:
                case "2":
                    powerString = context.getString(R.string.value_with_prefixed_unit,
                            (double) power / 1000000,
                            context.getString(R.string.kilo),
                            context.getString(R.string.watts));
                    break;
            }
        }
        return context.getString(R.string.power, powerString);
    }

    /**
     * Convert a round-trip light time value to string.
     * @param rtlt the round-trip light time of the target, in microseconds
     * @return the round-trip light time string, e.g. "12.34 d"
     */
    private String getRTLTString(long rtlt) {
        String rtltString;
        if (rtlt < 0) {
            rtltString = context.getString(R.string.no_data);
        } else if (rtlt < 60000000) {
            rtltString = context.getString(R.string.value_with_unit,
                    (double) rtlt / 1000000,
                    context.getString(R.string.seconds));
        } else if (rtlt < 3600000000l) {
            rtltString = context.getString(R.string.value_with_unit,
                    (double) rtlt / 60000000,
                    context.getString(R.string.minutes));
        } else if (rtlt < 86400000000l) {
            rtltString = context.getString(R.string.value_with_unit,
                    (double) rtlt / 3600000000l,
                    context.getString(R.string.hours));
        } else {
            rtltString = context.getString(R.string.value_with_unit,
                    (double) rtlt / 86400000000l,
                    context.getString(R.string.days));
        }
        return context.getString(R.string.round_trip_light_time, rtltString);
    }

    /**
     * Convert a range value to string. The units are read from the context's preferences.
     * @param range the range of the target, in meters
     * @param isUp true if this is upleg range, false if this is a downleg range
     * @return the range string, e.g. "12.34M km"
     */
    private String getRangeString(long range, boolean isUp) {
        String unit;
        double rangeValue = range;
        switch (PrefsManager.getRangeUnit(context)) {
            default:
            case "1":
                unit = context.getString(R.string.kilometers);
                rangeValue /= 1000;
                break;
            case "2":
                unit = context.getString(R.string.miles);
                rangeValue /= 1609.344;
                break;
            case "3":
                unit = context.getString(R.string.astronomical_units);
                rangeValue /= 149597870700l;
                break;
        }

        String rangeString;
        if (rangeValue < 0) {
            rangeString = context.getString(R.string.no_data);
        } else if (rangeValue < 1) {
            rangeString = "<1.0 " + unit;
        }  else if (rangeValue < 1000) {
            rangeString = context.getString(R.string.value_with_suffix_and_unit,
                    rangeValue,
                    "", unit);
        } else if (rangeValue < 1000000) {
            rangeString = context.getString(R.string.value_with_suffix_and_unit,
                    rangeValue / 1000,
                    context.getString(R.string.thousand), unit);
        } else if (rangeValue < 1000000000) {
            rangeString = context.getString(R.string.value_with_suffix_and_unit,
                    rangeValue / 1000000,
                    context.getString(R.string.million), unit);
        } else  {
            rangeString = context.getString(R.string.value_with_suffix_and_unit,
                    rangeValue / 1000000000,
                    context.getString(R.string.billion), unit);
        }

        int direction = isUp ? R.string.upleg_range : R.string.downleg_range;
        return context.getString(direction, rangeString);
    }
}