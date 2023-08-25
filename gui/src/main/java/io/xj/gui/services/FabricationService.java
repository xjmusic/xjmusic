package io.xj.gui.services;

import io.xj.nexus.InputMode;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;
import io.xj.nexus.work.WorkConfiguration;
import io.xj.nexus.work.WorkFactory;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.Locale;

@org.springframework.stereotype.Service
public class FabricationService extends Service<Boolean> {
  final WorkFactory workFactory;

  final ObjectProperty<WorkConfiguration> configuration = new SimpleObjectProperty<>();

  public FabricationService(
    @Value("${input.template.key}") String defaultInputTemplateKey,
    @Value("${input.mode}") String defaultInputMode,
    @Value("${output.file.mode}") String defaultOutputFileMode,
    @Value("${output.mode}") String defaultOutputMode,
    @Value("${output.seconds}") Integer defaultOutputSeconds,
    WorkFactory workFactory
  ) {
    this.workFactory = workFactory;
    setConfiguration(new WorkConfiguration()
      .setInputMode(InputMode.valueOf(defaultInputMode.toUpperCase(Locale.ROOT)))
      .setInputTemplateKey(defaultInputTemplateKey)
      .setOutputFileMode(OutputFileMode.valueOf(defaultOutputFileMode.toUpperCase(Locale.ROOT)))
      .setOutputMode(OutputMode.valueOf(defaultOutputMode.toUpperCase(Locale.ROOT)))
      .setOutputPathPrefix(System.getProperty("user.home") + File.separator)
      .setOutputSeconds(defaultOutputSeconds));
  }

  public WorkConfiguration getConfiguration() {
    return configuration.get();
  }

  public void setConfiguration(WorkConfiguration value) {
    configuration.set(value);
  }

  protected Task<Boolean> createTask() {
    return new Task<>() {
      protected Boolean call() {
        return workFactory.start(configuration.get(), () -> {
          // no op; the WorkFactory start method blocks, then we rely on the JavaFX Service hooks
        });
      }
    };
  }
}
