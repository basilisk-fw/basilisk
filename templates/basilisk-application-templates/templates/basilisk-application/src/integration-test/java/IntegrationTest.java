package ${project_package};

import basilisk.javafx.test.BasiliskTestFXRule;
import org.junit.Rule;
import org.junit.Test;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

public class ${project_class_name}IntegrationTest {
    @Rule
    public BasiliskTestFXRule testfx = new BasiliskTestFXRule("mainWindow");

    @Test
    public void clickButton() {
        // given:
        verifyThat("#clickLabel", hasText("0"));

        // when:
        testfx.clickOn("#clickActionTarget");

        // then:
        verifyThat("#clickLabel", hasText("1"));
    }
}
