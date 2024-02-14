package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramSequenceBindingMeme;
import io.xj.hub.util.StringUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProgramSequenceMemeTagController {
  @FXML
  public TextField memeNameField;
  @FXML
  public Text memeNameLabel;
  @FXML
  public Button deleteMemeButton;
  public StackPane stackPane;
  private final SimpleStringProperty name = new SimpleStringProperty("");
  static final Logger LOG = LoggerFactory.getLogger(MemeTagController.class);
  private final ProjectService projectService;
  @FXML
  public AnchorPane memeParent;
  private ProgramSequenceBindingMeme currentMeme;
  private UUID sequenceBindingId;
  private VBox sequenceHolder;
  private AnchorPane parentAnchorPane;
  private HBox memeHolder;

  public ProgramSequenceMemeTagController(ProjectService projectService) {
    this.projectService = projectService;
  }

  public void setUp(Parent root, ProgramSequenceBindingMeme meme, UUID sequenceBindingId, HBox memeHolder, VBox sequenceHolder, AnchorPane parentAnchorPane) {
    this.sequenceHolder = sequenceHolder;
    this.parentAnchorPane = parentAnchorPane;
    this.currentMeme = meme;
    this.sequenceBindingId = sequenceBindingId;
    this.memeHolder=memeHolder;
    name.set(meme.getName());
    memeNameLabel.setText(name.get());
    memeNameField.setText(name.get());
    memeNameField.setVisible(false);
    memeNameField.setPrefColumnCount((int) computeTextWidth(memeNameField.getFont(), meme.getName()) + 5);
    memeNameLabel.setWrappingWidth((int) computeTextWidth(memeNameField.getFont(), meme.getName()) + 5);
    memeNameField.textProperty().bindBidirectional(name);
    projectService.getContent().getMemesOfSequenceBinding(sequenceBindingId);
    toggleVisibility();
    setMemeTextProcessing();
    deleteMemeButton.setOnAction(e -> deleteMemeTag(root, memeHolder));
  }

  private void updateMeme() {
    try {
      var memes = projectService.getContent().getMemesOfSequenceBinding(sequenceBindingId);
      String name = StringUtils.toMeme(memeNameField.getText());
      memes.forEach(sequenceBindingMeme -> {
        if (sequenceBindingMeme.getId().equals(currentMeme.getId())) {
          sequenceBindingMeme.setName(name);
          currentMeme.setName(name);
        }
      });
      projectService.getContent().getMemesOfSequenceBinding(sequenceBindingId).clear();
      projectService.getContent().putAll(memes);
      projectService.isModifiedProperty().set(true);
    } catch (Exception e) {
      LOG.error("Failed to update " + currentMeme.getName() + " Meme");
    }
  }

  /**
   * updates the label text on a meme item when the text is updated on the textfield
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

  private void deleteMemeTag(Parent root, HBox memeHolder) {
    try {
      projectService.getContent().getProgramSequenceBindings().removeIf(meme -> meme.getId().equals(currentMeme.getId()));
      //notify modification
      projectService.isModifiedProperty().set(true);
      // Remove the child node from memeHolder
      memeHolder.getChildren().remove(root);
      // Assuming memeHolder and memeParent are valid nodes
      if (isLastItemWithChildren() || isWithMoreChildren()) {
        sequenceHolder.setPrefWidth(sequenceHolder.getWidth() - memeParent.getPrefWidth());
      }

    } catch (Exception e) {
      LOG.error("Failed to delete Meme " + Arrays.toString(e.getStackTrace()) + " !!");
    }
  }

  private boolean isLastItemWithChildren() {
    AtomicBoolean isLastItemWithChildren = new AtomicBoolean(true);
    sequenceHolder.getChildren().forEach(node -> {
      if (node instanceof AnchorPane && node != parentAnchorPane) {
        ((AnchorPane) node).getChildren().forEach(anchorPaneChild -> {
          if (anchorPaneChild instanceof HBox && ((HBox) anchorPaneChild).getChildren().size() > 0) {
            isLastItemWithChildren.set(false);
          }
        });
      }
    });
    return isLastItemWithChildren.get();
  }

  private boolean isWithMoreChildren() {
    AtomicBoolean isWithMoreChildren = new AtomicBoolean(true);
    sequenceHolder.getChildren().forEach(node -> {
      if (node instanceof AnchorPane && node != parentAnchorPane) {
        ((AnchorPane) node).getChildren().forEach(anchorPaneChild -> {
          if (anchorPaneChild instanceof HBox && ((HBox) anchorPaneChild).getChildren().size() > memeHolder.getChildren().size()) {
            isWithMoreChildren.set(false);
          }
        });
      }
    });
    return isWithMoreChildren.get();
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
