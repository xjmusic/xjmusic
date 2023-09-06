// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.events;

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
