package ${project_package};

import basilisk.javafx.test.BasiliskTestFXClassRule;
import basilisk.javafx.test.FunctionalJavaFXRunner;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@RunWith(FunctionalJavaFXRunner.class)
public class ${project_class_name}FunctionalTest {
    @ClassRule
    public static BasiliskTestFXClassRule testfx = new BasiliskTestFXClassRule("mainWindow");

    @Test
    public void _01_clickButton() {
        // given:
        verifyThat("#clickLabel", hasText("0"));

        // when:
        testfx.clickOn("#clickActionTarget");

        // then:
        verifyThat("#clickLabel", hasText("1"));
    }
}
