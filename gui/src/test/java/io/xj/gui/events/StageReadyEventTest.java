package io.xj.gui.events;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class StageReadyEventTest {
  @Test
  public void getStage() {
    Stage stage = new Stage();
    StageReadyEvent stageReadyEvent = new StageReadyEvent(stage);
    assertSame(stage, stageReadyEvent.getStage());
  }
}
