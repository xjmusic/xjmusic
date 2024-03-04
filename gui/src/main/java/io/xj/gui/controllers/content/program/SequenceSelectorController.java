package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramSequence;
import jakarta.annotation.Nullable;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.SearchableComboBox;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SequenceSelectorController {
  private final ProjectService projectService;

  @FXML
  protected VBox container;

  @FXML
  protected SearchableComboBox<ProgramSequenceChoice> sequenceSearch;

  public SequenceSelectorController(
    ProjectService projectService
  ) {
    this.projectService = projectService;
  }

  /**
   Set up the sequence selector with the given program and current sequence.

   @param programId        the program ID
   @param onSelectSequence the consumer to select the sequence ID
   */
  public void setup(
    UUID programId,
    Consumer<UUID> onSelectSequence
  ) {
    // Set Program Sequence Choices
    sequenceSearch.setItems(FXCollections.observableList(
      projectService.getContent().getSequencesOfProgram(programId)
        .stream()
        .sorted(Comparator.comparing(ProgramSequence::getName))
        .map(ProgramSequenceChoice::new)
        .toList()
    ));

    sequenceSearch.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        onSelectSequence.accept(newValue.getId());
        closeWindow();
      }
    });

    sequenceSearch.requestFocus();
    sequenceSearch.show();
  }

  /**
   Close the window.
   */
  private void closeWindow() {
    Stage stage = (Stage) sequenceSearch.getScene().getWindow();
    stage.close();
  }

  /**
   This class is used to display the ProgramSequence name in the ChoiceBox while preserving the underlying ID
   */
  public record ProgramSequenceChoice(ProgramSequence programSequence) {
    @Override
    public String toString() {
      return Objects.nonNull(programSequence) ? programSequence.getName() : "Select...";
    }

    public @Nullable UUID getId() {
      return Objects.nonNull(programSequence) ? programSequence.getId() : null;
    }
  }
}
