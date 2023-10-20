package io.xj.gui.services;

import io.xj.gui.controllers.ReadyAfterBootController;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

/**
 This is an intermediary to compute the state of the UI based on the state of the application.
 <p>
 It exists to avoid circular dependency which would arise if the application services tried to sort this out themselves.
 <p>
 The GUI should use this service to determine some common states, e.g. "Should the fabrication settings appear disabled?"
 */
public interface UIStateService extends ReadyAfterBootController {
  /**
   @return Observable property of whether the fabrication action should appear disabled
   */
  BooleanBinding fabricationActionDisabledProperty();

  /**
   @return Observable property of whether the fabrication settings should appear disabled
   */
  BooleanBinding fabricationSettingsDisabledProperty();

  /**
   @return Observable property of the fabrication status text
   */
  StringBinding fabricationStatusTextProperty();

  /**
   @return Observable property of the fabrication progress
   */
  DoubleBinding fabricationProgressProperty();

  /**
   @return Observable property of whether the progress bar should be visible
   */
  BooleanBinding fabricationProgressBarVisibleProperty();

  /**
   @return Observable property of whether fabrication is active in file output mode
   */
  BooleanBinding fileOutputActiveProperty();

  /**
   @return Observable/settable property of the log level
   */
  StringProperty logLevelProperty();

  /**
   @return Observable property of whether the fabrication input mode should appear disabled
   */
  BooleanBinding fabricationInputModeDisabledProperty();

  /**
   @return Observable property of whether the fabrication output file mode should appear disabled
   */
  BooleanBinding fabricationOutputFileModeDisabledProperty();
}
