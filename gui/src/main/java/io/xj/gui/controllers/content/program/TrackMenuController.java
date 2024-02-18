package io.xj.gui.controllers.content.program;


import io.xj.gui.services.ProjectService;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TrackMenuController {
    @FXML
    public Button newTrack;
    @FXML
    public Button deleteTrack;
    private final ProjectService projectService;

    public TrackMenuController(ProjectService projectService) {
        this.projectService = projectService;
    }


    public void setUp(Parent root) {
    }
}
