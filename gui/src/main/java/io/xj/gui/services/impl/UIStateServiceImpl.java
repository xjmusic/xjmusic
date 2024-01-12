package io.xj.gui.services.impl;

import io.xj.gui.WorkstationLogAppender;
import io.xj.gui.services.FabricationService;
import io.xj.gui.services.LabService;
import io.xj.gui.services.LabStatus;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
import io.xj.nexus.ControlMode;
import io.xj.nexus.project.ProjectState;
import io.xj.nexus.work.WorkState;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableStringValue;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UIStateServiceImpl implements UIStateService {
  private final BooleanBinding hasCurrentProject;
  private final BooleanBinding isInputModeDisabled;
  private final BooleanBinding isManualFabricationActive;
  private final BooleanBinding isManualFabricationMode;
  private final BooleanBinding isProgressBarVisible;
  private final BooleanProperty logsTailing = new SimpleBooleanProperty(true);
  private final BooleanProperty logsVisible = new SimpleBooleanProperty(false);
  private final ObservableBooleanValue isFabricationSettingsDisabled;
  private final ObservableBooleanValue isMainActionButtonDisabled;
  private final ObservableDoubleValue progress;
  private final StringBinding statusText;
  private final StringProperty logLevel = new SimpleStringProperty(WorkstationLogAppender.LEVEL.get().toString());

  public UIStateServiceImpl(
    FabricationService fabricationService,
    LabService labService,
    ProjectService projectService
  ) {

    // Has a current project?
    hasCurrentProject = projectService.stateProperty().isNotEqualTo(ProjectState.Standby);

    // Is the fabrication settings button disabled?
    isFabricationSettingsDisabled = fabricationService.isStateActiveProperty();

    // Is the input mode selection dropdown disabled?
    isInputModeDisabled = labService.statusProperty().isEqualTo(LabStatus.Authenticated).not();

    // Is the main action button disabled?
    isMainActionButtonDisabled = fabricationService.inputTemplateProperty().isNull();

    // Is the workstation in a manual fabrication mode?
    isManualFabricationMode = fabricationService.controlModeProperty().isNotEqualTo(ControlMode.AUTO);

    // Is manual fabrication active?
    isManualFabricationActive =
      fabricationService.controlModeProperty().isNotEqualTo(ControlMode.AUTO)
        .and(fabricationService.stateProperty().isEqualTo(WorkState.Active));

    // Is the progress bar visible?
    isProgressBarVisible = Bindings.createBooleanBinding(
      () ->
        projectService.isStateLoadingProperty().get() ||
          fabricationService.isStateLoadingProperty().get(),
      projectService.isStateLoadingProperty(),
      fabricationService.isStateLoadingProperty());

    // Progress
    progress = Bindings.createDoubleBinding(
      () ->
        projectService.isStateLoadingProperty().get() ?
          projectService.progressProperty().get() :
          fabricationService.isStateLoadingProperty().get() ?
            fabricationService.progressProperty().get() :
            0.0,
      projectService.isStateLoadingProperty(),
      projectService.progressProperty(),
      fabricationService.isStateLoadingProperty(),
      fabricationService.progressProperty());

    // Status Text
    statusText = Bindings.createStringBinding(
      () -> Objects.equals(projectService.stateProperty().get(), ProjectState.Ready)
        ? fabricationService.stateTextProperty().getValue() : projectService.stateTextProperty().getValue(),
      projectService.stateProperty(),
      projectService.stateTextProperty(),
      fabricationService.stateTextProperty());

  }

  @Override
  public void onStageReady() {
    logLevel.addListener((o, ov, value) -> WorkstationLogAppender.setLevel(value));
  }

  @Override
  public void onStageClose() {
    // no op
  }

  @Override
  public BooleanProperty logsTailingProperty() {
    return logsTailing;
  }

  @Override
  public BooleanProperty logsVisibleProperty() {
    return logsVisible;
  }

  @Override
  public ObservableBooleanValue isFabricationSettingsDisabledProperty() {
    return isFabricationSettingsDisabled;
  }

  @Override
  public ObservableStringValue statusTextProperty() {
    return statusText;
  }

  @Override
  public ObservableBooleanValue isProgressBarVisibleProperty() {
    return isProgressBarVisible;
  }

  @Override
  public ObservableDoubleValue progressProperty() {
    return progress;
  }

  @Override
  public StringProperty logLevelProperty() {
    return logLevel;
  }

  @Override
  public ObservableBooleanValue isInputModeDisabledProperty() {
    return isInputModeDisabled;
  }

  @Override
  public ObservableBooleanValue isManualFabricationModeProperty() {
    return isManualFabricationMode;
  }

  @Override
  public ObservableBooleanValue isManualFabricationActiveProperty() {
    return isManualFabricationActive;
  }

  @Override
  public ObservableBooleanValue hasCurrentProjectProperty() {
    return hasCurrentProject;
  }

  @Override
  public ObservableBooleanValue isMainActionButtonDisabledProperty() {
    return isMainActionButtonDisabled;
  }
}
