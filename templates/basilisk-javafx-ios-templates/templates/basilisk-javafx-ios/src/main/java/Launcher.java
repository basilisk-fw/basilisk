package ${project_package};

import basilisk.javafx.JavaFXBasiliskApplication;

public class Launcher extends JavaFXBasiliskApplication {
    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
    }
}
