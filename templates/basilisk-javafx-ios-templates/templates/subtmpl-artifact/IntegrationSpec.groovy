package ${project_package}

import basilisk.javafx.test.BasiliskTestFXRule
import org.junit.Rule
import spock.lang.Specification

class ${project_class_name}IntegrationSpec extends Specification {
    @Rule
    public BasiliskTestFXRule testfx = new BasiliskTestFXRule("mainWindow")

    void "This is a smoke test" () {
        expect:
            false
    }
}