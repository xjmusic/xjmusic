package io.xj.nexus.project;

import io.xj.hub.HubContent;
import io.xj.hub.tables.pojos.Project;
import io.xj.nexus.hub_client.HubClientAccess;
import jakarta.annotation.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public interface ProjectManager {

  /**
   The audio base URL

   @return the audio base URL
   */
  String getAudioBaseUrl();

  /**
   Close the current project
   */
  void closeProject();

  /**
   Clone from a demo template

   @param audioBaseUrl     of the demo
   @param parentPathPrefix parent folder to the project folder
   @param templateShipKey  of the demo
   @param projectName      of the project folder and the project
   @return true if successful
   */
  boolean cloneProjectFromDemoTemplate(String audioBaseUrl, String parentPathPrefix, String templateShipKey, String projectName);

  /**
   Clone from a Lab Project

   @param access           control
   @param labBaseUrl       of the lab
   @param audioBaseUrl     of the lab
   @param parentPathPrefix parent folder to the project folder
   @param projectId        in the lab
   @param projectName      of the project folder and the project
   @return true if successful
   */
  boolean cloneFromLabProject(HubClientAccess access, String labBaseUrl, String audioBaseUrl, String parentPathPrefix, UUID projectId, String projectName);

  /**
   Open a project from a local file

   @param projectFilePath the path prefix of the project
   @return true if successful
   */
  boolean openProjectFromLocalFile(String projectFilePath);

  /**
   Create a project on the local disk

   @param parentPathPrefix the path prefix of the project
   @param projectName      the name of the project
   @return true if successful
   */
  boolean createProject(String parentPathPrefix, String projectName);

  /**
   Save the project
   */
  void saveProject();

  /**
   Cancel the project loading
   */
  void cancelProjectLoading();

  /**
   @return the path prefix of the project
   */
  String getProjectPathPrefix();

  /**
   @return the current project, or empty if not set
   */
  Optional<Project> getProject();

  /**
   @return the project file name
   */
  String getProjectFilename();

  /**
   @return the project file absolute path
   */
  String getPathToProjectFile();

  /**
   Set the project path prefix

   @param contentStoragePathPrefix the project path prefix
   */
  void setProjectPathPrefix(String contentStoragePathPrefix);

  /**
   @return a reference to the current content
   */
  HubContent getContent();

  /**
   Get the path to some instrument audio in the project

   @param instrumentId of the instrument
   @param waveformKey  of the audio
   @return the path to the audio
   */
  String getPathToInstrumentAudio(UUID instrumentId, String waveformKey);

  /**
   Set the audio base URL

   @param audioBaseUrl the audio base URL
   */
  void setAudioBaseUrl(String audioBaseUrl);

  /**
   Set the callback to be invoked when the progress changes

   @param onProgress the callback
   */
  void setOnProgress(@Nullable Consumer<Double> onProgress);

  /**
   Set the callback to be invoked when the project state changes

   @param onStateChange the callback
   */
  void setOnStateChange(@Nullable Consumer<ProjectState> onStateChange);

  /**
   Attach a listener to project updates

   @param type     the type of update to listen for
   @param listener the listener to attach
   */
  void addProjectUpdateListener(ProjectUpdate type, Runnable listener);

  /**
   Notify all listeners of a project update

   @param type the type of update
   */
  void notifyProjectUpdateListeners(ProjectUpdate type);
}
