// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.template;

import io.xj.gui.controllers.BrowserController;
import io.xj.gui.nav.Route;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.types.ViewTemplateMode;
import io.xj.hub.TemplateConfig;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.hub.util.StringUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

@Service
public class TemplateEditorController extends BrowserController {
  static final Logger LOG = LoggerFactory.getLogger(TemplateEditorController.class);
  private final TemplateAddBindingModalController templateAddBindingModalController;
  private final ObjectProperty<UUID> templateId = new SimpleObjectProperty<>(null);
  private final StringProperty name = new SimpleStringProperty("");
  private final StringProperty config = new SimpleStringProperty("");
  private final ObservableList<TemplateBinding> bindings = FXCollections.observableList(new ArrayList<>());

  @FXML
  SplitPane container;

  @FXML
  VBox fieldsContainer;

  @FXML
  TextField fieldName;

  @FXML
  TextArea fieldConfig;

  @FXML
  TableView<TemplateBinding> bindingsTable;

  public TemplateEditorController(
    @Value("classpath:/views/template/template-editor.fxml") Resource fxml,
    ApplicationContext ac,
    ThemeService themeService,
    ProjectService projectService,
    UIStateService uiStateService,
    TemplateAddBindingModalController templateAddBindingModalController
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.templateAddBindingModalController = templateAddBindingModalController;
  }

  @Override
  public void onStageReady() {
    var visible = Bindings.createBooleanBinding(
      () -> projectService.isStateReadyProperty().get()
        && uiStateService.navStateProperty().get().route() == Route.TemplateEditor,
      projectService.isStateReadyProperty(),
      uiStateService.navStateProperty());
    container.visibleProperty().bind(visible);
    container.managedProperty().bind(visible);

    fieldName.textProperty().bindBidirectional(name);
    fieldConfig.textProperty().bindBidirectional(config);
    fieldConfig.prefHeightProperty().bind(fieldsContainer.heightProperty().subtract(100));

    fieldName.focusedProperty().addListener((o, ov, v) -> {
      if (!v) {
        update("name", name.get());
      }
    });
    fieldConfig.focusedProperty().addListener((o, ov, v) -> {
      if (!v) {
        try {
          config.set(new TemplateConfig(config.get()).toString());
          update("config", config.get());
        } catch (Exception e) {
          LOG.error("Could not parse Template Config because {}", e.getMessage());
        }
      }
    });

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
          projectService.deleteContent(binding);
      });

    projectService.addProjectUpdateListener(TemplateBinding.class, this::updateBindings);

    uiStateService.templateModeProperty().addListener((o, ov, v) -> {
      if (Objects.equals(uiStateService.templateModeProperty().get(), ViewTemplateMode.TemplateEditor))
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

  @FXML
  private void handlePressAddBinding(ActionEvent ignored) {
    templateAddBindingModalController.addBindingToTemplate(templateId.get());
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
    updateBindings();
  }

  /**
   Update an attribute of the current template record with the given value

   @param attribute of template
   @param value     to set
   */
  private void update(String attribute, Object value) {
    if (Objects.nonNull(templateId.get())) {
      try {
        projectService.update(Template.class, templateId.get(), attribute, value);
      } catch (Exception e) {
        LOG.error("Could not update Template {}! {}\n{}", attribute, e, StringUtils.formatStackTrace(e));
      }
    }
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
