package io.xj.gui;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class WorkstationLogAppender extends AppenderBase<ILoggingEvent> {
  public static final AtomicReference<LogListener> LISTENER = new AtomicReference<>();

  public interface LogListener {
    void onLog(Level level, String context, String message);
  }

  @Override
  protected void append(ILoggingEvent eventObject) {
    var message = formatMessage(eventObject);
    System.out.println(message);
    if (LISTENER.get() != null) {
      LISTENER.get().onLog(eventObject.getLevel(), eventObject.getLoggerName(), message);
    }
  }

  private String formatMessage(ILoggingEvent eventObject) {
    return String.format("[%s] %s %s",
      eventObject.getLevel(),
      Arrays.stream(eventObject.getCallerData()).findFirst().map(this::formatCaller).orElse("-"),
      eventObject.getFormattedMessage()
    ) + formatMessageCauseSuffix(eventObject);
  }

  private String formatMessageCauseSuffix(ILoggingEvent eventObject) {
    return (Objects.nonNull(eventObject.getThrowableProxy()) &&
      Objects.nonNull(eventObject.getThrowableProxy().getCause()) &&
      Objects.nonNull(eventObject.getThrowableProxy().getCause().getMessage())) ?
      " (" + eventObject.getThrowableProxy().getCause().getMessage() + ")"
      : "";
  }

  private String formatCaller(StackTraceElement stackTraceElement) {
    return stackTraceElement.getClassName().replaceAll(".*\\.", "");
  }
}
