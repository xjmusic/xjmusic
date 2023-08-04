package io.xj.workstation.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MainWindowController {
  Logger LOG = LoggerFactory.getLogger(MainWindowController.class);

  @FXML
  protected void onQuit() {
    LOG.info("Will exit application");
    Platform.exit();
  }
}
