/*
 * Copyright 2016-2017 the original author or authors.
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
package org.example;

import basilisk.core.artifact.BasiliskView;
import basilisk.inject.MVCMember;
import basilisk.metadata.ArtifactProviderFor;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.kordamp.basilisk.runtime.javafx.artifact.AbstractJavaFXBasiliskView;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

@ArtifactProviderFor(BasiliskView.class)
public class AppView extends AbstractJavaFXBasiliskView {
    private TabPane tabPane;

    @Nonnull
    public TabPane getTabPane() {
        return tabPane;
    }

    @Override
    public void mvcGroupInit(@Nonnull Map<String, Object> args) {
        createMVCGroup("tab1");
        createMVCGroup("tab2");
    }

    @Override
    public void initUI() {
        Stage stage = (Stage) getApplication()
            .createApplicationContainer(Collections.<String, Object>emptyMap());
        stage.setTitle(getApplication().getConfiguration().getAsString("application.title"));
        tabPane = new TabPane();
        stage.setScene(new Scene(tabPane));
        stage.sizeToScene();
        getApplication().getWindowManager().attach("mainWindow", stage);
    }
}
