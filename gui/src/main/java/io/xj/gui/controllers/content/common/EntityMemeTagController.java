package io.xj.gui.controllers.content.common;

import io.xj.gui.utils.UiUtils;
import io.xj.hub.entity.EntityException;
import io.xj.hub.entity.EntityUtils;
import io.xj.hub.util.StringUtils;
import jakarta.annotation.Nullable;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Consumer;


@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EntityMemeTagController {
  static final Logger LOG = LoggerFactory.getLogger(EntityMemeTagController.class);
  private final Collection<Runnable> clearSubscriptions = new HashSet<>();
  private static final int MEME_NAME_PADDING = 15;
  private final SimpleStringProperty name = new SimpleStringProperty("");
  private Object currentMeme;

  @Nullable
  private Consumer<Object> doUpdate;

  @Nullable
  private Consumer<Object> doDelete;

  @FXML
  HBox container;

  @FXML
  TextField nameField;

  @FXML
  Button deleteMemeButton;

  /**
   Set up the meme in the controller

   @param meme     to set up
   @param doUpdate to update the meme
   @param doDelete to delete the meme
   @throws EntityException if the meme could not be set up
   */
  public void setup(
    Object meme,
    Consumer<Object> doUpdate,
    Consumer<Object> doDelete
  ) throws EntityException {
    this.currentMeme = meme;
    this.doUpdate = doUpdate;
    this.doDelete = doDelete;

    name.set(String.valueOf(EntityUtils.get(meme, "name").orElseThrow(() -> new RuntimeException("Could not get meme name"))));

    nameField.textProperty().bindBidirectional(name);
    clearSubscriptions.add(UiUtils.onBlur(nameField, this::doneEditing));

    updateTextWidth();
  }

  public void teardown() {
    for (Runnable subscription : clearSubscriptions) subscription.run();
  }

  /**
   Updates the meme name label when done editing
   */
  private void doneEditing() {
    Objects.requireNonNull(doUpdate, "no update callback set");
    try {
      name.set(StringUtils.toMeme(nameField.getText()));
      EntityUtils.set(currentMeme, "name", name.get());
      doUpdate.accept(currentMeme);
      nameField.getParent().requestFocus();

    } catch (Exception e) {
      LOG.error("Failed to update meme! {}\n{}", e, StringUtils.formatStackTrace(e));
    }
  }

  /**
   Deletes a meme
   */
  @FXML
  void deleteMeme() {
    Objects.requireNonNull(doDelete, "no delete callback set");
    try {
      doDelete.accept(currentMeme);
    } catch (Exception e) {
      LOG.error("Failed to delete Meme! {}\n{}", e, StringUtils.formatStackTrace(e));
    }
  }

  /**
   Computes the width of a text

   @param font to use for computation
   @param text for which to compute width
   @return the width of the text
   */
  private double computeTextWidth(Font font, String text) {
    javafx.scene.text.Text helper = new javafx.scene.text.Text();
    helper.setFont(font);
    helper.setText(text);
    return helper.getBoundsInLocal().getWidth();
  }

  /**
   Update the displayed text width
   */
  private void updateTextWidth() {
    nameField.setPrefWidth(computeTextWidth(nameField.getFont(), nameField.getText() + MEME_NAME_PADDING));
  }

  @FXML
  void handleKeyPressed(KeyEvent event) {
    if (event.getCode().equals(KeyCode.ENTER)) {
      event.consume();
      doneEditing();

    } else {
      updateTextWidth();
    }
  }
}
