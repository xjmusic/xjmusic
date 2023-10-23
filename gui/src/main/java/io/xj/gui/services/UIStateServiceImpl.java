package io.xj.gui.services;

import io.xj.gui.WorkstationLogAppender;
import io.xj.nexus.OutputMode;
import jakarta.annotation.Nullable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

  private final BooleanProperty logsVisible = new SimpleBooleanProperty(false);

  private final BooleanProperty logsTailing = new SimpleBooleanProperty(true);

  @Nullable
  private BooleanBinding isFabricationActionDisabled;

  @Nullable
  private BooleanBinding isFabricationSettingsDisabled;

  @Nullable
  private StringBinding fabricationStatusText;

  @Nullable
  private DoubleBinding fabricationProgress;

  @Nullable
  private BooleanBinding isProgressBarVisible;

  @Nullable
  private BooleanBinding isFileOutputActive;

  @Nullable
  private BooleanBinding isInputModeDisabled;

  @Nullable
  private BooleanBinding isOutputFileModeDisabled;

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
  public BooleanProperty logsTailingProperty() {
    return logsTailing;
  }

  @Override
  public BooleanProperty logsVisibleProperty() {
    return logsVisible;
  }

  @Override
  public BooleanBinding isFabricationActionDisabledProperty() {
    if (Objects.isNull(isFabricationActionDisabled))
      isFabricationActionDisabled = Bindings.createBooleanBinding(
        () -> preloaderService.runningProperty().get() || fabricationService.statusProperty().get() == FabricationStatus.Starting,

        fabricationService.statusProperty(),
        preloaderService.runningProperty());

    return isFabricationActionDisabled;
  }

  @Override
  public BooleanBinding isFabricationSettingsDisabledProperty() {
    if (Objects.isNull(isFabricationSettingsDisabled))
      isFabricationSettingsDisabled = Bindings.createBooleanBinding(
        () -> fabricationService.isStatusActive().get() || preloaderService.runningProperty().get(),

        fabricationService.isStatusActive(),
        preloaderService.runningProperty());

    return isFabricationSettingsDisabled;
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
          } else if (isFileOutputActiveProperty().get()) {
            return fabricationService.progressProperty().get();
          } else {
            return 0.0;
          }
        },

        isFileOutputActiveProperty(),
        fabricationService.progressProperty(),
        preloaderService.runningProperty(),
        preloaderService.progressProperty());

    return fabricationProgress;
  }

  @Override
  public BooleanBinding isProgressBarVisibleProperty() {
    if (Objects.isNull(isProgressBarVisible))
      isProgressBarVisible = Bindings.createBooleanBinding(
        () -> isFileOutputActiveProperty().get() || preloaderService.runningProperty().get(),

        isFileOutputActiveProperty(),
        preloaderService.runningProperty());

    return isProgressBarVisible;
  }

  @Override
  public BooleanBinding isFileOutputActiveProperty() {
    if (Objects.isNull(isFileOutputActive))
      isFileOutputActive = Bindings.createBooleanBinding(
        () -> fabricationService.isStatusActive().get() && fabricationService.isOutputModeFile().get(),

        fabricationService.statusProperty(),
        fabricationService.isOutputModeFile());

    return isFileOutputActive;
  }

  @Override
  public StringProperty logLevelProperty() {
    return logLevel;
  }

  @Override
  public BooleanBinding isInputModeDisabledProperty() {
    if (Objects.isNull(isInputModeDisabled))
      isInputModeDisabled = labService.statusProperty().isEqualTo(LabStatus.Authenticated).not();

    return isInputModeDisabled;
  }

  @Override
  public BooleanBinding isOutputFileModeDisabledProperty() {
    if (Objects.isNull(isOutputFileModeDisabled))
      isOutputFileModeDisabled = fabricationService.outputModeProperty().isEqualTo(OutputMode.FILE).not();

    return isOutputFileModeDisabled;
  }
}
