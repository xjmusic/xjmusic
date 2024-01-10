package io.xj.nexus.project;

public interface ProjectManager {

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
   Set the project path prefix

   @param pathPrefix on disk, with trailing slash
   */
  void setPathPrefix(String pathPrefix);
}
