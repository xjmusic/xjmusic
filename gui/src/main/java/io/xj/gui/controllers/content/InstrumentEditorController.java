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
public class InstrumentEditorController implements ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(InstrumentEditorController.class);
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

  public InstrumentEditorController(
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
      .and(uiStateService.contentModeProperty().isEqualTo(ContentMode.InstrumentEditor));
    container.visibleProperty().bind(visible);
    container.managedProperty().bind(visible);

    fieldName.textProperty().bindBidirectional(name);

    name.addListener((o, ov, v) -> dirty.set(true));

    uiStateService.contentModeProperty().addListener((o, ov, v) -> {
      if (Objects.equals(uiStateService.contentModeProperty().get(), ContentMode.InstrumentEditor))
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
    var instrument = projectService.getContent().getInstrument(id.get())
      .orElseThrow(() -> new RuntimeException("Could not find Instrument"));
    instrument.setName(name.get());
    if (projectService.updateInstrument(instrument))
      uiStateService.viewLibrary(instrument.getLibraryId());
  }

  @FXML
  protected void handlePressCancel() {
    uiStateService.viewLibrary(id.get());
  }

  /**
   Update the Instrument Editor with the current Instrument.
   */
  private void update() {
    if (Objects.isNull(uiStateService.currentInstrumentProperty().get()))
      return;
    var instrument = projectService.getContent().getInstrument(uiStateService.currentInstrumentProperty().get().getId())
      .orElseThrow(() -> new RuntimeException("Could not find Instrument"));
    LOG.info("Will edit Instrument \"{}\"", instrument.getName());
    this.id.set(instrument.getId());
    this.name.set(instrument.getName());
    this.dirty.set(false);
  }
}
