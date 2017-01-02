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
package basilisk.core.env

import org.kordamp.basilisk.runtime.core.env.EnvironmentProvider

public class EnvironmentTests extends GroovyTestCase {
    protected void tearDown() {
        System.setProperty(Environment.KEY, '')
    }

    void testGetCurrent() {
        System.setProperty('basilisk.env', 'prod')

        assert Environment.PRODUCTION == getCurrentEnvironment(null)

        System.setProperty('basilisk.env', 'dev')

        assert Environment.DEVELOPMENT == getCurrentEnvironment(null)

        System.setProperty('basilisk.env', 'soe')

        assert Environment.CUSTOM == getCurrentEnvironment(null)

    }

    void testGetEnvironment() {
        assert Environment.DEVELOPMENT == Environment.resolveEnvironment('dev')
        assert Environment.TEST == Environment.resolveEnvironment('test')
        assert Environment.PRODUCTION == Environment.resolveEnvironment('prod')
        assert !Environment.resolveEnvironment('doesntexist')
    }

    void testSystemPropertyOverridesMetadata() {
        Metadata metadata = new Metadata(new ByteArrayInputStream('basilisk.env=production'.bytes))

        assert Environment.PRODUCTION == getCurrentEnvironment(metadata)

        System.setProperty('basilisk.env', 'dev')

        assert Environment.DEVELOPMENT == getCurrentEnvironment(metadata)

        System.setProperty('basilisk.env', '')

        assert Environment.PRODUCTION == getCurrentEnvironment(metadata)

        metadata = new Metadata(new ByteArrayInputStream(''.bytes))

        assert Environment.DEVELOPMENT == getCurrentEnvironment(metadata)
    }

    private static Environment getCurrentEnvironment(Metadata metadata) {
        EnvironmentProvider provider = new EnvironmentProvider()
        provider.@metadata = metadata
        return provider.get()
    }
}
