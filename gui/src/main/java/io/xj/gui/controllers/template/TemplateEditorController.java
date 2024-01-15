// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.template;

import io.xj.gui.controllers.ReadyAfterBootController;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ProjectViewTemplateMode;
import io.xj.hub.tables.pojos.Template;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
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
  protected TextField fieldName;

  public TemplateEditorController(
    ProjectService projectService
  ) {
    this.projectService = projectService;
  }

  @Override
  public void onStageReady() {
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
  public void openTemplate(Template ref) {
    var template = projectService.getContent().getTemplate(ref.getId())
      .orElseThrow(() -> new RuntimeException("Could not find Template"));
    LOG.info("Will open Template \"{}\"", template.getName());
    this.id.set(template.getId());
    this.name.set(template.getName());

    projectService.viewTemplateModeProperty().set(ProjectViewTemplateMode.TemplateEditor);
  }
}
