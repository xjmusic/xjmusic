package io.xj.gui.controllers.content.program.event_edit_mode;

import io.xj.gui.controllers.content.common.PopupActionMenuController;
import io.xj.gui.types.GridChoice;
import io.xj.gui.types.ZoomChoice;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.UiUtils;
import io.xj.model.pojos.ProgramSequence;
import io.xj.model.pojos.ProgramSequencePattern;
import io.xj.model.pojos.ProgramSequencePatternEvent;
import io.xj.model.pojos.ProgramVoiceTrack;
import io.xj.model.util.StringUtils;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VoiceTrackTimelineController {
  static final Logger LOG = LoggerFactory.getLogger(VoiceTrackTimelineController.class);
  private final Collection<Runnable> unsubscriptions = new HashSet<>();
  private final ObservableList<SequencePatternEventController> eventControllers = FXCollections.observableArrayList();
  private final BooleanProperty isMousePressedInTimeline = new SimpleBooleanProperty(false);
  private final Resource eventFxml;
  private final int timelineHeight;
  private final int trackControlWidth;
  private final ApplicationContext ac;
  private final ProjectService projectService;
  private final UIStateService uiStateService;
  private final Runnable updateTrackName;
  private UUID programVoiceTrackId;
  private Runnable handleDeleteTrack;
  private Runnable handleCreateTrack;
  private ObjectProperty<UUID> patternId;

  @FXML
  HBox trackContainer;

  @FXML
  VBox trackControlContainer;

  @FXML
  AnchorPane timelineActiveRegion;

  @FXML
  Pane timelineBackground;

  @FXML
  Button trackActionLauncher;

  @FXML
  TextField trackNameField;

  @FXML
  AnchorPane trackAddContainer;

  @FXML
  Button addTrackButton;

  public VoiceTrackTimelineController(
    @Value("classpath:/views/content/program/edit_event_mode/sequence-pattern-event.fxml") Resource eventFxml,
    @Value("${programEditor.eventTimelineHeight}") int timelineHeight,
    @Value("${programEditor.trackControlWidth}") int trackControlWidth,
    ApplicationContext ac,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    this.eventFxml = eventFxml;
    this.timelineHeight = timelineHeight;
    this.trackControlWidth = trackControlWidth;
    this.ac = ac;
    this.projectService = projectService;
    this.uiStateService = uiStateService;

    updateTrackName = () -> projectService.update(ProgramVoiceTrack.class, programVoiceTrackId, "name", trackNameField.getText());
  }

  /**
   Set up the track controller

   @param programVoiceTrackId for which to set up the track
   @param handleCreateTrack   to create a new track
   @param handleDeleteTrack   to delete the track
   */
  public void setup(UUID programVoiceTrackId, Runnable handleCreateTrack, Runnable handleDeleteTrack, ObjectProperty<UUID> patternId) {
    this.programVoiceTrackId = programVoiceTrackId;
    this.handleDeleteTrack = handleDeleteTrack;
    this.handleCreateTrack = handleCreateTrack;
    this.patternId = patternId;

    ProgramVoiceTrack track = projectService.getContent().getProgramVoiceTrack(programVoiceTrackId).orElseThrow(() -> new RuntimeException("Track not found!"));

    trackContainer.setMinHeight(timelineHeight);
    trackContainer.setMaxHeight(timelineHeight);
    timelineBackground.setMinHeight(timelineHeight);
    timelineBackground.setMaxHeight(timelineHeight);
    timelineActiveRegion.setMinHeight(timelineHeight);
    timelineActiveRegion.setMaxHeight(timelineHeight);
    trackControlContainer.setMinWidth(trackControlWidth);
    trackControlContainer.setMaxWidth(trackControlWidth);

    ChangeListener<ZoomChoice> onZoomChange = (o, ov, v) -> Platform.runLater(this::setupTimeline);
    uiStateService.programEditorZoomProperty().addListener(onZoomChange);
    unsubscriptions.add(() -> uiStateService.programEditorZoomProperty().removeListener(onZoomChange));

    ChangeListener<GridChoice> onGridChange = (o, ov, v) -> Platform.runLater(this::setupTimeline);
    uiStateService.programEditorGridProperty().addListener(onGridChange);
    unsubscriptions.add(() -> uiStateService.programEditorGridProperty().removeListener(onGridChange));

    ChangeListener<ProgramSequence> onSequenceChange = (o, ov, v) -> Platform.runLater(this::setupTimeline);
    uiStateService.currentProgramSequenceProperty().addListener(onSequenceChange);
    unsubscriptions.add(() -> uiStateService.currentProgramSequenceProperty().removeListener(onSequenceChange));
    unsubscriptions.add(projectService.addProjectUpdateListener(ProgramSequence.class, this::setupTimeline));

    ChangeListener<UUID> onPatternIdChange = (o, ov, v) -> Platform.runLater(this::setupTimeline);
    patternId.addListener(onPatternIdChange);
    unsubscriptions.add(() -> patternId.removeListener(onPatternIdChange));
    unsubscriptions.add(projectService.addProjectUpdateListener(ProgramSequencePattern.class, this::setupTimeline));

    trackNameField.setText(track.getName());
    unsubscriptions.add(UiUtils.onBlur(trackNameField, updateTrackName));
    UiUtils.blurOnEnterKeyPress(trackNameField);

    unsubscriptions.add(projectService.addProjectUpdateListener(ProgramVoiceTrack.class, this::setupAddTrackButton));
    setupAddTrackButton();

    Platform.runLater(this::setupTimeline);
  }

  /**
   Teardown the track controller
   */
  public void teardown() {
    for (Runnable unsubscription : unsubscriptions) unsubscription.run();
    for (SequencePatternEventController controller : eventControllers) controller.teardown();
  }

  @FXML
  void handlePressedTrackActionLauncher() {
    uiStateService.launchPopupActionMenu(trackActionLauncher, (PopupActionMenuController controller) -> controller.setup(
        null,
        null,
        handleDeleteTrack,
        null
      )
    );
  }

  @FXML
  void handlePressedAddTrack() {
    handleCreateTrack.run();
  }

  @FXML
  void handleMousePressedTimeline(MouseEvent mouseEvent) {
    // keep track of whether a mouse press originated on the container as opposed to its children
    isMousePressedInTimeline.set(mouseEvent.getTarget() == timelineActiveRegion);
  }


  @FXML
  void handleMouseReleasedTimeline(MouseEvent mouseEvent) {
    // ignore clicks on children of the timeline container
    if (mouseEvent.getTarget() != timelineActiveRegion) return;
    if (isMousePressedInTimeline.not().get()) return;
    isMousePressedInTimeline.set(false);

    if (patternId.isNull().get()) {
      projectService.showWarningAlert("No Pattern", "Please create a pattern to add events", "You must create a pattern before adding events");
      return;
    }

    // get mouse X relative to clicked-on element
    double x = mouseEvent.getX();
    double beatWidth = uiStateService.programEditorZoomProperty().get().value() * uiStateService.getProgramEditorBaseSizePerBeat();
    double grid = uiStateService.programEditorGridProperty().get().value();
    double position = uiStateService.programEditorSnapProperty().get() ?
      grid * Math.round(x / (beatWidth * grid)) :
      x / (beatWidth);

    // if position is outside the pattern range, don't add an event
    if (position < 0 || position >= getCurrentPattern().map(ProgramSequencePattern::getTotal).map(Short::intValue).orElse(0))
      return;

    // add a new event at the clicked position
    try {
      ProgramSequencePatternEvent event = projectService.createProgramSequencePatternEvent(programVoiceTrackId, patternId.get(), position, grid);
      addEvent(event);
    } catch (Exception e) {
      LOG.error("Failed to add event to timeline! {}\n{}", e, StringUtils.formatStackTrace(e));
    }
  }

  /**
   Setup whether the add track button is visible--
   it's only visible if this track is the highest-order track for its voice
   */
  private void setupAddTrackButton() {
    Optional<ProgramVoiceTrack> track = projectService.getContent().getProgramVoiceTrack(programVoiceTrackId);
    if (track.isEmpty()) return; // track has been deleted, and this function was run as a callback from the update
    var tracksForVoice = projectService.getContent().getTracksOfVoice(track.get().getProgramVoiceId());
    float trackOrderMax = tracksForVoice.stream().map(ProgramVoiceTrack::getOrder).max(Float::compareTo).orElse(0f);
    addTrackButton.setVisible(trackOrderMax == track.get().getOrder());
  }

  /**
   Populate the track timeline
   */
  private void setupTimeline() {
    uiStateService.setupTimelineBackground(timelineBackground, timelineHeight);
    uiStateService.setupTimelineActiveRegion(timelineActiveRegion, getCurrentPattern().orElse(null));
    setupTimelineEvents();
  }

  private Optional<ProgramSequencePattern> getCurrentPattern() {
    if (patternId.isNull().get()) return Optional.empty();
    Optional<ProgramSequencePattern> pattern = projectService.getContent().getProgramSequencePattern(patternId.get());

    // In this case, the pattern has been deleted
    if (pattern.isEmpty()) patternId.set(null);

    return pattern;
  }

  /**
   Populate the track timeline with events
   */
  private void setupTimelineEvents() {
    for (SequencePatternEventController controller : eventControllers) controller.teardown();
    timelineActiveRegion.getChildren().clear();
    if (patternId.isNotNull().get())
      for (ProgramSequencePatternEvent event : projectService.getContent().getEventsOfPatternAndTrack(patternId.get(), programVoiceTrackId)) {
        addEvent(event);
      }
  }

  /**
   Add an event to the timeline
   */
  private void addEvent(ProgramSequencePatternEvent event) {
    try {
      FXMLLoader loader = new FXMLLoader(eventFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      SequencePatternEventController controller = loader.getController();
      eventControllers.add(controller);
      controller.setup(event.getId(),
        () -> {
          controller.teardown();
          eventControllers.remove(controller);
          timelineActiveRegion.getChildren().remove(root);
          projectService.deleteContent(ProgramSequencePatternEvent.class, event.getId());
        });
      timelineActiveRegion.getChildren().add(root);

    } catch (Exception e) {
      LOG.error("Failed to add event to timeline! {}\n{}", e, StringUtils.formatStackTrace(e));
    }
  }
}
