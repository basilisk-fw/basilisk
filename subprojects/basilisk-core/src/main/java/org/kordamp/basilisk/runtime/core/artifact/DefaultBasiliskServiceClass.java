/*
 * Copyright 2008-2016 the original author or authors.
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
package org.kordamp.basilisk.runtime.core.artifact;

import basilisk.core.BasiliskApplication;
import basilisk.core.artifact.BasiliskServiceClass;
import basilisk.util.BasiliskClassUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Andres Almiray
 */
public class DefaultBasiliskServiceClass extends DefaultBasiliskClass implements BasiliskServiceClass {
    protected final Set<String> serviceCache = new TreeSet<>();

    public DefaultBasiliskServiceClass(@Nonnull BasiliskApplication application, @Nonnull Class<?> clazz) {
        super(application, clazz, TYPE, TRAILING);
    }

    public void resetCaches() {
        super.resetCaches();
        serviceCache.clear();
    }

    @Nonnull
    public String[] getServiceNames() {
        if (serviceCache.isEmpty()) {
            for (Method method : getClazz().getMethods()) {
                String methodName = method.getName();
                if (!serviceCache.contains(methodName) &&
                    BasiliskClassUtils.isPlainMethod(method) &&
                    !BasiliskClassUtils.isEventHandler(methodName)) {
                    serviceCache.add(methodName);
                }
            }
        }

        return serviceCache.toArray(new String[serviceCache.size()]);
    }
}
