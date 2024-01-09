package io.xj.gui.services;

import io.xj.hub.tables.pojos.Project;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

import java.util.UUID;

public interface ProjectService {
  /**
   The Project View Mode

   @return the project view mode
   */
  ObjectProperty<ProjectViewMode> viewModeProperty();

  /**
   @return the current project
   */
  ObjectProperty<Project> currentProjectProperty();

  /**
   Open a Project from disk
   */
  void openProject(String path);

  /**
   Start a new Project
   */
  void createProject(String pathPrefix, String name);

  /**
   Clone a Project from the Lab
   */
  void cloneProject(String pathPrefix, UUID projectId, String name);

  /**
   @return Path prefix
   */
  StringProperty pathPrefixProperty();

  /**
   Save the project
   */
  void saveProject();
}
