// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.template;

import io.xj.gui.controllers.BrowserController;
import io.xj.gui.controllers.CmdModalController;
import io.xj.gui.types.Route;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.hub.tables.pojos.Template;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;

@Service
public class TemplateBrowserController extends BrowserController {
  static final Logger LOG = LoggerFactory.getLogger(TemplateBrowserController.class);
  private final CmdModalController cmdModalController;
  private final ObservableList<Template> templates = FXCollections.observableList(new ArrayList<>());

  @FXML
  StackPane container;

  @FXML
  TableView<Template> table;

  public TemplateBrowserController(
    @Value("classpath:/views/template/template-browser.fxml") Resource fxml,
    ApplicationContext ac,
    ThemeService themeService,
    ProjectService projectService,
    UIStateService uiStateService,
    CmdModalController cmdModalController
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.cmdModalController = cmdModalController;
  }

  @Override
  public void onStageReady() {
    var visible = Bindings.createBooleanBinding(
      () -> projectService.isStateReadyProperty().get()
        && uiStateService.navStateProperty().get() == Route.TemplateBrowser,
      projectService.isStateReadyProperty(),
      uiStateService.navStateProperty());
    container.visibleProperty().bind(visible);
    container.managedProperty().bind(visible);
    addColumn(table, 200, "name", "Name");
    addActionsColumn(Template.class, table,
      template -> uiStateService.editTemplate(template.getId()),
      null,
      cmdModalController::cloneTemplate,
      cmdModalController::deleteTemplate);
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
    projectService.addProjectUpdateListener(Template.class, this::updateTemplates);
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
