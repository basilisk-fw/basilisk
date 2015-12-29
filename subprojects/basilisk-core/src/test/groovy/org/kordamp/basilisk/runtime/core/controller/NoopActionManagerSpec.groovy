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
package org.kordamp.basilisk.runtime.core.controller

import basilisk.core.artifact.BasiliskController
import basilisk.core.controller.ActionManager
import integration.InvokeActionHandler
import integration.SimpleController
import integration.TestBasiliskApplication
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class NoopActionManagerSpec extends Specification {
    void 'Verify NoopActionManager'() {
        given:
        ActionManager actionManager = new NoopActionManager()
        BasiliskController controller = new SimpleController()
        controller.application = new TestBasiliskApplication()

        when:
        actionManager.createActions(controller)
        actionManager.invokeAction(controller, 'sayHello')
        actionManager.addActionHandler(new InvokeActionHandler())

        then:
        !actionManager.actionsFor(controller)
        !actionManager.actionFor(controller, 'sayHello')
        'sayHello' == actionManager.normalizeName('sayHello')
        'sayHello' == actionManager.normalizeName('sayHelloAction')
    }
}
