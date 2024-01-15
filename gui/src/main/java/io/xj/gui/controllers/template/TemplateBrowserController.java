// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.template;

import io.xj.gui.controllers.BrowserController;
import io.xj.gui.controllers.ReadyAfterBootController;
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
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;

@Service
public class TemplateBrowserController extends BrowserController implements ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(TemplateBrowserController.class);
  private final ProjectService projectService;
  private final TemplateEditorController templateEditorController;
  private final ObservableList<Template> templates = FXCollections.observableList(new ArrayList<>());

  @FXML
  protected TableView<Template> table;

  public TemplateBrowserController(
    ProjectService projectService,
    TemplateEditorController templateEditorController
  ) {
    this.projectService = projectService;
    this.templateEditorController = templateEditorController;
  }

  @Override
  public void onStageReady() {
    addColumn(table, 200, "name", "Name");
    setupData(
      table,
      templates,
      template -> LOG.debug("Did select Template"),
      templateEditorController::openTemplate
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
