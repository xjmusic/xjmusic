package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramMeme;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;


@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MemeTagController {
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
  private ProgramMeme currentMeme;
  private UUID programId;
  //  @Autowired
  private final ProgramEditorController programEditorController;

  public MemeTagController(ProjectService projectService, ProgramEditorController programEditorController) {
    this.projectService = projectService;
    this.programEditorController = programEditorController;
  }

  public void setUp(Parent root, ProgramMeme meme, UUID programId) {
    this.currentMeme = meme;
    this.programId = programId;
    name.set(meme.getName());
    memeNameLabel.setText(name.get());
    memeNameField.setText(name.get());
    memeNameField.setVisible(false);
    memeNameField.setPrefColumnCount((int) computeTextWidth(memeNameField.getFont(), meme.getName()) + 5);
    memeNameLabel.setWrappingWidth((int) computeTextWidth(memeNameField.getFont(), meme.getName()) + 5);
    memeNameField.textProperty().bindBidirectional(name);
    projectService.getContent().getMemesOfProgram(programId);
    toggleVisibility();
    setMemeTextProcessing();
    deleteMemeButton.setOnAction(e -> deleteMemeTag(root));
  }

  private void updateMeme() {
    try {
      var memes = projectService.getContent().getMemesOfProgram(programId);
      memes.forEach(programMeme -> {
        if (programMeme.getId().equals(currentMeme.getId())) {
          programMeme.setName(memeNameField.getText());
          currentMeme.setName(memeNameField.getText());
        }
      });
      projectService.getContent().getMemesOfProgram(programId).clear();
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
        memeNameLabel.setWrappingWidth(computeTextWidth(memeNameField.getFont(), memeNameField.getText() + 5));
        memeNameLabel.setText(memeNameField.getText());
        memeNameLabel.setVisible(true);
        updateMeme();
      }
    });
  }


  private void deleteMemeTag(Parent root) {
    try {
      projectService.getContent().getProgramMemes().removeIf(meme -> meme.getId().equals(currentMeme.getId()));
      //notify modification
      projectService.isModifiedProperty().set(true);
      //remove the meme from UI
      programEditorController.memeTagContainer.getChildren().remove(root);
      LOG.info("Deleted " + currentMeme.getName());
    } catch (Exception e) {
      LOG.error("Failed to delete Meme " + Arrays.toString(e.getStackTrace()) + " !!");
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
