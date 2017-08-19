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
package org.kordamp.basilisk.runtime.core;

import basilisk.core.CallableWithArgs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author Andres Almiray
 */
public class DefaultBasiliskApplication extends AbstractBasiliskApplication {
    private CallableWithArgs<?> containerGenerator;

    public DefaultBasiliskApplication() {
        this(EMPTY_ARGS);
    }

    public DefaultBasiliskApplication(@Nonnull String[] args) {
        super(args);
    }

    @Nullable
    public CallableWithArgs<?> getContainerGenerator() {
        return containerGenerator;
    }

    public void setContainerGenerator(@Nullable CallableWithArgs<?> containerGenerator) {
        this.containerGenerator = containerGenerator;
    }

    @Nonnull
    @Override
    public Object createApplicationContainer(@Nonnull Map<String, Object> attributes) {
        if (containerGenerator != null) {
            return containerGenerator.call(attributes);
        }
        return new Object();
    }
}
