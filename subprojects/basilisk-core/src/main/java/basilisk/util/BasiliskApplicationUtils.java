/*
 * Copyright 2008-2017 the original author or authors.
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
package basilisk.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

import static basilisk.util.BasiliskNameUtils.isBlank;

/**
 * Assorted utility methods and constants.
 *
 * @author Andres Almiray
 */
public final class BasiliskApplicationUtils {
    private BasiliskApplicationUtils() {
    }

    public static final boolean isWindows;
    public static final boolean isWindows95;
    public static final boolean isWindows98;
    public static final boolean isWindowsNT;
    public static final boolean isWindows2000;
    public static final boolean isWindows2003;
    public static final boolean isWindowsXP;
    public static final boolean isWindowsVista;
    public static final boolean isWindows7;
    public static final boolean isWindows8;
    public static final boolean isWindows10;

    public static final boolean isIOS;

    /**
     * True if running Linux, Solaris or MacOSX
     */
    public static final boolean isUnix;

    public static final boolean isLinux;
    public static final boolean isSolaris;
    public static final boolean isMacOSX;

    public static final String osArch;
    public static final String osName;
    public static final String osVersion;
    public static final String javaVersion;
    public static final boolean is64Bit;

    public static final boolean isJdk8;
    public static final boolean isJdk9;

    public static final String platform;
    public static final String basePlatform;

    static {
        osArch = System.getProperty("os.arch");
        osName = System.getProperty("os.name");
        is64Bit = osArch.contains("64");

        if (osName.contains("Windows")) {
            basePlatform = "windows";
            isWindows = true;
            isLinux = false;
            isUnix = false;
            isMacOSX = false;
            isSolaris = false;
            isIOS = false;
            if (osName.contains("95")) {
                isWindows95 = true;
                isWindows98 = false;
                isWindowsNT = false;
                isWindows2000 = false;
                isWindows2003 = false;
                isWindowsXP = false;
                isWindowsVista = false;
                isWindows7 = false;
                isWindows8 = false;
                isWindows10 = false;
            } else if (osName.contains("98")) {
                isWindows95 = false;
                isWindows98 = true;
                isWindowsNT = false;
                isWindows2000 = false;
                isWindows2003 = false;
                isWindowsXP = false;
                isWindowsVista = false;
                isWindows7 = false;
                isWindows8 = false;
                isWindows10 = false;
            } else if (osName.contains("NT")) {
                isWindows95 = false;
                isWindows98 = false;
                isWindowsNT = false;
                isWindows2000 = true;
                isWindows2003 = false;
                isWindowsXP = false;
                isWindowsVista = false;
                isWindows7 = false;
                isWindows8 = false;
                isWindows10 = false;
            } else if (osName.contains("2003")) {
                isWindows95 = false;
                isWindows98 = false;
                isWindowsNT = false;
                isWindows2000 = false;
                isWindows2003 = true;
                isWindowsXP = true;
                isWindowsVista = false;
                isWindows7 = false;
                isWindows8 = false;
                isWindows10 = false;
            } else if (osName.contains("XP")) {
                isWindows95 = false;
                isWindows98 = false;
                isWindowsNT = true;
                isWindows2000 = true;
                isWindows2003 = true;
                isWindowsXP = false;
                isWindowsVista = false;
                isWindows7 = false;
                isWindows8 = false;
                isWindows10 = false;
            } else if (osName.contains("Vista")) {
                isWindows95 = false;
                isWindows98 = false;
                isWindowsNT = false;
                isWindows2000 = false;
                isWindows2003 = false;
                isWindowsXP = false;
                isWindowsVista = true;
                isWindows7 = false;
                isWindows8 = false;
                isWindows10 = false;
            } else if (osName.contains("Windows 7")) {
                isWindows95 = false;
                isWindows98 = false;
                isWindowsNT = false;
                isWindows2000 = false;
                isWindows2003 = false;
                isWindowsXP = false;
                isWindowsVista = false;
                isWindows7 = true;
                isWindows8 = false;
                isWindows10 = false;
            } else if (osName.equals("Windows 8")) {
                isWindows95 = false;
                isWindows98 = false;
                isWindowsNT = false;
                isWindows2000 = false;
                isWindows2003 = false;
                isWindowsXP = false;
                isWindowsVista = false;
                isWindows7 = false;
                isWindows8 = true;
                isWindows10 = false;
            } else if (osName.equals("Windows 8.1") || osName.equals("Windows 10")) {
                isWindows95 = false;
                isWindows98 = false;
                isWindowsNT = false;
                isWindows2000 = false;
                isWindows2003 = false;
                isWindowsXP = false;
                isWindowsVista = false;
                isWindows7 = false;
                isWindows8 = false;
                isWindows10 = true;
            } else {
                isWindows95 = false;
                isWindows98 = false;
                isWindowsNT = false;
                isWindows2000 = false;
                isWindows2003 = false;
                isWindowsXP = false;
                isWindowsVista = false;
                isWindows7 = false;
                isWindows8 = false;
                isWindows10 = false;
            }
        } else if (osName.contains("Linux")) {
            basePlatform = "linux";
            isWindows = false;
            isLinux = true;
            isUnix = true;
            isMacOSX = false;
            isSolaris = false;
            isWindows95 = false;
            isWindows98 = false;
            isWindowsNT = false;
            isWindows2000 = false;
            isWindows2003 = false;
            isWindowsXP = false;
            isWindowsVista = false;
            isWindows7 = false;
            isWindows8 = false;
            isWindows10 = false;
            isIOS = false;
        } else if (osName.contains("Solaris") || osName.contains("SunOS")) {
            basePlatform = "solaris";
            isWindows = false;
            isLinux = false;
            isUnix = true;
            isMacOSX = false;
            isSolaris = true;
            isWindows95 = false;
            isWindows98 = false;
            isWindowsNT = false;
            isWindows2000 = false;
            isWindows2003 = false;
            isWindowsXP = false;
            isWindowsVista = false;
            isWindows7 = false;
            isWindows8 = false;
            isWindows10 = false;
            isIOS = false;
        } else if (osName.contains("Mac OS")) {
            basePlatform = "macosx";
            isWindows = false;
            isLinux = false;
            isUnix = true;
            isMacOSX = true;
            isSolaris = false;
            isWindows95 = false;
            isWindows98 = false;
            isWindowsNT = false;
            isWindows2000 = false;
            isWindows2003 = false;
            isWindowsXP = false;
            isWindowsVista = false;
            isWindows7 = false;
            isWindows8 = false;
            isWindows10 = false;
            isIOS = false;
        } else if (osName.contains("iOS")) {
            basePlatform = "ios";
            isWindows = false;
            isLinux = false;
            isUnix = true;
            isMacOSX = true;
            isSolaris = false;
            isWindows95 = false;
            isWindows98 = false;
            isWindowsNT = false;
            isWindows2000 = false;
            isWindows2003 = false;
            isWindowsXP = false;
            isWindowsVista = false;
            isWindows7 = false;
            isWindows8 = false;
            isWindows10 = false;
            isIOS = true;
        } else {
            basePlatform = "unknown";
            isWindows = false;
            isLinux = false;
            isUnix = false;
            isMacOSX = false;
            isSolaris = false;
            isWindows95 = false;
            isWindows98 = false;
            isWindowsNT = false;
            isWindows2000 = false;
            isWindows2003 = false;
            isWindowsXP = false;
            isWindowsVista = false;
            isWindows7 = false;
            isWindows8 = false;
            isWindows10 = false;
            isIOS = false;
        }

        osVersion = System.getProperty("os.version");
        javaVersion = System.getProperty("java.version");
        String version = "0";
        if (javaVersion != null && javaVersion.length() > 1) {
            version = javaVersion.substring(0, 3);
        } else if (isIOS) {
            version = "1.8";
        }

        if (version.startsWith("9")) {
            isJdk9 = true;
            isJdk8 = true;
        } else {
            isJdk9 = false;
            isJdk8 = false;
        }

        platform = basePlatform + (is64Bit && !isSolaris ? "64" : "");
    }

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public static Locale parseLocale(@Nullable String locale) {
        if (isBlank(locale)) { return Locale.getDefault(); }
        String[] parts = locale.split("_");
        switch (parts.length) {
            case 1:
                return new Locale(parts[0]);
            case 2:
                return new Locale(parts[0], parts[1]);
            case 3:
                return new Locale(parts[0], parts[1], parts[2]);
            default:
                return Locale.getDefault();
        }
    }
}
