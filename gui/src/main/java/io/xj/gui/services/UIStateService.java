package io.xj.gui.services;

import io.xj.gui.controllers.ReadyAfterBootController;
import io.xj.gui.modes.ContentMode;
import io.xj.gui.modes.TemplateMode;
import io.xj.gui.modes.ViewMode;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.Template;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.css.PseudoClass;

import java.util.Set;
import java.util.UUID;

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
   @return Observable property of the fabrication state text
   */
  StringBinding stateTextProperty();

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

  /**
   @return Observable property of whether the state text should be visible
   */
  BooleanBinding isStateTextVisibleProperty();

  /**
   The View Mode

   @return the project view mode
   */
  ObjectProperty<ViewMode> viewModeProperty();

  /**
   @return Observable property for whether the project is in content view mode
   */
  BooleanBinding isViewModeContentProperty();

  /**
   @return Observable property for whether the project is in fabrication view mode
   */
  BooleanBinding isViewModeFabricationProperty();

  /**
   @return the window title
   */
  ObservableStringValue windowTitleProperty();

  /**
   @return Observable property for the view content mode
   */
  ObjectProperty<ContentMode> contentModeProperty();

  /**
   @return Observable property for the view template mode
   */
  ObjectProperty<TemplateMode> templateModeProperty();

  /**
   Go up a content level in the browser
   */
  void goUpContentLevel();

  /**
   @return binding for whether it's possible to go up a content level
   */
  BooleanBinding isContentLevelUpPossibleProperty();

  /**
   @return Observable property for the current library being viewed
   */
  ObjectProperty<Library> currentLibraryProperty();

  /**
   @return Observable property for the current program being viewed
   */
  ObjectProperty<Program> currentProgramProperty();

  /**
   @return Observable property for the current instrument being viewed
   */
  ObjectProperty<Instrument> currentInstrumentProperty();

  /**
   @return Observable property for the current template being viewed
   */
  ObjectProperty<Template> currentTemplateProperty();

  /**
   View the given library in the content browser.

   @param libraryId ID of the Library to view
   */
  void viewLibrary(UUID libraryId);

  /**
   Edit the given library in the content browser.

   @param libraryId ID of the Library to edit
   */
  void editLibrary(UUID libraryId);

  /**
   Edit the given program in the content browser.

   @param programId ID of the Program to edit
   */
  void editProgram(UUID programId);

  /**
   Edit the given instrument in the content browser.

   @param instrumentId ID of the Instrument to edit
   */
  void editInstrument(UUID instrumentId);

  /**
   Edit the given template in the content browser.

   @param templateId ID of the Template to edit
   */
  void editTemplate(UUID templateId);

  /**
   @return Observable property for whether we are currently viewing an entity
   */
  BooleanBinding isViewingEntityProperty();

  /**
   @return Observable property for the name of the current library
   */
  StringBinding currentParentNameProperty();

  /**
   @return Observable property for the name of the current entity
   */
  StringBinding currentEntityNameProperty();

  /**
   @return Observable property for whether the create entity button should be visible
   */
  BooleanBinding isCreateEntityButtonVisibleProperty();

  /**
   @return Observable property for the text of the create entity button
   */
  StringBinding createEntityButtonTextProperty();
}
