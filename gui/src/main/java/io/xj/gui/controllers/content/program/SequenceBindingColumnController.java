package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ThemeService;
import io.xj.hub.util.StringUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
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

import static io.xj.gui.utils.WindowUtils.closeWindowOnClickingAway;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SequenceBindingColumnController {
  @FXML
  public VBox sequenceBindingColumnContainer;
  @FXML
  public Label offset;
  @FXML
  public Button addSequenceButton;
  @Value("classpath:/views/content/program/sequence-binding-item-creation.fxml")
  private Resource createBindingFxml;
  private final ApplicationContext applicationContext;
  private final Logger LOG = LoggerFactory.getLogger(SequenceBindingColumnController.class);
  private final ThemeService themeService;
  private HBox bindViewParentContainer;
  private int position;

  public SequenceBindingColumnController(
    ApplicationContext applicationContext,
    ThemeService themeService
  ) {
    this.applicationContext = applicationContext;
    this.themeService = themeService;
  }

  public void setUp(HBox bindViewParentContainer, int position, UUID programId) {
    this.position = position;
    this.bindViewParentContainer = bindViewParentContainer;
    offset.setText(String.valueOf(position - 1));
    addSequenceButton.setOnMouseClicked(e -> showSequenceBindingItemCreationUI(programId, e.getSceneX(), e.getSceneY()));
    HBox.setHgrow(sequenceBindingColumnContainer, Priority.ALWAYS);
  }

  protected void showSequenceBindingItemCreationUI(UUID programId, double sceneX, double sceneY) {
    try {
      Stage stage = new Stage(StageStyle.TRANSPARENT);
      FXMLLoader loader = new FXMLLoader(createBindingFxml.getURL());
      loader.setControllerFactory(applicationContext::getBean);
      Parent root = loader.load();
      // Apply a blur effect
      ColorAdjust darken = new ColorAdjust();
      darken.setBrightness(-0.5);
      sequenceBindingColumnContainer.getScene().getRoot().setEffect(darken);
      SequenceBindingItemCreationController creationController = loader.getController();
      creationController.setUp(bindViewParentContainer, sequenceBindingColumnContainer, position, programId);
      stage.setOnShown(event -> creationController.sequenceSearch.show());
      stage.setScene(new Scene(root));
      stage.initOwner(themeService.getMainScene().getWindow());
      stage.show();
      closeWindowOnClickingAway(stage);
      stage.setX(sequenceBindingColumnContainer.getScene().getWindow().getX() + sceneX - stage.getWidth() / 2);
      stage.setY(sequenceBindingColumnContainer.getScene().getWindow().getY() + sceneY - stage.getHeight() / 2);
      //remove the background blur
      stage.setOnHidden(e -> sequenceBindingColumnContainer.getScene().getRoot().setEffect(null));
    } catch (IOException e) {
      LOG.error("Error opening Sequence Search window!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }
}
