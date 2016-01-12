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
package sample.javafx.java;

import basilisk.javafx.test.BasiliskTestFXRule;
import org.junit.Rule;
import org.junit.Test;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

public class SampleIntegrationTest {
    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
    }

    @Rule
    public BasiliskTestFXRule testfx = new BasiliskTestFXRule("mainWindow");

    @Test
    public void typeNameAndClickButton() {
        // given:
        testfx.clickOn("#input").write("Basilisk");

        // when:
        testfx.clickOn("#sayHelloActionTarget");

        // then:
        verifyThat("#output", hasText("Hello Basilisk"));
    }

    @Test
    public void doNotTypeNameAndClickButton() {
        // given:
        testfx.clickOn("#input").write("");

        // when:
        testfx.clickOn("#sayHelloActionTarget");

        // then:
        verifyThat("#output", hasText("Howdy stranger!"));
    }
}