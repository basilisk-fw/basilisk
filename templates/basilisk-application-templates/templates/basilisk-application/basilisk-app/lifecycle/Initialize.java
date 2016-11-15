import basilisk.core.BasiliskApplication;
import org.kordamp.basilisk.runtime.core.AbstractLifecycleHandler;

import javax.annotation.Nonnull;
import javax.inject.Inject;

public class Initialize extends AbstractLifecycleHandler {
    @Inject
    public Initialize(@Nonnull BasiliskApplication application) {
        super(application);
    }

    @Override
    public void execute() {
    }
}