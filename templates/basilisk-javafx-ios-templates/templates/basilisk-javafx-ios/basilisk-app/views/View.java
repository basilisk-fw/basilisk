package ${project_package};

import basilisk.core.artifact.BasiliskView;
import basilisk.metadata.ArtifactProviderFor;
import basilisk.util.BasiliskApplicationUtils;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.kordamp.basilisk.runtime.javafx.artifact.AbstractJavaFXBasiliskView;

import java.util.Collections;

@ArtifactProviderFor(BasiliskView.class)
public class ${project_class_name}View extends AbstractJavaFXBasiliskView {
    private ${project_class_name}Controller controller;
    private ${project_class_name}Model model;

    @FXML
    private Label clickLabel;

    public void setController(${project_class_name}Controller controller) {
        this.controller = controller;
    }

    public void setModel(${project_class_name}Model model) {
        this.model = model;
    }

    @Override
    public void initUI() {
        Stage stage = (Stage) getApplication()
            .createApplicationContainer(Collections.<String,Object>emptyMap());
        stage.setTitle(getApplication().getConfiguration().getAsString("application.title"));
        stage.setScene(init());
        stage.sizeToScene();
        getApplication().getWindowManager().attach("mainWindow", stage);
    }

    // build the UI
    private Scene init() {
        Scene scene = new Scene(new Group());
        if (BasiliskApplicationUtils.isIOS) {
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            scene = new Scene(new Group(), bounds.getWidth(), bounds.getHeight());
        }
        scene.setFill(Color.WHITE);

        Node node = loadFromFXML();
        model.clickCountProperty().bindBidirectional(clickLabel.textProperty());
        if (node instanceof Parent) {
            scene.setRoot((Parent) node);
        } else {
            ((Group) scene.getRoot()).getChildren().addAll(node);
        }
        connectActions(node, controller);

        return scene;
    }
}
