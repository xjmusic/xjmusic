package io.xj.gui.services;

import jakarta.annotation.Nullable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ThemeService {
  Logger LOG = LoggerFactory.getLogger(ThemeService.class);

  final String defaultThemePath;
  final String darkThemePath;

  final BooleanProperty isDarkTheme = new SimpleBooleanProperty(true);

  public ThemeService(
    @Value("${gui.theme.default}") String defaultThemePath,
    @Value("${gui.theme.dark}") String darkThemePath
  ) {
    this.defaultThemePath = defaultThemePath;
    this.darkThemePath = darkThemePath;
  }

  public BooleanProperty isDarkThemeProperty() {
    return isDarkTheme;
  }

  public void setup(@Nullable Scene scene) {
    if (Objects.isNull(scene)) {
      LOG.warn("Scene is null, cannot setup theme");
      return;
    }
    if (!scene.getStylesheets().contains(defaultThemePath)) {
      scene.getStylesheets().add(defaultThemePath);
    }
    if (isDarkTheme.getValue()) {
      scene.getStylesheets().add(darkThemePath);
    } else {
      scene.getStylesheets().remove(darkThemePath);
    }
  }
}
