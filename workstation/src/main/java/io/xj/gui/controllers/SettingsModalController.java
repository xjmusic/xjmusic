// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.engine.fabricator.ControlMode;
import io.xj.gui.ProjectModalController;
import io.xj.gui.services.FabricationService;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.types.AudioFileContainer;
import io.xj.gui.utils.ProjectUtils;
import io.xj.gui.utils.UiUtils;
import io.xj.model.util.LocalFileUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SettingsModalController extends ProjectModalController {
  private static final String WINDOW_NAME = "Settings";
  private final FabricationService fabricationService;

  @FXML
  VBox generalSettingsContainer;

  @FXML
  VBox fabricationSettingsContainer;

  @FXML
  VBox buildSettingsContainer;

  @FXML
  ChoiceBox<AudioFileContainer> choiceOutputContainer;

  @FXML
  ChoiceBox<String> choiceOutputSampleBits;

  @FXML
  ChoiceBox<ControlMode> choiceControlMode;

  @FXML
  ToggleGroup navToggleGroup;

  @FXML
  ToggleButton navGeneral;

  @FXML
  ToggleButton navFabrication;

  @FXML
  ToggleButton navBuild;

  @FXML
  TextField fieldCraftAheadSeconds;

  @FXML
  TextField fieldDubAheadSeconds;

  @FXML
  TextField fieldMixerLengthSeconds;

  @FXML
  TextField fieldOutputChannels;

  @FXML
  TextField fieldOutputFrameRate;

  @FXML
  TextField fieldTimelineSegmentViewLimit;

  @FXML
  Button buttonClose;

  @FXML
  Button buttonResetFabricationSettings;

  @FXML
  Button buttonResetBuildSettings;

  @FXML
  TextField fieldProjectsPathPrefix;

  @FXML
  TextField fieldExportPathPrefix;

  @FXML
  Button buttonSelectProjectsPathDirectory;

  @FXML
  Button buttonSelectExportPathDirectory;


  public SettingsModalController(
    @Value("classpath:/views/settings-modal.fxml") Resource fxml,
    ConfigurableApplicationContext ac,
    ThemeService themeService,
    FabricationService fabricationService,
    UIStateService uiStateService,
    ProjectService projectService) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.fabricationService = fabricationService;
  }

  @Override
  public void onStageReady() {
    fabricationSettingsContainer.visibleProperty().bind(navFabrication.selectedProperty());
    fabricationSettingsContainer.managedProperty().bind(navFabrication.selectedProperty());
    choiceControlMode.valueProperty().bindBidirectional(fabricationService.controlModeProperty());
    choiceControlMode.setItems(FXCollections.observableArrayList(ControlMode.values()));
    fieldCraftAheadSeconds.textProperty().bindBidirectional(fabricationService.craftAheadSecondsProperty());
    fieldDubAheadSeconds.textProperty().bindBidirectional(fabricationService.dubAheadSecondsProperty());
    fieldMixerLengthSeconds.textProperty().bindBidirectional(fabricationService.mixerLengthSecondsProperty());
    fieldTimelineSegmentViewLimit.textProperty().bindBidirectional(fabricationService.timelineSegmentViewLimitProperty());

    buildSettingsContainer.visibleProperty().bind(navBuild.selectedProperty());
    buildSettingsContainer.managedProperty().bind(navBuild.selectedProperty());
    fieldOutputFrameRate.textProperty().bindBidirectional(projectService.outputFrameRateProperty());
    fieldOutputChannels.textProperty().bindBidirectional(projectService.outputChannelsProperty());
    // For now, choice of container is restricted to WAV. In the future we will use other containers, for example
    // Workstation & Unreal Plugin can build & utilize OGG audio files https://github.com/xjmusic/xjmusic/issues/423
    choiceOutputContainer.setDisable(true);
    choiceOutputContainer.setItems(FXCollections.observableArrayList(AudioFileContainer.values()));
    choiceOutputContainer.valueProperty().bindBidirectional(projectService.outputContainerProperty());
    // For now, output is restricted to 16-bit audio.
    choiceOutputSampleBits.setDisable(true);
    choiceOutputSampleBits.setItems(FXCollections.observableArrayList("8","16","24","32"));
    choiceOutputSampleBits.valueProperty().bindBidirectional(projectService.outputSampleBitsProperty());


    generalSettingsContainer.visibleProperty().bind(navGeneral.selectedProperty());
    generalSettingsContainer.managedProperty().bind(navGeneral.selectedProperty());
    fieldProjectsPathPrefix.textProperty().bindBidirectional(projectService.projectsPathPrefixProperty());
    fieldExportPathPrefix.textProperty().bindBidirectional(projectService.exportPathPrefixProperty());

    UiUtils.toggleGroupPreventDeselect(navToggleGroup);
  }

  @Override
  public void onStageClose() {
    // no op
  }

  @FXML
  void handleClose() {
    Stage stage = (Stage) buttonClose.getScene().getWindow();
    stage.close();
    onStageClose();
  }

  @FXML
  void handleResetFabricationSettings() {
    fabricationService.resetSettingsToDefaults();
  }

  @FXML
  void handleResetBuildSettings() {
    projectService.resetBuildSettingsToDefaults();
  }

  @FXML
  void handleSelectProjectPathDirectory() {
    var path = ProjectUtils.chooseDirectory(
      buttonSelectProjectsPathDirectory.getScene().getWindow(), "Choose projects folder", fieldProjectsPathPrefix.getText()
    );
    if (Objects.nonNull(path)) {
      fieldProjectsPathPrefix.setText(LocalFileUtils.addTrailingSlash(path));
    }
  }

  @FXML
  void handleSelectExportPathDirectory() {
    var path = ProjectUtils.chooseDirectory(
      buttonSelectExportPathDirectory.getScene().getWindow(), "Choose export folder", fieldExportPathPrefix.getText()
    );
    if (Objects.nonNull(path)) {
      fieldExportPathPrefix.setText(LocalFileUtils.addTrailingSlash(path));
    }
  }

  @Override
  public void launchModal() {
    createAndShowModal(WINDOW_NAME, null);
  }

  /**
   Launches the settings modal with the fabrication settings tab selected.
   */
  public void launchModalWithFabricationSettings() {
    createAndShowModal(WINDOW_NAME, () -> navFabrication.setSelected(true));
  }

  /**
   Launches the settings modal with the build settings tab selected.
   */
  public void launchModalWithBuildSettings() {
    createAndShowModal(WINDOW_NAME, () -> navBuild.setSelected(true));
  }
}
