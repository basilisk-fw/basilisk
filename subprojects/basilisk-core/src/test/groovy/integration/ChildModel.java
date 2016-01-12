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

import basilisk.core.mvc.MVCGroup;
import org.kordamp.basilisk.runtime.core.artifact.AbstractBasiliskModel;

public class ChildModel extends AbstractBasiliskModel {
    private MVCGroup parentGroup;
    private RootModel parentModel;

    public MVCGroup getParentGroup() {
        return parentGroup;
    }

    public void setParentGroup(MVCGroup parentGroup) {
        this.parentGroup = parentGroup;
    }

    public RootModel getParentModel() {
        return parentModel;
    }

    public void setParentModel(RootModel parentModel) {
        this.parentModel = parentModel;
    }
}
