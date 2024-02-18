package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramVoice;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.controlsfx.control.SearchableComboBox;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PatternSearchController {
    @FXML

    public VBox container;
    @FXML
    public SearchableComboBox<ProgramSequencePattern> patternSearch;
    private final ProjectService projectService;
    private final ProgramEditorController programEditorController;
    private ProgramSequencePattern programSequencePattern;

    private ProgramVoice voice;

    public PatternSearchController(ProjectService projectService,
                                   ProgramEditorController programEditorController){
        this.projectService=projectService;
        this.programEditorController=programEditorController;
    }
    public void setUp(ProgramSequencePattern programSequencePattern, ProgramVoice voice) {
        this.voice=voice;
        this.programSequencePattern=programSequencePattern;
        selectPassedSequence();
        setCombobox();
        // Request focus and show the ComboBox
        patternSearch.requestFocus();
        patternSearch.show();
    }


    private void setCombobox() {
        // Clear existing items
        patternSearch.getItems().clear();
        Collection<ProgramSequencePattern> programSequencePatterns=projectService.getContent().getPatternsOfSequence(programEditorController.getSequenceId());
        patternSearch.getItems().addAll(programSequencePatterns);

        // Set the cell factory to display the name of ProgramSequence
        patternSearch.setCellFactory(param -> new ListCell<>() {
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
        patternSearch.setConverter(new StringConverter<>() {
            @Override
            public String toString(ProgramSequencePattern object) {
                return object != null ? object.getName() : "";
            }

            @Override
            public ProgramSequencePattern fromString(String string) {
                return programSequencePatterns.stream()
                        .filter(programSequencePattern -> programSequencePattern.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

    }

    private void selectPassedSequence() {
        if (programSequencePattern != null) {
            ObservableList<ProgramSequencePattern> items = patternSearch.getItems();
            for (ProgramSequencePattern item : items) {
                if (item.getId().equals(programSequencePattern.getId())) {
                    patternSearch.getSelectionModel().select(item);
                    break;
                }
            }
        }
    }
}
