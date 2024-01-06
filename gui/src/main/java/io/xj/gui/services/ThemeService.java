// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import jakarta.annotation.Nullable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Service
public class ThemeService {
  private static final Logger LOG = LoggerFactory.getLogger(ThemeService.class);
  private static final int DEFAULT_FONT_SIZE = 12;
  private final String defaultThemePath;
  private final String darkThemePath;
  private final String fontPathPattern;

  final BooleanProperty isDarkTheme = new SimpleBooleanProperty(true);
  private final PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver;

  public ThemeService(
    @Value("${gui.theme.default}") String defaultThemePath,
    @Value("${gui.theme.dark}") String darkThemePath,
    @Value("${gui.resources.font.path.pattern}") String fontPathPattern
  ) {
    this.defaultThemePath = defaultThemePath;
    this.darkThemePath = darkThemePath;
    this.fontPathPattern = fontPathPattern;
    this.pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
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

  /**
   Fonts directory contains subdirectories with font files.
   */
  public void setupFonts() {
    try {
      Resource[] resources = pathMatchingResourcePatternResolver.getResources(fontPathPattern);

      for (Resource resource : resources) {
        if (!resource.isReadable()) {
          continue;
        }

        File file = resource.getFile();

        if (file.isDirectory()) {
          continue; // Skip directories
        }

        // Here you can load the font file as needed
        Font.loadFont(file.toURI().toString(), DEFAULT_FONT_SIZE);
      }

    } catch (IOException e) {
      LOG.error("Failed to load fonts from {}", fontPathPattern, e);
    }
  }
}
