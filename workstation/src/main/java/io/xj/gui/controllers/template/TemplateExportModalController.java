// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.template;

import io.xj.gui.ProjectModalController;
import io.xj.gui.services.CompilationService;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.ProjectUtils;
import io.xj.gui.utils.UiUtils;
import io.xj.model.pojos.Template;
import io.xj.model.util.LocalFileUtils;
import io.xj.model.util.StringUtils;
import jakarta.annotation.Nullable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static io.xj.engine.mixer.FixedSampleBits.FIXED_SAMPLE_BITS;

@Service
public class TemplateExportModalController extends ProjectModalController {
  private final ObjectProperty<Template> template = new SimpleObjectProperty<>();

  private final ObservableList<String> audioFormatOptions = FXCollections.observableList(List.of(
    "Original Source",
    "Converted for Mixing"
  ));
  private final CompilationService compilationService;

  @FXML
  ComboBox<String> selectAudioFormat;

  @FXML
  VBox container;

  @FXML
  TextField templateExportName;

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
    ThemeService themeService,
    CompilationService compilationService
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.compilationService = compilationService;
  }

  @Override
  public void onStageReady() {
    // Add slash to end of file output projectFilePath prefix
    fieldPathPrefix.textProperty().bindBidirectional(projectService.exportPathPrefixProperty());
    fieldPathPrefix.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
      if (!isNowFocused) {
        UiUtils.addTrailingSlash(fieldPathPrefix);
      }
    });

    // Template Export name is lower-scored
    if (!StringUtils.isNullOrEmpty(template.get().getShipKey()))
      templateExportName.textProperty().set(StringUtils.toLowerScored(template.get().getShipKey()));
    else
      templateExportName.textProperty().set(StringUtils.toLowerScored(template.get().getName()));
    templateExportName.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
      if (!isNowFocused) {
        templateExportName.setText(StringUtils.toLowerScored(templateExportName.getText()));
      }
    });
    buttonOK.disableProperty().bind(templateExportName.textProperty().isEmpty());

    // Audio output format selection
    selectAudioFormat.getItems().setAll(audioFormatOptions);
    selectAudioFormat.getSelectionModel().select(0);
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
    this.template.set(template);
    createAndShowModal(String.format("Export %s", template.getName()), null);
  }

  @FXML
  void handlePressSelectDirectory() {
    var path = ProjectUtils.chooseDirectory(
      buttonSelectDirectory.getScene().getWindow(), "Choose destination folder", fieldPathPrefix.getText()
    );
    if (Objects.nonNull(path)) {
      fieldPathPrefix.setText(LocalFileUtils.addTrailingSlash(path));
    }
  }

  @FXML
  void handlePressOK() {
    var projectName = StringUtils.toLowerScored(templateExportName.getText());
    Boolean conversion = Objects.equals(selectAudioFormat.getValue(), audioFormatOptions.get(1));
    @Nullable Integer conversionFrameRate = conversion ? Integer.valueOf(compilationService.outputFrameRateProperty().getValue()) : null;
    @Nullable Integer conversionSampleBits = conversion ? FIXED_SAMPLE_BITS : null;
    @Nullable Integer conversionChannel = conversion ? Integer.valueOf(compilationService.outputChannelsProperty().getValue()) : null;
    projectService.exportTemplate(
      template.get(),
      fieldPathPrefix.getText(),
      projectName,
      conversion,
      conversionFrameRate,
      conversionSampleBits,
      conversionChannel
    );
    Stage stage = (Stage) buttonCancel.getScene().getWindow();
    stage.close();
    onStageClose();
  }

  @FXML
  void handlePressCancel() {
    Stage stage = (Stage) buttonCancel.getScene().getWindow();
    stage.close();
    onStageClose();
  }
}
