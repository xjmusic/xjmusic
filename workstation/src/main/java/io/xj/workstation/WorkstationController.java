package io.xj.workstation;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class WorkstationController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to XJ music!");
    }
}
