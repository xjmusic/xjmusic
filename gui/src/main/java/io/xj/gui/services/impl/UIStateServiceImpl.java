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
import jakarta.annotation.Nullable;
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
  private final FabricationService fabricationService;
  private final LabService labService;
  private final ProjectService projectService;
  private final StringProperty logLevel = new SimpleStringProperty(WorkstationLogAppender.LEVEL.get().toString());
  private final BooleanProperty logsVisible = new SimpleBooleanProperty(false);
  private final BooleanProperty logsTailing = new SimpleBooleanProperty(true);

  private final ObservableBooleanValue isFabricationSettingsDisabled;

  @Nullable
  private StringBinding statusText;

  @Nullable
  private BooleanBinding isProgressBarVisible;

  @Nullable
  private BooleanBinding isInputModeDisabled;

  @Nullable
  private BooleanBinding isManualFabricationMode;

  @Nullable
  private BooleanBinding isManualFabricationActive;

  @Nullable
  private BooleanBinding hasCurrentProject;

  @Nullable
  private ObservableDoubleValue progress;
  private final ObservableBooleanValue isMainActionButtonDisabled;

  public UIStateServiceImpl(
    FabricationService fabricationService,
    LabService labService,
    ProjectService projectService
  ) {
    this.fabricationService = fabricationService;
    this.labService = labService;
    this.projectService = projectService;
    isFabricationSettingsDisabled = fabricationService.isStateActiveProperty();
    isMainActionButtonDisabled = fabricationService.inputTemplateProperty().isNull();
  }

  @Override
  public void onStageReady() {
    logLevel.addListener((observable, prior, value) -> WorkstationLogAppender.setLevel(value));
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
    // TODO is it necessary for all these UI state properties to be constructed lazily? Let's try all of them during construction
    if (Objects.isNull(statusText))
      statusText = Bindings.createStringBinding(
        () -> Objects.equals(projectService.stateProperty().get(), ProjectState.Ready)
          ? fabricationService.stateTextProperty().getValue() : projectService.stateTextProperty().getValue(),
        projectService.stateProperty(),
        projectService.stateTextProperty(),
        fabricationService.stateTextProperty());

    return statusText;
  }

  @Override
  public ObservableBooleanValue isProgressBarVisibleProperty() {
    if (Objects.isNull(isProgressBarVisible))
      isProgressBarVisible = Bindings.createBooleanBinding(
        () ->
          projectService.isStateLoadingProperty().get() ||
            fabricationService.isStateLoadingProperty().get(),

        projectService.isStateLoadingProperty(),
        fabricationService.isStateLoadingProperty());

    return isProgressBarVisible;
  }

  @Override
  public ObservableDoubleValue progressProperty() {
    if (Objects.isNull(progress))
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

    return progress;
  }

  @Override
  public StringProperty logLevelProperty() {
    return logLevel;
  }

  @Override
  public ObservableBooleanValue isInputModeDisabledProperty() {
    if (Objects.isNull(isInputModeDisabled))
      isInputModeDisabled = labService.statusProperty().isEqualTo(LabStatus.Authenticated).not();

    return isInputModeDisabled;
  }

  @Override
  public ObservableBooleanValue isManualFabricationModeProperty() {
    if (Objects.isNull(isManualFabricationMode))
      isManualFabricationMode = fabricationService.controlModeProperty().isNotEqualTo(ControlMode.AUTO);

    return isManualFabricationMode;
  }

  @Override
  public ObservableBooleanValue isManualFabricationActiveProperty() {
    if (Objects.isNull(isManualFabricationActive))
      isManualFabricationActive =
        fabricationService.controlModeProperty().isNotEqualTo(ControlMode.AUTO)
          .and(fabricationService.stateProperty().isEqualTo(WorkState.Active));

    return isManualFabricationActive;
  }

  @Override
  public ObservableBooleanValue hasCurrentProjectProperty() {
    if (Objects.isNull(hasCurrentProject))
      hasCurrentProject = projectService.stateProperty().isNotEqualTo(ProjectState.Standby);

    return hasCurrentProject;
  }

  @Override
  public ObservableBooleanValue isMainActionButtonDisabledProperty() {
    return isMainActionButtonDisabled;
  }
}
