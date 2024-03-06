package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramVoice;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
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
    private VoiceController voiceController;
    static final Logger LOG = LoggerFactory.getLogger(PatternMenuController.class);

    private ProgramSequencePattern activeProgramSequencePattern;

    public PatternMenuController(ProjectService projectService, ProgramEditorController programEditorController
    ) {
        this.programEditorController = programEditorController;
        this.projectService = projectService;
    }

    // TODO don't pass entities, only identifiers
    public void setup(Parent root, ProgramVoice voice, ProgramSequencePattern programSequencePattern, VoiceController voiceController) {
        this.voice = voice;
        this.voiceController = voiceController;
        activeProgramSequencePattern = programSequencePattern;
        newPattern.setOnAction(e -> createVoicePattern());
        deletePattern.setOnAction(e -> deletePattern());
    }

    private void createVoicePattern() {
        try {
            ProgramSequencePattern newPattern = new ProgramSequencePattern(UUID.randomUUID(), programEditorController.getProgramId(), programEditorController.getSequenceId(), voice.getId(), "XXX",
                    (short) 4);
            projectService.getContent().put(newPattern);
            voiceController.getProgramSequencePatternsOfThisVoice().add(newPattern);
            voiceController.updatePatternUI(newPattern);
            voiceController.setSelectedProgramSequencePattern(newPattern);
            projectService.showAlert(Alert.AlertType.INFORMATION, "", "Created", "New Pattern");
        } catch (Exception e) {
            LOG.error("Cannot create new ProgramSequencePattern");
        }
    }

    private void deletePattern() {
        try {
            projectService.deleteContent(activeProgramSequencePattern);
            voiceController.getProgramSequencePatternsOfThisVoice().remove(activeProgramSequencePattern);
            voiceController.setSelectedProgramSequencePattern(voiceController.getProgramSequencePatternsOfThisVoice().isEmpty() ?
                    null : voiceController.getProgramSequencePatternsOfThisVoice().get(0));
            voiceController.updatePatternUI(voiceController.getSelectedProgramSequencePattern());
        } catch (Exception e) {
            LOG.error("Failed to delete ProgramSequencePattern ");
        }
    }
}
