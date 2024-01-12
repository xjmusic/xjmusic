package io.xj.gui.services;

import io.xj.hub.tables.pojos.Project;

/**
 Store a recent project
 */
public record ProjectDescriptor(Project project, String filename, String path) {
  @Override
  public String toString() {
    return filename;
  }
}
