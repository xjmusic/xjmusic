package io.xj.gui.controllers.content.program;

import io.xj.gui.ProjectController;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProgramEditorModeEditController extends ProjectController {
  static final Logger LOG = LoggerFactory.getLogger(ProgramEditorModeEditController.class);
  private final ObjectProperty<UUID> programId = new SimpleObjectProperty<>();

  @FXML
  VBox container;

  /**
   Program Edit Bind-mode Controller

   @param fxml           FXML resource
   @param ac             application context
   @param themeService   common theme service
   @param uiStateService common UI state service
   @param projectService common project service
   */
  protected ProgramEditorModeEditController(
    @Value("classpath:/views/content/program/program-editor-mode-edit.fxml") Resource fxml,
    ApplicationContext ac,
    ThemeService themeService,
    UIStateService uiStateService,
    ProjectService projectService
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
  }

  @Override
  public void onStageReady() {
    container.visibleProperty().bind(uiStateService.programEditorBindModeProperty());
    container.managedProperty().bind(uiStateService.programEditorBindModeProperty());
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
  }
}
