package org.example.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.greeter.api.Greeter;

public class HelloFX extends Application {

    @Override
    public void start(Stage stage) {
        Greeter greeter = Greeter.getInstance().orElseThrow();
        String greeting = greeter.getGreeting("OpenJFX");
        Label label = new Label(greeting);
        label.setFont(Font.font(48));
        StackPane pane = new StackPane(label);
        Scene scene = new Scene(pane, 480, 240);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
