package io.xj.gui.controllers.content.common;

import io.xj.hub.entity.EntityException;
import io.xj.hub.entity.EntityUtils;
import io.xj.hub.util.StringUtils;
import jakarta.annotation.Nullable;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Consumer;


@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EntityMemeTagController<M extends Serializable> {
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
  private M currentMeme;
  @Nullable
  private Consumer<M> doUpdate;
  @Nullable
  private Consumer<M> doDelete;

  public EntityMemeTagController() {
  }

  public void setup(
    Parent root,
    M meme,
    Consumer<M> doUpdate,
    Consumer<M> doDelete
  ) throws EntityException {
    this.currentMeme = meme;
    this.doUpdate = doUpdate;
    this.doDelete = doDelete;
    name.set(String.valueOf(EntityUtils.get(meme, "name").orElseThrow(() -> new RuntimeException("Could not get meme name"))));
    memeNameLabel.setText(name.get());
    memeNameField.setText(name.get());
    memeNameField.setVisible(false);
    memeNameField.setPrefColumnCount((int) computeTextWidth(memeNameField.getFont(), name.get()) + 5);
    memeNameLabel.setWrappingWidth((int) computeTextWidth(memeNameField.getFont(), name.get()) + 5);
    memeNameField.textProperty().bindBidirectional(name);
    toggleVisibility();
    setMemeTextProcessing();
    deleteMemeButton.setOnAction(e -> deleteMemeTag(root));
  }

  private void updateMeme() {
    Objects.requireNonNull(doUpdate, "no update callback set");
    try {
      String name = StringUtils.toMeme(memeNameField.getText());
      EntityUtils.set(currentMeme, "name", name);
      doUpdate.accept(currentMeme);
    } catch (Exception e) {
      LOG.error("Failed to update meme!\n{}", StringUtils.formatStackTrace(e));
    }
  }

  /**
   updates the label text on a meme item when the text is updated on the textfield
   */
  private void setMemeTextProcessing() {
    memeNameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) {
        stackPane.setPrefWidth(computeTextWidth(memeNameField.getFont(), memeNameField.getText() + 5));
        memeNameField.setPrefWidth(computeTextWidth(memeNameField.getFont(), memeNameField.getText() + 5));
        memeNameLabel.setText(StringUtils.toMeme(memeNameField.getText()));
        memeNameLabel.setWrappingWidth(computeTextWidth(memeNameField.getFont(), memeNameLabel.getText() + 5));
        memeNameLabel.setVisible(true);
        updateMeme();
      }
    });
  }


  private void deleteMemeTag(Parent root) {
    Objects.requireNonNull(doDelete, "no delete callback set");
    try {
      doDelete.accept(currentMeme);
      LOG.info("Deleted meme \"{}\"", EntityUtils.get(currentMeme, "name").orElseThrow(() -> new RuntimeException("Could not get meme name")));
    } catch (Exception e) {
      LOG.error("Failed to delete Meme!\n{}", StringUtils.formatStackTrace(e));
    }
  }

  private double computeTextWidth(javafx.scene.text.Font font, String text) {
    javafx.scene.text.Text helper = new javafx.scene.text.Text();
    helper.setFont(font);
    helper.setText(text);
    return helper.getBoundsInLocal().getWidth();
  }

  private void toggleVisibility() {
    memeNameLabel.setOnMouseClicked(e -> {
      memeNameLabel.setVisible(false);
      memeNameField.setVisible(true);
      //shift focus to the nameField
      memeNameField.requestFocus();
      //set the caret to blink at the end position
      memeNameField.positionCaret(memeNameField.getPrefColumnCount());
    });
    // Add a focus listener to the TextField
    memeNameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue) {
        memeNameField.setVisible(false);
        memeNameLabel.setVisible(true);
      }
    });
  }
}
