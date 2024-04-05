// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.template;

import io.xj.gui.ProjectModalController;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.ProjectUtils;
import io.xj.gui.utils.TextUtils;
import io.xj.hub.pojos.Template;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class TemplateExportModalController extends ProjectModalController {
  private final BooleanProperty isExporting = new SimpleBooleanProperty(false);
  private final FloatProperty exportProgress = new SimpleFloatProperty(0.0f);

  @FXML
  ProgressBar progressBar;

  @FXML
  Label labelProgress;

  @FXML
  StackPane progressContainer;

  @FXML
  VBox container;

  @FXML
  TextField fieldProjectName;

  @FXML
  TextField fieldPathPrefix;

  @FXML
  Button buttonSelectDirectory;

  @FXML
  Button buttonOK;

  @FXML
  Button buttonCancel;

  public TemplateExportModalController(
    @Value("classpath:/views/template/template-export-modal.fxml") Resource fxml,
    ConfigurableApplicationContext ac,
    UIStateService uiStateService,
    ProjectService projectService,
    ThemeService themeService
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
  }

  @Override
  public void onStageReady() {
    // Add slash to end of "file output projectFilePath prefix"
    // https://www.pivotaltracker.com/story/show/186555998
    fieldPathPrefix.textProperty().bindBidirectional(projectService.exportPathPrefixProperty());
    fieldPathPrefix.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
      if (!isNowFocused) {
        TextUtils.addTrailingSlash(fieldPathPrefix);
      }
    });
    buttonOK.disableProperty().bind(fieldProjectName.textProperty().isEmpty().or(isExporting));
    fieldProjectName.disableProperty().bind(isExporting);
    fieldPathPrefix.disableProperty().bind(isExporting);
    buttonSelectDirectory.disableProperty().bind(isExporting);
    progressContainer.visibleProperty().bind(isExporting);
    progressContainer.managedProperty().bind(isExporting);
    progressBar.progressProperty().bind(exportProgress);
  }

  @Override
  public void onStageClose() {
    // no op
  }

  @Override
  public void launchModal() {
    // no op
  }

  /**
   Open the modal to export a template

   @param template to export
   */
  public void launchModal(Template template) {
    createAndShowModal(String.format("Export %s", template.getName()));
  }

  @FXML
  void handlePressSelectDirectory() {
    var path = ProjectUtils.chooseDirectory(
      buttonSelectDirectory.getScene().getWindow(), "Choose destination folder", fieldPathPrefix.getText()
    );
    if (Objects.nonNull(path)) {
      fieldPathPrefix.setText(TextUtils.addTrailingSlash(path));
    }
  }

  @FXML
  void handlePressOK() {
    var projectName = fieldProjectName.getText().replaceAll("[^a-zA-Z0-9 ]", "");
    isExporting.set(true);

    // TODO export template to target location, show progress in this modal and then close it
    // TODO While exporting, disable the OK button
  }

  @FXML
  void handlePressCancel() {
    Stage stage = (Stage) buttonCancel.getScene().getWindow();
    stage.close();
    onStageClose();
    // TODO if cancel is pressed while exporting, cancel the export and close the modal window
  }
}
