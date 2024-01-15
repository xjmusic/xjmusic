// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content;

import io.xj.gui.controllers.BrowserController;
import io.xj.gui.controllers.ReadyAfterBootController;
import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.Program;
import io.xj.nexus.project.ProjectUpdate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class ProgramBrowserController extends BrowserController implements ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(ProgramBrowserController.class);
  private final ProjectService projectService;
  private final ProgramEditorController programEditorController;
  private final ObservableList<Program> programs = FXCollections.observableList(new ArrayList<>());

  @FXML
  protected TableView<Program> table;

  public ProgramBrowserController(
    ProjectService projectService,
    ProgramEditorController programEditorController
  ) {
    this.projectService = projectService;
    this.programEditorController = programEditorController;
  }

  @Override
  public void onStageReady() {
    addColumn(table, 200, "name", "Name");
    addColumn(table, 90, "type", "Type");
    addColumn(table, 50, "key", "Key");
    addColumn(table, 50, "tempo", "Tempo");
    addColumn(table, 50, "density", "Density");
    setupData(
      table,
      programs,
      program -> LOG.info("Did select Program"),
      programEditorController::openProgram
    );
    projectService.addProjectUpdateListener(ProjectUpdate.Programs, this::updatePrograms);
  }

  @Override
  public void onStageClose() {
    // FUTURE: on stage close
  }

  /**
   Update the programs table data.
   */
  private void updatePrograms() {
    programs.setAll(projectService.getPrograms());
  }
}
