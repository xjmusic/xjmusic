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
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Service
public class ThemeService {
  static final Logger LOG = LoggerFactory.getLogger(ThemeService.class);
  static final int DEFAULT_FONT_SIZE = 12;
  final Resource fontsDirectory;

  final String defaultThemePath;
  final String darkThemePath;

  final BooleanProperty isDarkTheme = new SimpleBooleanProperty(true);

  public ThemeService(
    @Value("${gui.theme.default}") String defaultThemePath,
    @Value("${gui.theme.dark}") String darkThemePath,
    @Value("classpath:/fonts/") Resource fontsDirectory
  ) {
    this.defaultThemePath = defaultThemePath;
    this.darkThemePath = darkThemePath;
    this.fontsDirectory = fontsDirectory;
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
      String[] fontFolders = Objects.requireNonNull(fontsDirectory.getFile()).list();
      if (fontFolders != null) {
        for (String fontDir : fontFolders) {
          File[] fontFilesInDir = new File(fontsDirectory.getFile(), fontDir).listFiles();
          if (fontFilesInDir != null) {
            for (File fontFile : fontFilesInDir) {
              Font.loadFont(fontFile.toURI().toString(), DEFAULT_FONT_SIZE);
            }
          }
        }
      }

    } catch (IOException e) {
      LOG.error("Failed to load fonts from directory: {}", fontsDirectory.getFilename(), e);
    }
  }
}
