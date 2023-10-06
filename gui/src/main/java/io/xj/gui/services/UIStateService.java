package io.xj.gui.services;

import io.xj.lib.entity.EntityStore;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableValue;

/**
 This is an intermediary to compute the state of the UI based on the state of the application.
 <p>
 It exists to avoid circular dependency which would arise if the application services tried to sort this out themselves.
 <p>
 The GUI should use this service to determine some common states, e.g. "Should the fabrication settings appear disabled?"
 */
public interface UIStateService {
  /**
   Observable property of whether the fabrication settings should appear disabled

   @return property that is true when the settings should appear disabled
   */
  BooleanBinding fabricationSettingsDisabledProperty();

  /**
   Observable property of whether the fabrication action should appear disabled

   @return property that is true when the fabrication action should appear disabled
   */
  BooleanBinding fabricationActionDisabledProperty();

  /**
   Observable property of the fabrication status text

   @return fabrication status text
   */
  StringBinding fabricationStatusTextProperty();

  /**
   Observable property of whether fabrication is active in file output mode

   @return observable property
   */
  BooleanBinding isFileOutputActiveProperty();
}
