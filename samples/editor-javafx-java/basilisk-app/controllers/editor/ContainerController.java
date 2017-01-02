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
package editor;

import basilisk.core.artifact.BasiliskController;
import basilisk.inject.MVCMember;
import basilisk.metadata.ArtifactProviderFor;
import basilisk.transform.Threading;
import basilisk.util.CollectionUtils;
import org.kordamp.basilisk.runtime.core.artifact.AbstractBasiliskController;

import javax.annotation.Nullable;
import java.io.File;

import static basilisk.util.BasiliskNameUtils.isBlank;

@ArtifactProviderFor(BasiliskController.class)
public class ContainerController extends AbstractBasiliskController {
    @MVCMember private ContainerModel model;
    @MVCMember private ContainerView view;

    @Threading(Threading.Policy.SKIP)
    public void open() {
        File file = view.selectFile();
        if (file != null) {
            String mvcIdentifier = file.getName() + "-" + System.currentTimeMillis();
            createMVC("editor", mvcIdentifier, CollectionUtils.<String, Object>map()
                .e("document", new Document(file, file.getName()))
                .e("tabName", file.getName()));
        }
    }

    public void save() {
        EditorController controller = resolveEditorController();
        if (controller != null) {
            controller.saveFile();
        }
    }

    public void close() {
        EditorController controller = resolveEditorController();
        if (controller != null) {
            controller.closeFile();
        }
    }

    public void quit() {
        getApplication().shutdown();
    }

    @Nullable
    private EditorController resolveEditorController() {
        if (!isBlank(model.getMvcIdentifier())) {
            return getApplication().getMvcGroupManager()
                .findController(model.getMvcIdentifier(), EditorController.class);
        }
        return null;
    }
}