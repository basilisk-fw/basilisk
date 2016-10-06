package ${project_package};

import basilisk.core.artifact.BasiliskController;
import basilisk.inject.MVCMember;
import basilisk.metadata.ArtifactProviderFor;
import org.kordamp.basilisk.runtime.core.artifact.AbstractBasiliskController;

import basilisk.transform.Threading;

import javax.annotation.Nonnull;

@ArtifactProviderFor(BasiliskController.class)
public class ${project_class_name}Controller extends AbstractBasiliskController {
    private ${project_class_name}Model model;

    @MVCMember
    public void setModel(@Nonnull ${project_class_name}Model model) {
        this.model = model;
    }

    @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
    public void click() {
        int count = Integer.parseInt(model.getClickCount());
        model.setClickCount(String.valueOf(count + 1));
    }
}