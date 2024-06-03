package io.xj.gui.services;

import io.xj.gui.controllers.content.common.PopupActionMenuController;
import io.xj.gui.controllers.content.common.PopupSelectorMenuController;
import io.xj.gui.types.Route;
import io.xj.gui.types.GridChoice;
import io.xj.gui.types.ProgramEditorMode;
import io.xj.gui.types.ZoomChoice;
import io.xj.gui.utils.LaunchMenuPosition;
import io.xj.hub.pojos.Instrument;
import io.xj.hub.pojos.InstrumentAudio;
import io.xj.hub.pojos.Library;
import io.xj.hub.pojos.Program;
import io.xj.hub.pojos.ProgramSequence;
import io.xj.hub.pojos.ProgramSequencePattern;
import io.xj.hub.pojos.Template;
import io.xj.engine.work.FabricationState;
import jakarta.annotation.Nullable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import org.springframework.core.io.Resource;

import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 This is an intermediary to compute the state of the UI based on the state of the application.
 <p>
 It exists to avoid circular dependency which would arise if the application services tried to sort this out themselves.
 <p>
 The GUI should use this service to determine some common states, e.g. "Should the fabrication settings appear disabled?"
 */
public interface UIStateService extends ReadyAfterBoot {
  PseudoClass ACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("active");
  PseudoClass PENDING_PSEUDO_CLASS = PseudoClass.getPseudoClass("pending");
  PseudoClass FAILED_PSEUDO_CLASS = PseudoClass.getPseudoClass("failed");
  PseudoClass OPEN_PSEUDO_CLASS = PseudoClass.getPseudoClass("open");
  PseudoClass INVALID_PSEUDO_CLASS = PseudoClass.getPseudoClass("invalid");
  Set<FabricationState> FABRICATION_PENDING_STATES = Set.of(
    FabricationState.Starting,
    FabricationState.PreparingAudio,
    FabricationState.PreparedAudio,
    FabricationState.Initializing
  );
  Set<FabricationState> FABRICATION_FAILED_STATES = Set.of(
    FabricationState.Failed
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
   @return observable boolean property of whether we are in View Fabrication Progress Status Mode
   */
  BooleanBinding isViewProgressStatusModeProperty();

  /**
   @return observable boolean property of whether we are in View Content Navigation Status Mode
   */
  BooleanBinding isViewContentNavigationStatusModeProperty();

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
   The current Navigation Route

   @return the current navigation route
   */
  ObjectProperty<Route> navStateProperty();

  /**
   Navigate to the given route@param route to navigate
   */
  void navigateTo(Route route);

  /**
   Navigate back
   */
  void navigateBack();

  /**
   Navigate back
   */
  void navigateForward();

  /**
   @return the window title
   */
  ObservableStringValue windowTitleProperty();

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
   @return the sequences of the current program
   */
  ObservableList<ProgramSequence> sequencesOfCurrentProgramProperty();

  /**
   @return Observable property for the current instrument being viewed
   */
  ObjectProperty<Instrument> currentInstrumentProperty();

  /**
   @return Observable property for the current instrumentAudio being viewed
   */
  ObjectProperty<InstrumentAudio> currentInstrumentAudioProperty();

  /**
   @return Observable property for the current template being viewed
   */
  ObjectProperty<Template> currentTemplateProperty();

  /**
   View all libraries
   */
  void viewLibraries();

  /**
   View the given library in the content browser.

   @param libraryId ID of the Library to view
   */
  void viewLibrary(UUID libraryId);

  /**
   Edit the given library in the library editor.

   @param libraryId ID of the Library to edit
   */
  void editLibrary(UUID libraryId);

  /**
   Edit the given program in the program editor.

   @param programId ID of the Program to edit
   */
  void editProgram(UUID programId);

  /**
   Edit the given instrument in the instrument editor.

   @param instrumentId ID of the Instrument to edit
   */
  void editInstrument(UUID instrumentId);

  /**
   Edit the given instrument audio in the instrument audio editor

   @param instrumentAudioId ID of the audio to edit
   */
  void editInstrumentAudio(UUID instrumentAudioId);

  /**
   Edit the given template in the template editor.

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
   @return property of the current program editor mode
   */
  ObjectProperty<ProgramEditorMode> programEditorModeProperty();

  /**
   @return property program editor current program serquence
   */
  ObjectProperty<ProgramSequence> currentProgramSequenceProperty();

  /**
   Get the base size per beat in pixels
   */
  int getProgramEditorBaseSizePerBeat();

  /**
   @return grid choices for the program editor
   */
  ObservableList<GridChoice> getProgramEditorGridChoices();

  /**
   @return zoom choices for the program editor
   */
  ObservableList<ZoomChoice> getProgramEditorZoomChoices();

  /**
   @return the property for the grid setting for the program editor
   */
  ObjectProperty<GridChoice> programEditorGridProperty();

  /**
   @return the property for the zoom setting for the program editor
   */
  ObjectProperty<ZoomChoice> programEditorZoomProperty();

  /**
   @return the property for the snap setting for the program editor
   */
  BooleanProperty programEditorSnapProperty();

  /**
   Utility to launch a menu controller
   - apply the pseudo-class :open to the button and remove it after the menu closes
   - darken the background behind the menu
   - position the menu behind the button

   @param <T>              the type of the controller
   @param fxml             comprising the menu contents
   @param launcher         that opened the menu
   @param setupController  function to set up the controller
   @param position         target location for launcher menu
   @param darkenBackground whether to darken the background while the modal is open
   @param onClose          to run after the modal is closed
   */
  <T> void launchModalMenu(
    Resource fxml,
    Node launcher,
    Consumer<T> setupController,
    LaunchMenuPosition position,
    boolean darkenBackground,
    @Nullable Runnable onClose
  );

  /**
   Launch the popup selector menu from the given button

   @param launcher        source of the popup
   @param setupController setup the selector menu controller
   */
  void launchPopupSelectorMenu(Node launcher, Consumer<PopupSelectorMenuController> setupController);

  /**
   Launch the popup action menu from the given button

   @param launcher        source of the popup
   @param setupController setup the action menu controller
   */
  void launchPopupActionMenu(Node launcher, Consumer<PopupActionMenuController> setupController);

  /**
   Launch the quick action menu from the given button

   @param launcher        source of the popup
   @param mouseEvent      source of the menu launch
   @param setupController setup the action menu controller
   */
  void launchQuickActionMenu(Node launcher, MouseEvent mouseEvent, Consumer<PopupActionMenuController> setupController);

  /**
   Draw the timeline background

   @param timeline       in which to render the background grid
   @param timelineHeight height of the timeline
   */
  void setupTimelineBackground(Pane timeline, int timelineHeight);

  /**
   Draw the timeline active region

   @param timeline in which to render the active region
   @param pattern  current program sequence pattern
   */
  void setupTimelineActiveRegion(Pane timeline, @Nullable ProgramSequencePattern pattern);
}
