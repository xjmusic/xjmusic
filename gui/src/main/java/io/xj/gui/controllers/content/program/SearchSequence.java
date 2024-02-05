package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramSequence;
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

  public SearchSequence(ProjectService projectService) {
    this.projectService = projectService;
  }

  public void setUp(Collection<ProgramSequence> programSequences) {
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
