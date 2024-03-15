package io.xj.gui.services;

import io.xj.hub.HubContent;
import io.xj.hub.enums.ContentBindingType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.InstrumentMeme;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramMeme;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceBindingMeme;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import io.xj.hub.tables.pojos.Project;
import io.xj.hub.tables.pojos.Template;
import io.xj.nexus.project.ProjectState;
import jakarta.annotation.Nullable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableListValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableStringValue;
import javafx.scene.control.Alert;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public interface ProjectService {
  /**
   Close the project
   */
  void closeProject(@Nullable Runnable afterClose);

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
  void saveProject(Runnable onComplete);

  /**
   Workstation has Project → Push feature to publish the on-disk version of the project to the Lab (overwriting the Lab version)
   https://www.pivotaltracker.com/story/show/187004700
   <p>
   Once a project has been cloned to disk, choose Project -> Push to upload that local copy to the Lab, and overwrite the Lab copy.
   */
  void pushProject();

  /**
   Project Cleanup option to delete unused audio files from project folder
   https://www.pivotaltracker.com/story/show/186930458
   */
  void cleanupProject();

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
   @return Get the project content
   */
  HubContent getContent();

  /**
   Delete an entity

   @param entity to delete
   */
  void deleteContent(Object entity);

  /**
   Delete an entity by type and id

   @param type the class of the entity
   @param id   the id of the entity
   */
  void deleteContent(Class<?> type, UUID id);

  /**
   Delete a program voice. Show a confirmation dialog if the voice has tracks/patterns/events

   @param programVoiceId the id of the voice to delete
   @return true if the voice was deleted, false otherwise
   */
  boolean deleteProgramVoice(UUID programVoiceId);

  /**
   Delete a program sequence pattern. Show a confirmation dialog if the pattern has events

   @param programSequencePatternId the id of the pattern to delete
   @return true if the pattern was deleted, false otherwise
   */
  boolean deleteProgramSequencePattern(UUID programSequencePatternId);

  /**
   Delete a program voice track. Show a confirmation dialog if the track has events

   @param programVoiceTrackId the id of the track to delete
   @return true if the track was deleted, false otherwise
   */
  boolean deleteProgramVoiceTrack(UUID programVoiceTrackId);

  /**
   @return the list of recent projects
   */
  ObservableListValue<ProjectDescriptor> recentProjectsProperty();

  /**
   Attach a listener to project updates

   @param type     the type of update to listen for
   @param listener the listener to attach
   @return runnable to unsubscribe, should be run when the listening component is tearing itself down
   */
  <N extends Serializable> Runnable addProjectUpdateListener(Class<N> type, Runnable listener);

  /**
   Notify all listeners of a project update

   @param type     the type of update
   @param modified whether the update modified the project
   */
  <N> void didUpdate(Class<N> type, boolean modified);

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

  /**
   @return the current project property
   */
  ObservableObjectValue<Project> currentProjectProperty();

  /**
   Create a new template

   @param name of the new template
   @return the new template
   */
  Template createTemplate(String name) throws Exception;

  /**
   Create a new template binding

   @param templateId         template id
   @param contentBindingType content binding type
   @param targetId           target id
   */
  void createTemplateBinding(UUID templateId, ContentBindingType contentBindingType, UUID targetId);

  /**
   Create a new library

   @param name of the new library
   @return the new library
   */
  Library createLibrary(String name) throws Exception;

  /**
   Create a new program

   @param library the library
   @param name    of the new program
   @return the new program
   */
  Program createProgram(Library library, String name) throws Exception;

  /**
   Create a new program sequence

   @param programId for which to create a program sequence
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

   @param programId for which to create a program voice
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
   Create a new Program Meme

   @param programId for which to create a program meme
   @return the new program meme
   */
  ProgramMeme createProgramMeme(UUID programId) throws Exception;

  /**
   Create a new Program Sequence Binding Meme

   @param programSequenceBindingId for which to create a program sequence binding meme
   @return the new program sequence binding meme
   */
  ProgramSequenceBindingMeme createProgramSequenceBindingMeme(UUID programSequenceBindingId) throws Exception;

  /**
   Create a new instrument

   @param library the library
   @param name    of the new instrument
   @return the new instrument
   */
  Instrument createInstrument(Library library, String name) throws Exception;

  /**
   Create a new instrument meme

   @param instrumentId for which to create a instrument meme
   @return the new instrument meme
   @throws Exception if the instrument meme cannot be created
   */
  InstrumentMeme createInstrumentMeme(UUID instrumentId) throws Exception;

  /**
   Create a new instrument audio

   @param instrument    in which to create an audio
   @param audioFilePath to import audio from disk
   @return the new instrument
   */
  InstrumentAudio createInstrumentAudio(Instrument instrument, String audioFilePath) throws Exception;

  /**
   Move the program to the given library

   @param id      the program uuid
   @param library the library
   @return the program
   */
  Program moveProgram(UUID id, Library library) throws Exception;

  /**
   Move the instrument to the given library

   @param id      the instrument uuid
   @param library the library
   @return the instrument
   */
  Instrument moveInstrument(UUID id, Library library) throws Exception;

  /**
   Clone the given template with a new nam

   @param fromId clone from template
   @param name   the new name
   */
  Template cloneTemplate(UUID fromId, String name) throws Exception;

  /**
   Clone the given library with a new name

   @param fromId clone from library
   @param name   the new name
   @return the new library
   */
  Library cloneLibrary(UUID fromId, String name) throws Exception;

  /**
   Clone the given program with a new name

   @param fromId    clone from program
   @param libraryId the new library id
   @param name      the new name
   @return the new program
   */
  Program cloneProgram(UUID fromId, UUID libraryId, String name) throws Exception;

  /**
   Clone the given program sequence with a new name

   @param fromId clone from program sequence
   @return the new program
   */
  ProgramSequence cloneProgramSequence(UUID fromId) throws Exception;

  /**
   Clone the given program sequence pattern with a new name

   @param fromId clone from program sequence pattern
   @return the new program sequence pattern
   */
  ProgramSequencePattern cloneProgramSequencePattern(UUID fromId) throws Exception;

  /**
   Clone the given instrument with a new name

   @param fromId    clone from instrument
   @param libraryId the new library id
   @param name      the new name
   @return the new instrument
   */
  Instrument cloneInstrument(UUID fromId, UUID libraryId, String name) throws Exception;

  /**
   Update an entity

   @param entity to update
   @param <N>    type of entity
   */
  <N> void update(N entity);

  /**
   Update an entity attribute

   @param type      of entity
   @param id        of entity
   @param attribute to update
   @param value     to set
   @param <N>       type of entity
   */
  <N> void update(Class<N> type, UUID id, String attribute, Object value);

  /**
   Update the given library

   @param library to update
   @return true if successful
   */
  boolean updateLibrary(Library library);

  /**
   Update the given program sequence pattern total from a text field value
   <p>
   Changing pattern length, if there are events past the end of the new length, ask for confirmation and delete those events
   Creating pattern or changing pattern total, length is not allowed to be longer than sequence or less than 1

   @param programSequencePatternId the id of the pattern to update
   @param totalString              the new total, string value for parsing
   @return true if successful
   */
  boolean updateProgramSequencePatternTotal(UUID programSequencePatternId, String totalString);

  /**
   Update the given program sequence total from a text field value
   <p>
   Changing sequence total, not allowed to be shorter than the longest pattern

   @param programSequenceId the id of the pattern to update
   @param totalString       the new total, string value for parsing
   @return true if successful
   */
  boolean updateProgramSequenceTotal(UUID programSequenceId, String totalString);

  /**
   Update the program type, but confirm and delete unused parts based on the new type
   <p>
   - Changing type to a Main program, confirm then delete any voice tracks, sequence patterns, and sequence pattern events
   - Changing type to a Macro program, confirm then delete any sequence chords, sequence chord voicings, voices, voice tracks, sequence patterns, and sequence pattern events
   - Changing type to a Detail or Beat program, confirm then delete any sequence bindings, sequence chords, sequence chord voicings, and sequence binding memes

   @param programId program for which to change type
   @param type      new program type
   @return true if successful
   */
  boolean updateProgramType(UUID programId, ProgramType type);

  /**
   Get the path prefix to the audio folder for an instrument

   @param instrumentId of the instrument
   @return the path prefix to the audio
   */
  String getPathPrefixToInstrumentAudio(UUID instrumentId);

  /**
   Get path to an instrument audio waveform

   @param audio for which to get path
   @return path to waveform
   */
  String getPathToInstrumentAudioWaveform(InstrumentAudio audio);

  /**
   Whether the project has been modified since loading content

   @return observable boolean property
   */
  BooleanProperty isModifiedProperty();

  /**
   Whether the project is a demo project
   <p>
   Project cloned from demo should NOT be allowed to Push project https://www.pivotaltracker.com/story/show/187242588

   @return observable boolean property
   */
  BooleanProperty isDemoProjectProperty();

  /**
   Prompt to close a modified project
   */
  void promptToSaveChanges(Runnable afterConfirmation);

  /**
   Show a simple warning alert

   @param title  of the alert
   @param header of the alert
   @param body   of the alert
   */
  void showWarningAlert(String title, String header, String body);

  /**
   Show alert

   @param type   of alert
   @param title  of alert
   @param header of alert
   @param body   of alert
   */
  void showAlert(Alert.AlertType type, String title, String header, @Nullable String body);

  /**
   Show error dialog, which allows the user to copy and paste the error message

   @param title  of the dialog
   @param header of the dialog
   @param body   of the dialog
   */
  void showErrorDialog(String title, String header, String body);

  /**
   Show a custom confirmation dialog with Yes/No options.

   @param title   title of the dialog
   @param header  header of the dialog
   @param content content of the dialog
   @return true if the user clicked 'Yes', false otherwise
   */
  boolean showConfirmationDialog(String title, String header, String content);
}
