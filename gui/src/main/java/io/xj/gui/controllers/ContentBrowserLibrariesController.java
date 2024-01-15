// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.Library;
import io.xj.nexus.project.ProjectUpdate;
import io.xj.nexus.ship.broadcast.StreamPlayer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
public class ContentBrowserLibrariesController implements ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(ContentBrowserLibrariesController.class);
  private final ProjectService projectService;
  private final ObjectProperty<Library> selectedLibrary = new SimpleObjectProperty<>();

  @FXML
  protected TableView<Library> table;

  public ContentBrowserLibrariesController(
    ProjectService projectService
  ) {
    this.projectService = projectService;
  }

  @Override
  public void onStageReady() {
    // Library Name
    TableColumn<Library, String> nameColumn = new TableColumn<>("Name");
    nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
    nameColumn.setPrefWidth(300);
    table.getColumns().add(nameColumn);

    // Data and update listener
    ObservableList<Library> data = FXCollections.observableList(projectService.getLibraries());
    table.setItems(data);
    table.setOnMousePressed(event -> {
      if (event.isPrimaryButtonDown())
        selectedLibrary.set(table.getSelectionModel().getSelectedItem());
      if (event.getClickCount() == 2) {
        LOG.info("Will open Library in editor");
      }
    });
    projectService.addProjectUpdateListener(ProjectUpdate.LIBRARIES, () -> {
      data.setAll(projectService.getContent().getLibraries().stream().sorted(Comparator.comparing(Library::getName)).toList());
    });
  }

  @Override
  public void onStageClose() {
    // FUTURE: close sub controllers
  }

}
