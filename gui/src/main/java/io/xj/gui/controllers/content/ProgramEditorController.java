// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content;

import io.xj.gui.controllers.ReadyAfterBootController;
import io.xj.gui.modes.ContentMode;
import io.xj.gui.modes.ViewMode;
import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.Program;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProgramEditorController implements ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(ProgramEditorController.class);
  private final ProjectService projectService;
  private final ObjectProperty<UUID> libraryId = new SimpleObjectProperty<>(null);
  private final ObjectProperty<UUID> id = new SimpleObjectProperty<>(null);
  private final StringProperty name = new SimpleStringProperty("");

  @FXML
  protected VBox container;

  @FXML
  protected TextField fieldName;

  @FXML
  protected Button backButton;

  public ProgramEditorController(
    ProjectService projectService
  ) {
    this.projectService = projectService;
  }

  @Override
  public void onStageReady() {
    var visible = projectService.isStateReadyProperty()
      .and(projectService.viewModeProperty().isEqualTo(ViewMode.Content))
      .and(projectService.contentModeProperty().isEqualTo(ContentMode.ProgramEditor));
    container.visibleProperty().bind(visible);
    container.managedProperty().bind(visible);

    backButton.textProperty().bind(Bindings.createStringBinding(
      () -> String.format("« Programs of \"%s\" Library", projectService.getContent().getLibrary(libraryId.get())
        .orElseThrow(() -> new RuntimeException("Could not find Library for Program"))
        .getName()),
      libraryId
    ));

    fieldName.textProperty().bindBidirectional(name);
  }

  @Override
  public void onStageClose() {
    // FUTURE: on stage close
  }

  /**
   Open the given program in the content editor.

   @param ref program to open
   */
  public void editProgram(Program ref) {
    var program = projectService.getContent().getProgram(ref.getId())
      .orElseThrow(() -> new RuntimeException("Could not find Program"));
    LOG.info("Will open Program \"{}\"", program.getName());
    this.id.set(program.getId());
    this.libraryId.set(program.getLibraryId());
    this.name.set(program.getName());

    projectService.contentModeProperty().set(ContentMode.ProgramEditor);
  }

  @FXML
  protected void handleBackToProgramBrowser() {
    projectService.contentModeProperty().set(ContentMode.ProgramBrowser);
  }
}
