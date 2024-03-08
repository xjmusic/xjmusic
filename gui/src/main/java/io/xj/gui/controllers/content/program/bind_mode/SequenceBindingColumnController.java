package io.xj.gui.controllers.content.program.bind_mode;

import io.xj.gui.controllers.content.common.PopupSelectorMenuController;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.utils.UiUtils;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.util.StringUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
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

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SequenceBindingColumnController {
  private final Logger LOG = LoggerFactory.getLogger(SequenceBindingColumnController.class);
  private final ApplicationContext ac;
  private final ProjectService projectService;
  private final ThemeService themeService;
  private final Resource sequenceBindingItemFxml;
  private final Resource popupSelectorMenuFxml;
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
    @Value("classpath:/views/content/program/bind_mode/sequence-binding-item.fxml") Resource sequenceBindingItemFxml,
    @Value("classpath:/views/content/common/popup-selector-menu.fxml") Resource popupSelectorMenuFxml,
    ApplicationContext ac,
    ProjectService projectService,
    ThemeService themeService
  ) {
    this.popupSelectorMenuFxml = popupSelectorMenuFxml;
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
    sequenceBindingColumnContentContainer.getChildren().clear();
    sequenceBindingItemControllers.clear();
  }

  @FXML
  protected void handlePressedAddSequenceBinding() {
    UiUtils.launchModalMenu(addSequenceButton, popupSelectorMenuFxml, ac, themeService.getMainScene().getWindow(), true,
      (PopupSelectorMenuController controller) -> controller.setup(
        projectService.getContent().getSequencesOfProgram(programId),
        this::createSequenceBinding
      )
    );
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
      sequenceBindingItemControllers.add(controller);
      controller.setup(programSequenceBinding, () -> {
        if (!projectService.getContent().getMemesOfSequenceBinding(programSequenceBinding.getId()).isEmpty()) {
          projectService.showWarningAlert("Failure", "Found Meme on Sequence Binding", "Cannot delete sequence binding because it contains a meme.");
        } else {
          controller.teardown();
          sequenceBindingItemControllers.remove(controller);
          sequenceBindingColumnContentContainer.getChildren().remove(root);
          projectService.deleteContent(programSequenceBinding);
        }
      });
      sequenceBindingColumnContentContainer.getChildren().add(root);
    } catch (IOException e) {
      LOG.error("Error adding Program Sequence Binding Item! {}\n{}", e, StringUtils.formatStackTrace(e));
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
