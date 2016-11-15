package ${project_package};

import basilisk.javafx.test.BasiliskTestFXClassRule;
import basilisk.javafx.test.FunctionalJavaFXRunner;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(FunctionalJavaFXRunner.class)
public class ${project_class_name}FunctionalTest {
    @ClassRule
    public static BasiliskTestFXClassRule testfx = new BasiliskTestFXClassRule("mainWindow");

    @Test
    public void smokeTest() {
        fail("Not yet implemented!");
    }
}