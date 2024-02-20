package io.xj.gui.controllers.content.common;

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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.function.Consumer;


@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EntityMemeTagController {
  private static final int MEME_NAME_PADDING = 15;
  @FXML
  public AnchorPane memeParent;
  @FXML
  public TextField memeNameField;
  @FXML
  public Text memeNameLabel;
  @FXML
  public Button deleteMemeButton;
  public StackPane stackPane;
  private final SimpleStringProperty name = new SimpleStringProperty("");
  static final Logger LOG = LoggerFactory.getLogger(EntityMemeTagController.class);
  private Object currentMeme;
  @Nullable
  private Consumer<Object> doUpdate;
  @Nullable
  private Consumer<Object> doDelete;

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

    memeNameField.setVisible(false);
    memeNameField.textProperty().bindBidirectional(name);
    memeNameField.focusedProperty().addListener((o, ov, v) -> {
      if (!v) {
        doneEditing();
      }
    });

    memeNameLabel.setText(name.get());
    memeNameLabel.setOnMouseClicked((MouseEvent event) -> this.beginEditing());

    deleteMemeButton.setOnAction(e -> deleteMemeTag());

    updateTextWidth();
  }

  /**
   Updates the meme name label when done editing
   */
  private void doneEditing() {
    Objects.requireNonNull(doUpdate, "no update callback set");
    try {
      name.set(StringUtils.toMeme(memeNameField.getText()));
      EntityUtils.set(currentMeme, "name", name.get());
      doUpdate.accept(currentMeme);
      updateTextWidth();
      memeNameLabel.setText(name.get());
      memeNameField.setVisible(false);
      memeNameField.setManaged(false);
      memeNameLabel.setVisible(true);

    } catch (Exception e) {
      LOG.error("Failed to update meme!\n{}", StringUtils.formatStackTrace(e));
    }
  }

  /**
   Deletes a meme
   */
  private void deleteMemeTag() {
    Objects.requireNonNull(doDelete, "no delete callback set");
    try {
      doDelete.accept(currentMeme);
    } catch (Exception e) {
      LOG.error("Failed to delete Meme!\n{}", StringUtils.formatStackTrace(e));
    }
  }

  /**
   Computes the width of a text

   @param font to use for computation
   @param text for which to compute width
   @return the width of the text
   */
  private double computeTextWidth(javafx.scene.text.Font font, String text) {
    javafx.scene.text.Text helper = new javafx.scene.text.Text();
    helper.setFont(font);
    helper.setText(text);
    return helper.getBoundsInLocal().getWidth();
  }

  /**
   Toggles the visibility of the memeNameLabel vs editable memeNameField
   */
  private void beginEditing() {
    memeNameLabel.setVisible(false);
    memeNameField.setVisible(true);
    memeNameField.setManaged(true);
    memeNameField.requestFocus();
  }

  /**
   Update the displayed text width
   */
  private void updateTextWidth() {
    stackPane.setPrefWidth(computeTextWidth(memeNameField.getFont(), memeNameField.getText() + MEME_NAME_PADDING));
    memeNameField.setPrefWidth(computeTextWidth(memeNameField.getFont(), memeNameField.getText() + MEME_NAME_PADDING));
    memeNameField.setPrefWidth(computeTextWidth(memeNameField.getFont(), memeNameField.getText() + MEME_NAME_PADDING));
  }

  @FXML
  protected void handleKeyPressed(KeyEvent event) {
    if (event.getCode().equals(KeyCode.ENTER)) {
      event.consume();
      doneEditing();

    } else {
      updateTextWidth();
    }
  }
}
