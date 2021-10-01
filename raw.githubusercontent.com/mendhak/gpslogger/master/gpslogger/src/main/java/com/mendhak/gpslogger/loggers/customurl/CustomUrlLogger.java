/*
 * Copyright (C) 2016 mendhak
 *
 * This file is part of GPSLogger for Android.
 *
 * GPSLogger for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * GPSLogger for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GPSLogger for Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mendhak.gpslogger.loggers.customurl;

import android.location.Location;

import com.birbit.android.jobqueue.JobManager;
import com.mendhak.gpslogger.common.AppSettings;
import com.mendhak.gpslogger.common.PreferenceHelper;
import com.mendhak.gpslogger.common.SerializableLocation;
import com.mendhak.gpslogger.common.Session;
import com.mendhak.gpslogger.common.Strings;
import com.mendhak.gpslogger.common.Systems;
import com.mendhak.gpslogger.common.events.UploadEvents;
import com.mendhak.gpslogger.loggers.FileLogger;


import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;

public class CustomUrlLogger implements FileLogger {

    private final String name = "URL";
    private final String customLoggingUrl;
    private final int batteryLevel;
    private final String httpMethod;
    private final String httpBody;
    private final String httpHeaders;
    private final String basicAuthUsername;
    private final String basicAuthPassword;

    public CustomUrlLogger(String customLoggingUrl, int batteryLevel, String httpMethod, String httpBody, String httpHeaders) {
        this(customLoggingUrl,batteryLevel, httpMethod, httpBody, httpHeaders, "","");

    }

    public CustomUrlLogger(String customLoggingUrl, int batteryLevel, String httpMethod, String httpBody, String httpHeaders, String basicAuthUsername, String basicAuthPassword) {
        this.customLoggingUrl = customLoggingUrl;
        this.batteryLevel = batteryLevel;
        this.httpMethod = httpMethod;
        this.httpBody = httpBody;
        this.httpHeaders = httpHeaders;
        this.basicAuthUsername = basicAuthUsername;
        this.basicAuthPassword = basicAuthPassword;
    }

    @Override
    public void write(Location loc) throws Exception {
        if (!Session.getInstance().hasDescription()) {
            annotate("", loc);
        }
    }

    @Override
    public void annotate(String description, Location loc) throws Exception {

        String finalUrl = getFormattedTextblock(customLoggingUrl, loc, description, Systems.getAndroidId(), batteryLevel, Strings.getBuildSerial(),
                Session.getInstance().getStartTimeStamp(), Session.getInstance().getCurrentFormattedFileName(), PreferenceHelper.getInstance().getCurrentProfileName(), Session.getInstance().getTotalTravelled());
        String finalBody = getFormattedTextblock(httpBody, loc, description, Systems.getAndroidId(), batteryLevel, Strings.getBuildSerial(),
                Session.getInstance().getStartTimeStamp(), Session.getInstance().getCurrentFormattedFileName(), PreferenceHelper.getInstance().getCurrentProfileName(), Session.getInstance().getTotalTravelled());
        String finalHeaders = getFormattedTextblock(httpHeaders, loc, description, Systems.getAndroidId(), batteryLevel, Strings.getBuildSerial(),
                Session.getInstance().getStartTimeStamp(), Session.getInstance().getCurrentFormattedFileName(), PreferenceHelper.getInstance().getCurrentProfileName(), Session.getInstance().getTotalTravelled());


        JobManager jobManager = AppSettings.getJobManager();
        jobManager.addJobInBackground(new CustomUrlJob(new CustomUrlRequest(finalUrl,httpMethod, finalBody, finalHeaders, basicAuthUsername, basicAuthPassword), new UploadEvents.CustomUrl()));
    }


    public String getFormattedTextblock(String customLoggingUrl, Location loc, String description, String androidId,
                                        float batteryLevel, String buildSerial, long sessionStartTimeStamp, String fileName, String profileName, double distance)
            throws Exception {

        String logUrl = customLoggingUrl;
        SerializableLocation sLoc = new SerializableLocation(loc);
        logUrl = logUrl.replaceAll("(?i)%lat", String.valueOf(sLoc.getLatitude()));
        logUrl = logUrl.replaceAll("(?i)%lon", String.valueOf(sLoc.getLongitude()));
        logUrl = logUrl.replaceAll("(?i)%sat", String.valueOf(sLoc.getSatelliteCount()));
        logUrl = logUrl.replaceAll("(?i)%desc", String.valueOf(URLEncoder.encode(Strings.htmlDecode(description), "UTF-8")));
        logUrl = logUrl.replaceAll("(?i)%alt", String.valueOf(sLoc.getAltitude()));
        logUrl = logUrl.replaceAll("(?i)%acc", String.valueOf(sLoc.getAccuracy()));
        logUrl = logUrl.replaceAll("(?i)%dir", String.valueOf(sLoc.getBearing()));
        logUrl = logUrl.replaceAll("(?i)%prov", String.valueOf(sLoc.getProvider()));
        logUrl = logUrl.replaceAll("(?i)%spd", String.valueOf(sLoc.getSpeed()));
        logUrl = logUrl.replaceAll("(?i)%timestamp", String.valueOf(sLoc.getTime()/1000));
        logUrl = logUrl.replaceAll("(?i)%time", String.valueOf(Strings.getIsoDateTime(new Date(sLoc.getTime()))));
        logUrl = logUrl.replaceAll("(?i)%date", String.valueOf(Strings.getIsoCalendarDate(new Date(sLoc.getTime()))));
        logUrl = logUrl.replaceAll("(?i)%starttimestamp", String.valueOf(sessionStartTimeStamp/1000));
        logUrl = logUrl.replaceAll("(?i)%batt", String.valueOf(batteryLevel));
        logUrl = logUrl.replaceAll("(?i)%aid", String.valueOf(androidId));
        logUrl = logUrl.replaceAll("(?i)%ser", String.valueOf(buildSerial));
        logUrl = logUrl.replaceAll("(?i)%act", String.valueOf(sLoc.getDetectedActivity()));
        logUrl = logUrl.replaceAll("(?i)%filename", fileName);
        logUrl = logUrl.replaceAll("(?i)%profile",URLEncoder.encode(profileName, "UTF-8"));
        logUrl = logUrl.replaceAll("(?i)%hdop", sLoc.getHDOP());
        logUrl = logUrl.replaceAll("(?i)%vdop", sLoc.getVDOP());
        logUrl = logUrl.replaceAll("(?i)%pdop", sLoc.getPDOP());
        logUrl = logUrl.replaceAll("(?i)%dist", String.valueOf((int)distance));

        return logUrl;
    }


    @Override
    public String getName() {
        return name;
    }
}


