package io.xj.gui.controllers.content.program;

import io.xj.gui.ProjectController;
import io.xj.gui.modes.ProgramEditorMode;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ModeEditController extends ProjectController {
  static final Logger LOG = LoggerFactory.getLogger(ModeEditController.class);
  private final ObjectProperty<UUID> programId = new SimpleObjectProperty<>();
  private final BooleanBinding active;

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
  protected ModeEditController(
    @Value("classpath:/views/content/program/mode-edit.fxml") Resource fxml,
    ApplicationContext ac,
    ThemeService themeService,
    UIStateService uiStateService,
    ProjectService projectService
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);

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

  @FXML
  protected void handlePressedAddVoice() {
    LOG.info("Add voice button pressed");
  }

  /**
   Setup the controller for a specific program

   @param programId to edit
   */
  public void setup(UUID programId) {
    this.programId.set(programId);
  }

  /**
    Teardown the controller
   */
  public void teardown() {
    // TODO teardown all interior content
  }
}
