package io.xj.gui.controllers.content.program.edit_mode;

import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
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

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PatternMenuControllerLegacy {
    private final ProjectService projectService;
    private final UIStateService uiStateService;
    @FXML
    public Button newPattern;
    @FXML
    public Button deletePattern;
    @FXML
    public Button clonePattern;
    static final Logger LOG = LoggerFactory.getLogger(PatternMenuControllerLegacy.class);

    private ProgramSequencePattern activeProgramSequencePattern;

    public PatternMenuControllerLegacy(
      ProjectService projectService,
      UIStateService uiStateService
    ) {
        this.uiStateService = uiStateService;
        this.projectService = projectService;
    }

    // TODO don't pass entities, only identifiers
    public void setup(Parent root, ProgramVoice voice, ProgramSequencePattern programSequencePattern, VoiceController voiceController) {
/*
  TODO pattern menu controller setup
        activeProgramSequencePattern = programSequencePattern;
        newPattern.setOnAction(e -> createVoicePattern());
        deletePattern.setOnAction(e -> deletePattern());
*/
    }

/*
 TODO pattern menu controller create voice pattern
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
*/

/*
  TODO pattern menu controller delete pattern
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
*/
}
