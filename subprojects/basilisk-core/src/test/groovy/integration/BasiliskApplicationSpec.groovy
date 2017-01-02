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
package integration

import basilisk.core.ApplicationBootstrapper
import basilisk.core.BasiliskApplication
import basilisk.core.LifecycleHandler
import basilisk.core.ShutdownHandler
import basilisk.core.addon.BasiliskAddon
import basilisk.core.artifact.ArtifactHandler
import basilisk.core.artifact.BasiliskController
import basilisk.core.artifact.BasiliskControllerClass
import basilisk.core.artifact.BasiliskModel
import basilisk.core.artifact.BasiliskModelClass
import basilisk.core.artifact.BasiliskServiceClass
import basilisk.core.artifact.BasiliskView
import basilisk.core.artifact.BasiliskViewClass
import basilisk.core.controller.ActionHandler
import basilisk.core.controller.ActionManager
import basilisk.core.env.ApplicationPhase
import basilisk.core.env.Lifecycle
import basilisk.core.mvc.MVCFunction
import basilisk.core.mvc.MVCGroup
import org.kordamp.basilisk.runtime.core.DefaultApplicationBootstrapper
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import javax.annotation.Nonnull
import javax.annotation.Nullable

import static basilisk.util.AnnotationUtils.named
import static basilisk.util.AnnotationUtils.typed
import static java.util.Arrays.asList

@Stepwise
class BasiliskApplicationSpec extends Specification {
    @Shared
    private static BasiliskApplication application

    @Shared
    private static List<Invokable> invokables = []

    static {
        System.setProperty('org.slf4j.simpleLogger.defaultLogLevel', 'trace')
        System.setProperty('basilisk.full.stacktrace', 'true')
    }

    def setupSpec() {
        application = new TestBasiliskApplication(['foo', 'bar'] as String[])
        application.addShutdownHandler(new TestShutdownHandler())
    }

    def cleanupSpec() {
        //when:
        assert application.shutdown()

        // then:
        assert ApplicationPhase.SHUTDOWN == application.phase

        for (Invokable invokable : invokables) {
            assert invokable.invoked
        }
    }

    def "Bootstrap application"() {
        given:
        ApplicationBootstrapper bootstrapper = new DefaultApplicationBootstrapper(application)

        when:
        bootstrapper.bootstrap()
        List<Invokable> invokables = new ArrayList<>()
        for (Lifecycle lifecycle : asList(Lifecycle.INITIALIZE, Lifecycle.STARTUP, Lifecycle.READY, Lifecycle.SHUTDOWN)) {
            invokables << application.injector.getInstance(LifecycleHandler, named(lifecycle.name))
        }
        for (BasiliskAddon addon : application.injector.getInstances(BasiliskAddon)) {
            invokables << addon
        }
        bootstrapper.run()

        then:
        ApplicationPhase.MAIN == application.phase
    }

    def 'Check artifact controller'() {
        given:
        BasiliskControllerClass controllerClass = application.artifactManager.findBasiliskClass(IntegrationController)
        controllerClass.resetCaches()

        expect:
        controllerClass.eventNames == []
        controllerClass.actionNames == ['abort', 'contextualFailure', 'contextualSuccess', 'handleException', 'sayHello', 'throwException']
    }

    def 'Check artifact model'() {
        given:
        BasiliskModelClass modelClass = application.artifactManager.findBasiliskClass(IntegrationModel)
        BasiliskModel model = application.mvcGroupManager.findGroup('integration').model
        modelClass.resetCaches()

        when:
        modelClass.setModelPropertyValue(model, 'input', 'INPUT')

        then:
        modelClass.eventNames == []
        modelClass.propertyNames == ['input', 'output']
        modelClass.getModelPropertyValue(model, 'input') == 'INPUT'
    }

    def 'Check artifact service'() {
        given:
        BasiliskServiceClass serviceClass = application.artifactManager.findBasiliskClass(IntegrationService)
        serviceClass.resetCaches()

        expect:
        serviceClass.eventNames == []
        serviceClass.serviceNames == ['sayHello']
    }

    def 'Check artifact view'() {
        given:
        BasiliskViewClass viewClass = application.artifactManager.findBasiliskClass(IntegrationView)
        viewClass.resetCaches()

        expect:
        viewClass.eventNames == ['RandomEvent']
    }

    def "Check groups"() {
        // given:
        InvokeActionHandler handler = application.injector.getInstance(ActionHandler)
        assert handler.configure
        assert ['foo', 'bar'] == application.startupArgs

        when:
        MVCGroup group = application.mvcGroupManager.findGroup('integration')

        then:
        group
        handler.configure
        !handler.abort
        !handler.before
        !handler.after
        !handler.exception
        !handler.update
    }

    def 'Invoke abort Action'() {
        given:
        InvokeActionHandler handler = application.injector.getInstance(ActionHandler)
        MVCGroup group = application.mvcGroupManager.findGroup('integration')
        invokables << group.view

        when:
        group.controller.invokeAction('abort')

        then:
        handler.configure
        handler.abort
        !handler.before
        handler.after
        !handler.exception
        !handler.update
    }

    def 'Invoke contextualSuccess Action'() {
        given:
        InvokeActionHandler handler = application.injector.getInstance(ActionHandler)
        MVCGroup group = application.mvcGroupManager.findGroup('integration')
        invokables << group.view
        group.context['key'] = 'VALUE'

        when:
        group.controller.invokeAction('contextualSuccess')

        then:
        handler.configure
        handler.before
        handler.after
        !handler.exception
        !handler.update
        group.controller.key == 'VALUE'
    }

    def 'Invoke contextualFailure Action'() {
        given:
        InvokeActionHandler handler = application.injector.getInstance(ActionHandler)
        MVCGroup group = application.mvcGroupManager.findGroup('integration')
        invokables << group.view

        when:
        group.controller.invokeAction('contextualFailure')

        then:
        def e = thrown(IllegalStateException)
        e.message =~ /Could not find an instance of type java.lang.String/
    }

    def 'Invoke sayHello Action'() {
        given:
        InvokeActionHandler handler = application.injector.getInstance(ActionHandler)
        MVCGroup group = application.mvcGroupManager.findGroup('integration')
        invokables << group.view

        when:
        group.controller.invokeAction('sayHello')

        then:
        handler.configure
        handler.before
        handler.after
        !handler.exception
        !handler.update
    }

    def 'Invoke handleException Action'() {
        given:
        InvokeActionHandler handler = application.injector.getInstance(ActionHandler)
        MVCGroup group = application.mvcGroupManager.findGroup('integration')

        when:
        group.controller.invokeAction('handleException')

        then:
        handler.configure
        handler.before
        handler.after
        handler.exception
        !handler.update
    }

    def 'Invoke throwException Action'() {
        given:
        InvokeActionHandler handler = application.injector.getInstance(ActionHandler)
        MVCGroup group = application.mvcGroupManager.findGroup('integration')

        when:
        group.controller.invokeAction('throwException')

        then:
        thrown(RuntimeException)
        handler.before
        handler.after
        handler.exception
    }

    def 'Update Action'() {
        given:
        InvokeActionHandler handler = application.injector.getInstance(ActionHandler)
        ActionManager actionManager = application.actionManager

        when:
        actionManager.updateActions()

        then:
        handler.update
    }

    def 'Verify ActionManager'() {
        given:
        BasiliskController controller = application.mvcGroupManager.findGroup('integration').controller

        expect:
        application.actionManager.actionsFor(controller).keySet() == (['abort', 'contextualFailure', 'contextualSuccess', 'handleException', 'sayHello', 'throwException'] as Set)
        application.actionManager.normalizeName('fooAction') == 'foo'
        application.actionManager.normalizeName('foo') == 'foo'
        application.actionManager.actionFor(controller, 'sayHello')
        !application.actionManager.actionFor(controller, 'unknown')
    }

    def 'Verify AddonManager'() {
        expect:
        application.addonManager.addons.size() == 2
        application.addonManager.findAddon('integration')
        application.addonManager.findAddon('IntegrationBasiliskAddon')
        application.addonManager.addons.containsKey('integration')
        application.addonManager.findAddon('groups')
        application.addonManager.findAddon('GroupsBasiliskAddon')
        application.addonManager.addons.containsKey('groups')
    }

    def 'Verify MVCGroupManager'() {
        expect:
        application.mvcGroupManager.configurations.size() == 6
        application.mvcGroupManager.findConfiguration('integration')
        application.mvcGroupManager.findConfiguration('simple')
        application.mvcGroupManager.findConfiguration('sample')
        application.mvcGroupManager.findConfiguration('root')
        application.mvcGroupManager.findConfiguration('child')
        application.mvcGroupManager.findConfiguration('args')
        application.mvcGroupManager.configurations.containsKey('integration')
        application.mvcGroupManager.configurations.containsKey('simple')
        application.mvcGroupManager.configurations.containsKey('sample')
        application.mvcGroupManager.configurations.containsKey('root')
        application.mvcGroupManager.configurations.containsKey('child')
        application.mvcGroupManager.configurations.containsKey('args')

        application.mvcGroupManager.models.containsKey('integration')
        application.mvcGroupManager.controllers.containsKey('integration')
        application.mvcGroupManager.views.containsKey('integration')
        application.mvcGroupManager.models.containsKey('sample')
        application.mvcGroupManager.controllers.containsKey('sample')
        application.mvcGroupManager.views.containsKey('sample')

        application.mvcGroupManager.findModel('integration', IntegrationModel)
        application.mvcGroupManager.findView('integration', IntegrationView)
        application.mvcGroupManager.findController('integration', IntegrationController)

        !application.mvcGroupManager.findModel('foo', IntegrationModel)
        !application.mvcGroupManager.findView('foo', IntegrationView)
        !application.mvcGroupManager.findController('foo', IntegrationController)

        application.mvcGroupManager.getModel('integration', IntegrationModel)
        application.mvcGroupManager.getView('integration', IntegrationView)
        application.mvcGroupManager.getController('integration', IntegrationController)
    }

    def 'Verify ArtifactManager'() {
        given:
        BasiliskModel model = application.mvcGroupManager.findGroup('integration').model
        ArtifactHandler modelHandler = application.injector.getInstance(ArtifactHandler, typed(BasiliskModel))

        expect:
        application.artifactManager.findBasiliskClass('integration.SimpleModel')
        application.artifactManager.findBasiliskClass('integration.SimpleModel', 'model')
        application.artifactManager.findBasiliskClass(SimpleModel)
        application.artifactManager.findBasiliskClass(SimpleModel, 'model')
        application.artifactManager.findBasiliskClass(model)

        !application.artifactManager.findBasiliskClass('integration.SampleModel')
        !application.artifactManager.findBasiliskClass('integration.SampleModel', 'model')
        !application.artifactManager.findBasiliskClass('integration.SampleModel', 'domain')
        !application.artifactManager.findBasiliskClass(SimpleModel, 'domain')
        !application.artifactManager.findBasiliskClass(SimpleModel, 'controller')

        application.artifactManager.getClassesOfType('model').clazz == [IntegrationModel, SimpleModel, RootModel, ChildModel, ArgsModel]
        !application.artifactManager.getClassesOfType('domain')

        application.artifactManager.allClasses*.clazz.sort() == [
            IntegrationModel, IntegrationView, IntegrationController, IntegrationService,
            SimpleModel, SimpleView, SimpleController,
            RootModel, RootView, RootController,
            ChildModel, ChildView, ChildController,
            ArgsModel, ArgsView, ArgsController
        ].sort()

        modelHandler.artifactType == BasiliskModel
        modelHandler.trailing == 'Model'
        modelHandler.type == 'model'
        modelHandler.classesByName.keySet() == (['integration.IntegrationModel', 'integration.SimpleModel', 'integration.RootModel', 'integration.ChildModel', 'integration.ArgsModel'] as Set)
        modelHandler.classes.clazz == [IntegrationModel, SimpleModel, RootModel, ChildModel, ArgsModel]
        modelHandler.findClassFor('integrationModel')
        modelHandler.findClassFor('integration')
        !modelHandler.findClassFor('sample')
        !modelHandler.findClassFor('s')
        modelHandler.application == application
        modelHandler.getClassFor(IntegrationModel)
        !modelHandler.getClassFor(IntegrationController)
        modelHandler.isArtifact(IntegrationModel)
        !modelHandler.isArtifact(IntegrationController)
        modelHandler.isArtifact(modelHandler.classes[0])
    }

    def 'Verify withMvcGroup(type , handler)'() {
        given:
        List checks = []

        when:
        application.mvcGroupManager.withMVC('simple', new MVCFunction() {
            void apply(
                @Nullable BasiliskModel model, @Nullable BasiliskView view, @Nullable BasiliskController controller) {
                checks << (model instanceof SimpleModel)
                checks << (view instanceof SimpleView)
                checks << (controller instanceof SimpleController)
                checks << (controller.mvcId == 'simple')
                checks << (controller.key == null)
            }
        })

        then:
        checks.every { it == true }
    }

    def 'Verify withMvcGroup(type, id , handler)'() {
        given:
        List checks = []

        when:
        application.mvcGroupManager.withMVC('simple', 'simple-1', new MVCFunction() {
            void apply(
                @Nullable BasiliskModel model, @Nullable BasiliskView view, @Nullable BasiliskController controller) {
                checks << (model instanceof SimpleModel)
                checks << (view instanceof SimpleView)
                checks << (controller instanceof SimpleController)
                checks << (controller.mvcId == 'simple-1')
                checks << (controller.key == null)
            }
        })

        then:
        checks.every { it == true }
    }

    def 'Verify withMvcGroup(type, id, map , handler)'() {
        given:
        List checks = []

        when:
        application.mvcGroupManager.withMVC('simple', 'simple-2', [key: 'basilisk'], new MVCFunction() {
            void apply(
                @Nullable BasiliskModel model, @Nullable BasiliskView view, @Nullable BasiliskController controller) {
                checks << (model instanceof SimpleModel)
                checks << (view instanceof SimpleView)
                checks << (controller instanceof SimpleController)
                checks << (controller.mvcId == 'simple-2')
                checks << (controller.key == 'basilisk')
            }
        })

        then:
        checks.every { it == true }
    }

    def 'Verify withMvcGroup(type, map , handler)'() {
        given:
        List checks = []

        when:
        application.mvcGroupManager.withMVC('simple', [key: 'basilisk'], new MVCFunction() {
            void apply(
                @Nullable BasiliskModel model, @Nullable BasiliskView view, @Nullable BasiliskController controller) {
                checks << (model instanceof SimpleModel)
                checks << (view instanceof SimpleView)
                checks << (controller instanceof SimpleController)
                checks << (controller.mvcId == 'simple')
                checks << (controller.key == 'basilisk')
            }
        })

        then:
        checks.every { it == true }
    }

    def 'Verify withMvcGroup(map, type, id , handler)'() {
        given:
        List checks = []

        when:
        application.mvcGroupManager.withMVC('simple', 'simple-2', key: 'basilisk', new MVCFunction() {
            void apply(
                @Nullable BasiliskModel model, @Nullable BasiliskView view, @Nullable BasiliskController controller) {
                checks << (model instanceof SimpleModel)
                checks << (view instanceof SimpleView)
                checks << (controller instanceof SimpleController)
                checks << (controller.mvcId == 'simple-2')
                checks << (controller.key == 'basilisk')
            }
        })

        then:
        checks.every { it == true }
    }

    def 'Verify withMvcGroup(map, type , handler)'() {
        given:
        List checks = []

        when:
        application.mvcGroupManager.withMVC('simple', key: 'basilisk', new MVCFunction() {
            void apply(
                @Nullable BasiliskModel model, @Nullable BasiliskView view, @Nullable BasiliskController controller) {
                checks << (model instanceof SimpleModel)
                checks << (view instanceof SimpleView)
                checks << (controller instanceof SimpleController)
                checks << (controller.mvcId == 'simple')
                checks << (controller.key == 'basilisk')
            }
        })

        then:
        checks.every { it == true }
    }

    def 'Verify createMVCGroup(type)'() {
        given:
        MVCGroup group = application.mvcGroupManager.createMVCGroup('simple')

        expect:
        group.model instanceof SimpleModel
        group.view instanceof SimpleView
        group.controller instanceof SimpleController
        group.controller.mvcId == 'simple'
        group.controller.key == null

        cleanup:
        group.destroy()
    }

    def 'Verify createMVCGroup(type, id)'() {
        given:
        MVCGroup group = application.mvcGroupManager.createMVCGroup('simple', 'simple-1')

        expect:
        group.model instanceof SimpleModel
        group.view instanceof SimpleView
        group.controller instanceof SimpleController
        group.controller.mvcId == 'simple-1'
        group.controller.key == null

        cleanup:
        group.destroy()
    }

    def 'Verify createMVCGroup(type, map)'() {
        given:
        MVCGroup group = application.mvcGroupManager.createMVCGroup('simple', [key: 'basilisk'])

        expect:
        group.model instanceof SimpleModel
        group.view instanceof SimpleView
        group.controller instanceof SimpleController
        group.controller.mvcId == 'simple'
        group.controller.key == 'basilisk'

        cleanup:
        group.destroy()
    }

    def 'Verify createMVCGroup(map, type)'() {
        given:
        MVCGroup group = application.mvcGroupManager.createMVCGroup('simple', key: 'basilisk')

        expect:
        group.model instanceof SimpleModel
        group.view instanceof SimpleView
        group.controller instanceof SimpleController
        group.controller.mvcId == 'simple'
        group.controller.key == 'basilisk'

        cleanup:
        group.destroy()
    }

    def 'Verify createMVCGroup(type, id, map)'() {
        given:
        MVCGroup group = application.mvcGroupManager.createMVCGroup('simple', 'simple-2', [key: 'basilisk'])

        expect:
        group.model instanceof SimpleModel
        group.view instanceof SimpleView
        group.controller instanceof SimpleController
        group.controller.mvcId == 'simple-2'
        group.controller.key == 'basilisk'

        cleanup:
        group.destroy()
    }

    def 'Verify createMVCGroup(map, type, id)'() {
        given:
        MVCGroup group = application.mvcGroupManager.createMVCGroup('simple', 'simple-2', key: 'basilisk')

        expect:
        group.model instanceof SimpleModel
        group.view instanceof SimpleView
        group.controller instanceof SimpleController
        group.controller.mvcId == 'simple-2'
        group.controller.key == 'basilisk'

        cleanup:
        group.destroy()
    }

    def 'Verify createMVC(type)'() {
        given:
        List members = application.mvcGroupManager.createMVC('simple')

        expect:
        members[0] instanceof SimpleModel
        members[1] instanceof SimpleView
        members[2] instanceof SimpleController
        members[2].mvcId == 'simple'
        members[2].key == null

        cleanup:
        members[2].mvcGroup.destroy()
    }

    def 'Verify createMVC(type, id)'() {
        given:
        List members = application.mvcGroupManager.createMVC('simple', 'simple-1')

        expect:
        members[0] instanceof SimpleModel
        members[1] instanceof SimpleView
        members[2] instanceof SimpleController
        members[2].mvcId == 'simple-1'
        members[2].key == null

        cleanup:
        members[2].mvcGroup.destroy()
    }

    def 'Verify createMVC(type, map)'() {
        given:
        List members = application.mvcGroupManager.createMVC('simple', [key: 'basilisk'])

        expect:
        members[0] instanceof SimpleModel
        members[1] instanceof SimpleView
        members[2] instanceof SimpleController
        members[2].mvcId == 'simple'
        members[2].key == 'basilisk'

        cleanup:
        members[2].mvcGroup.destroy()
    }

    def 'Verify createMVC(map, type)'() {
        given:
        List members = application.mvcGroupManager.createMVC('simple', key: 'basilisk')

        expect:
        members[0] instanceof SimpleModel
        members[1] instanceof SimpleView
        members[2] instanceof SimpleController
        members[2].mvcId == 'simple'
        members[2].key == 'basilisk'

        cleanup:
        members[2].mvcGroup.destroy()
    }

    def 'Verify createMVC(type, id, map)'() {
        given:
        List members = application.mvcGroupManager.createMVC('simple', 'simple-2', [key: 'basilisk'])

        expect:
        members[0] instanceof SimpleModel
        members[1] instanceof SimpleView
        members[2] instanceof SimpleController
        members[2].mvcId == 'simple-2'
        members[2].key == 'basilisk'

        cleanup:
        members[2].mvcGroup.destroy()
    }

    def 'Verify createMVC(map, type, id)'() {
        given:
        List members = application.mvcGroupManager.createMVC('simple', 'simple-2', key: 'basilisk')

        expect:
        members[0] instanceof SimpleModel
        members[1] instanceof SimpleView
        members[2] instanceof SimpleController
        members[2].mvcId == 'simple-2'
        members[2].key == 'basilisk'

        cleanup:
        members[2].mvcGroup.destroy()
    }

    private
    static class TestShutdownHandler implements ShutdownHandler, Invokable {
        private boolean invoked

        @Override
        boolean canShutdown(@Nonnull BasiliskApplication application) {
            return true
        }

        @Override
        void onShutdown(@Nonnull BasiliskApplication application) {
            invoked = true
        }

        boolean isInvoked() {
            return invoked
        }
    }
}
