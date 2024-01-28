// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.fabrication;

import io.xj.gui.controllers.ReadyAfterBootModalController;
import io.xj.gui.services.FabricationService;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.hub.tables.pojos.Template;
import io.xj.nexus.ControlMode;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class FabricationSettingsModalController extends ReadyAfterBootModalController {
  static final String FABRICATION_SERVICE_WINDOW_NAME = "Fabrication Settings";
  private final FabricationService fabricationService;
  private final ProjectService projectService;

  @FXML
  ChoiceBox<ControlMode> choiceControlMode;

  @FXML
  ChoiceBox<TemplateChoice> choiceTemplate;

  @FXML
  Label labelControlMode;

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
  public Button buttonClose;

  @FXML
  public Button buttonReset;

  public FabricationSettingsModalController(
    @Value("classpath:/views/fabrication/fabrication-settings-modal.fxml") Resource fxml,
    ConfigurableApplicationContext ac,
    ThemeService themeService,
    FabricationService fabricationService,
    ProjectService projectService
  ) {
    super(fxml, ac, themeService);
    this.fabricationService = fabricationService;
    this.projectService = projectService;
  }

  @Override
  public void onStageReady() {
    choiceControlMode.valueProperty().bindBidirectional(fabricationService.controlModeProperty());
    choiceControlMode.setItems(FXCollections.observableArrayList(ControlMode.values()));

    choiceTemplate.setItems(FXCollections.observableArrayList(projectService.getContent().getTemplates().stream().map(TemplateChoice::new).toList()));
    choiceTemplate.setOnAction(event -> {
      TemplateChoice choice = choiceTemplate.getValue();
      if (Objects.nonNull(choice)) {
        fabricationService.inputTemplateProperty().set(choice.template());
      }
    });
    choiceTemplate.setValue(new TemplateChoice(fabricationService.inputTemplateProperty().get()));

    fieldCraftAheadSeconds.textProperty().bindBidirectional(fabricationService.craftAheadSecondsProperty());
    fieldDubAheadSeconds.textProperty().bindBidirectional(fabricationService.dubAheadSecondsProperty());
    fieldMixerLengthSeconds.textProperty().bindBidirectional(fabricationService.mixerLengthSecondsProperty());
    fieldOutputFrameRate.textProperty().bindBidirectional(fabricationService.outputFrameRateProperty());
    fieldOutputChannels.textProperty().bindBidirectional(fabricationService.outputChannelsProperty());

    fieldTimelineSegmentViewLimit.textProperty().bindBidirectional(fabricationService.timelineSegmentViewLimitProperty());
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
  void handleReset() {
    fabricationService.resetSettingsToDefaults();
  }

  @Override
  public void launchModal() {
    createAndShowModal(FABRICATION_SERVICE_WINDOW_NAME);
  }

  /**
   This class is used to display the template name in the ChoiceBox while preserving the underlying ID
   */
  public record TemplateChoice(Template template) {
    @Override
    public String toString() {
      return Objects.nonNull(template) ? template.getName() : "Select...";
    }
  }
}
