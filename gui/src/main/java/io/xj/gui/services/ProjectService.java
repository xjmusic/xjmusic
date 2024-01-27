package io.xj.gui.services;

import io.xj.hub.HubContent;
import io.xj.hub.enums.ContentBindingType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.Project;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.nexus.project.ProjectState;
import io.xj.nexus.project.ProjectUpdate;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableListValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableStringValue;

import java.util.List;
import java.util.UUID;

public interface ProjectService {
  /**
   Close the project

   @return true if the project was closed, or false if the user cancelled the close (maybe because the project has unsaved modifications)
   */
  boolean closeProject();

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
   @return Get the project content
   */
  HubContent getContent();

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
   Notify all listeners of a project update@param type the type of update

   @param modified
   */
  void didUpdate(ProjectUpdate type, boolean modified);

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
   Delete the template by id

   @param template the template
   */
  void deleteTemplate(Template template);

  /**
   Delete the template binding by id

   @param binding the template binding
   */
  void deleteTemplateBinding(TemplateBinding binding);

  /**
   Delete the library by id

   @param library the library
   */
  void deleteLibrary(Library library);

  /**
   Delete the program by id

   @param program the program
   */
  void deleteProgram(Program program);

  /**
   Delete the instrument by id

   @param instrument the instrument
   */
  void deleteInstrument(Instrument instrument);

  /**
   Create a new template

   @param name of the new template
   @return the new template
   */
  Template createTemplate(String name) throws Exception;

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
   Create a new instrument

   @param library the library
   @param name    of the new instrument
   @return the new instrument
   */
  Instrument createInstrument(Library library, String name) throws Exception;

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
   @return the new template
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
   @param name   the new name
   @return the new program
   */
  ProgramSequence cloneProgramSequence(UUID fromId, String name) throws Exception;

  /**
   Clone the given program sequence pattern with a new name

   @param fromId clone from program sequence pattern
   @param name   the new name
   @return the new program sequence pattern
   */
  ProgramSequencePattern cloneProgramSequencePattern(UUID fromId, String name) throws Exception;

  /**
   Clone the given instrument with a new name

   @param fromId    clone from instrument
   @param libraryId the new library id
   @param name      the new name
   @return the new instrument
   */
  Instrument cloneInstrument(UUID fromId, UUID libraryId, String name) throws Exception;

  /**
   Update the given library

   @param library to update
   @return true if successful
   */
  boolean updateLibrary(Library library);

  /**
   Update the given program

   @param program to update
   @return true if successful
   */
  boolean updateProgram(Program program);

  /**
   Update the given instrument

   @param instrument to update
   @return true if successful
   */
  boolean updateInstrument(Instrument instrument);

  /**
   Update the given template

   @param template to update
   @return true if successful
   */
  boolean updateTemplate(Template template);

  /**
   Create a new template binding

   @param templateId         template id
   @param contentBindingType content binding type
   @param targetId           target id
   */
  void addTemplateBinding(UUID templateId, ContentBindingType contentBindingType, UUID targetId);

  /**
   Whether the project has been modified since loading content

   @return observable boolean property
   */
  BooleanProperty isModifiedProperty();

  /**
   Prompt to close a modified project

   @return true if user approves request to close project
   */
  boolean confirmCloseIfModified();
}
