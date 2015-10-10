/*
 * Copyright 2008-2015 the original author or authors.
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
package org.kordamp.basilisk.runtime.core;

import basilisk.core.BasiliskApplication;
import com.apple.mrj.MRJAboutHandler;
import com.apple.mrj.MRJApplicationUtils;
import com.apple.mrj.MRJPrefsHandler;
import com.apple.mrj.MRJQuitHandler;

import javax.annotation.Nonnull;

import static basilisk.util.BasiliskNameUtils.capitalize;
import static java.util.Arrays.asList;

/**
 * Handles Linux integration.
 *
 * @author Andres Almiray
 */
public class DefaultMacOSXPlatformHandler extends DefaultPlatformHandler {
    @Override
    public void handle(@Nonnull BasiliskApplication application) {
        super.handle(application);

        // use unified menu bar
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        // set menu bar title
        String title = application.getConfiguration().getAsString("application.title", "Basilisk");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", capitalize(title));


        boolean skipAbout = application.getConfiguration().getAsBoolean("osx.noabout", false);
        boolean skipPrefs = application.getConfiguration().getAsBoolean("osx.noprefs", false);
        boolean skipQuit = application.getConfiguration().getAsBoolean("osx.noquit", false);

        BasiliskMacOSXSupport handler = new BasiliskMacOSXSupport(application, skipQuit);
        if (!skipAbout) MRJApplicationUtils.registerAboutHandler(handler);
        if (!skipPrefs) MRJApplicationUtils.registerPrefsHandler(handler);
        MRJApplicationUtils.registerQuitHandler(handler);
    }

    private static class BasiliskMacOSXSupport implements MRJAboutHandler, MRJQuitHandler, MRJPrefsHandler {
        private final BasiliskApplication application;
        private final boolean noquit;

        private BasiliskMacOSXSupport(@Nonnull BasiliskApplication application, boolean noquit) {
            this.application = application;
            this.noquit = noquit;
        }

        @Override
        public void handleAbout() {
            application.getEventRouter().publishEvent("OSXAbout", asList(application));
        }

        @Override
        public void handlePrefs() throws IllegalStateException {
            application.getEventRouter().publishEvent("OSXPrefs", asList(application));
        }

        @Override
        public void handleQuit() {
            if (noquit) {
                application.getEventRouter().publishEvent("OSXQuit", asList(application));
            } else {
                application.shutdown();
            }
        }
    }
}