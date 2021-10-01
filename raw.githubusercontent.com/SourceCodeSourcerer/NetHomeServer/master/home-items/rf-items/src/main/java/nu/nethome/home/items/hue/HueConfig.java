/*
 * Copyright (C) 2005-2014, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome  (http://www.nethome.nu)
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nu.nethome.home.items.hue;

import org.json.JSONObject;

/**
 *
 */
public class HueConfig {

    private final String name;
    private final String swVersion;
    private final String mac;

    public HueConfig(String name, String swVersion, String mac) {
        this.name = name;
        this.swVersion = swVersion;
        this.mac = mac;
    }

    public HueConfig(JSONObject data) {
        this.name = data.getString("name");
        this.swVersion = data.getString("swversion");
        this.mac = data.getString("mac");
    }

    public String getName() {
        return name;
    }

    public String getSwVersion() {
        return swVersion;
    }

    public String getMac() {
        return mac;
    }
}
