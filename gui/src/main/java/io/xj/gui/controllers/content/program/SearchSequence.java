package io.xj.gui.controllers.content.program;

import io.xj.hub.tables.pojos.ProgramSequence;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.controlsfx.control.SearchableComboBox;
import org.springframework.stereotype.Component;

@Component
public class SearchSequence {
  @FXML
  public VBox container;
  @FXML
  public SearchableComboBox<ProgramSequence> sequenceSearch;
  private ProgramSequence programSequence;
  private final ProgramEditorController programEditorController;

  public SearchSequence(ProgramEditorController programEditorController) {
    this.programEditorController = programEditorController;
  }

  public void setUp(ProgramSequence programSequence) {
    this.programSequence = programSequence;
    selectPassedSequence();
    setCombobox();
    // Request focus and show the ComboBox
    sequenceSearch.requestFocus();
    sequenceSearch.show();
    selectAnElement();
  }

  private void selectAnElement() {
    sequenceSearch.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        programEditorController.setSequenceId(newValue.getId());
        programEditorController.sequencePropertyName.set(newValue.getName());
      }
    });
  }

  private void setCombobox() {
    // Clear existing items
    sequenceSearch.getItems().clear();

    sequenceSearch.getItems().addAll(programEditorController.programSequenceObservableList);

    // Set the cell factory to display the name of ProgramSequence
    sequenceSearch.setCellFactory(param -> new ListCell<>() {
      @Override
      protected void updateItem(ProgramSequence item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
        } else {
          setText(item.getName());
        }
      }
    });

    // Set the string converter to get ProgramSequence from its name
    sequenceSearch.setConverter(new StringConverter<>() {
      @Override
      public String toString(ProgramSequence object) {
        return object != null ? object.getName() : "";
      }

      @Override
      public ProgramSequence fromString(String string) {
        return programEditorController.programSequenceObservableList.stream()
          .filter(sequence -> sequence.getName().equals(string))
          .findFirst()
          .orElse(null);
      }
    });

    // Set the selected value
    sequenceSearch.setValue(programSequence);
  }

  private void selectPassedSequence() {
    if (programSequence != null) {
      ObservableList<ProgramSequence> items = sequenceSearch.getItems();
      for (ProgramSequence item : items) {
        if (item.getId().equals(programSequence.getId())) {
          sequenceSearch.getSelectionModel().select(item);
          break;
        }
      }
    }
  }
}
