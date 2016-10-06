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

import basilisk.inject.MVCMember;
import org.kordamp.basilisk.runtime.core.artifact.AbstractBasiliskController;

import javax.annotation.Nonnull;
import java.util.Map;

public class RootController extends AbstractBasiliskController {
    private RootModel model;
    private RootView view;

    @MVCMember
    public void setModel(RootModel model) {
        this.model = model;
    }

    @MVCMember
    public void setView(RootView view) {
        this.view = view;
    }

    @Override
    public void mvcGroupInit(@Nonnull Map<String, Object> args) {
        getMvcGroup().getContext().put("KEY", "VALUE");
    }
}
