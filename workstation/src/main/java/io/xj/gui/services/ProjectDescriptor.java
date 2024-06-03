package io.xj.gui.services;

import io.xj.model.pojos.Project;

/**
 Store a recent project
 */
public record ProjectDescriptor(Project project, String projectFilename, String projectFilePath) {
  @Override
  public String toString() {
    return projectFilename;
  }
}
