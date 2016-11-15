package ${project_package};

import basilisk.core.artifact.BasiliskModel;
import basilisk.metadata.ArtifactProviderFor;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import org.kordamp.basilisk.runtime.core.artifact.AbstractBasiliskModel;

import javax.annotation.Nonnull;

@ArtifactProviderFor(BasiliskModel.class)
public class ${project_class_name}Model extends AbstractBasiliskModel {
    private StringProperty clickCount;

    @Nonnull
    public final StringProperty clickCountProperty() {
        if (clickCount == null) {
            clickCount = new SimpleStringProperty(this, "clickCount", "0");
        }
        return clickCount;
    }

    public void setClickCount(String clickCount) {
        clickCountProperty().set(clickCount);
    }

    public String getClickCount() {
        return clickCountProperty().get();
    }
}