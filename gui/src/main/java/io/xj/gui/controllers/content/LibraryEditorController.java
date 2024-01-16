// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content;

import io.xj.gui.controllers.ReadyAfterBootController;
import io.xj.gui.modes.ContentMode;
import io.xj.gui.modes.ViewMode;
import io.xj.gui.services.ProjectService;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LibraryEditorController implements ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(LibraryEditorController.class);
  private final ProjectService projectService;
  private final ObjectProperty<UUID> id = new SimpleObjectProperty<>(null);
  private final StringProperty name = new SimpleStringProperty("");

  @FXML
  protected VBox container;

  @FXML
  protected TextField fieldName;

  public LibraryEditorController(
    ProjectService projectService
  ) {
    this.projectService = projectService;
  }

  @Override
  public void onStageReady() {
    var visible = projectService.isStateReadyProperty()
      .and(projectService.viewModeProperty().isEqualTo(ViewMode.Content))
      .and(projectService.contentModeProperty().isEqualTo(ContentMode.LibraryEditor));
    container.visibleProperty().bind(visible);
    container.managedProperty().bind(visible);

    fieldName.textProperty().bindBidirectional(name);
  }

  @Override
  public void onStageClose() {
    // FUTURE: on stage close
  }

  /**
   Open the given library in the content editor.

   @param libraryId library to open
   */
  public void editLibrary(UUID libraryId) {
    var library = projectService.getContent().getLibrary(libraryId)
      .orElseThrow(() -> new RuntimeException("Could not find Library"));
    LOG.info("Will open Library \"{}\"", library.getName());
    this.id.set(library.getId());
    this.name.set(library.getName());

    projectService.contentModeProperty().set(ContentMode.LibraryEditor);
    projectService.viewModeProperty().set(ViewMode.Content);
  }
}
