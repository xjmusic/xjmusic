package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.utils.UiUtils;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.util.StringUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

import static io.xj.gui.services.UIStateService.OPEN_PSEUDO_CLASS;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SequenceBindingColumnController {
  private final Logger LOG = LoggerFactory.getLogger(SequenceBindingColumnController.class);
  private final ApplicationContext ac;
  private final ProjectService projectService;
  private final ThemeService themeService;
  private final Resource sequenceBindingItemFxml;
  private final Resource sequenceSelectorFxml;
  private UUID programId;
  private int offset;
  private final Collection<SequenceBindingItemController> sequenceBindingItemControllers = new HashSet<>();

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
    @Value("classpath:/views/content/program/sequence-selector.fxml") Resource sequenceSelectorFxml,
    ApplicationContext ac,
    ProjectService projectService,
    ThemeService themeService
  ) {
    this.sequenceSelectorFxml = sequenceSelectorFxml;
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
    Objects.requireNonNull(programId, "Program ID cannot be null");
    this.offset = offset;
    this.programId = programId;
    offsetText.setText(String.valueOf(offset));

    var bindings = projectService.getContent().getBindingsAtOffsetOfProgram(programId, offset, false);
    for (var binding : bindings) {
      addProgramSequenceBindingItem(binding);
    }
  }

  /**
   Called before this controller is removed from the stage
   */
  public void teardown() {
    for (SequenceBindingItemController controller : sequenceBindingItemControllers)
      controller.teardown();
    sequenceBindingItemControllers.clear();
  }

  @FXML
  protected void handlePressedAddSequenceBinding() {
    try {
      addSequenceButton.pseudoClassStateChanged(OPEN_PSEUDO_CLASS, true);
      Stage stage = new Stage(StageStyle.TRANSPARENT);
      FXMLLoader loader = new FXMLLoader(sequenceSelectorFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      SequenceSelectorController controller = loader.getController();
      controller.setup(programId, this::createSequenceBinding);
      stage.setScene(new Scene(root));
      stage.initOwner(themeService.getMainScene().getWindow());
      stage.show();
      UiUtils.darkenBackgroundUntilClosed(stage, addSequenceButton.getScene(),
        () -> addSequenceButton.pseudoClassStateChanged(OPEN_PSEUDO_CLASS, false));
      UiUtils.closeWindowOnClickingAway(stage);
      UiUtils.setStagePositionBelowParentNode(stage, addSequenceButton);
    } catch (IOException e) {
      LOG.error("Error opening Sequence Search window!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }

  /**
   Add a program sequence binding item to the sequence binding column.

   @param programSequenceBinding to add
   */
  private void addProgramSequenceBindingItem(ProgramSequenceBinding programSequenceBinding) {
    try {
      FXMLLoader loader = new FXMLLoader(sequenceBindingItemFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      SequenceBindingItemController controller = loader.getController();
      controller.setup(programSequenceBinding, () -> {
        if (!projectService.getContent().getMemesOfSequenceBinding(programSequenceBinding.getId()).isEmpty()) {
          projectService.showWarningAlert("Failure", "Found Meme on Sequence Binding", "Cannot delete sequence binding because it contains a meme.");
        } else {
          controller.teardown();
          sequenceBindingColumnContentContainer.getChildren().remove(root);
          projectService.deleteContent(programSequenceBinding);
        }
      });
      sequenceBindingColumnContentContainer.getChildren().add(root);
    } catch (IOException e) {
      LOG.error("Error adding Program Sequence Binding Item!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }

  /**
   Create a new sequence binding.

   @param sequenceId which to bind
   */
  private void createSequenceBinding(UUID sequenceId) {
    ProgramSequenceBinding binding = new ProgramSequenceBinding();
    binding.setId(UUID.randomUUID());
    binding.setProgramId(programId);
    binding.setOffset(offset);
    binding.setProgramSequenceId(sequenceId);
    projectService.update(binding);
    addProgramSequenceBindingItem(binding);
  }
}
