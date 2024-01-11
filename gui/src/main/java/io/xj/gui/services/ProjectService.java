package io.xj.gui.services;

import io.xj.hub.HubContent;
import io.xj.hub.tables.pojos.Project;
import io.xj.nexus.project.ProjectState;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;

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
   Open a project

   @param path on disk
   */
  void openProject(String path);

  /**
   Create a new project

   @param pathPrefix on disk
   @param name       of the project
   */
  void createProject(String pathPrefix, String name);

  /**
   Clone from a Lab Project

   @param pathPrefix on disk
   @param projectId  in the lab
   @param name       of the project
   */
  void cloneFromLabProject(String pathPrefix, UUID projectId, String name);

  /**
   Clone from a demo template

   @param pathPrefix      on disk
   @param templateShipKey of the demo
   @param name            of the project
   */
  void cloneFromDemoTemplate(String pathPrefix, String templateShipKey, String name);

  /**
   Save the project
   */
  void saveProject();

  /**
   @return Path prefix
   */
  StringProperty basePathPrefixProperty();

  /**
   @return observable progress property
   */
  DoubleProperty progressProperty();

  /**
   @return observable state property
   */
  ObjectProperty<ProjectState> stateProperty();

  /**
   @return observable state text property
   */
  ObservableStringValue stateTextProperty();

  /**
   @return Observable property for whether the project is in a loading state
   */
  ObservableBooleanValue isStateLoadingProperty();

  /**
   @return Observable property for whether the project is in a ready state
   */
  ObservableBooleanValue isStateReadyProperty();

  /**
   @return Get the project content
   */
  HubContent getContent();

  /**
   @return the window title
   */
  ObservableStringValue windowTitleProperty();
}
