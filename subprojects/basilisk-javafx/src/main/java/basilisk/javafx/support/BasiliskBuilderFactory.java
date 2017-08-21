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
package basilisk.javafx.support;

import basilisk.core.BasiliskApplication;
import basilisk.core.mvc.MVCGroup;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.util.Builder;
import javafx.util.BuilderFactory;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static basilisk.util.BasiliskNameUtils.isBlank;
import static basilisk.util.BasiliskNameUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 * @since 1.0.0
 */
public class BasiliskBuilderFactory implements BuilderFactory {
    private final BasiliskApplication application;
    private final MVCGroup mvcGroup;
    private final BuilderFactory delegate;

    public BasiliskBuilderFactory(@Nonnull BasiliskApplication application, @Nonnull MVCGroup mvcGroup) {
        this(application, mvcGroup, application.getApplicationClassLoader().get());
    }

    public BasiliskBuilderFactory(@Nonnull BasiliskApplication application, @Nonnull MVCGroup mvcGroup, @Nonnull ClassLoader classLoader) {
        this.application = requireNonNull(application, "Argument 'application' must not be null");
        this.mvcGroup = requireNonNull(mvcGroup, "Argument 'mvcGroup' must not be null");
        this.delegate = new JavaFXBuilderFactory(requireNonNull(classLoader, "Argument 'classLoader' must not be null"));
    }

    @Override
    public Builder<?> getBuilder(Class<?> type) {
        if (type == MetaComponent.class) {
            return new MetaComponentBuilder(application, mvcGroup);
        }
        return delegate.getBuilder(type);
    }

    private static class MetaComponentBuilder extends MetaComponent implements Builder<Object> {
        private final BasiliskApplication application;
        private final MVCGroup mvcGroup;

        public MetaComponentBuilder(@Nonnull BasiliskApplication application, @Nonnull MVCGroup mvcGroup) {
            this.application = requireNonNull(application, "Argument 'application' must not be null");
            this.mvcGroup = requireNonNull(mvcGroup, "Argument 'mvcGroup' must not be null");
        }

        @Override
        public Object build() {
            String mvcType = requireNonBlank(getMvcType(), "Must define a value for 'mvcType'");
            String mvcId = getMcvId();
            if (isBlank(mvcId) || application.getMvcGroupManager().findGroup(mvcId) != null) {
                mvcId = mvcType + "-" + UUID.randomUUID().toString();
            }

            MVCGroup group = mvcGroup.createMVCGroup(mvcType, mvcId, toMap(getMvcArgs()));
            return group.getContext().get(group.getMvcId() + "-rootNode");
        }

        @Nonnull
        private Map<String, Object> toMap(@Nonnull List<MvcArg> mvcArgs) {
            Map<String, Object> args = new LinkedHashMap<>();
            mvcArgs.forEach(arg -> args.put(arg.getName(), arg.getValue()));
            return args;
        }
    }
}
