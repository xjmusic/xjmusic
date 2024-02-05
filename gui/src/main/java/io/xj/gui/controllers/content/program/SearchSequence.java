package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramSequence;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.SearchableComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class SearchSequence {
  private final Logger LOG = LoggerFactory.getLogger(SearchSequence.class);
  private final ProjectService projectService;

  @FXML
  public VBox container;
  @FXML
  public SearchableComboBox<Label> sequenceSearch;

  public SearchSequence(ProjectService projectService) {
    this.projectService = projectService;
  }

  public void sequenceSearchUIInitializer(Collection<ProgramSequence> programSequences, Stage stage) {
    programSequences.forEach(programSequence -> {
      Label sequenceLabel = new Label();
      sequenceLabel.setId(String.valueOf(programSequence.getId()));
      sequenceLabel.setText(programSequence.getName());
      sequenceSearch.getItems().add(sequenceLabel);
    });
    sequenceSearch.requestFocus();
    sequenceSearch.show();
  }
}
