// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content;

import io.xj.gui.controllers.BrowserController;
import io.xj.gui.controllers.ReadyAfterBootController;
import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.Instrument;
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
public class InstrumentBrowserController extends BrowserController implements ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(InstrumentBrowserController.class);
  private final ProjectService projectService;
  private final InstrumentEditorController instrumentEditorController;
  private final ObservableList<Instrument> instruments = FXCollections.observableList(new ArrayList<>());

  @FXML
  protected TableView<Instrument> table;

  public InstrumentBrowserController(
    ProjectService projectService,
    InstrumentEditorController instrumentEditorController
  ) {
    this.projectService = projectService;
    this.instrumentEditorController = instrumentEditorController;
  }

  @Override
  public void onStageReady() {
    addColumn(table, 200, "name", "Name");
    addColumn(table, 90, "type", "Type");
    addColumn(table, 90, "mode", "Mode");
    addColumn(table, 50, "density", "Density");
    addColumn(table, 50, "volume", "Volume");
    setupData(
      table,
      instruments,
      instrument -> LOG.info("Did select Instrument"),
      instrumentEditorController::openInstrument
    );
    projectService.addProjectUpdateListener(ProjectUpdate.Instruments, this::updateInstruments);
  }

  @Override
  public void onStageClose() {
    // FUTURE: on stage close
  }

  /**
   Update the instruments table data.
   */
  private void updateInstruments() {
    instruments.setAll(projectService.getInstruments());
  }
}
