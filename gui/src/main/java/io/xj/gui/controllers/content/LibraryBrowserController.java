// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content;

import io.xj.gui.controllers.BrowserController;
import io.xj.gui.controllers.ReadyAfterBootController;
import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.Library;
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
public class LibraryBrowserController extends BrowserController implements ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(LibraryBrowserController.class);
  private final ProjectService projectService;
  private final LibraryEditorController libraryEditorController;
  private final ObservableList<Library> librarys = FXCollections.observableList(new ArrayList<>());

  @FXML
  protected TableView<Library> table;

  public LibraryBrowserController(
    ProjectService projectService,
    LibraryEditorController libraryEditorController
  ) {
    this.projectService = projectService;
    this.libraryEditorController = libraryEditorController;
  }

  @Override
  public void onStageReady() {
    addColumn(table, 200, "name", "Name");
    setupData(
      table,
      librarys,
      library -> LOG.debug("Did select Library"),
      libraryEditorController::openLibrary
    );
    projectService.addProjectUpdateListener(ProjectUpdate.Libraries, this::updateLibraries);
  }

  @Override
  public void onStageClose() {
    // FUTURE: on stage close
  }

  /**
   Update the librarys table data.
   */
  private void updateLibraries() {
    librarys.setAll(projectService.getLibraries());
  }
}
