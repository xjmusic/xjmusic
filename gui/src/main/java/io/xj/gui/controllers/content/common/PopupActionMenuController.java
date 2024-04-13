package io.xj.gui.controllers.content.common;


import jakarta.annotation.Nullable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PopupActionMenuController {
  @Nullable
  private Runnable onCreated;

  @Nullable
  private Runnable onDelete;

  @Nullable
  private Runnable onDuplicate;

  @FXML
  HBox container;

  @FXML
  Button createButton;

  @FXML
  Button deleteButton;

  @FXML
  Button duplicateButton;


  /**
   Set up the popup menu

   @param onCreate    callback to create track
   @param onDelete    callback to delete track
   @param onDuplicate callback to duplicate track
   */
  public void setup(
    @Nullable String createText,
    @Nullable Runnable onCreate,
    @Nullable Runnable onDelete,
    @Nullable Runnable onDuplicate
  ) {
    this.onCreated = onCreate;
    this.onDelete = onDelete;
    this.onDuplicate = onDuplicate;

    if (Objects.nonNull(createText)) createButton.setText(createText);
    createButton.setVisible(Objects.nonNull(onCreate));
    createButton.setManaged(Objects.nonNull(onCreate));
    deleteButton.setVisible(Objects.nonNull(onDelete));
    deleteButton.setManaged(Objects.nonNull(onDelete));
    duplicateButton.setVisible(Objects.nonNull(onDuplicate));
    duplicateButton.setManaged(Objects.nonNull(onDuplicate));
  }

  /**
   Close the menu
   */
  private void teardown() {
    Stage stage = (Stage) container.getScene().getWindow();
    stage.close();
  }

  @FXML
  void handlePressedCreate() {
    Objects.requireNonNull(onCreated);
    onCreated.run();
    teardown();
  }

  @FXML
  void handlePressedDelete() {
    Objects.requireNonNull(onDelete);
    onDelete.run();
    teardown();
  }

  @FXML
  void handlePressedDuplicate() {
    Objects.requireNonNull(onDuplicate);
    onDuplicate.run();
    teardown();
  }
}
