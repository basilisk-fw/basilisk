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

import basilisk.core.BasiliskApplication;
import basilisk.util.CollectionUtils;
import org.kordamp.basilisk.runtime.core.addon.AbstractBasiliskAddon;

import javax.annotation.Nonnull;
import javax.inject.Named;
import java.util.Map;

@Named("groups")
public class GroupsAddon extends AbstractBasiliskAddon implements Invokable {
    private boolean invoked;

    @Override
    public void init(@Nonnull BasiliskApplication application) {
        invoked = true;
    }

    @Override
    public boolean isInvoked() {
        return invoked;
    }

    @Nonnull
    @Override
    public Map<String, Map<String, Object>> getMvcGroups() {
        return CollectionUtils.<String, Map<String, Object>>map()
            .e("root", CollectionUtils.<String, Object>map()
                .e("model", "integration.RootModel")
                .e("view", "integration.RootView")
                .e("controller", "integration.RootController"))
            .e("child", CollectionUtils.<String, Object>map()
                .e("model", "integration.ChildModel")
                .e("view", "integration.ChildView")
                .e("controller", "integration.ChildController"))
            .e("args", CollectionUtils.<String, Object>map()
                .e("model", "integration.ArgsModel")
                .e("view", "integration.ArgsView")
                .e("controller", "integration.ArgsController"));
    }
}
