package io.xj.workstation.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainWindowController {
  Logger LOG = LoggerFactory.getLogger(MainWindowController.class);

    @FXML
    private Label welcomeText;

    @FXML
    protected void onQuit() {
        welcomeText.setText("Welcome to XJ music!");
    }
}
