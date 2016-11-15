package ${project_package}

import basilisk.core.test.BasiliskUnitRule
import basilisk.core.test.TestFor
import org.junit.Rule
import spock.lang.Specification

@TestFor(${project_class_name})
class ${project_class_name}Spec extends Specification {
    private ${project_class_name} ${artifact_type}

    @Rule
    public final BasiliskUnitRule basilisk = new BasiliskUnitRule()

    void "This is a smoke test" () {
        expect:
            false
    }
}