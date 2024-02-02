// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.services;

import jakarta.annotation.Nullable;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

@Service
public class ThemeService {
  private static final Logger LOG = LoggerFactory.getLogger(ThemeService.class);
  private static final int DEFAULT_FONT_SIZE = 12;
  private final String defaultThemePath;
  private final String fontPathPattern;
  private final ResourceLoader resourceLoader;
  private final PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver;

  public ThemeService(
    @Value("${gui.theme.default}") String defaultThemePath,
    @Value("${gui.resources.font.path.pattern}") String fontPathPattern,
    ResourceLoader resourceLoader
  ) {
    this.defaultThemePath = defaultThemePath;
    this.fontPathPattern = fontPathPattern;
    this.resourceLoader = resourceLoader;
    this.pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
  }

  public void setup(@Nullable Scene scene) {
    if (Objects.isNull(scene)) {
      LOG.warn("Scene is null, cannot setup theme");
      return;
    }
    if (!scene.getStylesheets().contains(defaultThemePath)) {
      scene.getStylesheets().add(defaultThemePath);
    }
  }

  /**
   Fonts directory contains subdirectories with font files.
   */
  public void setupFonts() {
    try {
      for (Resource resource : pathMatchingResourcePatternResolver.getResources(fontPathPattern))
        if (resource.isReadable())
          Font.loadFont(resourceLoader.getResource(resource.getURI().toString()).getInputStream(), DEFAULT_FONT_SIZE);

    } catch (IOException e) {
      LOG.error("Failed to load fonts from {}", fontPathPattern, e);
    }
  }
}
