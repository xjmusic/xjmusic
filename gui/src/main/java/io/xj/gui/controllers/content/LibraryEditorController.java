// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content;

import io.xj.gui.controllers.ReadyAfterBootController;
import io.xj.gui.modes.ContentMode;
import io.xj.gui.modes.ViewMode;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

import java.util.Objects;
import java.util.UUID;

@Service
public class LibraryEditorController implements ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(LibraryEditorController.class);
  private final ProjectService projectService;
  private final UIStateService uiStateService;
  private final ObjectProperty<UUID> id = new SimpleObjectProperty<>(null);
  private final BooleanProperty dirty = new SimpleBooleanProperty(false);
  private final StringProperty name = new SimpleStringProperty("");

  @FXML
  protected VBox container;

  @FXML
  protected TextField fieldName;

  @FXML
  protected Button buttonOK;

  @FXML
  protected Button buttonCancel;

  public LibraryEditorController(
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    this.projectService = projectService;
    this.uiStateService = uiStateService;
  }

  @Override
  public void onStageReady() {
    var visible = projectService.isStateReadyProperty()
      .and(uiStateService.viewModeProperty().isEqualTo(ViewMode.Content))
      .and(uiStateService.contentModeProperty().isEqualTo(ContentMode.LibraryEditor));
    container.visibleProperty().bind(visible);
    container.managedProperty().bind(visible);

    fieldName.textProperty().bindBidirectional(name);

    name.addListener((o, ov, v) -> dirty.set(true));

    uiStateService.contentModeProperty().addListener((o, ov, v) -> {
      if (Objects.equals(uiStateService.contentModeProperty().get(), ContentMode.LibraryEditor))
        update();
    });

    buttonOK.disableProperty().bind(dirty.not());
  }

  @Override
  public void onStageClose() {
    // FUTURE: on stage close
  }

  @FXML
  protected void handlePressOK() {
    var library = projectService.getContent().getLibrary(id.get())
      .orElseThrow(() -> new RuntimeException("Could not find Library"));
    library.setName(name.get());
    if (projectService.updateLibrary(library))
      uiStateService.viewLibraries();
  }

  @FXML
  protected void handlePressCancel() {
    uiStateService.viewLibrary(id.get());
  }

  /**
   Update the Library Editor with the current Library.
   */
  private void update() {
    if (Objects.isNull(uiStateService.currentLibraryProperty().get()))
      return;
    var library = projectService.getContent().getLibrary(uiStateService.currentLibraryProperty().get().getId())
      .orElseThrow(() -> new RuntimeException("Could not find Library"));
    LOG.info("Will edit Library \"{}\"", library.getName());
    this.id.set(library.getId());
    this.name.set(library.getName());
    this.dirty.set(false);
  }

}
