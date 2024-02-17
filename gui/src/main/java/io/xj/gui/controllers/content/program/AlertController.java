package io.xj.gui.controllers.content.program;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class AlertController {
  @FXML
  public Label titleLabel;
  @FXML
  public Label messageLabel;
  @FXML
  public AnchorPane container;

  public void setUp(String title, String message, String color){
    titleLabel.setText(title);
    messageLabel.setText(message);
    container.setStyle("-fx-background-color:"+color+";");
  }
}
