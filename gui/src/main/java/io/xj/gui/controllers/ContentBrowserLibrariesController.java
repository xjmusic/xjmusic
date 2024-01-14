// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.Library;
import io.xj.nexus.project.ProjectUpdate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
public class ContentBrowserLibrariesController implements ReadyAfterBootController {
  private final ProjectService projectService;

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
    projectService.addProjectUpdateListener(ProjectUpdate.LIBRARIES, () -> {
      data.setAll(projectService.getContent().getLibraries().stream().sorted(Comparator.comparing(Library::getName)).toList());
    });
  }

  @Override
  public void onStageClose() {
    // FUTURE: close sub controllers
  }

}
