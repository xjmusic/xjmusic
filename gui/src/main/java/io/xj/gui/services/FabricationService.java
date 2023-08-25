package io.xj.gui.services;

import io.xj.nexus.InputMode;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;
import io.xj.nexus.work.WorkConfiguration;
import io.xj.nexus.work.WorkFactory;
import javafx.beans.property.*;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.Locale;

@org.springframework.stereotype.Service
public class FabricationService extends Service<Boolean> {
  final WorkFactory workFactory;

  final ObjectProperty<FabricationStatus> status = new SimpleObjectProperty<>(FabricationStatus.Standby);
  final StringProperty inputTemplateKey = new SimpleStringProperty();
  final StringProperty outputPathPrefix = new SimpleStringProperty();
  final ObjectProperty<InputMode> inputMode = new SimpleObjectProperty<>();
  final ObjectProperty<OutputFileMode> outputFileMode = new SimpleObjectProperty<>();
  final ObjectProperty<OutputMode> outputMode = new SimpleObjectProperty<>();
  final StringProperty outputSeconds = new SimpleStringProperty();

  public FabricationService(
    @Value("${input.mode}") String defaultInputMode,
    @Value("${input.template.key}") String defaultInputTemplateKey,
    @Value("${output.file.mode}") String defaultOutputFileMode,
    @Value("${output.mode}") String defaultOutputMode,
    @Value("${output.seconds}") Integer defaultOutputSeconds,
    WorkFactory workFactory
  ) {
    this.workFactory = workFactory;
    inputMode.set(InputMode.valueOf(defaultInputMode.toUpperCase(Locale.ROOT)));
    inputTemplateKey.set(defaultInputTemplateKey);
    outputFileMode.set(OutputFileMode.valueOf(defaultOutputFileMode.toUpperCase(Locale.ROOT)));
    outputMode.set(OutputMode.valueOf(defaultOutputMode.toUpperCase(Locale.ROOT)));
    outputPathPrefix.set(System.getProperty("user.home") + File.separator);
    outputSeconds.set(Integer.toString(defaultOutputSeconds));
    setOnCancelled((WorkerStateEvent ignored) -> status.set(FabricationStatus.Cancelled));
    setOnFailed((WorkerStateEvent ignored) -> status.set(FabricationStatus.Failed));
    setOnReady((WorkerStateEvent ignored) -> status.set(FabricationStatus.Standby));
    setOnRunning((WorkerStateEvent ignored) -> status.set(FabricationStatus.Active));
    setOnScheduled((WorkerStateEvent ignored) -> status.set(FabricationStatus.Starting));
    setOnSucceeded((WorkerStateEvent ignored) -> status.set(FabricationStatus.Done));
  }

  protected Task<Boolean> createTask() {
    return new Task<>() {
      protected Boolean call() {
        var configuration = new WorkConfiguration()
          .setInputMode(inputMode.get())
          .setInputTemplateKey(inputTemplateKey.get())
          .setOutputFileMode(outputFileMode.get())
          .setOutputMode(outputMode.get())
          .setOutputPathPrefix(outputPathPrefix.get())
          .setOutputSeconds(Integer.parseInt(outputSeconds.get()));
        return workFactory.start(configuration, () -> {
          // no op; the WorkFactory start method blocks, then we rely on the JavaFX Service hooks
        });
      }
    };
  }

  public ObjectProperty<FabricationStatus> statusProperty() {
    return status;
  }

  public StringProperty inputTemplateKeyProperty() {
    return inputTemplateKey;
  }

  public StringProperty outputPathPrefixProperty() {
    return outputPathPrefix;
  }

  public ObjectProperty<InputMode> inputModeProperty() {
    return inputMode;
  }

  public ObjectProperty<OutputFileMode> outputFileModeProperty() {
    return outputFileMode;
  }

  public ObjectProperty<OutputMode> outputModeProperty() {
    return outputMode;
  }

  public StringProperty outputSecondsProperty() {
    return outputSeconds;
  }


}
