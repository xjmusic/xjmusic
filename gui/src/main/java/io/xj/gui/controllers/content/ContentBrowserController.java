// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content;

import io.xj.gui.controllers.BrowserController;
import io.xj.gui.controllers.CmdModalController;
import io.xj.gui.types.Route;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.hub.pojos.Instrument;
import io.xj.hub.pojos.Library;
import io.xj.hub.pojos.Program;
import jakarta.annotation.Nullable;
import javafx.beans.binding.Bindings;
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
import java.util.Set;

@Service
public class ContentBrowserController extends BrowserController {
  static final Logger LOG = LoggerFactory.getLogger(ContentBrowserController.class);
  static final Set<Route> CONTENT_BROWSER_ROUTES = Set.of(
    Route.ContentLibraryBrowser,
    Route.ContentProgramBrowser,
    Route.ContentInstrumentBrowser
  );
  private final CmdModalController cmdModalController;
  private final ObservableList<Library> libraries = FXCollections.observableList(new ArrayList<>());
  private final ObservableList<Program> programs = FXCollections.observableList(new ArrayList<>());
  private final ObservableList<Instrument> instruments = FXCollections.observableList(new ArrayList<>());

  @FXML
  StackPane container;

  @FXML
  TableView<Library> librariesTable;

  @FXML
  TableView<Program> programsTable;

  @FXML
  TableView<Instrument> instrumentsTable;

  public ContentBrowserController(
    @Value("classpath:/views/content/content-browser.fxml") Resource fxml,
    ApplicationContext ac,
    ThemeService themeService,
    ProjectService projectService,
    UIStateService uiStateService,
    CmdModalController cmdModalController
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.cmdModalController = cmdModalController;
  }

  @Override
  public void onStageReady() {
    initLibraries();
    initPrograms();
    initInstruments();

    var isLibraryBrowser = uiStateService.navStateProperty().isEqualTo(Route.ContentLibraryBrowser);
    librariesTable.visibleProperty().bind(isLibraryBrowser);
    librariesTable.managedProperty().bind(isLibraryBrowser);

    var isProgramBrowser = uiStateService.navStateProperty().isEqualTo(Route.ContentProgramBrowser);
    programsTable.visibleProperty().bind(isProgramBrowser);
    programsTable.managedProperty().bind(isProgramBrowser);

    var isInstrumentBrowser = uiStateService.navStateProperty().isEqualTo(Route.ContentInstrumentBrowser);
    instrumentsTable.visibleProperty().bind(isInstrumentBrowser);
    instrumentsTable.managedProperty().bind(isInstrumentBrowser);

    var visible = Bindings.createBooleanBinding(
      () -> projectService.isStateReadyProperty().get() && (
        CONTENT_BROWSER_ROUTES.contains(uiStateService.navStateProperty().get())
      ),
      projectService.isStateReadyProperty(),
      uiStateService.navStateProperty()
    );

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
    addColumn(librariesTable, 300, "name", "Name");
    addActionsColumn(Library.class, librariesTable,
      library -> uiStateService.editLibrary(library.getId()),
      null,
      cmdModalController::duplicateLibrary,
      cmdModalController::deleteLibrary, null);
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
    addColumn(programsTable, 300, "name", "Name");
    addColumn(programsTable, 90, "type", "Type");
    addColumn(programsTable, 90, "state", "State");
    addColumn(programsTable, 50, "key", "Key");
    addColumn(programsTable, 50, "tempo", "Tempo");
    addActionsColumn(Program.class, programsTable,
      program -> uiStateService.editProgram(program.getId()),
      cmdModalController::moveProgram,
      cmdModalController::duplicateProgram,
      cmdModalController::deleteProgram, null);
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
    addColumn(instrumentsTable, 300, "name", "Name");
    addColumn(instrumentsTable, 90, "type", "Type");
    addColumn(instrumentsTable, 90, "mode", "Mode");
    addColumn(instrumentsTable, 90, "state", "State");
    addColumn(instrumentsTable, 50, "volume", "Volume");
    addActionsColumn(Instrument.class, instrumentsTable,
      instrument -> uiStateService.editInstrument(instrument.getId()),
      cmdModalController::moveInstrument,
      cmdModalController::duplicateInstrument,
      cmdModalController::deleteInstrument, null);
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
