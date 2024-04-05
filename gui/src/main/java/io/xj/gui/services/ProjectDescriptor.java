package io.xj.gui.services;

import io.xj.hub.pojos.Project;

/**
 Store a recent project
 */
public record ProjectDescriptor(Project project, String projectFilename, String projectFilePath) {
  @Override
  public String toString() {
    return projectFilename;
  }
}
