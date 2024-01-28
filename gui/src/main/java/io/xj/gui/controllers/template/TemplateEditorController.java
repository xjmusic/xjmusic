// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.template;

import io.xj.gui.controllers.BrowserController;
import io.xj.gui.controllers.ReadyAfterBootController;
import io.xj.gui.modes.TemplateMode;
import io.xj.gui.modes.ViewMode;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
import io.xj.hub.TemplateConfig;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.nexus.project.ProjectUpdate;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
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
public class TemplateEditorController extends BrowserController implements ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(TemplateEditorController.class);
  private final ProjectService projectService;
  private final UIStateService uiStateService;
  private final TemplateAddBindingController templateAddBindingController;
  private final ObjectProperty<UUID> templateId = new SimpleObjectProperty<>(null);
  private final StringProperty name = new SimpleStringProperty("");
  private final StringProperty config = new SimpleStringProperty("");
  private final BooleanProperty dirty = new SimpleBooleanProperty(false);
  private final ObservableList<TemplateBinding> bindings = FXCollections.observableList(new ArrayList<>());

  @FXML
  protected SplitPane container;

  @FXML
  protected VBox fieldsContainer;

  @FXML
  protected TextField fieldName;

  @FXML
  protected TextArea fieldConfig;

  @FXML
  protected Button buttonSave;

  @FXML
  protected TableView<TemplateBinding> bindingsTable;

  public TemplateEditorController(
    ProjectService projectService,
    UIStateService uiStateService,
    TemplateAddBindingController templateAddBindingController
  ) {
    this.projectService = projectService;
    this.uiStateService = uiStateService;
    this.templateAddBindingController = templateAddBindingController;
  }

  @Override
  public void onStageReady() {
    var visible = projectService.isStateReadyProperty()
      .and(uiStateService.viewModeProperty().isEqualTo(ViewMode.Templates))
      .and(uiStateService.templateModeProperty().isEqualTo(TemplateMode.TemplateEditor));
    container.visibleProperty().bind(visible);
    container.managedProperty().bind(visible);

    fieldName.textProperty().bindBidirectional(name);
    fieldConfig.textProperty().bindBidirectional(config);
    fieldConfig.prefHeightProperty().bind(fieldsContainer.heightProperty().subtract(100));

    name.addListener((o, ov, v) -> dirty.set(true));
    config.addListener((o, ov, v) -> dirty.set(true));

    bindingsTable.setItems(bindings);

    TableColumn<TemplateBinding, String> typeColumn = new TableColumn<>("Type");
    typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
    typeColumn.setPrefWidth(100);
    bindingsTable.getColumns().add(typeColumn);

    TableColumn<TemplateBinding, String> nameColumn = new TableColumn<>("Name");
    nameColumn.setCellValueFactory(row -> getTargetName(row.getValue()));
    nameColumn.setPrefWidth(300);
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

    addActionsColumn(TemplateBinding.class, bindingsTable,
      null,
      null,
      null,
      binding -> {
        if (Objects.nonNull(binding))
          projectService.deleteTemplateBinding(binding);
      });

    projectService.addProjectUpdateListener(ProjectUpdate.TemplateBindings, this::updateBindings);

    uiStateService.templateModeProperty().addListener((o, ov, v) -> {
      if (Objects.equals(uiStateService.templateModeProperty().get(), TemplateMode.TemplateEditor))
        update();
    });

    buttonSave.disableProperty().bind(dirty.not());
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

  @FXML
  protected void handlePressSave() {
    var template = projectService.getContent().getTemplate(templateId.get())
      .orElseThrow(() -> new RuntimeException("Could not find Template"));
    template.setName(name.get());
    try {
      template.setConfig(new TemplateConfig(config.get()).toString());
    } catch (Exception e) {
      LOG.error("Could not parse Template config!", e);
      return;
    }
    if (projectService.updateTemplate(template))
      uiStateService.viewTemplates();
  }

  @FXML
  private void handlePressAddBinding(ActionEvent ignored) {
    templateAddBindingController.addBindingToTemplate(templateId.get());
  }

  /**
   Update the Template Editor with the current Template.
   */
  private void update() {
    if (uiStateService.currentTemplateProperty().isNull().get())
      return;
    var template = projectService.getContent().getTemplate(uiStateService.currentTemplateProperty().get().getId())
      .orElseThrow(() -> new RuntimeException("Could not find Template"));
    LOG.info("Will edit Template \"{}\"", template.getName());
    this.templateId.set(template.getId());
    this.name.set(template.getName());
    this.config.set(template.getConfig());
    this.dirty.set(false);
    updateBindings();
  }

  /**
   Update the libraries table data.
   */
  private void updateBindings() {
    if (uiStateService.currentTemplateProperty().isNull().get())
      return;
    bindings.setAll(projectService.getContent().getTemplateBindings().stream()
      .filter(binding -> Objects.equals(uiStateService.currentTemplateProperty().get().getId(), binding.getTemplateId()))
      .toList());
  }
}
