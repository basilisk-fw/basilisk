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
package basilisk.core.test;

import basilisk.core.ApplicationBootstrapper;
import basilisk.core.BasiliskApplication;
import basilisk.core.artifact.BasiliskArtifact;
import basilisk.core.artifact.BasiliskClass;
import basilisk.core.env.Environment;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.kordamp.basilisk.runtime.core.DefaultBasiliskApplication;
import org.kordamp.basilisk.runtime.core.TestApplicationBootstrapper;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

import static java.util.Objects.requireNonNull;

/**
 * @author Andres Almiray
 */
public class BasiliskUnitRule implements MethodRule {
    private String[] startupArgs;
    private Class<? extends BasiliskApplication> applicationClass;
    private Class<? extends ApplicationBootstrapper> applicationBootstrapper;

    public BasiliskUnitRule() {
        this(DefaultBasiliskApplication.EMPTY_ARGS, DefaultBasiliskApplication.class, TestApplicationBootstrapper.class);
    }

    public BasiliskUnitRule(@Nonnull Class<? extends BasiliskApplication> applicationClass) {
        this(DefaultBasiliskApplication.EMPTY_ARGS, applicationClass, TestApplicationBootstrapper.class);
    }

    public BasiliskUnitRule(@Nonnull Class<? extends BasiliskApplication> applicationClass, @Nonnull Class<? extends ApplicationBootstrapper> applicationBootstrapper) {
        this(DefaultBasiliskApplication.EMPTY_ARGS, applicationClass, applicationBootstrapper);
    }

    public BasiliskUnitRule(@Nonnull String[] startupArgs) {
        this(startupArgs, DefaultBasiliskApplication.class, TestApplicationBootstrapper.class);
    }

    public BasiliskUnitRule(@Nonnull String[] startupArgs, @Nonnull Class<? extends BasiliskApplication> applicationClass) {
        this(startupArgs, applicationClass, TestApplicationBootstrapper.class);
    }

    public BasiliskUnitRule(@Nonnull String[] startupArgs, @Nonnull Class<? extends BasiliskApplication> applicationClass, @Nonnull Class<? extends ApplicationBootstrapper> applicationBootstrapper) {
        requireNonNull(startupArgs, "Argument 'startupArgs' must not be null");
        this.startupArgs = Arrays.copyOf(startupArgs, startupArgs.length);
        this.applicationClass = requireNonNull(applicationClass, "Argument 'applicationClass' must not be null");
        this.applicationBootstrapper = requireNonNull(applicationBootstrapper, "Argument 'applicationBootstrapper' must not be null");
        if (!Environment.isSystemSet()) {
            System.setProperty(Environment.KEY, Environment.TEST.getName());
        }
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                BasiliskApplication application = instantiateApplication();
                ApplicationBootstrapper bootstrapper = instantiateApplicationBootstrapper(application, target);

                bootstrapper.bootstrap();
                application.initialize();
                application.getInjector().injectMembers(target);
                handleTestForAnnotation(application, target);

                before(application, target);
                try {
                    base.evaluate();
                } finally {
                    after(application, target);
                }
            }
        };
    }

    protected void before(@Nonnull BasiliskApplication application, @Nonnull Object target) throws Throwable {

    }

    protected void after(@Nonnull BasiliskApplication application, @Nonnull Object target) {
        application.shutdown();
    }

    @Nonnull
    private BasiliskApplication instantiateApplication() throws Exception {
        String[] array = new String[0];
        Constructor<? extends BasiliskApplication> ctor = applicationClass.getDeclaredConstructor(array.getClass());
        return ctor.newInstance(new Object[]{startupArgs});
    }

    @Nonnull
    private ApplicationBootstrapper instantiateApplicationBootstrapper(@Nonnull BasiliskApplication application, @Nonnull Object testCase) throws Exception {
        Constructor<? extends ApplicationBootstrapper> constructor = applicationBootstrapper.getDeclaredConstructor(BasiliskApplication.class);
        ApplicationBootstrapper bootstrapper = constructor.newInstance(application);
        if (bootstrapper instanceof TestCaseAware) {
            ((TestCaseAware) bootstrapper).setTestCase(testCase);
        }
        return bootstrapper;
    }

    private void handleTestForAnnotation(@Nonnull BasiliskApplication application, @Nonnull Object target) throws Exception {
        TestFor testFor = target.getClass().getAnnotation(TestFor.class);
        if (testFor != null) {
            Class<? extends BasiliskArtifact> artifactClass = testFor.value();
            BasiliskArtifact artifact = application.getArtifactManager().newInstance(artifactClass);
            BasiliskClass basiliskClass = artifact.getBasiliskClass();
            Field artifactField = target.getClass().getDeclaredField(basiliskClass.getArtifactType());
            artifactField.setAccessible(true);
            artifactField.set(target, artifact);
        }
    }
}
