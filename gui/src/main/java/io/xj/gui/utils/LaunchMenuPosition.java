package io.xj.gui.utils;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 Determine the intended position of a popup menu when launched
 */
public class LaunchMenuPosition {
  private final int targetX;
  private final int targetY;

  static public LaunchMenuPosition from(MouseEvent event) {
    return new LaunchMenuPosition((int) event.getScreenX(), (int) event.getScreenY());
  }

  static public LaunchMenuPosition from(Node launcher) {
    return new LaunchMenuPosition(launcher);
  }

  private LaunchMenuPosition(int targetX, int targetY) {
    this.targetX = targetX;
    this.targetY = targetY;
  }

  private LaunchMenuPosition(Node launcher) {
    var p = launcher.localToScene(0, 0);
    targetX = (int) (launcher.getScene().getWindow().getX() + p.getX() + launcher.getBoundsInLocal().getWidth() / 2);
    targetY = (int) (launcher.getScene().getWindow().getY() + p.getY() + launcher.getBoundsInLocal().getHeight() * 2);
  }

  public void move(Window window, Stage child) {
    child.setX(Math.min(window.getX() + window.getWidth() - child.getWidth(), Math.max(window.getX(), targetX - child.getWidth() / 2)));
    child.setY(Math.min(window.getY() + window.getHeight() - child.getHeight(), Math.max(window.getY(), targetY)));
  }
}