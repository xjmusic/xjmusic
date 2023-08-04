package io.xj.workstation.events;

import javafx.stage.Stage;
import org.springframework.context.ApplicationEvent;

public class StageReadyEvent extends ApplicationEvent {
  public Stage getStage() {
    return (Stage) getSource();
  }

  public StageReadyEvent(Stage source) {
    super(source);
  }
}
