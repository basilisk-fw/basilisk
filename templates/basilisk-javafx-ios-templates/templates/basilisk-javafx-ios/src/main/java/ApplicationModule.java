package ${project_package};

import basilisk.core.event.EventHandler;
import basilisk.core.injection.Module;
import org.kordamp.basilisk.runtime.core.injection.AbstractModule;
import org.kordamp.basilisk.runtime.util.ResourceBundleProvider;
import org.kordamp.jipsy.ServiceProviderFor;

import java.util.ResourceBundle;
import static basilisk.util.AnnotationUtils.named;

@ServiceProviderFor(Module.class)
public class ApplicationModule extends AbstractModule {
    @Override
    protected void doConfigure() {
        bind(ResourceBundle.class)
            .withClassifier(named("applicationResourceBundle"))
            .toProvider(new ResourceBundleProvider("${project_package}.Config"))
            .asSingleton();

        bind(EventHandler.class)
            .to(ApplicationEventHandler.class)
            .asSingleton();
    }
}