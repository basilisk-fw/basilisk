package ${project_package};

import basilisk.core.artifact.ArtifactManager;
import basilisk.core.test.BasiliskUnitRule;
import basilisk.core.test.TestFor;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

@TestFor(${project_class_name}Controller.class)
public class ${project_class_name}ControllerTest {
    static {
        // force initialization JavaFX Toolkit
        new javafx.embed.swing.JFXPanel();
    }

    @Inject
    private ArtifactManager artifactManager;

    private ${project_class_name}Controller controller;

    @Rule
    public final BasiliskUnitRule basilisk = new BasiliskUnitRule();

    @Test
    public void executeClickAction() {
        // given:
        ${project_class_name}Model model = artifactManager.newInstance(${project_class_name}Model.class);
        controller.setModel(model);

        // when:
        controller.invokeAction("click");
        await().atMost(2, SECONDS);

        // then:
        assertEquals("1", model.getClickCount());
    }
}
