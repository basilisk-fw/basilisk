package $

{project_package};

    import basilisk.core.injection.Module;
    import org.kordamp.basilisk.runtime.core.injection.AbstractModule;
    import org.kordamp.jipsy.ServiceProviderFor;

    import javax.inject.Named;

@Named("${plugin_name}")
@ServiceProviderFor(Module.class)
public class $ {
    project_class_name
}Module extends AbstractModule{
@Override
protected void doConfigure(){
    }
    }
