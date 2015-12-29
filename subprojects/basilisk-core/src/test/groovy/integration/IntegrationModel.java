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
package integration;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.kordamp.basilisk.runtime.core.artifact.AbstractBasiliskModel;

public class IntegrationModel extends AbstractBasiliskModel {
    private StringProperty input;
    private StringProperty output;

    public String getInput() {
        return inputProperty().get();
    }

    public StringProperty inputProperty() {
        if (input == null) {
            input = new SimpleStringProperty(this, "input");
        }
        return input;
    }

    public void setInput(String input) {
        this.inputProperty().set(input);
    }

    public String getOutput() {
        return outputProperty().get();
    }

    public StringProperty outputProperty() {
        if (output == null) {
            output = new SimpleStringProperty(this, "output");
        }
        return output;
    }

    public void setOutput(String output) {
        this.outputProperty().set(output);
    }
}
