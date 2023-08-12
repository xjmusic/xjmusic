package io.xj.gui.listeners;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.concurrent.atomic.AtomicReference;

public class MainWindowLogAppender extends AppenderBase<ILoggingEvent> {
  public static final AtomicReference<LogListener> LISTENER = new AtomicReference<>();

  public interface LogListener {
    void onLog(String message);
  }

  @Override
  protected void append(ILoggingEvent eventObject) {
    System.out.println(eventObject.toString());
    if (LISTENER.get() != null) {
      LISTENER.get().onLog(eventObject.toString());
    }
  }
}
