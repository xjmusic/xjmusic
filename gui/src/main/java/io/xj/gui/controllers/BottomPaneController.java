package io.xj.gui.controllers;

import ch.qos.logback.classic.Level;
import io.xj.gui.WorkstationLogAppender;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

@Service
public class BottomPaneController extends VBox implements ReadyAfterBootController {
  private final LogQueue logQueue;
  private final ObservableList<LogRecord> logItems = FXCollections.observableArrayList();
  private static final int MAX_ENTRIES = 10_000;
  private static final int LOG_LIST_VIEW_HEIGHT = 368;
  private final static PseudoClass debug = PseudoClass.getPseudoClass("debug");
  private final static PseudoClass info = PseudoClass.getPseudoClass("info");
  private final static PseudoClass warn = PseudoClass.getPseudoClass("warn");
  private final static PseudoClass error = PseudoClass.getPseudoClass("error");
  private final BooleanProperty logsTailing = new SimpleBooleanProperty(true);
  private final BooleanProperty logsVisible = new SimpleBooleanProperty(false);
  private final DoubleProperty refreshRate = new SimpleDoubleProperty(1);

  @FXML
  public Label labelStatus;

  @FXML
  public ToggleButton toggleShowLogs;

  @FXML
  public ToggleButton toggleTailLogs;

  @FXML
  protected ListView<BottomPaneController.LogRecord> logListView;

  @Override
  public void onStageReady() {
    Timeline logTransfer = new Timeline(
      new KeyFrame(
        Duration.seconds(1),
        event -> {
          logQueue.drainTo(logItems);

          if (logItems.size() > MAX_ENTRIES) {
            logItems.remove(0, logItems.size() - MAX_ENTRIES);
          }

          if (logsTailing.get()) {
            logListView.scrollTo(logItems.size());
          }
        }
      )
    );
    logTransfer.setCycleCount(Timeline.INDEFINITE);
    logTransfer.rateProperty().bind(refreshRateProperty());
    logTransfer.play();

    logListView.setCellFactory(param -> new ListCell<>() {
      @Override
      protected void updateItem(LogRecord item, boolean empty) {
        super.updateItem(item, empty);

        pseudoClassStateChanged(debug, false);
        pseudoClassStateChanged(info, false);
        pseudoClassStateChanged(warn, false);
        pseudoClassStateChanged(error, false);

        if (item == null || empty) {
          setText(null);
          return;
        }

        setText((Objects.nonNull(item.context()) ? item.context() : "") + " " + item.message());

        switch (item.level().toInt()) {
          case Level.DEBUG_INT, Level.TRACE_INT, Level.ALL_INT -> pseudoClassStateChanged(debug, true);
          case Level.INFO_INT -> pseudoClassStateChanged(info, true);
          case Level.WARN_INT -> pseudoClassStateChanged(warn, true);
          case Level.ERROR_INT -> pseudoClassStateChanged(error, true);
        }
      }
    });
    logListView.setItems(logItems);

    toggleTailLogs.setSelected(logsTailing.get());
    toggleShowLogs.setSelected(logsVisible.get());
    updateLogsVisibility();
  }

  @FXML
  public void toggleShowLogs(ActionEvent ignored) {
    logsVisible.set(toggleShowLogs.isSelected());
    updateLogsVisibility();
  }

  @FXML
  public void toggleTailLogs(ActionEvent ignored) {
    logsTailing.set(toggleTailLogs.isSelected());
  }

  public DoubleProperty refreshRateProperty() {
    return refreshRate;
  }

  public BottomPaneController(
  ) {
    logQueue = new LogQueue();

    // bind to the log appender
    WorkstationLogAppender.LISTENER.set(this::appendLogLine);
  }

  public void appendLogLine(Level level, String context, String line) {
    if (Objects.nonNull(line))
      try {
        Platform.runLater(() -> logQueue.offer(new LogRecord(level, context, line)));
      } catch (Exception e) {
        // no op
      }
  }

  public void setStatusText(String status) {
    labelStatus.setText(status);
  }

  private void updateLogsVisibility() {
    if (logsVisible.get()) {
      logListView.setVisible(true);
      toggleTailLogs.setDisable(false);
      logListView.setMinHeight(LOG_LIST_VIEW_HEIGHT);
      logListView.setPrefHeight(LOG_LIST_VIEW_HEIGHT);
      logListView.setMaxHeight(LOG_LIST_VIEW_HEIGHT);
    } else {
      toggleTailLogs.setDisable(true);
      logListView.setVisible(false);
      logListView.setMinHeight(0);
      logListView.setPrefHeight(0);
      logListView.setMaxHeight(0);
    }
  }
  static class LogQueue {

    private static final int MAX_LOG_ENTRIES = 1_000_000;

    private final BlockingDeque<LogRecord> queue = new LinkedBlockingDeque<>(MAX_LOG_ENTRIES);

    public void drainTo(Collection<? super LogRecord> collection) {
      queue.drainTo(collection);
    }

    public void offer(LogRecord record) {
      queue.offer(record);
    }
  }

  record LogRecord(Level level, String context, String message) {
  }
}
