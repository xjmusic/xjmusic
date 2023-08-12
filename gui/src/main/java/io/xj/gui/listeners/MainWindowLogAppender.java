package io.xj.gui.listeners;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import io.xj.gui.controllers.MainWindowController;
import org.springframework.stereotype.Service;

@Service
public class MainWindowLogAppender extends AppenderBase<ILoggingEvent> {
  private final MainWindowController mainWindowController;
  public MainWindowLogAppender(MainWindowController mainWindowController) {
    this.mainWindowController = mainWindowController;
  }

  @Override
  protected void append(ILoggingEvent eventObject) {
    System.out.println(eventObject.toString());
    mainWindowController.appendLogLine(eventObject.toString());
    var hello = 123;// todo
    // todo textArea.appendText(message + "\n");
  }
}
