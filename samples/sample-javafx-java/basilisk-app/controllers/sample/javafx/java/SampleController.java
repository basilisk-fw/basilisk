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
package sample.javafx.java;

import basilisk.core.artifact.BasiliskController;
import basilisk.inject.MVCMember;
import basilisk.metadata.ArtifactProviderFor;
import org.kordamp.basilisk.runtime.core.artifact.AbstractBasiliskController;

import javax.annotation.Nonnull;
import javax.inject.Inject;

@ArtifactProviderFor(BasiliskController.class)
public class SampleController extends AbstractBasiliskController {
    private SampleModel model;                                          //<1>

    @Inject
    private SampleService sampleService;                                //<2>

    @MVCMember
    public void setModel(@Nonnull SampleModel model) {
        this.model = model;
    }

    public void sayHello() {                                            //<3>
        final String result = sampleService.sayHello(model.getInput());
        runInsideUIAsync(new Runnable() {                               //<4>
            @Override
            public void run() {
                model.setOutput(result);
            }
        });
    }
}