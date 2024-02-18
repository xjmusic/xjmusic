package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ThemeService;
import io.xj.hub.util.StringUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

import static io.xj.gui.controllers.content.program.ProgramEditorController.closeWindowOnClickingAway;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SequenceSelectorController {
  @FXML
  public VBox sequenceSelector;
  @FXML
  public Label offSet;
  @FXML
  public Button addSequenceButton;
  @Value("classpath:/views/content/program/sequence-binding-item-creation.fxml")
  private Resource createBindingFxml;
  private final ApplicationContext applicationContext;
  private final Logger LOG = LoggerFactory.getLogger(SequenceSelectorController.class);
  private final ThemeService themeService;
  private HBox bindViewParentContainer;
  private int position;

  public SequenceSelectorController(
    ApplicationContext applicationContext,
    ThemeService themeService
  ) {
    this.applicationContext = applicationContext;
    this.themeService = themeService;
  }

  public void setUp(HBox bindViewParentContainer, int position, UUID programId) {
    this.position = position;
    this.bindViewParentContainer = bindViewParentContainer;
    offSet.setText(String.valueOf(position - 1));
    addSequenceButton.setOnAction(e -> showSequenceBindingItemCreationUI(programId));
    HBox.setHgrow(sequenceSelector, Priority.ALWAYS);
  }

  protected void showSequenceBindingItemCreationUI(UUID programId) {
    try {
      Stage stage = new Stage(StageStyle.TRANSPARENT);
      FXMLLoader loader = new FXMLLoader(createBindingFxml.getURL());
      loader.setControllerFactory(applicationContext::getBean);
      Parent root = loader.load();
      // Apply a blur effect
      GaussianBlur blur = new GaussianBlur();
      sequenceSelector.getScene().getRoot().setEffect(blur);
      SequenceBindingItemCreationController creationController = loader.getController();
      creationController.setUp(bindViewParentContainer, sequenceSelector, position, programId);
      stage.setOnShown(event -> creationController.sequenceSearch.show());
      stage.setScene(new Scene(root));
      stage.initOwner(themeService.getMainScene().getWindow());
      stage.show();
      closeWindowOnClickingAway(stage);
      centerOnScreen(stage);
      //remove the background blur
      stage.setOnHidden(e -> sequenceSelector.getScene().getRoot().setEffect(null));
    } catch (IOException e) {
      LOG.error("Error opening Sequence Search window!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }

  // Method to center a UI element on the screen
  public static void centerOnScreen(Stage stage) {
    // Get the screen dimensions
    double screenWidth = stage.getOwner().getWidth(); // or Screen.getPrimary().getVisualBounds().getWidth()
    double screenHeight = stage.getOwner().getHeight(); // or Screen.getPrimary().getVisualBounds().getHeight()

    // Calculate the position of the stage
    double stageWidth = stage.getWidth();
    double stageHeight = stage.getHeight();
    double x = (screenWidth - stageWidth) / 2;
    double y = (screenHeight - stageHeight) / 2;

    // Set the stage position
    stage.setX(x);
    stage.setY(y);
  }
}
