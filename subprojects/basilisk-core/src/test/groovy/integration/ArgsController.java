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

import org.kordamp.basilisk.runtime.core.artifact.AbstractBasiliskController;

import javax.annotation.Nonnull;
import java.util.Map;

public class ArgsController extends AbstractBasiliskController {
    private ArgsModel model;
    private ArgsView view;
    @Nonnull
    private String arg1;
    private String arg2;

    public void setModel(ArgsModel model) {
        this.model = model;
    }

    public void setView(ArgsView view) {
        this.view = view;
    }

    @Nonnull
    public String getArg1() {
        return arg1;
    }

    public String getArg2() {
        return arg2;
    }

    public void setArg2(@Nonnull String arg2) {
        this.arg2 = arg2;
    }

    @Override
    public void mvcGroupInit(@Nonnull Map<String, Object> args) {
        getMvcGroup().getContext().put("KEY", "VALUE");
    }
}