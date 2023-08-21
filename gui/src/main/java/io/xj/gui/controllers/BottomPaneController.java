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
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

@Service
public class BottomPaneController extends VBox implements ReadyAfterBootController {
  private final Log log;
  private final ObservableList<LogRecord> logItems = FXCollections.observableArrayList();
  private static final int MAX_ENTRIES = 10_000;
  private final static PseudoClass debug = PseudoClass.getPseudoClass("debug");
  private final static PseudoClass info = PseudoClass.getPseudoClass("info");
  private final static PseudoClass warn = PseudoClass.getPseudoClass("warn");
  private final static PseudoClass error = PseudoClass.getPseudoClass("error");
  private final BooleanProperty showTimestamp = new SimpleBooleanProperty(false);
  private final BooleanProperty tail = new SimpleBooleanProperty(false);
  private final DoubleProperty refreshRate = new SimpleDoubleProperty(1);

  public DoubleProperty refreshRateProperty() {
    return refreshRate;
  }

  @FXML
  protected ListView<BottomPaneController.LogRecord> logListView;

  public BottomPaneController(
  ) {
    log = new Log();

    // bind to the log appender
    WorkstationLogAppender.LISTENER.set(this::appendLogLine);
  }

  public void appendLogLine(Level level, String context, String line) {
    if (Objects.nonNull(line))
      try {
        Platform.runLater(() -> log.offer(new LogRecord(level, context, line)));
      } catch (Exception e) {
        // no op
      }
  }

  @Override
  public void onStageReady() {
    Timeline logTransfer = new Timeline(
      new KeyFrame(
        Duration.seconds(1),
        event -> {
          log.drainTo(logItems);

          if (logItems.size() > MAX_ENTRIES) {
            logItems.remove(0, logItems.size() - MAX_ENTRIES);
          }

          if (tail.get()) {
            logListView.scrollTo(logItems.size());
          }
        }
      )
    );
    logTransfer.setCycleCount(Timeline.INDEFINITE);
    logTransfer.rateProperty().bind(refreshRateProperty());

    logTransfer.play();

    logListView.setCellFactory(param -> new ListCell<>() {
      {
        showTimestamp.addListener(observable -> updateItem(this.getItem(), this.isEmpty()));
      }

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
  }

  static class Log {
    private static final int MAX_LOG_ENTRIES = 1_000_000;

    private final BlockingDeque<LogRecord> log = new LinkedBlockingDeque<>(MAX_LOG_ENTRIES);

    public void drainTo(Collection<? super LogRecord> collection) {
      log.drainTo(collection);
    }

    public void offer(LogRecord record) {
      log.offer(record);
    }
  }

  record LogRecord(Level level, String context, String message) {
  }
}
