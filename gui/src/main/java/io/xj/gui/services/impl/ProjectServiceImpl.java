package io.xj.gui.services.impl;

import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ProjectViewMode;
import io.xj.hub.tables.pojos.Project;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.prefs.Preferences;

@Service
public class ProjectServiceImpl implements ProjectService {
  static final Logger LOG = LoggerFactory.getLogger(ProjectServiceImpl.class);
  private static final String defaultPathPrefix = System.getProperty("user.home") + File.separator + "Documents";
  private final Preferences prefs = Preferences.userNodeForPackage(ProjectServiceImpl.class);
  private final ObjectProperty<ProjectViewMode> viewMode = new SimpleObjectProperty<>(ProjectViewMode.CONTENT);
  private final ObjectProperty<Project> currentProject = new SimpleObjectProperty<>();
  private final StringProperty pathPrefix = new SimpleStringProperty();

  public ProjectServiceImpl(
  ) {
    attachPreferenceListeners();
    setAllFromPreferencesOrDefaults();
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
    // todo implement projectService.openProject()
  }

  @Override
  public void createProject(String pathPrefix, String name) {
    LOG.info("Creating project {} at {}", name, pathPrefix);
    // todo implement projectService.newProject()
  }

  @Override
  public void cloneProject() {
    // todo implement projectService.cloneProjectFromLab()
  }

  @Override
  public StringProperty pathPrefixProperty() {
    return pathPrefix;
  }

  /**
   Attach preference listeners.
   */
  private void attachPreferenceListeners() {
    pathPrefix.addListener((o, ov, value) -> prefs.put("pathPrefix", value));
  }

  /**
   Set all properties from preferences, else defaults.
   */
  private void setAllFromPreferencesOrDefaults() {
    pathPrefix.set(prefs.get("pathPrefix", defaultPathPrefix));
  }

}
