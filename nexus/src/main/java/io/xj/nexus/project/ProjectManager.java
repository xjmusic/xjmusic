package io.xj.nexus.project;

import io.xj.hub.HubContent;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.Project;
import io.xj.hub.tables.pojos.Template;
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

   @param hubAccess        control
   @param hubBaseUrl       of the lab
   @param audioBaseUrl     of the lab
   @param parentPathPrefix parent folder to the project folder
   @param projectId        in the lab
   @param projectName      of the project folder and the project
   @return true if successful
   */
  boolean cloneFromLabProject(HubClientAccess hubAccess, String hubBaseUrl, String audioBaseUrl, String parentPathPrefix, UUID projectId, String projectName);

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
   Project Cleanup option to delete unused audio files from project folder
   https://www.pivotaltracker.com/story/show/186930458

   @return results
   */
  ProjectCleanupResults cleanupProject();

  /**
   Project Sync option to delete unused audio files from project folder
   https://www.pivotaltracker.com/story/show/186930458
   <p>
   Once a project has been cloned to disk, choose Project -> Sync to update that local copy with any
   updates from the Lab, and the Lab copy with any updates from local.

   @param hubAccess    control
   @param hubBaseUrl   of the lab
   @param audioBaseUrl of the lab
   @return results
   */
  ProjectPushResults pushProject(HubClientAccess hubAccess, String hubBaseUrl, String audioBaseUrl);

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
   Get the path prefix to the audio folder for an instrument

   @param instrumentId of the instrument
   @return the path prefix to the audio
   */
  String getPathPrefixToInstrumentAudio(UUID instrumentId);

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
   Create a new template

   @param name of the template
   @return the new template
   */
  Template createTemplate(String name) throws Exception;

  /**
   Create a new library

   @param name of the library
   @return the new library
   */
  Library createLibrary(String name) throws Exception;

  /**
   Create a new program
   <p>
   When creating a new Program, source default values from the first available
   1. Program in the same library
   2. any Programs in the project
   3. defaults
   https://www.pivotaltracker.com/story/show/187042551

   @param library parent containing program
   @param name    of the program
   @return the new program
   */
  Program createProgram(Library library, String name) throws Exception;

  /**
   Create a new instrument
   <p>
   When creating a new Instrument, source default values from the first available
   1. Instrument in the same library
   2. any Instruments in the project
   3. defaults
   https://www.pivotaltracker.com/story/show/187042551

   @param library parent containing instrument
   @param name    of the instrument
   @return the new instrument
   */
  Instrument createInstrument(Library library, String name) throws Exception;

  /**
   Create a new instrument audio
   <p>
   When creating a new Instrument Audio, source default values from the first available
   1. Instrument Audios in the same instrument
   2. Instrument Audios in the same library
   3. Programs in the same library
   4. any Instrument Audios in the project
   5. any Programs in the project
   6. defaults
   https://www.pivotaltracker.com/story/show/187042551

   @param instrument    in which to create an audio
   @param audioFilePath to import audio from disk
   @return the new instrument
   */
  InstrumentAudio createInstrumentAudio(Instrument instrument, String audioFilePath) throws Exception;

  /**
   Move a program to a new library

   @param id        of program to move
   @param libraryId new library id
   @return the moved program
   */
  Program moveProgram(UUID id, UUID libraryId) throws Exception;

  /**
   Move a instrument to a new library

   @param id        of instrument to move
   @param libraryId new library id
   @return the moved instrument
   */
  Instrument moveInstrument(UUID id, UUID libraryId) throws Exception;

  /**
   Copy the instrument audio waveform from one audio to another@param instrumentAudioId new instrument audio
   */
  void renameWaveformIfNecessary(UUID instrumentAudioId) throws Exception;

  /**
   Clone a Template from a source template by id

   @param fromId source template id
   @param name   name of the new template
   @return the new template
   */
  Template cloneTemplate(UUID fromId, String name) throws Exception;

  /**
   Clone a Library from a source library by id

   @param fromId source library id
   @param name   name of the new library
   @return the new library
   */
  Library cloneLibrary(UUID fromId, String name) throws Exception;

  /**
   Clone a Program from a source program by id

   @param fromId    source program id
   @param libraryId new library id
   @param name      name of the new program
   @return the new program
   */
  Program cloneProgram(UUID fromId, UUID libraryId, String name) throws Exception;

  /**
   Clone a Program Sequence from a source program sequence by id
   Note: Does not clone the program sequence bindings (that would cause duplicate bindings at all the same offsets)

   @param fromId source program sequence id
   @param name   name of the new program sequence
   @return the new program sequence
   */
  ProgramSequence cloneProgramSequence(UUID fromId, String name) throws Exception;

  /**
   Clone a Program Sequence Pattern from a source program sequence pattern by id

   @param fromId source program sequence pattern id
   @param name   name of the new program sequence pattern
   @return the new program sequence pattern
   */
  ProgramSequencePattern cloneProgramSequencePattern(UUID fromId, String name) throws Exception;

  /**
   Clone a Instrument from a source instrument by id

   @param fromId    source instrument id
   @param libraryId new library id
   @param name      name of the new instrument
   @return the new instrument
   */
  Instrument cloneInstrument(UUID fromId, UUID libraryId, String name) throws Exception;
}

