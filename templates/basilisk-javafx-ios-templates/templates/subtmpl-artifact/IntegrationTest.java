package ${project_package};

import basilisk.javafx.test.BasiliskTestFXRule;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.fail;

public class ${project_class_name}IntegrationTest {
    @Rule
    public BasiliskTestFXRule testfx = new BasiliskTestFXRule("mainWindow");

    @Test
    public void smokeTest(){
        fail("Not yet implemented!");
    }
}