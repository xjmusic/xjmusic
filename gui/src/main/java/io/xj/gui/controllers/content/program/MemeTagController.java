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

import java.util.UUID;

public class MemeTagController {
  @FXML
  public TextField memeNameField;
  @FXML
  public Text memeNameLabel;
  @FXML
  public Button deleteMemeButton;
  public StackPane stackPane;

  private final SimpleStringProperty name=new SimpleStringProperty("");

  public void memeTagInitializer(ProgramEditorController programEditorController, Parent root,
                                 ProjectService projectService, ProgramMeme meme, UUID programId) {
    name.set(meme.getName());
    memeNameLabel.setText(name.get());
    memeNameField.setText(name.get());
    memeNameField.setVisible(false);
    memeNameField.setPrefColumnCount((int) computeTextWidth(memeNameField.getFont(), meme.getName()) + 10);
    memeNameLabel.setWrappingWidth((int) computeTextWidth(memeNameField.getFont(), meme.getName()) + 10);
    memeNameField.textProperty().bindBidirectional(name);
    memeNameLabel.textProperty().bind(name);
    deleteMemeButton.setOnAction(e -> {
      programEditorController.memeTagContainer.getChildren().remove(root);
      projectService.getContent().getMemesOfProgram(programId).remove(meme);
      //awaiting confirmation dialog
    });
    // Set the initial width of the TextField based on the text length
    memeNameField.textProperty().addListener((observable, oldValue, newValue) -> {
      double textWidth = computeTextWidth(memeNameField.getFont(), newValue);
      memeNameField.setPrefWidth(textWidth + 10); // Adding some padding
      memeNameLabel.setWrappingWidth(textWidth + 10);
      stackPane.setPrefWidth(textWidth + 15);
    });
    toggleVisibility();
  }

  private double computeTextWidth(javafx.scene.text.Font font, String text) {
    javafx.scene.text.Text helper = new javafx.scene.text.Text();
    helper.setFont(font);
    helper.setText(text);
    return helper.getBoundsInLocal().getWidth();
  }

  private void toggleVisibility() {
    memeNameLabel.setOnMouseClicked(e->{
      memeNameLabel.setVisible(false);
      memeNameField.setVisible(true);
    });
    // Add a focus listener to the TextField
    memeNameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
        if (!newValue) {
          // Save the text (replace this with your saving logic)
          // Hide the TextField or perform other actions
          memeNameField.setVisible(false);
          memeNameLabel.setVisible(true);
        }
      });
  }
}
