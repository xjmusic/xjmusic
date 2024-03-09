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
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
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
  private final ThemeService themeService;
  private final Resource programSequencePatternEventFxml;
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
  AnchorPane trackTimelineContainer;

  @FXML
  Pane trackTimelineBackground;

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
    this.programSequencePatternEventFxml = eventFxml;
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
    trackTimelineBackground.setMinHeight(trackHeight);
    trackTimelineBackground.setMaxHeight(trackHeight);
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
    // TODO populate the timeline

    drawTimelineBackground();
  }

  /**
   Draw the timeline background
   */
  private void drawTimelineBackground() {
    // clear background items
    trackTimelineBackground.getChildren().clear();

    // if there's no sequence, don't draw the timeline
    if (uiStateService.currentProgramSequenceProperty().isNull().get()) {
      trackTimelineBackground.setMinWidth(0);
      trackTimelineBackground.setMaxWidth(0);
      return;
    }

    // variables
    int sequenceTotal = uiStateService.currentProgramSequenceProperty().get().getTotal();
    int sizePerBeat = uiStateService.getProgramEditorBaseSizePerBeat();
    double zoom = uiStateService.programEditorZoomProperty().get().value();
    double grid = uiStateService.programEditorGridProperty().get().value();

    // compute the total width
    var width = sequenceTotal * sizePerBeat * zoom;
    trackTimelineBackground.setMinWidth(width);
    trackTimelineBackground.setMaxWidth(width);

    // draw active region for the current pattern total
    if (patternId.isNotNull().get()) {
      ProgramSequencePattern pattern = projectService.getContent().getProgramSequencePattern(patternId.get()).orElseThrow(() -> new RuntimeException("Pattern not found!"));
      Rectangle rectangle = new Rectangle();
      rectangle.setWidth(sizePerBeat * zoom * pattern.getTotal());
      rectangle.setHeight(trackHeight);
      rectangle.setFill(Color.valueOf("#353535"));
      trackTimelineBackground.getChildren().add(rectangle);
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
      trackTimelineBackground.getChildren().add(gridLine);
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
    trackTimelineBackground.getChildren().add(dottedLine);
  }

/*
  TODO  Extract the numeric part from the selection
  private void updateTimelineGridProperty(String selection) {
    updateGrid(selection, programEditorController);
  }
*/

/*
  TODO update the grid
    static void updateGrid(String selection, ProgramEditorController programEditorController) {
      String[] parts = selection.split("/");
      if (parts.length > 1) {
        try {
          int baseValue = Integer.parseInt(parts[1]);
          programEditorController.getTimelineGridProperty().set(baseValue);
        } catch (NumberFormatException e) {
          System.err.println("Failed to parse grid selection: " + selection);
        }
      }
    }
*/

/*
  TODO setup track item
    try {
      FXMLLoader loader = new FXMLLoader(trackFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      addTrackButton_1.setVisible(false);
      TrackController trackController = loader.getController();
      trackController.setup(root, voice, voiceController, newTrack);
      voiceContainer.getChildren().add(root);
    } catch (IOException e) {
      log.error("Error adding Track item view! {}\n{}", e, StringUtils.formatStackTrace(e));
    }
*/

  /*
  TODO populate the timeline
    timeLineAnchorpane.getChildren().removeIf(node -> (node instanceof Line || node instanceof Rectangle));
    if (0 < programEditorController.getSequenceTotal()) {
      for (double b = 0; b <= programEditorController.getSequenceTotal(); b += ((double) 1 / programEditorController.getTimelineGridSize())) {
        double gridLineX = b * voiceController.getBaseSizePerBeat().get() * programEditorController.getZoomFactor();
        VoiceController.drawGridLines(b, gridLineX, timeLineAnchorpane, voiceController.getTimelineHeight(), voiceController.doublePropertyProperty());
      }
      greyTheActiveArea();
    }
*/

/*
  TODO add program sequence pattern event item controller
    try {
      FXMLLoader loader = new FXMLLoader(programSequencePatternEventItem.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      AnchorPane.setTopAnchor(root, 0.0);
      AnchorPane.setBottomAnchor(root, 0.0);
      ProgramSequencePatternEvent programSequencePatternEvent = new ProgramSequencePatternEvent(UUID.randomUUID(), voiceController.getProgramVoiceTrack().getProgramId(), programEditorController.getSequenceId(), voiceController.getProgramVoiceTrack().getId(), 0.125f, 0.125f, 0.125f, "X");
      EventController patternEventItemController = loader.getController();
      patternEventItemController.setup(root, timeLineAnchorpane, programSequencePatternEvent, voiceController);
      patternEventItemController.getEventPositionProperty.set(event.getX() - ((voiceController.getBaseSizePerBeat().doubleValue() * programEditorController.getZoomFactor()) +
        patternEventItemController.getEventPositionProperty.get()));
      // Add the new property item to the AnchorPane
      timeLineAnchorpane.getChildren().add(root);
      projectService.getContent().put(programSequencePatternEvent);

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
