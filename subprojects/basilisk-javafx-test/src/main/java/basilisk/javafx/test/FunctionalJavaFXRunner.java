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

import org.junit.internal.runners.statements.Fail;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static org.junit.runner.Description.createTestDescription;

/**
 * @author Andres Almiray
 * @since 0.2.0
 */
public class FunctionalJavaFXRunner extends BlockJUnit4ClassRunner {
    private BasiliskTestFXClassRule testFXClassRule;

    public FunctionalJavaFXRunner(@Nonnull Class<?> klass) throws InitializationError {
        super(klass);
    }

    private static class FailureListener extends RunListener {
        private final BasiliskTestFXClassRule testfx;

        private FailureListener(BasiliskTestFXClassRule testfx) {
            this.testfx = testfx;
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
            testfx.setFailures(true);
        }
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        List<FrameworkMethod> methods = new ArrayList<>(super.computeTestMethods());
        Collections.sort(methods, new Comparator<FrameworkMethod>() {
            @Override
            public int compare(FrameworkMethod a, FrameworkMethod b) {
                return a.getName().compareTo(b.getName());
            }
        });
        return unmodifiableList(methods);
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        try {
            resolveTestFXClassRule(method);
            notifier.addFirstListener(new FailureListener(testFXClassRule));
        } catch (Exception e) {
            notifier.fireTestFailure(new Failure(createTestDescription(method.getDeclaringClass(), method.getName()), e));
        }
        super.runChild(method, notifier);
    }

    @Override
    protected Statement withBefores(FrameworkMethod method, Object target, Statement statement) {
        try {
            resolveTestFXClassRule(method);
            testFXClassRule.injectMembers(target);
        } catch (Exception e) {
            return new Fail(e);
        }
        return super.withBefores(method, target, statement);
    }

    @Override
    protected boolean isIgnored(FrameworkMethod child) {
        if (super.isIgnored(child)) {
            return true;
        }

        try {
            resolveTestFXClassRule(child);
            return testFXClassRule.hasFailures();
        } catch (Exception e) {
            return true;
        }
    }

    private void resolveTestFXClassRule(FrameworkMethod child) throws NoSuchFieldException, IllegalAccessException {
        if (testFXClassRule == null) {
            for (Field field : child.getDeclaringClass().getFields()) {
                if (BasiliskTestFXClassRule.class.isAssignableFrom(field.getType())) {
                    testFXClassRule = (BasiliskTestFXClassRule) field.get(null);
                    return;
                }
            }
            throw new IllegalStateException("Class " + child.getDeclaringClass().getName() + " does not define a field of type " + BasiliskTestFXClassRule.class.getName());
        }
    }
}