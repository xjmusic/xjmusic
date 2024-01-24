// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.template;

import io.xj.gui.controllers.ReadyAfterBootController;
import io.xj.gui.modes.TemplateMode;
import io.xj.gui.modes.ViewMode;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
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
  private final UIStateService uiStateService;
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
    UIStateService uiStateService
  ) {
    this.projectService = projectService;
    this.uiStateService = uiStateService;
  }

  @Override
  public void onStageReady() {
    var visible = projectService.isStateReadyProperty()
      .and(uiStateService.viewModeProperty().isEqualTo(ViewMode.Templates))
      .and(uiStateService.templateModeProperty().isEqualTo(TemplateMode.TemplateEditor));
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
              case Library -> uiStateService.editLibrary(binding.getTargetId());
              case Program -> uiStateService.editProgram(binding.getTargetId());
              case Instrument -> uiStateService.editInstrument(binding.getTargetId());
            }
          });
        }
      });

    uiStateService.templateModeProperty().addListener((o, ov, v) -> {
      if (Objects.equals(uiStateService.templateModeProperty().get(), TemplateMode.TemplateEditor))
        update();
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
   Update the Template Editor with the current Template.
   */
  private void update() {
    if (Objects.isNull(uiStateService.currentTemplateProperty().get()))
      return;
    var template = projectService.getContent().getTemplate(uiStateService.currentTemplateProperty().get().getId())
      .orElseThrow(() -> new RuntimeException("Could not find Template"));
    LOG.info("Will edit Template \"{}\"", template.getName());
    this.id.set(template.getId());
    this.name.set(template.getName());
    bindings.setAll(projectService.getContent().getTemplateBindings().stream()
      .filter(binding -> Objects.equals(template.getId(), binding.getTemplateId()))
      .toList());
  }
}
