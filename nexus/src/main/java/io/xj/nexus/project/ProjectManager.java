package io.xj.nexus.project;

import io.xj.hub.HubContent;
import jakarta.annotation.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public interface ProjectManager {

  /**
   The audio base URL

   @return the audio base URL
   */
  String getAudioBaseUrl();

  /**
   Set the audio base URL

   @param audioBaseUrl new value
   */
  void setAudioBaseUrl(String audioBaseUrl);

  /**
   Clone from a demo template@param templateShipKey of the demo

   @param name of the project
   */
  void cloneFromDemoTemplate(String templateShipKey, String name);

  /**
   @return the path prefix of the project
   */
  String getPathPrefix();

  /**
   Set the project path prefix

   @param pathPrefix on disk, with trailing slash
   */
  void setPathPrefix(String pathPrefix);

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
