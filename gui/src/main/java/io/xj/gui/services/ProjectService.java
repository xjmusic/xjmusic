package io.xj.gui.services;

import javafx.beans.property.ObjectProperty;

public interface ProjectService {
  /**
   The Project View Mode

   @return the project view mode
   */
  ObjectProperty<ProjectViewMode> viewModeProperty();
}
