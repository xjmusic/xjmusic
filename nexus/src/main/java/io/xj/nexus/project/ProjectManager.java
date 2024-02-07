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
   Workstation has Project Cleanup option to delete unused audio files from project folder
   https://www.pivotaltracker.com/story/show/186930458@return
   */
  int cleanupProject();

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

   @param library parent containing program
   @param name    of the program
   @return the new program
   */
  Program createProgram(Library library, String name) throws Exception;

  /**
   Create a new instrument

   @param library parent containing instrument
   @param name    of the instrument
   @return the new instrument
   */
  Instrument createInstrument(Library library, String name) throws Exception;

  /**
   Create a new instrument audio

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
   Copy the instrument audio waveform from one audio to another@param audio   new instrument audio
   */
  void updateInstrumentAudioAndCopyWaveformFile(InstrumentAudio audio) throws Exception;

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

