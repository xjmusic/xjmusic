// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.template;

import io.xj.gui.controllers.ReadyAfterBootController;
import io.xj.gui.controllers.content.InstrumentEditorController;
import io.xj.gui.controllers.content.LibraryEditorController;
import io.xj.gui.controllers.content.ProgramEditorController;
import io.xj.gui.modes.TemplateMode;
import io.xj.gui.modes.ViewMode;
import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplateBinding;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

@Service
public class TemplateEditorController implements ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(TemplateEditorController.class);
  private final ProjectService projectService;
  private final LibraryEditorController libraryEditorController;
  private final ProgramEditorController programEditorController;
  private final InstrumentEditorController instrumentEditorController;
  private final ObjectProperty<UUID> id = new SimpleObjectProperty<>(null);
  private final StringProperty name = new SimpleStringProperty("");
  private final ObservableList<TemplateBinding> bindings = FXCollections.observableList(new ArrayList<>());

  @FXML
  protected VBox container;

  @FXML
  protected TextField fieldName;

  @FXML
  protected TableView<TemplateBinding> bindingsTable;

  public TemplateEditorController(
    ProjectService projectService,
    LibraryEditorController libraryEditorController,
    ProgramEditorController programEditorController,
    InstrumentEditorController instrumentEditorController
  ) {
    this.projectService = projectService;
    this.libraryEditorController = libraryEditorController;
    this.programEditorController = programEditorController;
    this.instrumentEditorController = instrumentEditorController;
  }

  @Override
  public void onStageReady() {
    var visible = projectService.isStateReadyProperty()
      .and(projectService.viewModeProperty().isEqualTo(ViewMode.Templates))
      .and(projectService.templateModeProperty().isEqualTo(TemplateMode.TemplateEditor));
    container.visibleProperty().bind(visible);
    container.managedProperty().bind(visible);

    fieldName.textProperty().bindBidirectional(name);

    bindingsTable.setItems(bindings);

    TableColumn<TemplateBinding, String> typeColumn = new TableColumn<>("Type");
    typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
    typeColumn.setPrefWidth(200);
    bindingsTable.getColumns().add(typeColumn);

    TableColumn<TemplateBinding, String> nameColumn = new TableColumn<>("Name");
    nameColumn.setCellValueFactory(row -> getTargetName(row.getValue()));
    nameColumn.setPrefWidth(200);
    bindingsTable.getColumns().add(nameColumn);

    bindingsTable.setOnMousePressed(
      event -> {
        if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
          Platform.runLater(() -> {
            var binding = bindingsTable.getSelectionModel().getSelectedItem();
            switch (binding.getType()) {
              case Library -> libraryEditorController.editLibrary(binding.getTargetId());
              case Program -> programEditorController.editProgram(binding.getTargetId());
              case Instrument -> instrumentEditorController.editInstrument(binding.getTargetId());
            }
          });
        }
      });
  }

  /**
   Get Template Binding Target Name

   @param binding for which to get name
   @return template binding name
   */
  private ObservableValue<String> getTargetName(TemplateBinding binding) {
    return switch (binding.getType()) {
      case Library -> new ReadOnlyStringWrapper(projectService.getContent().getLibrary(binding.getTargetId())
        .orElseThrow(() -> new RuntimeException("Can't find Library for Template Binding"))
        .getName());
      case Program -> new ReadOnlyStringWrapper(projectService.getContent().getProgram(binding.getTargetId())
        .orElseThrow(() -> new RuntimeException("Can't find Program for Template Binding"))
        .getName());
      case Instrument -> new ReadOnlyStringWrapper(projectService.getContent().getInstrument(binding.getTargetId())
        .orElseThrow(() -> new RuntimeException("Can't find Instrument for Template Binding"))
        .getName());
    };
  }

  @Override
  public void onStageClose() {
    // FUTURE: on stage close
  }

  /**
   Open the given template in the content editor.

   @param ref template to open
   */
  public void editTemplate(Template ref) {
    var template = projectService.getContent().getTemplate(ref.getId())
      .orElseThrow(() -> new RuntimeException("Could not find Template"));
    LOG.info("Will open Template \"{}\"", template.getName());
    this.id.set(template.getId());
    this.name.set(template.getName());
    bindings.setAll(projectService.getContent().getTemplateBindings().stream()
      .filter(binding -> Objects.equals(ref.getId(), binding.getTemplateId()))
      .toList());

    projectService.templateModeProperty().set(TemplateMode.TemplateEditor);
  }
}
