// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import ch.qos.logback.classic.Level;
import io.xj.gui.WorkstationLogAppender;
import io.xj.gui.services.LabService;
import jakarta.annotation.Nullable;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

@Service
public class MainPaneBottomController extends VBox implements ReadyAfterBootController {
  final BooleanProperty logsTailing = new SimpleBooleanProperty(true);
  final BooleanProperty logsVisible = new SimpleBooleanProperty(false);
  final Integer refreshRateSeconds;
  final LabService labService;
  final LogQueue logQueue;
  final ObservableList<LogRecord> logItems = FXCollections.observableArrayList();
  final static PseudoClass debug = PseudoClass.getPseudoClass("debug");
  final static PseudoClass error = PseudoClass.getPseudoClass("error");
  final static PseudoClass info = PseudoClass.getPseudoClass("info");
  final static PseudoClass warn = PseudoClass.getPseudoClass("warn");
  static final int LOG_LIST_VIEW_HEIGHT = 368;
  static final int MAX_ENTRIES = 10_000;
  final ModalLabConnectionController modalLabConnectionController;

  @Nullable
  Timeline refresh;

  @FXML
  public Button buttonLab;

  @FXML
  public Label labelLabStatus;

  @FXML
  public ToggleButton toggleShowLogs;

  @FXML
  public ToggleButton toggleTailLogs;

  @FXML
  protected ListView<MainPaneBottomController.LogRecord> logListView;


  public MainPaneBottomController(
    @Value("${gui.logs.refresh.seconds}") Integer refreshRateSeconds,
    LabService labService,
    ModalLabConnectionController modalLabConnectionController
  ) {
    this.refreshRateSeconds = refreshRateSeconds;
    this.labService = labService;
    this.modalLabConnectionController = modalLabConnectionController;
    logQueue = new LogQueue();

    // bind to the log appender
    WorkstationLogAppender.LISTENER.set(this::appendLogLine);
  }

  @Override
  public void onStageReady() {
    labelLabStatus.textProperty().bind(labService.statusProperty().map(Enum::toString));

    refresh = new Timeline(
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
    refresh.setCycleCount(Timeline.INDEFINITE);
    refresh.setRate(refreshRateSeconds);
    refresh.play();

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

    toggleTailLogs.selectedProperty().bindBidirectional(logsTailing);
    toggleShowLogs.selectedProperty().bindBidirectional(logsVisible);
    logListView.visibleProperty().bind(logsVisible);
    toggleTailLogs.visibleProperty().bind(logsVisible);
    logListView.minHeightProperty().bind(logsVisible.map((v) -> v ? LOG_LIST_VIEW_HEIGHT : 0));
    logListView.prefHeightProperty().bind(logsVisible.map((v) -> v ? LOG_LIST_VIEW_HEIGHT : 0));
    logListView.maxHeightProperty().bind(logsVisible.map((v) -> v ? LOG_LIST_VIEW_HEIGHT : 0));
  }

  @Override
  public void onStageClose() {
    if (Objects.nonNull(refresh)) {
      refresh.stop();
    }
  }

  public void appendLogLine(Level level, String context, String line) {
    if (Objects.nonNull(line))
      try {
        Platform.runLater(() -> logQueue.offer(new LogRecord(level, context, line)));
      } catch (Exception e) {
        // no op
      }
  }

  @FXML
  public void handleButtonLabPressed(ActionEvent ignored) {
    modalLabConnectionController.launchModal();
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

  record LogRecord(Level level, String context, String message) {
  }
}
