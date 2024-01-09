package io.xj.gui.services;

import io.xj.gui.controllers.ReadyAfterBootController;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;

/**
 This is an intermediary to compute the state of the UI based on the state of the application.
 <p>
 It exists to avoid circular dependency which would arise if the application services tried to sort this out themselves.
 <p>
 The GUI should use this service to determine some common states, e.g. "Should the fabrication settings appear disabled?"
 */
public interface UIStateService extends ReadyAfterBootController {

  /**
   @return Observable property of whether the logs should be tailing
   */
  BooleanProperty logsTailingProperty();

  /**
   @return Observable property of whether the logs are visible
   */
  BooleanProperty logsVisibleProperty();

  /**
   @return Observable property of whether the fabrication settings should appear disabled
   */
  ObservableBooleanValue isFabricationSettingsDisabledProperty();

  /**
   @return Observable property of the fabrication status text
   */
  ObservableStringValue fabricationStatusTextProperty();

  /**
   @return Observable property of whether the progress bar should be visible
   */
  ObservableBooleanValue isProgressBarVisibleProperty();

  /**
   @return Observable/settable property of the log level
   */
  StringProperty logLevelProperty();

  /**
   @return Observable property of whether the fabrication input mode should appear disabled
   */
  ObservableBooleanValue isInputModeDisabledProperty();

  /**
   @return Observable property of whether the fabrication is in manual mode;
   */
  ObservableBooleanValue isManualFabricationModeProperty();

  /**
   @return Observable property of whether the fabrication is active and in manual mode;
   */
  ObservableBooleanValue isManualFabricationActiveProperty();

  ObservableBooleanValue isFabricationDisabledProperty();
}
