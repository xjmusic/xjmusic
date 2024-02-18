package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramVoice;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PatternMenuController {
    private final ProjectService projectService;
    @FXML
    public Button newPattern;
    @FXML
    public Button deletePattern;
    @FXML
    public Button clonePattern;
    private ProgramVoice voice;

    public PatternMenuController(ProjectService projectService) {
        this.projectService = projectService;
    }

    public void setUp(Parent root, ProgramVoice voice) {
        this.voice=voice;
        newPattern.setOnAction(e->createVoicePattern());
    }

    private void createVoicePattern(){
//        projectService.getContent().put();
    }
}
