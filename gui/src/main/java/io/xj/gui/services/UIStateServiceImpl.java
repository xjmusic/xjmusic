package io.xj.gui.services;

import io.xj.gui.WorkstationLogAppender;
import io.xj.nexus.OutputMode;
import jakarta.annotation.Nullable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UIStateServiceImpl implements UIStateService {
  private final FabricationService fabricationService;
  private final PreloaderService preloaderService;
  private final LabService labService;
  private final StringProperty logLevel = new SimpleStringProperty(WorkstationLogAppender.LEVEL.get().toString());

  @Nullable
  private BooleanBinding fabricationActionDisabled;

  @Nullable
  private BooleanBinding fabricationSettingsDisabled;

  @Nullable
  private StringBinding fabricationStatusText;

  @Nullable
  private DoubleBinding fabricationProgress;

  @Nullable
  private BooleanBinding fabricationProgressBarVisible;

  @Nullable
  private BooleanBinding fileOutputActive;

  @Nullable
  private BooleanBinding fabricationInputModeDisabled;

  @Nullable
  private BooleanBinding fabricationOutputFileModeDisabled;

  public UIStateServiceImpl(
    FabricationService fabricationService,
    PreloaderService preloaderService,
    LabService labService
  ) {
    this.fabricationService = fabricationService;
    this.preloaderService = preloaderService;
    this.labService = labService;
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
  public BooleanBinding fabricationActionDisabledProperty() {
    if (Objects.isNull(fabricationActionDisabled))
      fabricationActionDisabled = Bindings.createBooleanBinding(
        () -> preloaderService.runningProperty().get() || fabricationService.statusProperty().get() == FabricationStatus.Starting,

        fabricationService.statusProperty(),
        preloaderService.runningProperty());

    return fabricationActionDisabled;
  }

  @Override
  public BooleanBinding fabricationSettingsDisabledProperty() {
    if (Objects.isNull(fabricationSettingsDisabled))
      fabricationSettingsDisabled = Bindings.createBooleanBinding(
        () -> fabricationService.isStatusActive().get() || preloaderService.runningProperty().get(),

        fabricationService.isStatusActive(),
        preloaderService.runningProperty());

    return fabricationSettingsDisabled;
  }

  @Override
  public StringBinding fabricationStatusTextProperty() {
    if (Objects.isNull(fabricationStatusText))
      fabricationStatusText = Bindings.createStringBinding(
        () -> {
          if (preloaderService.runningProperty().get())
            return "Preloading";
          else
            return String.format("Fabrication %s", fabricationService.statusProperty().get().toString());
        },

        fabricationService.statusProperty(),
        preloaderService.runningProperty());

    return fabricationStatusText;
  }

  @Override
  public DoubleBinding fabricationProgressProperty() {
    if (Objects.isNull(fabricationProgress))
      fabricationProgress = Bindings.createDoubleBinding(
        () -> {
          if (preloaderService.runningProperty().get()) {
            return preloaderService.progressProperty().get();
          } else if (fileOutputActiveProperty().get()) {
            return fabricationService.progressProperty().get();
          } else {
            return 0.0;
          }
        },

        fileOutputActiveProperty(),
        fabricationService.progressProperty(),
        preloaderService.runningProperty(),
        preloaderService.progressProperty());

    return fabricationProgress;
  }

  @Override
  public BooleanBinding fabricationProgressBarVisibleProperty() {
    if (Objects.isNull(fabricationProgressBarVisible))
      fabricationProgressBarVisible = Bindings.createBooleanBinding(
        () -> fileOutputActiveProperty().get() || preloaderService.runningProperty().get(),

        fileOutputActiveProperty(),
        preloaderService.runningProperty());

    return fabricationProgressBarVisible;
  }

  @Override
  public BooleanBinding fileOutputActiveProperty() {
    if (Objects.isNull(fileOutputActive))
      fileOutputActive = Bindings.createBooleanBinding(
        () -> fabricationService.isStatusActive().get() && fabricationService.isOutputModeFile().get(),

        fabricationService.statusProperty(),
        fabricationService.isOutputModeFile());

    return fileOutputActive;
  }

  @Override
  public StringProperty logLevelProperty() {
    return logLevel;
  }

  @Override
  public BooleanBinding fabricationInputModeDisabledProperty() {
    if (Objects.isNull(fabricationInputModeDisabled))
      fabricationInputModeDisabled = labService.statusProperty().isEqualTo(LabStatus.Authenticated).not();

    return fabricationInputModeDisabled;
  }

  @Override
  public BooleanBinding fabricationOutputFileModeDisabledProperty() {
    if (Objects.isNull(fabricationOutputFileModeDisabled))
      fabricationOutputFileModeDisabled = fabricationService.outputModeProperty().isEqualTo(OutputMode.FILE).not();

    return fabricationOutputFileModeDisabled;
  }
}
