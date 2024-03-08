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
  private Runnable onClone;

  @FXML
  HBox container;

  @FXML
  Button createButton;

  @FXML
  Button deleteButton;

  @FXML
  Button cloneButton;


  /**
   Setup the popup menu

   @param onCreate callback to create track
   @param onDelete callback to delete track
   @param onClone  callback to clone track
   */
  public void setup(
    String createText,
    @Nullable Runnable onCreate,
    @Nullable Runnable onDelete,
    @Nullable Runnable onClone
  ) {
    this.onCreated = onCreate;
    this.onDelete = onDelete;
    this.onClone = onClone;

    createButton.setText(createText);
    createButton.setVisible(Objects.nonNull(onCreate));
    createButton.setManaged(Objects.nonNull(onCreate));
    deleteButton.setVisible(Objects.nonNull(onDelete));
    deleteButton.setManaged(Objects.nonNull(onDelete));
    cloneButton.setVisible(Objects.nonNull(onClone));
    cloneButton.setManaged(Objects.nonNull(onClone));
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
  void handlePressedClone() {
    Objects.requireNonNull(onClone);
    onClone.run();
    teardown();
  }
}
