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
package org.kordamp.basilisk.runtime.util;

import basilisk.core.resources.ResourceHandler;
import basilisk.util.PropertiesReader;
import basilisk.util.PropertiesResourceBundle;
import basilisk.util.ResourceBundleReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import static basilisk.util.BasiliskNameUtils.requireNonBlank;

/**
 * @author Andres Almiray
 */
public class DefaultCompositeResourceBundleBuilder extends AbstractCompositeResourceBundleBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultCompositeResourceBundleBuilder.class);
    protected static final String PROPERTIES_SUFFIX = ".properties";
    protected static final String CLASS_SUFFIX = ".class";

    protected final PropertiesReader propertiesReader;
    protected final ResourceBundleReader resourceBundleReader;

    @Inject
    public DefaultCompositeResourceBundleBuilder(@Nonnull ResourceHandler resourceHandler, @Nonnull PropertiesReader propertiesReader, @Nonnull ResourceBundleReader resourceBundleReader) {
        super(resourceHandler);
        this.propertiesReader = propertiesReader;
        this.resourceBundleReader = resourceBundleReader;
    }

    @Nonnull
    protected Collection<ResourceBundle> loadBundlesFor(@Nonnull String fileName) {
        requireNonBlank(fileName, ERROR_FILENAME_BLANK);
        List<ResourceBundle> bundles = new ArrayList<>();
        bundles.addAll(loadBundleFromClass(fileName));
        bundles.addAll(loadBundleFromProperties(fileName));
        return bundles;
    }

    @Nonnull
    protected Collection<ResourceBundle> loadBundleFromProperties(@Nonnull String fileName) {
        requireNonBlank(fileName, ERROR_FILENAME_BLANK);
        List<ResourceBundle> bundles = new ArrayList<>();
        List<URL> resources = getResources(fileName, PROPERTIES_SUFFIX);
        if (resources != null) {
            for (URL resource : resources) {
                if (null == resource) { continue; }
                try {
                    Properties properties = propertiesReader.load(resource.openStream());
                    bundles.add(new PropertiesResourceBundle(properties));
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return bundles;
    }

    @Nonnull
    protected Collection<ResourceBundle> loadBundleFromClass(@Nonnull String fileName) {
        List<ResourceBundle> bundles = new ArrayList<>();
        URL resource = getResourceAsURL(fileName, CLASS_SUFFIX);
        if (null != resource) {
            String url = resource.toString();
            String className = fileName.replace('/', '.');
            try {
                Class<?> klass = loadClass(className);
                if (ResourceBundle.class.isAssignableFrom(klass)) {
                    bundles.add(resourceBundleReader.read(newInstance(klass)));
                }
            } catch (ClassNotFoundException e) {
                // ignore
            } catch (Exception e) {
                LOG.warn("An error occurred while loading resource bundle " + fileName + " from " + url, e);
            }
        }
        return bundles;
    }

    protected Class<?> loadClass(String className) throws ClassNotFoundException {
        return getResourceHandler().classloader().loadClass(className);
    }

    protected ResourceBundle newInstance(Class<?> klass) throws IllegalAccessException, InstantiationException {
        return (ResourceBundle) klass.newInstance();
    }
}
