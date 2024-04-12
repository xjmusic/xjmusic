// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import ch.qos.logback.classic.Level;
import io.xj.gui.ProjectController;
import io.xj.gui.WorkstationLogAppender;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import jakarta.annotation.Nullable;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

@Service
public class MainPaneBottomController extends ProjectController {
  private static final String INDENT_SUB_LINES = "  ";
  private static final int LOG_LIST_ROW_HEIGHT = 20;
  private static final int MAX_ENTRIES = 10_000;
  private final Integer refreshRateSeconds;
  private final LogQueue logQueue;
  private final ObservableList<LogRecord> logItems = FXCollections.observableArrayList();

  @Nullable
  Timeline refresh;

  @FXML
  ListView<MainPaneBottomController.LogRecord> logListView;

  public MainPaneBottomController(
    @Value("classpath:/views/main-pane-bottom.fxml") Resource fxml,
    @Value("${view.logsRefreshSeconds}") Integer refreshRateSeconds,
    ApplicationContext ac,
    ThemeService themeService,
    UIStateService uiStateService,
    ProjectService projectService
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.refreshRateSeconds = refreshRateSeconds;
    logQueue = new LogQueue();

    // bind to the log appender
    WorkstationLogAppender.setListener(this::appendLogLine);
  }

  @Override
  public void onStageReady() {
    refresh = new Timeline(
      new KeyFrame(
        Duration.seconds(1),
        event -> {
          logQueue.drainTo(logItems);

          if (logItems.size() > MAX_ENTRIES) {
            logItems.remove(0, logItems.size() - MAX_ENTRIES);
          }

          if (uiStateService.logsTailingProperty().get()) {
            logListView.scrollTo(logItems.size());
          }
        }
      )
    );
    refresh.setCycleCount(Timeline.INDEFINITE);
    refresh.setRate(refreshRateSeconds);
    refresh.play();

    logListView.setCellFactory(param -> new ListCell<>() {
      @Override
      protected void updateItem(LogRecord item, boolean empty) {
        super.updateItem(item, empty);

        if (item==null || empty) {
          setText(null);
          return;
        }

        setText((Objects.nonNull(item.context()) ? item.context():"") + " " + item.message());

        getStyleClass().removeAll("debug", "info", "warn", "error");
        getStyleClass().add(item.level().toString().toLowerCase());
      }
    });
    logListView.setItems(logItems);
    logListView.setFixedCellSize(LOG_LIST_ROW_HEIGHT);

    logListView.visibleProperty().bind(uiStateService.logsVisibleProperty());
    logListView.managedProperty().bind(uiStateService.logsVisibleProperty());
  }

  @Override
  public void onStageClose() {
    if (Objects.nonNull(refresh)) {
      refresh.stop();
    }
  }

  public void appendLogLine(Level level, String context, String message) {
    if (Objects.nonNull(message))
      try {
        String[] lines = message.split("\n");
        logQueue.offer(new LogRecord(level, context, lines[0]));
        if (lines.length > 1) {
          for (int i = 1; i < lines.length; i++) {
            logQueue.offer(new LogRecord(level, INDENT_SUB_LINES, lines[i]));
          }
        }
      } catch (Exception e) {
        // no op
      }

    if (level.equals(Level.ERROR)) Platform.runLater(() -> projectService.showErrorDialog("Error", context, message));
  }

  static class LogQueue {

    static final int MAX_LOG_ENTRIES = 1_000_000;

    final BlockingDeque<LogRecord> queue = new LinkedBlockingDeque<>(MAX_LOG_ENTRIES);

    public void drainTo(Collection<? super LogRecord> collection) {
      queue.drainTo(collection);
    }

    public void offer(LogRecord record) {
      queue.offer(record);
    }
  }

  public record LogRecord(Level level, String context, String message) {
  }
}
