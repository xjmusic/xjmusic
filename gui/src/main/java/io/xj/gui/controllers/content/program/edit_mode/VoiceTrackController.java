package io.xj.gui.controllers.content.program.edit_mode;

import io.xj.gui.controllers.content.common.PopupActionMenuController;
import io.xj.gui.modes.GridChoice;
import io.xj.gui.modes.ZoomChoice;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.UiUtils;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import io.xj.hub.util.StringUtils;
import jakarta.annotation.Nullable;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VoiceTrackController {
  static final Logger LOG = LoggerFactory.getLogger(VoiceTrackController.class);
  private final Collection<Runnable> unsubscriptions = new HashSet<>();
  private final ObservableList<VoiceTrackEventController> eventControllers = FXCollections.observableArrayList();
  private final BooleanProperty isMousePressedInTimeline = new SimpleBooleanProperty(false);
  private final Resource eventFxml;
  private final int trackHeight;
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
  AnchorPane timelineEventsContainer;

  @FXML
  Pane timelineBackground;

  @FXML
  Pane timelineActiveRegion;

  @FXML
  Button trackActionLauncher;

  @FXML
  TextField trackNameField;

  @FXML
  AnchorPane trackAddContainer;

  @FXML
  Button addTrackButton;

  public VoiceTrackController(
    @Value("classpath:/views/content/program/edit_mode/voice-track-event.fxml") Resource eventFxml,
    @Value("${programEditor.trackHeight}") int trackHeight,
    @Value("${programEditor.trackControlWidth}") int trackControlWidth,
    ApplicationContext ac,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    this.eventFxml = eventFxml;
    this.trackHeight = trackHeight;
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

    trackContainer.setMinHeight(trackHeight);
    trackContainer.setMaxHeight(trackHeight);
    timelineBackground.setMinHeight(trackHeight);
    timelineBackground.setMaxHeight(trackHeight);
    timelineActiveRegion.setMinHeight(trackHeight);
    timelineActiveRegion.setMaxHeight(trackHeight);
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
    for (VoiceTrackEventController controller : eventControllers) controller.teardown();
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
    setupTimelineBackground(getCurrentPattern().orElse(null));
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
   Draw the timeline background
   */
  private void setupTimelineBackground(@Nullable ProgramSequencePattern pattern) {
    // clear background items
    timelineBackground.getChildren().clear();

    // if there's no sequence, don't draw the timeline
    if (uiStateService.currentProgramSequenceProperty().isNull().get()) {
      timelineBackground.setMinWidth(0);
      timelineBackground.setMaxWidth(0);
      timelineActiveRegion.setMinWidth(0);
      timelineActiveRegion.setMaxWidth(0);
      return;
    }

    // variables
    int sequenceTotal = uiStateService.currentProgramSequenceProperty().get().getTotal();
    double beatWidth = uiStateService.getProgramEditorBaseSizePerBeat() * uiStateService.programEditorZoomProperty().get().value();
    double grid = uiStateService.programEditorGridProperty().get().value();

    // compute the total width
    var width = sequenceTotal * beatWidth;
    timelineBackground.setMinWidth(width);
    timelineBackground.setMaxWidth(width);

    // draw active region for the current pattern total
    if (Objects.nonNull(pattern)) {
      timelineActiveRegion.setMinWidth(beatWidth * pattern.getTotal());
      timelineActiveRegion.setMaxWidth(beatWidth * pattern.getTotal());
    } else {
      timelineActiveRegion.setMinWidth(0);
      timelineActiveRegion.setMaxWidth(0);
    }

    // draw vertical grid lines
    double x;
    for (double b = 0; b < sequenceTotal; b += grid) {
      x = b * beatWidth;
      Line gridLine = new Line();
      gridLine.setStroke(b % 1 == 0 ? Color.valueOf("#505050") : Color.valueOf("#3d3d3d"));
      gridLine.setStrokeWidth(2);
      gridLine.setStartX(x);
      gridLine.setStartY(1);
      gridLine.setEndX(x);
      gridLine.setEndY(trackHeight - 1);
      timelineBackground.getChildren().add(gridLine);
    }

    // draw horizontal dotted line from x=0 to x=width at y = trackHeight /2
    Line dottedLine = new Line();
    dottedLine.setStroke(Color.valueOf("#585858"));
    dottedLine.setStrokeWidth(2);
    dottedLine.setStartX(1);
    dottedLine.setStartY(trackHeight / 2.0);
    dottedLine.setEndX(width - 1);
    dottedLine.setEndY(trackHeight / 2.0);
    dottedLine.getStrokeDashArray().addAll(2d, 4d);
    timelineBackground.getChildren().add(dottedLine);
  }

  /**
   Populate the track timeline with events
   */
  private void setupTimelineEvents() {
    timelineEventsContainer.getChildren().clear();
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
      VoiceTrackEventController controller = loader.getController();
      eventControllers.add(controller);
      controller.setup(event.getId(),
        () -> {
          controller.teardown();
          eventControllers.remove(controller);
          timelineEventsContainer.getChildren().remove(root);
          projectService.deleteContent(ProgramSequencePatternEvent.class, event.getId());
        });
      timelineEventsContainer.getChildren().add(root);

    } catch (Exception e) {
      LOG.error("Failed to add event to timeline! {}\n{}", e, StringUtils.formatStackTrace(e));
    }
  }
}
