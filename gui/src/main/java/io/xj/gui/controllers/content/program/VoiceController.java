package io.xj.gui.controllers.content.program;

import io.xj.hub.util.StringUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VoiceController {
  @FXML
  public VBox voiceControlsContainer;
  @FXML
  public Button deleteButton;
  @FXML
  public Text voiceName;
  @FXML
  public ComboBox<String> voiceCombobox;
  @FXML
  public Button searchPatternDropDown;
  @FXML
  public Button patternMenuButton;
  @FXML
  public TextField patternNameField;
  @FXML
  public Label noPatternLabel;
  @FXML
  public Label patternTotalCountLabel;
  @FXML
  public AnchorPane trackContainer;
  @FXML
  public Group trackButtonGroup;
  @FXML
  public Button addTrackButton;
  @FXML
  public Button trackMenuButton;
  @FXML
  public Button addTrackButton_1;
  @FXML
  public Text trackName;
  private final ProgramEditorController programEditorController;
  @FXML
  public GridPane grid;
  static final Logger LOG = LoggerFactory.getLogger(VoiceController.class);
  @FXML
  public VBox voiceContainer;

  @Value("classpath:/views/content/program/track.fxml")
  private Resource trackFxml;
  private final ApplicationContext ac;

  public VoiceController(ProgramEditorController programEditorController,
                         ApplicationContext ac
                         ){
    this.programEditorController=programEditorController;
    this.ac=ac;
  }

  protected void setUp(Parent root) {
    deleteVoice(root);
  }

  private void deleteVoice(Parent root){
   deleteButton.setOnAction(e-> programEditorController.editModeContainer.getChildren().remove(root));
   hideItemsBeforeTrackIsCreated();
  }

  private void hideItemsBeforeTrackIsCreated(){
    addTrackButton.toFront();
    addTrackButton_1.setVisible(false);
    trackName.setVisible(false);
    grid.setVisible(false);
    addNewTrackToCurrentVoiceLine();
    addNewTrackToNewLine();
  }

  private void addNewTrackToCurrentVoiceLine(){
    addTrackButton.setOnAction(e->showItemsAfterTrackIsCreated());
  }

  private void addNewTrackToNewLine(){
    addTrackButton_1.setOnAction(e->addTrackItemToNewLine());
  }

  protected void addTrackItemToNewLine(){
    TrackController.trackItem(trackFxml, ac, voiceContainer, LOG);
  }

  private void showItemsAfterTrackIsCreated(){
    trackMenuButton.toFront();
    addTrackButton_1.setVisible(true);
    trackName.setVisible(true);
    grid.setVisible(true);
  }
}
