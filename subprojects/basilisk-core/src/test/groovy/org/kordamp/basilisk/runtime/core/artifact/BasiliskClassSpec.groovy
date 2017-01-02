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
package org.kordamp.basilisk.runtime.core.artifact

import basilisk.core.artifact.BasiliskClass
import integration.SimpleModel
import integration.TestBasiliskApplication
import spock.lang.Shared
import spock.lang.Specification

class BasiliskClassSpec extends Specification {
    @Shared
    private BasiliskClass basiliskClass = new DefaultBasiliskModelClass(new TestBasiliskApplication(), SimpleModel)

    void 'Verify properties'() {
        expect:
        basiliskClass.artifactType == 'model'
        basiliskClass.clazz == SimpleModel
        basiliskClass.fullName == 'integration.SimpleModel'
        basiliskClass.logicalPropertyName == 'simple'
        basiliskClass.name == 'Simple'
        basiliskClass.naturalName == 'Simple Model'
        basiliskClass.packageName == 'integration'
        basiliskClass.propertyName == 'simpleModel'
        basiliskClass.shortName == 'SimpleModel'
    }
}
