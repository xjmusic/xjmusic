// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content;

import io.xj.gui.controllers.BrowserController;
import io.xj.gui.controllers.CmdModalController;
import io.xj.gui.modes.ContentMode;
import io.xj.gui.modes.ViewMode;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import jakarta.annotation.Nullable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;

@Service
public class ContentBrowserController extends BrowserController {
  static final Logger LOG = LoggerFactory.getLogger(ContentBrowserController.class);
  private final ProjectService projectService;
  private final UIStateService uiStateService;
  private final CmdModalController cmdModalController;
  private final ObservableList<Library> libraries = FXCollections.observableList(new ArrayList<>());
  private final ObservableList<Program> programs = FXCollections.observableList(new ArrayList<>());
  private final ObservableList<Instrument> instruments = FXCollections.observableList(new ArrayList<>());

  @FXML
  protected StackPane container;

  @FXML
  protected TableView<Library> librariesTable;

  @FXML
  protected TableView<Program> programsTable;

  @FXML
  protected TableView<Instrument> instrumentsTable;

  public ContentBrowserController(
    @Value("classpath:/views/content/content-browser.fxml") Resource fxml,
    ApplicationContext ac,
    ThemeService themeService,
    ProjectService projectService,
    UIStateService uiStateService,
    CmdModalController cmdModalController
  ) {
    super(fxml, ac, themeService);
    this.projectService = projectService;
    this.uiStateService = uiStateService;
    this.cmdModalController = cmdModalController;
  }

  @Override
  public void onStageReady() {
    initLibraries();
    initPrograms();
    initInstruments();

    var isLibraryBrowser = uiStateService.contentModeProperty().isEqualTo(ContentMode.LibraryBrowser);
    librariesTable.visibleProperty().bind(isLibraryBrowser);
    librariesTable.managedProperty().bind(isLibraryBrowser);

    var isProgramBrowser = uiStateService.contentModeProperty().isEqualTo(ContentMode.ProgramBrowser);
    programsTable.visibleProperty().bind(isProgramBrowser);
    programsTable.managedProperty().bind(isProgramBrowser);

    var isInstrumentBrowser = uiStateService.contentModeProperty().isEqualTo(ContentMode.InstrumentBrowser);
    instrumentsTable.visibleProperty().bind(isInstrumentBrowser);
    instrumentsTable.managedProperty().bind(isInstrumentBrowser);

    var visible = projectService.isStateReadyProperty()
      .and(uiStateService.viewModeProperty().isEqualTo(ViewMode.Content))
      .and(
        uiStateService.contentModeProperty().isEqualTo(ContentMode.LibraryBrowser)
          .or(uiStateService.contentModeProperty().isEqualTo(ContentMode.ProgramBrowser))
          .or(uiStateService.contentModeProperty().isEqualTo(ContentMode.InstrumentBrowser)));
    container.visibleProperty().bind(visible);
    container.managedProperty().bind(visible);

    uiStateService.currentLibraryProperty().addListener((o, ov, value) -> {
      updatePrograms(value);
      updateInstruments(value);
    });
  }

  @Override
  public void onStageClose() {
    // FUTURE: on stage close
  }

  /**
   Initialize the libraries table.
   */
  private void initLibraries() {
    addColumn(librariesTable, 200, "name", "Name");
    addActionsColumn(Library.class, librariesTable,
      library -> uiStateService.editLibrary(library.getId()),
      null,
      cmdModalController::cloneLibrary,
      cmdModalController::deleteLibrary);
    setupData(
      librariesTable,
      libraries,
      library -> {
        if (Objects.nonNull(library))
          LOG.debug("Did select Library \"{}\"", library.getName());
      },
      library -> {
        if (Objects.nonNull(library))
          uiStateService.viewLibrary(library.getId());
      }
    );
    projectService.addProjectUpdateListener(Library.class, this::updateLibraries);
  }

  /**
   Update the libraries table data.
   */
  private void updateLibraries() {
    libraries.setAll(projectService.getLibraries());
  }

  /**
   Initialize the programs table.
   */
  private void initPrograms() {
    addColumn(programsTable, 200, "name", "Name");
    addColumn(programsTable, 90, "type", "Type");
    addColumn(programsTable, 50, "key", "Key");
    addColumn(programsTable, 50, "tempo", "Tempo");
    addColumn(programsTable, 50, "density", "Density");
    addActionsColumn(Program.class, programsTable,
      program -> uiStateService.editProgram(program.getId()),
      cmdModalController::moveProgram,
      cmdModalController::cloneProgram,
      cmdModalController::deleteProgram);
    setupData(
      programsTable,
      programs,
      program -> {
        if (Objects.nonNull(program))
          LOG.debug("Did select Program \"{}\"", program.getName());
      },
      program -> {
        if (Objects.nonNull(program))
          uiStateService.editProgram(program.getId());
      }
    );
    projectService.addProjectUpdateListener(Program.class, () -> updatePrograms(uiStateService.currentLibraryProperty().get()));
  }

  /**
   Update the programs table data.
   */
  private void updatePrograms(@Nullable Library library) {
    programs.setAll(projectService.getPrograms().stream()
      .filter(program -> Objects.isNull(library) || Objects.equals(program.getLibraryId(), library.getId()))
      .toList());
  }

  /**
   Initialize the instruments table.
   */
  private void initInstruments() {
    addColumn(instrumentsTable, 200, "name", "Name");
    addColumn(instrumentsTable, 90, "type", "Type");
    addColumn(instrumentsTable, 90, "mode", "Mode");
    addColumn(instrumentsTable, 50, "density", "Density");
    addColumn(instrumentsTable, 50, "volume", "Volume");
    addActionsColumn(Instrument.class, instrumentsTable,
      instrument -> uiStateService.editInstrument(instrument.getId()),
      cmdModalController::moveInstrument,
      cmdModalController::cloneInstrument,
      cmdModalController::deleteInstrument);
    setupData(
      instrumentsTable,
      instruments,
      instrument -> {
        if (Objects.nonNull(instrument))
          LOG.debug("Did select Instrument \"{}\"", instrument.getName());
      },
      instrument -> {
        if (Objects.nonNull(instrument))
          uiStateService.editInstrument(instrument.getId());
      }
    );
    projectService.addProjectUpdateListener(Instrument.class,
      () -> updateInstruments(uiStateService.currentLibraryProperty().get()));
  }

  /**
   Update the instruments table data.
   */
  private void updateInstruments(@Nullable Library library) {
    instruments.setAll(projectService.getInstruments().stream()
      .filter(instrument -> Objects.isNull(library) || Objects.equals(instrument.getLibraryId(), library.getId()))
      .toList());
  }
}
