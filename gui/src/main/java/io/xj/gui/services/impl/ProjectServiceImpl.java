package io.xj.gui.services.impl;

import io.xj.gui.services.LabService;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ProjectViewMode;
import io.xj.hub.tables.pojos.Project;
import io.xj.nexus.project.ProjectManager;
import io.xj.nexus.project.ProjectManagerImpl;
import io.xj.nexus.project.ProjectState;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.UUID;
import java.util.prefs.Preferences;

@Service
public class ProjectServiceImpl implements ProjectService {
  static final Logger LOG = LoggerFactory.getLogger(ProjectServiceImpl.class);
  private static final String defaultPathPrefix = System.getProperty("user.home") + File.separator + "Documents";
  private final Preferences prefs = Preferences.userNodeForPackage(ProjectServiceImpl.class);
  private final ObjectProperty<ProjectViewMode> viewMode = new SimpleObjectProperty<>(ProjectViewMode.CONTENT);
  private final ObjectProperty<Project> currentProject = new SimpleObjectProperty<>();
  private final StringProperty basePathPrefix = new SimpleStringProperty();
  private final DoubleProperty progress = new SimpleDoubleProperty();
  private final ObjectProperty<ProjectState> state = new SimpleObjectProperty<>(ProjectState.Standby);
  private final LabService labService;
  private final ProjectManager projectManager;

  public ProjectServiceImpl(
    LabService labService
  ) {
    this.labService = labService;
    attachPreferenceListeners();
    setAllFromPreferencesOrDefaults();
    this.projectManager = new ProjectManagerImpl(
      this.progress::set,
      this.state::set
    );
  }

  @Override
  public ObjectProperty<ProjectViewMode> viewModeProperty() {
    return viewMode;
  }

  @Override
  public ObjectProperty<Project> currentProjectProperty() {
    return currentProject;
  }

  @Override
  public void openProject(String path) {
    LOG.info("Opening project at {}", path);
    // TODO implement projectService.openProject()
  }

  @Override
  public void createProject(String pathPrefix, String name) {
    LOG.info("Creating project {} at {}", name, pathPrefix);
    // TODO implement projectService.newProject()
  }

  @Override
  public void cloneFromLabProject(String pathPrefix, UUID projectId, String name) {
    LOG.info("Cloning from lab project {} ({}) at {}", name, projectId, pathPrefix);
    // TODO implement projectService.cloneProjectFromLab()
  }

  @Override
  public void cloneFromDemoTemplate(String pathPrefix, String templateShipKey, String name) {
    LOG.info("Will clone from demo template \"{}\" ({}) at {}", name, templateShipKey, pathPrefix);
    projectManager.setPathPrefix(pathPrefix + File.separator + name + File.separator);
    projectManager.setAudioBaseUrl(labService.hubConfigProperty().get().getAudioBaseUrl());
    Platform.runLater(() -> projectManager.cloneFromDemoTemplate(templateShipKey, name));
  }

  @Override
  public void saveProject() {
    LOG.info("Saving project");
    // TODO implement projectService.saveProject()
  }

  @Override
  public StringProperty basePathPrefixProperty() {
    return basePathPrefix;
  }

  @Override
  public DoubleProperty progressProperty() {
    return progress;
  }

  @Override
  public ObjectProperty<ProjectState> stateProperty() {
    return state;
  }

  /**
   Attach preference listeners.
   */
  private void attachPreferenceListeners() {
    basePathPrefix.addListener((o, ov, value) -> prefs.put("pathPrefix", value));
  }

  /**
   Set all properties from preferences, else defaults.
   */
  private void setAllFromPreferencesOrDefaults() {
    basePathPrefix.set(prefs.get("pathPrefix", defaultPathPrefix));
  }
}
