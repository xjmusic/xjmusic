package io.xj.nexus.project;

import io.xj.hub.HubContent;
import io.xj.hub.tables.pojos.Project;
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
   Clone from a demo template@param templateShipKey of the demo

   @param name of the project
   */
  void cloneFromDemoTemplate(String templateShipKey, String name);

  /**
   @return the path prefix of the project
   */
  String getProjectPathPrefix();

  /**
   Set the path by project path prefix and project name

   @param pathPrefix   on disk, with trailing slash
   @param projectName  of the project
   @param audioBaseUrl the audio base URL
   */
  void setup(String pathPrefix, String projectName, String audioBaseUrl);

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
  String getProjectFilePath();

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
   Set the callback to be invoked when the progress changes

   @param onProgress the callback
   */
  void setOnProgress(@Nullable Consumer<Double> onProgress);

  /**
   Set the callback to be invoked when the project state changes

   @param onStateChange the callback
   */
  void setOnStateChange(@Nullable Consumer<ProjectState> onStateChange);
}
