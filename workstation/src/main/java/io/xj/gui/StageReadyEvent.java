// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui;

import jakarta.annotation.Nullable;
import javafx.stage.Stage;
import org.springframework.context.ApplicationEvent;

import java.util.Optional;

public class StageReadyEvent extends ApplicationEvent {
  public Optional<String> getOpenProjectFilePath() {
    return Optional.ofNullable(openProjectFilePath);
  }

  @Nullable
  private final String openProjectFilePath;

  public Stage getStage() {
    return (Stage) getSource();
  }

  public StageReadyEvent(Stage source, @Nullable String openProjectFilePath) {
    super(source);
    this.openProjectFilePath = openProjectFilePath;
  }
}
