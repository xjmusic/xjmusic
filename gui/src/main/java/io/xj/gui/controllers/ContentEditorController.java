// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.ProjectEditMode;
import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.Template;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ContentEditorController implements ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(ContentEditorController.class);
  private final ContentTemplateEditorController contentTemplateEditorController;
  private final ContentLibraryEditorController contentLibraryEditorController;
  private final ContentProgramEditorController contentProgramEditorController;
  private final ContentInstrumentEditorController contentInstrumentEditorController;
  private final ProjectService projectService;

  @FXML
  protected VBox templateEditor;

  @FXML
  protected VBox libraryEditor;

  @FXML
  protected VBox programEditor;

  @FXML
  protected VBox instrumentEditor;

  public ContentEditorController(
    ContentTemplateEditorController contentTemplateEditorController,
    ContentLibraryEditorController contentLibraryEditorController,
    ContentProgramEditorController contentProgramEditorController,
    ContentInstrumentEditorController contentInstrumentEditorController,
    ProjectService projectService
  ) {
    this.contentTemplateEditorController = contentTemplateEditorController;
    this.contentLibraryEditorController = contentLibraryEditorController;
    this.contentProgramEditorController = contentProgramEditorController;
    this.contentInstrumentEditorController = contentInstrumentEditorController;
    this.projectService = projectService;
  }

  @Override
  public void onStageReady() {
    contentTemplateEditorController.onStageReady();
    contentLibraryEditorController.onStageReady();
    contentProgramEditorController.onStageReady();
    contentInstrumentEditorController.onStageReady();

    templateEditor.visibleProperty().bind(projectService.editModeProperty().isEqualTo(ProjectEditMode.Template));
    libraryEditor.visibleProperty().bind(projectService.editModeProperty().isEqualTo(ProjectEditMode.Library));
    programEditor.visibleProperty().bind(projectService.editModeProperty().isEqualTo(ProjectEditMode.Program));
    instrumentEditor.visibleProperty().bind(projectService.editModeProperty().isEqualTo(ProjectEditMode.Instrument));
  }

  @Override
  public void onStageClose() {
    // FUTURE: on stage close
  }

  /**
   Open the given template in the content editor.

   @param template the template to open
   */
  public void openTemplate(Template template) {
    LOG.info("Will open Template \"{}\"", template.getName());
    contentTemplateEditorController.setTemplate(template);
projectService.editModeProperty().set(ProjectEditMode.Template);
  }

  /**
   Open the given library in the content editor.

   @param library the library to open
   */
  public void openLibrary(Library library) {
    LOG.info("Will open Library \"{}\"", library.getName());
    contentLibraryEditorController.setLibrary(library);
    projectService.editModeProperty().set(ProjectEditMode.Library);
  }

  /**
   Open the given program in the content editor.

   @param program the program to open
   */
  public void openProgram(Program program) {
    LOG.info("Will open Program \"{}\"", program.getName());
    contentProgramEditorController.setProgram(program);
    projectService.editModeProperty().set(ProjectEditMode.Program);
  }

  /**
   Open the given instrument in the content editor.

   @param instrument the instrument to open
   */
  public void openInstrument(Instrument instrument) {
    LOG.info("Will open Instrument \"{}\"", instrument.getName());
    contentInstrumentEditorController.setInstrument(instrument);
    projectService.editModeProperty().set(ProjectEditMode.Instrument);
  }
}
