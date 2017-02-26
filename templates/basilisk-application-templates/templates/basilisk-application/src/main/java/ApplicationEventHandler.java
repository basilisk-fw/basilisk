package ${project_package};

import basilisk.core.event.EventHandler;
import basilisk.exceptions.BasiliskViewInitializationException;
import javafx.application.Platform;

public class ApplicationEventHandler implements EventHandler {
    public void onUncaughtBasiliskViewInitializationException(BasiliskViewInitializationException x) {
        Platform.exit();
    }
}