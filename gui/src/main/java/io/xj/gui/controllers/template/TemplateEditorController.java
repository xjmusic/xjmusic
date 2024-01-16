// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.template;

import io.xj.gui.controllers.ReadyAfterBootController;
import io.xj.gui.modes.TemplateMode;
import io.xj.gui.modes.ViewMode;
import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.Template;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TemplateEditorController implements ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(TemplateEditorController.class);
  private final ProjectService projectService;
  private final ObjectProperty<UUID> id = new SimpleObjectProperty<>(null);
  private final StringProperty name = new SimpleStringProperty("");

  @FXML
  protected VBox container;

  @FXML
  protected TextField fieldName;

  @FXML
  protected Button backButton;

  public TemplateEditorController(
    ProjectService projectService
  ) {
    this.projectService = projectService;
  }

  @Override
  public void onStageReady() {
    var visible = projectService.isStateReadyProperty()
      .and(projectService.viewModeProperty().isEqualTo(ViewMode.Template))
      .and(projectService.templateModeProperty().isEqualTo(TemplateMode.TemplateEditor));
    container.visibleProperty().bind(visible);
    container.managedProperty().bind(visible);

    backButton.textProperty().set("« Templates");

    fieldName.textProperty().bindBidirectional(name);
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

    projectService.templateModeProperty().set(TemplateMode.TemplateEditor);
  }

  @FXML
  protected void handleBackToTemplateBrowser() {
    projectService.templateModeProperty().set(TemplateMode.TemplateBrowser);
  }
}
