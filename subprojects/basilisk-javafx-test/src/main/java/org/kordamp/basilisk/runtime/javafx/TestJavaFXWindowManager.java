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
package org.kordamp.basilisk.runtime.javafx;

import basilisk.core.BasiliskApplication;
import basilisk.javafx.JavaFXWindowDisplayHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;

import static basilisk.javafx.test.TestContext.getTestContext;
import static basilisk.util.BasiliskNameUtils.isBlank;

/**
 * @author Andres Almiray
 */
public class TestJavaFXWindowManager extends DefaultJavaFXWindowManager {
    public TestJavaFXWindowManager(@Nonnull BasiliskApplication application, @Nonnull @Named("windowDisplayHandler") JavaFXWindowDisplayHandler windowDisplayHandler) {
        super(application, windowDisplayHandler);
    }

    @Nullable
    @Override
    protected Object resolveStartingWindowFromConfiguration() {
        String startingWindowName = getTestContext().getWindowName();
        return !isBlank(startingWindowName) ? startingWindowName : super.resolveStartingWindowFromConfiguration();
    }
}