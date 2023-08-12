package io.xj.gui.services;

import io.xj.nexus.work.WorkConfiguration;
import io.xj.nexus.work.WorkFactory;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

@org.springframework.stereotype.Service
public class FabricationService extends Service<Boolean> {
  private final WorkFactory workFactory;
  private final ObjectProperty<WorkConfiguration> configuration = new SimpleObjectProperty<>();

  public FabricationService(WorkFactory workFactory) {
    this.workFactory = workFactory;
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
