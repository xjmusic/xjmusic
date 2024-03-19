// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content;

import io.xj.gui.ProjectController;
import io.xj.gui.nav.Route;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.types.ViewContentMode;
import javafx.beans.binding.Bindings;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class LibraryEditorController extends ProjectController {
  static final Logger LOG = LoggerFactory.getLogger(LibraryEditorController.class);
  private final ObjectProperty<UUID> libraryId = new SimpleObjectProperty<>(null);
  private final BooleanProperty dirty = new SimpleBooleanProperty(false);
  private final StringProperty name = new SimpleStringProperty("");

  @FXML
  VBox container;

  @FXML
  TextField fieldName;

  @FXML
  Button buttonOK;

  @FXML
  Button buttonCancel;

  public LibraryEditorController(
    @Value("classpath:/views/content/library-editor.fxml") Resource fxml,
    ApplicationContext ac,
    ThemeService themeService,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
  }

  @Override
  public void onStageReady() {
    var visible = Bindings.createBooleanBinding(
      () -> projectService.isStateReadyProperty().get()
        && uiStateService.navStateProperty().get().route() == Route.ContentLibraryEditor,
      projectService.isStateReadyProperty(),
      uiStateService.navStateProperty());
    container.visibleProperty().bind(visible);
    container.managedProperty().bind(visible);

    fieldName.textProperty().bindBidirectional(name);

    name.addListener((o, ov, v) -> dirty.set(true));

    uiStateService.contentModeProperty().addListener((o, ov, v) -> {
      if (Objects.equals(uiStateService.contentModeProperty().get(), ViewContentMode.LibraryEditor))
        setup();
    });

    buttonOK.disableProperty().bind(dirty.not());
  }

  @Override
  public void onStageClose() {
    // FUTURE: on stage close
  }

  @FXML
  void handlePressOK() {
    var library = projectService.getContent().getLibrary(libraryId.get())
      .orElseThrow(() -> new RuntimeException("Could not find Library"));
    library.setName(name.get());
    if (projectService.updateLibrary(library))
      uiStateService.viewLibraries();
  }

  @FXML
  void handlePressCancel() {
    uiStateService.viewLibraries();
  }

  /**
   Update the Library Editor with the current Library.
   */
  private void setup() {
    if (Objects.isNull(uiStateService.currentLibraryProperty().get()))
      return;
    var library = projectService.getContent().getLibrary(uiStateService.currentLibraryProperty().get().getId())
      .orElseThrow(() -> new RuntimeException("Could not find Library"));
    LOG.info("Will edit Library \"{}\"", library.getName());
    this.libraryId.set(library.getId());
    this.name.set(library.getName());
    this.dirty.set(false);
  }

}
