package io.xj.gui.services;

import io.xj.hub.HubContent;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.Template;
import io.xj.nexus.project.ProjectState;
import io.xj.nexus.project.ProjectUpdate;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableListValue;
import javafx.beans.value.ObservableStringValue;

import java.util.List;
import java.util.UUID;

public interface ProjectService {
  /**
   The Project View Mode

   @return the project view mode
   */
  ObjectProperty<ProjectViewMode> viewModeProperty();

  /**
   The Project Edit Mode

   @return the project edit mode
   */
  ObjectProperty<ProjectEditMode> editModeProperty();

  /**
   Close the project
   */
  void closeProject();

  /**
   Open a project

   @param projectFilePath to .XJ project file on disk
   */
  void openProject(String projectFilePath);

  /**
   Create a new project

   @param parentPathPrefix on disk
   @param projectName      of the project
   */
  void createProject(String parentPathPrefix, String projectName);

  /**
   Clone from a Lab Project

   @param parentPathPrefix on disk
   @param projectId        in the lab
   @param projectName      of the project
   */
  void cloneFromLabProject(String parentPathPrefix, UUID projectId, String projectName);

  /**
   Clone from a demo template

   @param parentPathPrefix on disk, parent of the project folder
   @param templateShipKey  of the demo
   @param projectName      of the project folder and the project
   */
  void cloneFromDemoTemplate(String parentPathPrefix, String templateShipKey, String projectName);

  /**
   Save the project
   */
  void saveProject();

  /**
   Cancel the project loading
   */
  void cancelProjectLoading();

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
  BooleanBinding isStateLoadingProperty();

  /**
   @return Observable property for whether the project is in a ready state
   */
  BooleanBinding isStateReadyProperty();

  /**
   @return Observable property for whether the project is in a standby state
   */
  BooleanBinding isStateStandbyProperty();

  /**
   @return Observable property for whether the project is in content view mode
   */
  BooleanBinding isViewModeContentProperty();

  /**
   @return Observable property for whether the project is in fabrication view mode
   */
  BooleanBinding isViewModeFabricationProperty();

  /**
   @return Get the project content
   */
  HubContent getContent();

  /**
   @return the window title
   */
  ObservableStringValue windowTitleProperty();

  /**
   @return the list of recent projects
   */
  ObservableListValue<ProjectDescriptor> recentProjectsProperty();

  /**
   Attach a listener to project updates

   @param type     the type of update to listen for
   @param listener the listener to attach
   */
  void addProjectUpdateListener(ProjectUpdate type, Runnable listener);

  /**
   Get the current list of non-deleted libraries sorted by name

   @return the list of libraries
   */
  List<Library> getLibraries();

  /**
   Get the current list of non-deleted programs sorted by name

   @return the list of programs
   */
  List<Program> getPrograms();

  /**
   Get the current list of non-deleted instruments sorted by name

   @return the list of instruments
   */
  List<Instrument> getInstruments();

  /**
   Get the current list of non-deleted templates sorted by name

   @return the list of templates
   */
  List<Template> getTemplates();
}
