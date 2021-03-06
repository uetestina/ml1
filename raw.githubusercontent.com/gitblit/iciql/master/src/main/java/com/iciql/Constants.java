/*
 * Copyright 2011 James Moger.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iciql;

import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Iciql constants.
 */
public class Constants {

    public static final String NAME = "iciql";

    // The build script extracts this exact line so be careful editing it
    // and only use A-Z a-z 0-9 .-_ in the string.
    public static final String API_CURRENT = "15";

    public static String getVersion() {
        return getManifestValue("implementation-version", "0.0.0-SNAPSHOT");
    }

    public static String getBuildDate() {
        return getManifestValue("build-date", "PENDING");
    }

    private static String getManifestValue(String attrib, String defaultValue) {
        Class<?> clazz = Constants.class;
        String className = clazz.getSimpleName() + ".class";
        String classPath = clazz.getResource(className).toString();
        try {
            String manifestPath;
            if (classPath.indexOf('!') > -1) {
                manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
            } else {
                String pkgPath = "/" + clazz.getPackage().getName().replace('.', '/');
                manifestPath = classPath.substring(0, classPath.indexOf(pkgPath)) + "/META-INF/MANIFEST.MF";
            }
            Manifest manifest = new Manifest(new URL(manifestPath).openStream());
            Attributes attr = manifest.getMainAttributes();
            String value = attr.getValue(attrib);
            return value;
        } catch (Exception e) {
        }
        return defaultValue;
    }
}
