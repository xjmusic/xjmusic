package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramSequence;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.controlsfx.control.SearchableComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class SearchSequence {
  @FXML
  public VBox container;
  @FXML
  public SearchableComboBox<Label> sequenceSearch;
  private final Logger LOG = LoggerFactory.getLogger(SearchSequence.class);
  private final ProjectService projectService;
  private ProgramSequence programSequence;

  public SearchSequence(ProjectService projectService) {
    this.projectService = projectService;
  }

  public void setUp(Collection<ProgramSequence> programSequences, ProgramSequence programSequence) {
    this.programSequence = programSequence;
    programSequences.forEach(sequence -> {
      Label sequenceLabel = new Label();
      sequenceLabel.setId(String.valueOf(sequence.getId()));
      sequenceLabel.setText(sequence.getName());
      sequenceSearch.getItems().add(sequenceLabel);
    });
    selectPassedSequence();
    // Request focus and show the ComboBox
    sequenceSearch.requestFocus();
    sequenceSearch.show();
  }

  private void selectPassedSequence() {
    if (programSequence != null) {
      ObservableList<Label> items = sequenceSearch.getItems();
      for (Label label : items) {
        if (label.getId().equals(String.valueOf(programSequence.getId()))) {
          sequenceSearch.getSelectionModel().select(label);
          break;
        }
      }
    }
  }
}
