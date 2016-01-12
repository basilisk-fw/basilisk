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

import basilisk.javafx.test.BasiliskTestFXClassRule;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SampleFunctionalTest {
    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
    }

    @ClassRule
    public static BasiliskTestFXClassRule testfx = new BasiliskTestFXClassRule("mainWindow");

    @Test
    public void doNotTypeNameAndClickButton() {
        // given:
        testfx.clickOn("#input").write("");

        // when:
        testfx.clickOn("#sayHelloActionTarget");

        // then:
        verifyThat("#output", hasText("Howdy stranger!"));
    }

    @Test
    public void typeNameAndClickButton() {
        // given:
        testfx.clickOn("#input").write("Basilisk");

        // when:
        testfx.clickOn("#sayHelloActionTarget");

        // then:
        verifyThat("#output", hasText("Hello Basilisk"));
    }
}