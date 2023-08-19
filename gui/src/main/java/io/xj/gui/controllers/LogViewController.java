package io.xj.gui.controllers;

import ch.qos.logback.classic.Level;
import io.xj.gui.WorkstationLogAppender;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

@Service
public class LogViewController extends ListView<LogViewController.LogRecord> {
  private final Log log;
  Logger LOG = LoggerFactory.getLogger(LogViewController.class);

  private final ObservableList<LogRecord> logItems = FXCollections.observableArrayList();

  private final Node logView;

  private static final int MAX_ENTRIES = 10_000;
  private final static PseudoClass debug = PseudoClass.getPseudoClass("debug");
  private final static PseudoClass info = PseudoClass.getPseudoClass("info");
  private final static PseudoClass warn = PseudoClass.getPseudoClass("warn");
  private final static PseudoClass error = PseudoClass.getPseudoClass("error");
  private final BooleanProperty showTimestamp = new SimpleBooleanProperty(false);
  private final ObjectProperty<Level> filterLevel = new SimpleObjectProperty<>(null);
  private final BooleanProperty tail = new SimpleBooleanProperty(false);
  private final BooleanProperty paused = new SimpleBooleanProperty(false);
  private final DoubleProperty refreshRate = new SimpleDoubleProperty(60);

  public BooleanProperty pausedProperty() {
    return paused;
  }

  public DoubleProperty refreshRateProperty() {
    return refreshRate;
  }


  public LogViewController(
  ) {
    logView = new VBox();
    VBox.setVgrow(logView, Priority.ALWAYS);
    log = new Log();

    // bind to the log appender
    WorkstationLogAppender.LISTENER.set(this::appendLogLine);

    logView.getStyleClass().add("log-view");

    Timeline logTransfer = new Timeline(
      new KeyFrame(
        Duration.seconds(1),
        event -> {
          log.drainTo(logItems);

          if (logItems.size() > MAX_ENTRIES) {
            logItems.remove(0, logItems.size() - MAX_ENTRIES);
          }

          if (tail.get()) {
            scrollTo(logItems.size());
          }
        }
      )
    );
    logTransfer.setCycleCount(Timeline.INDEFINITE);
    logTransfer.rateProperty().bind(refreshRateProperty());

    this.pausedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue && logTransfer.getStatus() == Animation.Status.RUNNING) {
        logTransfer.pause();
      }

      if (!newValue && logTransfer.getStatus() == Animation.Status.PAUSED && getParent() != null) {
        logTransfer.play();
      }
    });

    this.parentProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == null) {
        logTransfer.pause();
      } else {
        if (!paused.get()) {
          logTransfer.play();
        }
      }
    });

    filterLevel.addListener((observable, oldValue, newValue) -> setItems(
      new FilteredList<>(
        logItems,
        logRecord ->
          logRecord.getLevel().toInt() >=
            filterLevel.get().toInt()
      )
    ));
    filterLevel.set(Level.ALL);

    setCellFactory(param -> new ListCell<>() {
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

        String context =
          (item.getContext() == null)
            ? ""
            : item.getContext() + " ";

        switch (item.getLevel().toInt()) {
          case Level.DEBUG_INT, Level.TRACE_INT, Level.ALL_INT -> pseudoClassStateChanged(debug, true);
          case Level.INFO_INT -> pseudoClassStateChanged(info, true);
          case Level.WARN_INT -> pseudoClassStateChanged(warn, true);
          case Level.ERROR_INT -> pseudoClassStateChanged(error, true);
        }
      }
    });
  }


  public Node getLogView() {
    return logView;
  }

  public void appendLogLine(Level level, String context, String line) {
    if (Objects.nonNull(line))
      try {
        Platform.runLater(() -> log.offer(new LogRecord(level, context, line)));
      } catch (Exception e) {
        // no op
      }
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


  static class LogRecord {
    private final Level level;
    private final String context;
    private final String message;

    public LogRecord(Level level, String context, String message) {
      this.level = level;
      this.context = context;
      this.message = message;
    }

    public Level getLevel() {
      return level;
    }

    public String getContext() {
      return context;
    }

    public String getMessage() {
      return message;
    }
  }
}
