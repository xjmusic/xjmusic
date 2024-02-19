package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramVoice;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

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
    private final ProgramEditorController programEditorController;
    static final Logger LOG = LoggerFactory.getLogger(PatternMenuController.class);

    public PatternMenuController(ProjectService projectService, ProgramEditorController programEditorController) {
        this.programEditorController = programEditorController;
        this.projectService = projectService;
    }

    public void setUp(Parent root, ProgramVoice voice) {
        this.voice = voice;
        newPattern.setOnAction(e -> createVoicePattern());
        deletePattern.setOnAction(e->deletePattern());
    }

    private void createVoicePattern() {
       try {
           ProgramSequencePattern newPattern = new ProgramSequencePattern(UUID.randomUUID(), programEditorController.getProgramId(), programEditorController.getSequenceId(), voice.getId(), "XXX",
                   (short) 4);
           projectService.getContent().put(newPattern);
       } catch (Exception e) {
           LOG.error("Cannot create new ProgramSequencePattern");
       }
    }

    private void deletePattern() {
        try {
//            projectService.deleteContent();
        } catch (Exception e) {
            LOG.error("Cannot create new ProgramSequencePattern");
        }
    }
}
