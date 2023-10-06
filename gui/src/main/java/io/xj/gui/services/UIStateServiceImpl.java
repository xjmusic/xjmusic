package io.xj.gui.services;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import org.springframework.stereotype.Service;

@Service
public class UIStateServiceImpl implements UIStateService {
  private final FabricationService fabricationService;
  private final PreloaderService preloaderService;

  public UIStateServiceImpl(
    FabricationService fabricationService,
    PreloaderService preloaderService
  ) {
    this.fabricationService = fabricationService;
    this.preloaderService = preloaderService;
  }

  @Override
  public BooleanBinding fabricationSettingsDisabledProperty() {
    // todo cache the value of all these bindings such that we only create one of each, but don't create it until the first time that the method is called
    return Bindings.createBooleanBinding(
      () -> fabricationService.isStatusActive().get() || preloaderService.runningProperty().get(),
      fabricationService.isStatusActive(), preloaderService.runningProperty());
  }

  @Override
  public BooleanBinding fabricationActionDisabledProperty() {
    return Bindings.createBooleanBinding(
      () -> preloaderService.runningProperty().get() || fabricationService.statusProperty().get() != FabricationStatus.Starting,
      fabricationService.statusProperty(), preloaderService.runningProperty());
  }

  @Override
  public StringBinding fabricationStatusTextProperty() {
    return Bindings.createStringBinding(
      () -> {
        if (preloaderService.runningProperty().get())
          return "Preloading";
        else
          return String.format("Fabrication %s", fabricationService.statusProperty().get().toString());
      },
      fabricationService.statusProperty(), preloaderService.runningProperty());
  }

  @Override
  public BooleanBinding isFileOutputActiveProperty() {
    return Bindings.createBooleanBinding(
      () -> fabricationService.isStatusActive().get() && fabricationService.isOutputModeFile().get(),
      fabricationService.statusProperty(), fabricationService.isOutputModeFile());
  }
}
