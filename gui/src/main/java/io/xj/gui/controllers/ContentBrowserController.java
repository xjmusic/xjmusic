// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.Template;
import io.xj.nexus.project.ProjectUpdate;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
public class ContentBrowserController implements ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(ContentBrowserController.class);
  private final ProjectService projectService;
  private final ObjectProperty<Library> selectedLibrary = new SimpleObjectProperty<>();

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

  public ContentBrowserController(
    ProjectService projectService
  ) {
    this.projectService = projectService;
  }

  @Override
  public void onStageReady() {
    initLibrariesTable();
    initProgramsTable();
    initInstrumentsTable();
    initTemplatesTable();

    container.visibleProperty().bind(projectService.isStateReadyProperty());
    container.managedProperty().bind(projectService.isStateReadyProperty());
  }

  @Override
  public void onStageClose() {
    // FUTURE: on stage close
  }

  /**
   Initialize the libraries table.
   */
  private void initLibrariesTable() {
    // Library Name
    TableColumn<Library, String> nameColumn = new TableColumn<>("Name");
    nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    nameColumn.setPrefWidth(300);
    librariesTable.getColumns().add(nameColumn);

    // Data and update listener
    ObservableList<Library> data = FXCollections.observableList(projectService.getLibraries());
    librariesTable.setItems(data);
    librariesTable.setOnMousePressed(event -> {
      if (event.isPrimaryButtonDown())
        selectedLibrary.set(librariesTable.getSelectionModel().getSelectedItem());
      if (event.getClickCount() == 2) {
        LOG.info("Will open Library in editor");
      }
    });
    projectService.addProjectUpdateListener(ProjectUpdate.LIBRARIES, () -> {
      data.setAll(projectService.getContent().getLibraries().stream().sorted(Comparator.comparing(Library::getName)).toList());
    });
  }

  /**
   Initialize the programs table.
   */
  private void initProgramsTable() {
    // Program Name
    TableColumn<Program, String> nameColumn = new TableColumn<>("Name");
    nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    nameColumn.setPrefWidth(300);
    programsTable.getColumns().add(nameColumn);

    // Data and update listener
    ObservableList<Program> data = FXCollections.observableList(projectService.getPrograms());
    programsTable.setItems(data);
    programsTable.setOnMousePressed(event -> {
      if (event.getClickCount() == 2) {
        LOG.info("Will open Program in editor");
      }
    });
    projectService.addProjectUpdateListener(ProjectUpdate.PROGRAMS, () -> {
      data.setAll(projectService.getContent().getPrograms().stream().sorted(Comparator.comparing(Program::getName)).toList());
    });
  }

  /**
   Initialize the instruments table.
   */
  private void initInstrumentsTable() {
    // Instrument Name
    TableColumn<Instrument, String> nameColumn = new TableColumn<>("Name");
    nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    nameColumn.setPrefWidth(300);
    instrumentsTable.getColumns().add(nameColumn);

    // Data and update listener
    ObservableList<Instrument> data = FXCollections.observableList(projectService.getInstruments());
    instrumentsTable.setItems(data);
    instrumentsTable.setOnMousePressed(event -> {
      if (event.getClickCount() == 2) {
        LOG.info("Will open Instrument in editor");
      }
    });
    projectService.addProjectUpdateListener(ProjectUpdate.INSTRUMENTS, () -> {
      data.setAll(projectService.getContent().getInstruments().stream().sorted(Comparator.comparing(Instrument::getName)).toList());
    });
  }

  /**
   Initialize the templates table.
   */
  private void initTemplatesTable() {
    // Template Name
    TableColumn<Template, String> nameColumn = new TableColumn<>("Name");
    nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    nameColumn.setPrefWidth(300);
    templatesTable.getColumns().add(nameColumn);

    // Data and update listener
    ObservableList<Template> data = FXCollections.observableList(projectService.getTemplates());
    templatesTable.setItems(data);
    templatesTable.setOnMousePressed(event -> {
      if (event.getClickCount() == 2) {
        LOG.info("Will open Template in editor");
      }
    });
    projectService.addProjectUpdateListener(ProjectUpdate.TEMPLATES, () -> {
      data.setAll(projectService.getContent().getTemplates().stream().sorted(Comparator.comparing(Template::getName)).toList());
    });
  }

}
