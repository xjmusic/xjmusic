// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.template;

import io.xj.gui.controllers.BrowserController;
import io.xj.gui.controllers.ReadyAfterBootController;
import io.xj.gui.modes.TemplateMode;
import io.xj.gui.modes.ViewMode;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
import io.xj.hub.tables.pojos.Template;
import io.xj.nexus.project.ProjectUpdate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;

@Service
public class TemplateBrowserController extends BrowserController implements ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(TemplateBrowserController.class);
  private final ProjectService projectService;
  private final UIStateService uiStateService;
  private final ObservableList<Template> templates = FXCollections.observableList(new ArrayList<>());

  @FXML
  protected StackPane container;

  @FXML
  protected TableView<Template> table;

  public TemplateBrowserController(
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
      .and(uiStateService.templateModeProperty().isEqualTo(TemplateMode.TemplateBrowser));
    container.visibleProperty().bind(visible);
    container.managedProperty().bind(visible);

    addColumn(table, 200, "name", "Name");
    setupData(
      table,
      templates,
      template -> {
        if (Objects.nonNull(template))
          LOG.debug("Did select Template \"{}\"", template.getName());
      },
      template -> {
        if (Objects.nonNull(template))
          uiStateService.editTemplate(template.getId());
      }
    );
    projectService.addProjectUpdateListener(ProjectUpdate.Templates, this::updateTemplates);
  }

  @Override
  public void onStageClose() {
    // FUTURE: on stage close
  }

  /**
   Update the templates table data.
   */
  private void updateTemplates() {
    templates.setAll(projectService.getTemplates());
  }
}
