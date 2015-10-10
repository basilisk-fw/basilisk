package ${project_package}

import basilisk.javafx.test.BasiliskTestFXClassRule
import spock.lang.Specification
import spock.lang.Stepwise

@Stepwise
class ${project_class_name}FunctionalSpec extends Specification {
    private static BasiliskTestFXClassRule testfx = new BasiliskTestFXClassRule('mainWindow')

    void setupSpec() {
        testfx.setup()
    }

    void cleanupSpec() {
        testfx.cleanup()
    }

    void "This is a smoke test" () {
        expect:
        false
    }
}