// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.Template;
import io.xj.nexus.project.ProjectUpdate;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

@Service
public class ContentBrowserController implements ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(ContentBrowserController.class);
  private final ProjectService projectService;
  private final ContentEditorController contentEditorController;
  private final ObservableList<Library> libraries = FXCollections.observableList(new ArrayList<>());
  private final ObservableList<Program> programs = FXCollections.observableList(new ArrayList<>());
  private final ObservableList<Instrument> instruments = FXCollections.observableList(new ArrayList<>());
  private final ObservableList<Template> templates = FXCollections.observableList(new ArrayList<>());
  private final ObjectProperty<Library> viewedLibrary = new SimpleObjectProperty<>(null);

  @FXML
  protected TabPane container;

  @FXML
  protected TableView<Library> librariesTable;

  @FXML
  protected TableView<Program> programsTable;

  @FXML
  protected TableView<Instrument> instrumentsTable;

  @FXML
  protected TableView<Template> templatesTable;

  @FXML
  protected Tab templatesTab;

  @FXML
  protected Tab librariesTab;

  @FXML
  protected Tab programsTab;

  @FXML
  protected Tab instrumentsTab;

  public ContentBrowserController(
    ProjectService projectService,
    ContentEditorController contentEditorController
  ) {
    this.projectService = projectService;
    this.contentEditorController = contentEditorController;
  }

  @Override
  public void onStageReady() {
    initLibraries();
    initPrograms();
    initInstruments();
    initTemplates();

    viewedLibrary.addListener((o, ov, value) -> {
      updatePrograms();
      updateInstruments();
    });

    // FUTURE: after content creation/editing, we should not need to update the tables
    templatesTab.disableProperty().bind(Bindings.createBooleanBinding(templates::isEmpty, templates));
    librariesTab.disableProperty().bind(Bindings.createBooleanBinding(libraries::isEmpty, libraries));
    programsTab.disableProperty().bind(Bindings.createBooleanBinding(programs::isEmpty, programs));
    instrumentsTab.disableProperty().bind(Bindings.createBooleanBinding(instruments::isEmpty, instruments));
  }

  @Override
  public void onStageClose() {
    // FUTURE: on stage close
  }

  /**
   Initialize the templates table.
   */
  private void initTemplates() {
    addColumn(templatesTable, 200, "name", "Name");
    setupData(
      templatesTable,
      templates,
      template -> LOG.info("Did select Template"),
      contentEditorController::openTemplate
    );
    projectService.addProjectUpdateListener(ProjectUpdate.Templates, this::updateTemplates);
  }

  /**
   Update the templates table data.
   */
  private void updateTemplates() {
    templates.setAll(projectService.getTemplates());
  }

  /**
   Initialize the libraries table.
   */
  private void initLibraries() {
    addColumn(librariesTable, 200, "name", "Name");
    setupData(
      librariesTable,
      libraries,
      viewedLibrary::set,
      contentEditorController::openLibrary
    );
    projectService.addProjectUpdateListener(ProjectUpdate.Libraries, this::updateLibraries);
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
    setupData(
      programsTable,
      programs,
      program -> LOG.info("Did select Program"),
      contentEditorController::openProgram
    );
    projectService.addProjectUpdateListener(ProjectUpdate.Programs, this::updatePrograms);
  }

  /**
   Update the programs table data.
   */
  private void updatePrograms() {
    programs.setAll(projectService.getPrograms().stream()
      .filter(program -> Objects.isNull(viewedLibrary.get()) || Objects.equals(program.getLibraryId(), viewedLibrary.get().getId()))
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
    setupData(
      instrumentsTable,
      instruments,
      instrument -> LOG.info("Did select Instrument"),
      contentEditorController::openInstrument
    );
    projectService.addProjectUpdateListener(ProjectUpdate.Instruments, this::updateInstruments);
  }

  /**
   Update the instruments table data.
   */
  private void updateInstruments() {
    instruments.setAll(projectService.getInstruments().stream()
      .filter(instrument -> Objects.isNull(viewedLibrary.get()) || Objects.equals(instrument.getLibraryId(), viewedLibrary.get().getId()))
      .toList());
  }

  /**
   Add a column to a table

   @param <N>      type of table
   @param table    for which to add column
   @param property of column
   @param name     of column
   */
  private <N> void addColumn(TableView<N> table, int width, String property, String name) {
    TableColumn<N, String> nameColumn = new TableColumn<>(name);
    nameColumn.setCellValueFactory(new PropertyValueFactory<>(property));
    nameColumn.setPrefWidth(width);
    table.getColumns().add(nameColumn);
  }

  /**
   Setup the data for the libraries table.@param <N>   type of table

   @param table for which to setup data
   @param data  observable list
   */
  private <N> void setupData(TableView<N> table, ObservableList<N> data, Consumer<N> setSelectedItem, Consumer<N> openItem) {
    table.setItems(data);
    table.setOnMousePressed(
      event -> {
        if (event.isPrimaryButtonDown())
          switch (event.getClickCount()) {
            case 1 -> setSelectedItem.accept(table.getSelectionModel().getSelectedItem());
            case 2 -> openItem.accept(table.getSelectionModel().getSelectedItem());
          }
      });
  }
}
