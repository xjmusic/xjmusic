package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.util.StringUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.input.MouseEvent;
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
  private final Logger LOG = LoggerFactory.getLogger(SequenceBindingColumnController.class);
  private final ApplicationContext ac;
  private final ProjectService projectService;
  private final ThemeService themeService;
  private final Resource sequenceBindingItemFxml;
  private final Resource sequenceBindingItemCreationFxml;
  private UUID programId;
  private int offset;
  @FXML
  public VBox sequenceBindingColumnContainer;

  @FXML
  public VBox sequenceBindingColumnContentContainer;

  @FXML
  public Label offsetText;

  @FXML
  public Button addSequenceButton;

  public SequenceBindingColumnController(
      @Value("classpath:/views/content/program/sequence-binding-item.fxml") Resource sequenceBindingItemFxml,
      @Value("classpath:/views/content/program/sequence-binding-item-creation.fxml") Resource sequenceBindingItemCreationFxml,
      ApplicationContext ac,
      ProjectService projectService,
      ThemeService themeService
  ) {
    this.sequenceBindingItemCreationFxml = sequenceBindingItemCreationFxml;
    this.sequenceBindingItemFxml = sequenceBindingItemFxml;
    this.ac = ac;
    this.projectService = projectService;
    this.themeService = themeService;
  }

  /**
   Set up the sequence binding column with the given offset and program ID.

   @param offset    the offset
   @param programId the program ID
   */
  public void setup(int offset, UUID programId) {
    this.offset = offset;
    this.programId = programId;
    offsetText.setText(String.valueOf(offset));

    var bindings = projectService.getContent().getBindingsAtOffsetOfProgram(programId, offset, false);
    for (var binding : bindings) {
      addProgramSequenceBindingItem(binding);
    }
  }

  void addProgramSequenceBindingItem(ProgramSequenceBinding programSequenceBinding) {
    try {
      FXMLLoader loader = new FXMLLoader(sequenceBindingItemFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      SequenceBindingItemController sequenceBindingItemController = loader.getController();
      sequenceBindingItemController.setup(programSequenceBinding, () -> {
        if (!projectService.getContent().getMemesOfSequenceBinding(programSequenceBinding.getId()).isEmpty()) {
          projectService.showWarningAlert("Failure", "Found Meme on Sequence Binding", "Cannot delete sequence binding because it contains a meme.");
        } else {
          sequenceBindingColumnContentContainer.getChildren().remove(root);
          projectService.deleteContent(programSequenceBinding);
        }
      });
      sequenceBindingColumnContentContainer.getChildren().add(root);
    } catch (IOException e) {
      LOG.error("Error adding Program Sequence Binding Item!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }

  protected void showSequenceBindingItemCreationUI(UUID programId, double sceneX, double sceneY) {
    try {
      Scene scene = sequenceBindingColumnContainer.getScene();
      Stage stage = new Stage(StageStyle.TRANSPARENT);
      FXMLLoader loader = new FXMLLoader(sequenceBindingItemCreationFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      // Apply a blur effect
      ColorAdjust darken = new ColorAdjust();
      darken.setBrightness(-0.5);
      scene.getRoot().setEffect(darken);
      SequenceBindingItemCreationController creationController = loader.getController();
      creationController.setup(programId, this::createSequenceBinding);
      stage.setOnShown(event -> creationController.sequenceSearch.show());
      stage.setScene(new Scene(root));
      stage.initOwner(themeService.getMainScene().getWindow());
      stage.show();
      closeWindowOnClickingAway(stage, null);
      stage.setX(scene.getWindow().getX() + sceneX - stage.getWidth() / 2);
      stage.setY(scene.getWindow().getY() + sceneY - stage.getHeight() / 2);
      //remove the background blur
      stage.setOnHidden(e -> scene.getRoot().setEffect(null));
    } catch (IOException e) {
      LOG.error("Error opening Sequence Search window!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }

  private void createSequenceBinding(UUID sequenceId) {
    ProgramSequenceBinding binding = new ProgramSequenceBinding();
    binding.setId(UUID.randomUUID());
    binding.setProgramId(programId);
    binding.setOffset(offset);
    binding.setProgramSequenceId(sequenceId);
    projectService.update(binding);
    addProgramSequenceBindingItem(binding);
  }

  @FXML
  protected void handlePressedAddSequenceBinding(MouseEvent e) {
    showSequenceBindingItemCreationUI(programId, e.getSceneX(), e.getSceneY());
  }
}
