package io.xj.gui.controllers.content.program.edit_mode;

import io.xj.gui.ProjectController;
import io.xj.gui.modes.ProgramEditorMode;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.util.StringUtils;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

@Service
public class EditModeController extends ProjectController {
  static final Logger LOG = LoggerFactory.getLogger(EditModeController.class);
  private final ObjectProperty<UUID> programId = new SimpleObjectProperty<>();
  private final Resource voiceFxml;
  private final BooleanBinding active;
  private final Collection<VoiceController> voiceControllers = new HashSet<>();

  @FXML
  VBox container;

  @FXML
  VBox voiceContainer;

  @FXML
  Button addVoiceButton;

  /**
   Program Edit Bind-mode Controller

   @param fxml           FXML resource
   @param ac             application context
   @param themeService   common theme service
   @param uiStateService common UI state service
   @param projectService common project service
   */
  protected EditModeController(
    @Value("classpath:/views/content/program/edit_mode/edit-mode.fxml") Resource fxml,
    @Value("classpath:/views/content/program/edit_mode/voice.fxml") Resource voiceFxml,
    ApplicationContext ac,
    ThemeService themeService,
    UIStateService uiStateService,
    ProjectService projectService
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.voiceFxml = voiceFxml;

    active = uiStateService.programEditorModeProperty().isEqualTo(ProgramEditorMode.Edit);
  }

  @Override
  public void onStageReady() {
    container.visibleProperty().bind(active);
    container.managedProperty().bind(active);
  }

  @Override
  public void onStageClose() {
    // no op
  }

  /**
   Setup the controller for a specific program

   @param programId to edit
   */
  public void setup(UUID programId) {
    this.programId.set(programId);

    for (ProgramVoice programVoice : projectService.getContent().getVoicesOfProgram(programId)) {
      addVoice(programVoice);
    }
  }

  /**
   Teardown the controller
   */
  public void teardown() {
    for (VoiceController controller : voiceControllers)
      controller.teardown();
    voiceContainer.getChildren().clear();
    voiceControllers.clear();
  }

  @FXML
  protected void handlePressedAddVoice() {
    try {
      ProgramVoice programVoice = projectService.createProgramVoice(programId.get());
      addVoice(programVoice);
    } catch (Exception e) {
      LOG.error("Could not create new Voice", e);
    }
  }

  /**
   Add a program sequence binding item to the sequence binding column.

   @param programVoice to add
   */
  private void addVoice(ProgramVoice programVoice) {
    try {
      FXMLLoader loader = new FXMLLoader(voiceFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      VoiceController controller = loader.getController();
      voiceControllers.add(controller);
      controller.setup(programVoice, () -> {
        if (!projectService.getContent().getTracksOfVoice(programVoice.getId()).isEmpty()) {
          projectService.showWarningAlert("Failure", "Found Track in Voice", "Cannot delete voice because it contains a track.");
        } else {
          controller.teardown();
          voiceControllers.remove(controller);
          voiceContainer.getChildren().remove(root);
          projectService.deleteContent(programVoice);
        }
      });
      voiceContainer.getChildren().add(root);
    } catch (IOException e) {
      LOG.error("Error adding Voice! {}\n{}", e,  StringUtils.formatStackTrace(e));
    }
  }
}
