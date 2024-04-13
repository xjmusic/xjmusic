package io.xj.nexus.project;

import io.xj.hub.HubContent;
import io.xj.hub.pojos.Instrument;
import io.xj.hub.pojos.InstrumentAudio;
import io.xj.hub.pojos.InstrumentMeme;
import io.xj.hub.pojos.Library;
import io.xj.hub.pojos.Program;
import io.xj.hub.pojos.ProgramMeme;
import io.xj.hub.pojos.ProgramSequence;
import io.xj.hub.pojos.ProgramSequenceBindingMeme;
import io.xj.hub.pojos.ProgramSequencePattern;
import io.xj.hub.pojos.ProgramSequencePatternEvent;
import io.xj.hub.pojos.ProgramVoice;
import io.xj.hub.pojos.ProgramVoiceTrack;
import io.xj.hub.pojos.Project;
import io.xj.hub.pojos.Template;
import jakarta.annotation.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public interface ProjectManager {

  /**
   The audio base URL

   @return the audio base URL
   */
  String getDemoBaseUrl();

  /**
   Close the current project
   */
  void closeProject();

  /**
   Create project from a demo template

   @param baseUrl          of the demo
   @param templateKey      of the demo
   @param parentPathPrefix parent folder to the project folder
   @param projectName      of the project folder and the project
   @return true if successful
   */
  boolean createProjectFromDemoTemplate(String baseUrl, String templateKey, String parentPathPrefix, String projectName);

  /**
   Export a template as JSON with all its audio (original or prepared)

   @param template             to export
   @param parentPathPrefix     parent folder to the template export folder
   @param exportName           of the export
   @param conversion           whether to convert the audio
   @param conversionFrameRate  target Frame Rate for conversion
   @param conversionSampleBits target Sample Bits for conversion
   @param conversionChannels   target Channels for conversion
   @return true if successful
   */
  boolean exportTemplate(
    Template template,
    String parentPathPrefix,
    String exportName,
    Boolean conversion,
    @Nullable Integer conversionFrameRate,
    @Nullable Integer conversionSampleBits,
    @Nullable Integer conversionChannels
  );

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
   Cancel the project loading
   */
  void cancelOperation();

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
   Get the content for a template

   @param template for which to get content
   @return the content
   */
  HubContent getContent(Template template);

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
   Set the callback to be invoked when the progress label changes

   @param onProgressLabel the callback
   */
  void setOnProgressLabel(@Nullable Consumer<String> onProgressLabel);

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
   Create a new program sequence
   <p>
   Workstation creating new program sequence, populate it with defaults from other sequences or program
   When creating a new Program Sequence, source default values from the first available
   1. Program Sequences in the same program
   2. Program Sequences in the same library
   3. Programs in the same library
   4. any Program Sequences in the project
   5. any Programs in the project
   6. defaults

   @param programId for which to create a sequence
   @return the new program sequence
   */
  ProgramSequence createProgramSequence(UUID programId) throws Exception;

  /**
   Create a new program sequence pattern

   @param programId         for which to create a program sequence pattern
   @param programSequenceId for which to create a program sequence pattern
   @param programVoiceId    for which to create a program sequence pattern
   @return the new program sequence pattern
   */
  ProgramSequencePattern createProgramSequencePattern(UUID programId, UUID programSequenceId, UUID programVoiceId) throws Exception;

  /**
   Create a new program sequence pattern event

   @param trackId   for which to create a program sequence pattern event
   @param patternId for which to create a program sequence pattern event
   @param position  of the new event in the pattern
   @param duration  of the new event in the pattern
   @return the new program sequence pattern event
   */
  ProgramSequencePatternEvent createProgramSequencePatternEvent(UUID trackId, UUID patternId, double position, double duration) throws Exception;

  /**
   Create a new program voice

   @param programId for which to create a voice
   @return the new program voice
   */
  ProgramVoice createProgramVoice(UUID programId) throws Exception;

  /**
   Create a new program voice track

   @param voiceId for which to create a track
   @return the new program voice track
   */
  ProgramVoiceTrack createProgramVoiceTrack(UUID voiceId) throws Exception;

  /**
   Create a new program meme

   @param programId for which to create meme
   @return the new program meme
   @throws Exception if the instrument meme cannot be created
   */
  ProgramMeme createProgramMeme(UUID programId) throws Exception;

  /**
   Create a new Program Sequence Binding Meme

   @param programSequenceBindingId for which to create a program sequence binding meme
   @return the new program sequence binding meme
   @throws Exception if the program sequence binding meme cannot be created
   */
  ProgramSequenceBindingMeme createProgramSequenceBindingMeme(UUID programSequenceBindingId) throws Exception;

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
   Create a new instrument meme

   @param instrumentId for which to create meme
   @return the new instrument meme
   @throws Exception if the instrument meme cannot be created
   */
  InstrumentMeme createInstrumentMeme(UUID instrumentId) throws Exception;

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
   Copy the instrument audio waveform from one audio to another

   @param instrumentAudioId new instrument audio
   */
  void renameWaveformIfNecessary(UUID instrumentAudioId) throws Exception;

  /**
   Duplicate a Template from a source template by id

   @param fromId source template id
   @param name   name of the new template
   @return the new template
   */
  Template duplicateTemplate(UUID fromId, String name) throws Exception;

  /**
   Duplicate a Library from a source library by id

   @param fromId source library id
   @param name   name of the new library
   @return the new library
   */
  Library duplicateLibrary(UUID fromId, String name) throws Exception;

  /**
   Duplicate a Program from a source program by id

   @param fromId    source program id
   @param libraryId new library id
   @param name      name of the new program
   @return the new program
   */
  Program duplicateProgram(UUID fromId, UUID libraryId, String name) throws Exception;

  /**
   Duplicate a Program Sequence from a source program sequence by id
   Note: Does not duplicate the program sequence bindings (that would cause duplicate bindings at all the same offsets)

   @param fromId source program sequence id
   @return the new program sequence
   */
  ProgramSequence duplicateProgramSequence(UUID fromId) throws Exception;

  /**
   Duplicate a Program Sequence Pattern from a source program sequence pattern by id

   @param fromId source program sequence pattern id
   @return the new program sequence pattern
   */
  ProgramSequencePattern duplicateProgramSequencePattern(UUID fromId) throws Exception;

  /**
   Duplicate a Instrument from a source instrument by id

   @param fromId    source instrument id
   @param libraryId new library id
   @param name      name of the new instrument
   @return the new instrument
   */
  Instrument duplicateInstrument(UUID fromId, UUID libraryId, String name) throws Exception;
}

