package io.xj.gui.services;

import io.xj.gui.controllers.ReadyAfterBootController;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;

import java.util.Set;

/**
 This is an intermediary to compute the state of the UI based on the state of the application.
 <p>
 It exists to avoid circular dependency which would arise if the application services tried to sort this out themselves.
 <p>
 The GUI should use this service to determine some common states, e.g. "Should the fabrication settings appear disabled?"
 */
public interface UIStateService extends ReadyAfterBootController {
  PseudoClass ACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("active");
  PseudoClass PENDING_PSEUDO_CLASS = PseudoClass.getPseudoClass("pending");
  PseudoClass FAILED_PSEUDO_CLASS = PseudoClass.getPseudoClass("failed");
  Set<LabState> LAB_PENDING_STATES = Set.of(
    LabState.Connecting,
    LabState.Configuring
  );
  Set<LabState> LAB_FAILED_STATES = Set.of(
    LabState.Unauthorized,
    LabState.Failed
  );

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
  BooleanBinding isFabricationSettingsDisabledProperty();

  /**
   @return Observable property of the fabrication status text
   */
  StringBinding statusTextProperty();

  /**
   @return Observable property of whether the progress bar should be visible
   */
  BooleanBinding isProgressBarVisibleProperty();

  /**
   @return Observable property of the progress
   */
  DoubleBinding progressProperty();

  /**
   @return Observable/settable property of the log level
   */
  StringProperty logLevelProperty();

  /**
   @return Observable property of whether the fabrication is in manual mode;
   */
  BooleanBinding isManualFabricationModeProperty();

  /**
   @return Observable property of whether the fabrication is active and in manual mode;
   */
  BooleanBinding isManualFabricationActiveProperty();

  /**
   @return Observable property of whether a project is currently open
   */
  BooleanBinding hasCurrentProjectProperty();

  /**
   @return Observable property of whether the main action button should appear disabled
   */
  BooleanBinding isMainActionButtonDisabledProperty();
}
