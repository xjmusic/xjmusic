// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content;

import io.xj.gui.controllers.ReadyAfterBootController;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ProjectViewContentMode;
import io.xj.hub.tables.pojos.Instrument;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InstrumentEditorController implements ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(InstrumentEditorController.class);
  private final ProjectService projectService;
  private final ObjectProperty<UUID> id = new SimpleObjectProperty<>(null);
  private final StringProperty name = new SimpleStringProperty("");

  @FXML
  protected TextField fieldName;

  public InstrumentEditorController(
    ProjectService projectService
  ) {
    this.projectService = projectService;
  }

  @Override
  public void onStageReady() {
    fieldName.textProperty().bindBidirectional(name);
  }

  @Override
  public void onStageClose() {
    // FUTURE: on stage close
  }

  /**
   Open the given instrument in the content editor.

   @param ref instrument to open
   */
  public void openInstrument(Instrument ref) {
    var instrument = projectService.getContent().getInstrument(ref.getId())
      .orElseThrow(() -> new RuntimeException("Could not find Instrument"));
    LOG.info("Will open Instrument \"{}\"", instrument.getName());
    this.id.set(instrument.getId());
    this.name.set(instrument.getName());

    projectService.viewContentModeProperty().set(ProjectViewContentMode.InstrumentEditor);
  }
}
