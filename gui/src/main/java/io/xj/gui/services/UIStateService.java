package io.xj.gui.services;

import io.xj.gui.controllers.ReadyAfterBootController;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;

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
   Observable property of whether the fabrication action should appear disabled

   @return observable true when the fabrication action should appear disabled
   */
  BooleanBinding isFabricationActionDisabledProperty();

  /**
   Observable property of whether the fabrication settings should appear disabled

   @return observable true when the settings should appear disabled
   */
  BooleanBinding isFabricationSettingsDisabledProperty();

  /**
   Observable property of the fabrication status text

   @return observable fabrication status text
   */
  StringBinding fabricationStatusTextProperty();

  /**
   Observable property of the fabrication progress

   @return observable fabrication progress
   */
  DoubleBinding fabricationProgressProperty();

  /**
   Observable property of whether the progress bar should be visible

   @return observable true if the progress bar should be visible
   */
  BooleanBinding isProgressBarVisibleProperty();

  /**
   Observable property of whether fabrication is active in file output mode

   @return observable true if active in file output mode
   */
  BooleanBinding isFileOutputActiveProperty();

  /**
   Observable/settable property of the log level

   @return observable/settable log level
   */
  StringProperty logLevelProperty();

  /**
   @return Observable property of whether the fabrication input mode should appear disabled
   */
  BooleanBinding isInputModeDisabledProperty();

  /**
   @return Observable property of whether the fabrication output file mode should appear disabled
   */
  BooleanBinding isOutputFileModeDisabledProperty();
}
