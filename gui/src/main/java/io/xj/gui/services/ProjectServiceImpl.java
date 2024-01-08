package io.xj.gui.services;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl implements ProjectService {
  private static final Logger LOG = LoggerFactory.getLogger(ProjectServiceImpl.class);
  private final ObjectProperty<ProjectViewMode> viewMode = new SimpleObjectProperty<>(ProjectViewMode.CONTENT);

  public ProjectServiceImpl() {
  }

  @Override
  public ObjectProperty<ProjectViewMode> viewModeProperty() {
    return viewMode;
  }
}
