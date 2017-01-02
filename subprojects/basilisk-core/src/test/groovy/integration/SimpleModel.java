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
package integration;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.kordamp.basilisk.runtime.core.artifact.AbstractBasiliskModel;

public class SimpleModel extends AbstractBasiliskModel {
    private StringProperty value1;
    private StringProperty value2;

    public String getValue1() {
        return value1Property().get();
    }

    public StringProperty value1Property() {
        if (value1 == null) {
            value1 = new SimpleStringProperty(this, "value1");
        }
        return value1;
    }

    public void setValue1(String value1) {
        value1Property().set(value1);
    }

    public String getValue2() {
        return value2Property().get();
    }

    public StringProperty value2Property() {
        if (value2 == null) {
            value2 = new SimpleStringProperty(this, "value2");
        }
        return value2;
    }

    public void setValue2(String value2) {
        value2Property().set(value2);
    }
}
