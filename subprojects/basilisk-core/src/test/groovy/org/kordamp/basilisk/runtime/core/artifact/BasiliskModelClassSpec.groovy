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
package org.kordamp.basilisk.runtime.core.artifact

import basilisk.core.artifact.BasiliskModelClass
import integration.SimpleModel
import integration.TestBasiliskApplication
import spock.lang.Shared
import spock.lang.Specification

class BasiliskModelClassSpec extends Specification {
    @Shared
    private BasiliskModelClass basiliskClass = new DefaultBasiliskModelClass(new TestBasiliskApplication(), SimpleModel)

    void 'Get and Set properties on model instance'() {
        given:
        SimpleModel model = new SimpleModel()
        model.application = new TestBasiliskApplication()

        // expect:
        assert basiliskClass.getModelPropertyValue(model, 'value1') == null
        assert basiliskClass.getModelPropertyValue(model, 'value2') == null

        when:
        basiliskClass.setModelPropertyValue(model, 'value1', 'value1')
        basiliskClass.setModelPropertyValue(model, 'value2', 'value2')

        then:
        basiliskClass.getModelPropertyValue(model, 'value1') == 'value1'
        basiliskClass.getModelPropertyValue(model, 'value2') == 'value2'
    }
}
