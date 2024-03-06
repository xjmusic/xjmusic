package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramVoice;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.controlsfx.control.SearchableComboBox;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PatternSelectorController {
    private final ProjectService projectService;
    private final ProgramEditorController programEditorController;
    private Collection<ProgramSequencePattern> programSequencePatternsOfVoice;
    private ProgramVoice voice;
    private  VoiceController voiceController;

    @FXML
    protected VBox container;

    @FXML
    protected SearchableComboBox<ProgramSequencePattern> patternSelector;

    public PatternSelectorController(ProjectService projectService, ProgramEditorController programEditorController) {
        this.projectService = projectService;
        this.programEditorController = programEditorController;
    }

    // TODO don't pass entities, only identifiers
    public void setup(Collection<ProgramSequencePattern> programSequencePatternsOfVoice, ProgramVoice voice, VoiceController voiceController, Stage stage) {
        this.voice = voice;
        this.voiceController=voiceController;
        this.programSequencePatternsOfVoice = programSequencePatternsOfVoice;
        setCombobox();
        selectProgramSequencePattern(stage);
        patternSelector.show();
    }

    private void setCombobox() {
        // Clear existing items
        patternSelector.getItems().clear();
        patternSelector.getItems().addAll(programSequencePatternsOfVoice);

        // Set the cell factory to display the name of ProgramSequence
        patternSelector.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ProgramSequencePattern item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        // Set the string converter to get ProgramSequence from its name
        patternSelector.setConverter(new StringConverter<>() {
            @Override
            public String toString(ProgramSequencePattern object) {
                return object != null ? object.getName() : "";
            }

            @Override
            public ProgramSequencePattern fromString(String string) {
                return programSequencePatternsOfVoice.stream()
                        .filter(programSequencePattern -> programSequencePattern.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
        patternSelector.getSelectionModel().selectFirst();
        voiceController.setSelectedProgramSequencePattern(patternSelector.getSelectionModel().getSelectedItem());
    }

    private void selectProgramSequencePattern(Stage stage) {
        patternSelector.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                voiceController.setSelectedProgramSequencePattern(newValue);
                voiceController.updatePatternUI(newValue);
                Platform.runLater(stage::close);
            }
        });
    }

}
