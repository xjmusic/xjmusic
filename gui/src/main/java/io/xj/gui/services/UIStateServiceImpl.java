package io.xj.gui.services;

import jakarta.annotation.Nullable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringBinding;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UIStateServiceImpl implements UIStateService {
  private final FabricationService fabricationService;
  private final PreloaderService preloaderService;

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
  private BooleanBinding isFileOutputActive;

  public UIStateServiceImpl(
    FabricationService fabricationService,
    PreloaderService preloaderService
  ) {
    this.fabricationService = fabricationService;
    this.preloaderService = preloaderService;
  }

  @Override
  public BooleanBinding fabricationActionDisabledProperty() {
    if (Objects.isNull(fabricationActionDisabled))
      fabricationActionDisabled = Bindings.createBooleanBinding(
        () -> preloaderService.runningProperty().get() || fabricationService.statusProperty().get() != FabricationStatus.Starting,

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
  public BooleanBinding fabricationProgressBarVisibleProperty() {
    if (Objects.isNull(fabricationProgressBarVisible))
      fabricationProgressBarVisible = Bindings.createBooleanBinding(
        () -> isFileOutputActiveProperty().get() || preloaderService.runningProperty().get(),

        isFileOutputActiveProperty(),
        preloaderService.runningProperty());

    return fabricationProgressBarVisible;
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
}
