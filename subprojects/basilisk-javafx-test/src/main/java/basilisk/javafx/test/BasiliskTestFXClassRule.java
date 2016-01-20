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
package basilisk.javafx.test;

import basilisk.core.ApplicationEvent;
import basilisk.core.RunnableWithArgs;
import basilisk.core.env.Environment;
import basilisk.exceptions.BasiliskException;
import basilisk.javafx.JavaFXBasiliskApplication;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.kordamp.basilisk.runtime.core.DefaultBasiliskApplication;
import org.kordamp.basilisk.runtime.javafx.TestJavaFXBasiliskApplication;
import org.testfx.api.FxToolkit;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import static basilisk.javafx.test.TestContext.getTestContext;
import static basilisk.util.BasiliskNameUtils.requireNonBlank;
import static com.jayway.awaitility.Awaitility.await;
import static java.util.Objects.requireNonNull;

/**
 * A JUnit Rule that starts the application once per test class.
 *
 * @author Andres Almiray
 */
public class BasiliskTestFXClassRule extends TestFX implements TestRule {
    protected String windowName;
    protected String[] startupArgs;
    protected Class<? extends TestJavaFXBasiliskApplication> applicationClass;
    protected JavaFXBasiliskApplication application;

    public BasiliskTestFXClassRule(@Nonnull String windowName) {
        this(TestJavaFXBasiliskApplication.class, windowName, DefaultBasiliskApplication.EMPTY_ARGS);
    }

    public BasiliskTestFXClassRule(@Nonnull Class<? extends TestJavaFXBasiliskApplication> applicationClass, @Nonnull String windowName) {
        this(applicationClass, windowName, DefaultBasiliskApplication.EMPTY_ARGS);
    }

    public BasiliskTestFXClassRule(@Nonnull Class<? extends TestJavaFXBasiliskApplication> applicationClass, @Nonnull String windowName, @Nonnull String[] startupArgs) {
        this.applicationClass = requireNonNull(applicationClass, "Argument 'applicationClass' must not be null");
        this.windowName = requireNonBlank(windowName, "Argument 'windowName' cannot be blank");
        requireNonNull(startupArgs, "Argument 'startupArgs' must not be null");
        this.startupArgs = new String[startupArgs.length];
        System.arraycopy(startupArgs, 0, this.startupArgs, 0, startupArgs.length);
        if (!Environment.isSystemSet()) {
            System.setProperty(Environment.KEY, Environment.TEST.getName());
        }
    }

    public void setup() {
        initialize();

        try {
            FxToolkit.registerPrimaryStage();

            application = (JavaFXBasiliskApplication) FxToolkit.setupApplication(applicationClass);
            final WindowShownHandler startingWindow = new WindowShownHandler(windowName);
            application.getEventRouter().addEventListener(ApplicationEvent.WINDOW_SHOWN.getName(), startingWindow);

            await().until(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return startingWindow.isShowing();
                }
            });
        } catch (TimeoutException e) {
            throw new BasiliskException("An error occurred while starting up the application", e);
        }
    }

    public void cleanup() {
        if (application != null) {
            application.shutdown();
            try {
                FxToolkit.cleanupApplication(application);
            } catch (TimeoutException e) {
                throw new BasiliskException("An error occurred while shutting down the application", e);
            }
        }
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                setup();
                try {
                    base.evaluate();
                } finally {
                    cleanup();
                }
            }
        };
    }

    protected void initialize() {
        getTestContext().setWindowName(windowName);
    }

    private static class WindowShownHandler implements RunnableWithArgs {
        private final String windowName;
        private boolean showing;

        private WindowShownHandler(String windowName) {
            this.windowName = windowName;
        }

        public boolean isShowing() {
            return showing;
        }

        @Override
        public void run(Object... args) {
            if (args != null && args.length > 0 && args[0] instanceof CharSequence) {
                showing = windowName.equals(String.valueOf(args[0]));
            }
        }
    }
}
