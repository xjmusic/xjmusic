package io.xj.gui.controllers.content.program.edit_mode;

import io.xj.gui.controllers.content.common.PopupActionMenuController;
import io.xj.gui.modes.GridChoice;
import io.xj.gui.modes.ZoomChoice;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.UiUtils;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import io.xj.hub.util.StringUtils;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
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
import javafx.scene.shape.Rectangle;
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
import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TrackController {
  static final Logger LOG = LoggerFactory.getLogger(TrackController.class);
  private final Collection<Runnable> unsubscriptions = new HashSet<>();
  private final ObservableList<EventController> eventControllers = FXCollections.observableArrayList();
  private final ThemeService themeService;
  private final Resource eventFxml;
  private final Resource popupActionMenuFxml;
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
  Button trackActionLauncher;

  @FXML
  TextField trackNameField;

  @FXML
  AnchorPane trackAddContainer;

  @FXML
  Button addTrackButton;

  public TrackController(
    @Value("classpath:/views/content/program/edit_mode/event.fxml") Resource eventFxml,
    @Value("classpath:/views/content/common/popup-action-menu.fxml") Resource popupActionMenuFxml,
    @Value("${programEditor.trackHeight}") int trackHeight,
    @Value("${programEditor.trackControlWidth}") int trackControlWidth,
    ApplicationContext ac,
    ThemeService themeService,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    this.eventFxml = eventFxml;
    this.popupActionMenuFxml = popupActionMenuFxml;
    this.trackHeight = trackHeight;
    this.trackControlWidth = trackControlWidth;
    this.ac = ac;
    this.themeService = themeService;
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
    for (EventController controller : eventControllers) controller.teardown();
  }

  @FXML
  void handlePressedTrackActionLauncher() {
    UiUtils.launchModalMenu(trackActionLauncher, popupActionMenuFxml, ac, themeService.getMainScene().getWindow(),
      true, (PopupActionMenuController controller) -> controller.setup(
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
  void handlePressedTimeline(MouseEvent mouseEvent) {
    if (patternId.isNull().get()) {
      projectService.showWarningAlert("No Pattern", "Please create a pattern to add events", "You must create a pattern before adding events");
      return;
    }

    // get mouse X relative to clicked-on element
    double x = mouseEvent.getX();
    double sizePerBeat = uiStateService.getProgramEditorBaseSizePerBeat();
    double zoom = uiStateService.programEditorZoomProperty().get().value();
    double grid = uiStateService.programEditorGridProperty().get().value();
    double position = uiStateService.programEditorSnapProperty().get() ?
      grid * Math.round(x / (sizePerBeat * zoom * grid)) :
      x / (sizePerBeat * zoom);

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
    ProgramVoiceTrack track = projectService.getContent().getProgramVoiceTrack(programVoiceTrackId).orElseThrow(() -> new RuntimeException("Track not found!"));
    var tracksForVoice = projectService.getContent().getTracksOfVoice(track.getProgramVoiceId());
    float trackOrderMax = tracksForVoice.stream().map(ProgramVoiceTrack::getOrder).max(Float::compareTo).orElse(0f);
    addTrackButton.setVisible(trackOrderMax == track.getOrder());
  }

  /**
   Populate the track timeline
   */
  private void setupTimeline() {
    setupTimelineBackground();
    setupTimelineEvents();
  }

  /**
   Draw the timeline background
   */
  private void setupTimelineBackground() {
    // clear background items
    timelineBackground.getChildren().clear();

    // if there's no sequence, don't draw the timeline
    if (uiStateService.currentProgramSequenceProperty().isNull().get()) {
      timelineBackground.setMinWidth(0);
      timelineBackground.setMaxWidth(0);
      return;
    }

    // variables
    int sequenceTotal = uiStateService.currentProgramSequenceProperty().get().getTotal();
    int sizePerBeat = uiStateService.getProgramEditorBaseSizePerBeat();
    double zoom = uiStateService.programEditorZoomProperty().get().value();
    double grid = uiStateService.programEditorGridProperty().get().value();

    // compute the total width
    var width = sequenceTotal * sizePerBeat * zoom;
    timelineBackground.setMinWidth(width);
    timelineBackground.setMaxWidth(width);

    // draw active region for the current pattern total
    if (patternId.isNotNull().get()) {
      ProgramSequencePattern pattern = projectService.getContent().getProgramSequencePattern(patternId.get()).orElseThrow(() -> new RuntimeException("Pattern not found!"));
      Rectangle rectangle = new Rectangle();
      rectangle.setWidth(sizePerBeat * zoom * pattern.getTotal());
      rectangle.setHeight(trackHeight);
      rectangle.setFill(Color.valueOf("#353535"));
      timelineBackground.getChildren().add(rectangle);
    }

    // draw vertical grid lines
    double x;
    for (double b = 0; b < sequenceTotal; b += grid) {
      x = b * sizePerBeat * zoom;
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
      AnchorPane.setTopAnchor(root, 0.0);
      AnchorPane.setBottomAnchor(root, 0.0);
      EventController controller = loader.getController();
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

/*
  TODO  Extract the numeric part from the selection
  private void updateTimelineGridProperty(String selection) {
    updateGrid(selection, programEditorController);
  }
*/
  
/*
  TODO add program sequence pattern event item controller
    try {

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
*/

  /*
  TODO if any of this is useful
    defaultTrackNameFieldPrefWidth = trackNameField.getPrefWidth();
    trackNameField.setText(track.getName());

    trackNameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
      try {
        if (!newValue) {
          adjustWidthWithTextIncrease();
          trackNameProperty.set(trackNameField.getText());
          projectService.update(ProgramVoiceTrack.class, track.getId(), "name", trackNameProperty.get());
        }
      } catch (Exception e) {
        LOG.info("Failed to update ProgramVoiceTrack name");
      }
    });
*/

/*
  TODO track controller add a listener to the selected item property
    programEditorController.gridChooser.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        // Parse the new value to update the IntegerProperty
        updateTimelineGridProperty(newValue);
        Platform.runLater(this::populateTimeline);
      }
    });
*/

/*
  TODO track controller add a listener to the selected item property
    voiceController.patternTotalCountChooser.focusedProperty().addListener((observable, oldValue, newValue) -> {
      try {
        if (!newValue) {
          voiceController.getTotalValueFactory().setValue(voiceController.patternTotalCountChooser.getValue());
          projectService.update(ProgramSequencePattern.class, voiceController.getSelectedProgramSequencePattern().getId(), "total",
            voiceController.getTotalValueFactory().getValue());
          Platform.runLater(this::populateTimeline);
        }
      } catch (Exception e) {
        LOG.info("Failed to update ProgramSequencePattern total");
      }
    });
*/

/*
  TODO track controller add a listener to the selected item property
    // Add a listener to the selected item property
    programEditorController.sequenceTotalChooser.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        programEditorController.setSequenceTotal(programEditorController.sequenceTotalChooser.getValue());
        Platform.runLater(this::populateTimeline);
      }
    });
*/

/*
  todo something like this
    timeLineAnchorpane.setOnMouseClicked(this::addProgramSequencePatternEventItemController);
    timeLineAnchorpane.setCursor(Cursor.CROSSHAIR);
*/

}
