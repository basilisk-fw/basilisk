package ${project_package};

import basilisk.core.test.BasiliskUnitRule;
import basilisk.core.test.TestFor;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.fail;

@TestFor(${project_class_name}.class)
public class ${project_class_name}Test {
    static {
        // force initialization JavaFX Toolkit
        new javafx.embed.swing.JFXPanel();
    }

    private ${project_class_name} ${artifact_type};

    @Rule
    public final BasiliskUnitRule basilisk = new BasiliskUnitRule();

    @Test
    public void smokeTest() {
        fail("Not yet implemented!");
    }
}