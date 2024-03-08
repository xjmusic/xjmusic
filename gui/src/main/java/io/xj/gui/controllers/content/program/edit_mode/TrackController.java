package io.xj.gui.controllers.content.program.edit_mode;

import io.xj.gui.modes.GridChoice;
import io.xj.gui.modes.ZoomChoice;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import static io.xj.gui.controllers.content.program.edit_mode.VoiceController.computeTextWidth;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TrackController {
  static final Logger LOG = LoggerFactory.getLogger(TrackController.class);
  private final Resource trackFxml;
  private final Resource trackMenuFxml;
  private final Resource programSequencePatternEventItem;
  private final ThemeService themeService;
  private final ApplicationContext ac;
  private final ProjectService projectService;
  private final UIStateService uiStateService;
  private final SimpleStringProperty trackNameProperty = new SimpleStringProperty();
  private final ChangeListener<GridChoice> onGridChange = (observable, oldValue, newValue) -> Platform.runLater(this::populateTimeline);
  private final ChangeListener<ZoomChoice> onZoomChange = (observable, oldValue, newValue) -> Platform.runLater(this::populateTimeline);
  private double defaultTrackNameFieldPrefWidth = 0;
  private ProgramVoiceTrack track;

  @FXML
  public VBox trackControlsContainer;

  @FXML
  public AnchorPane trackContainer;

  @FXML
  public Button trackMenuButton;

  @FXML
  public TextField trackNameField;

  @FXML
  public Button addTrackButton;

  @FXML
  public AnchorPane timeLineAnchorpane;


  public TrackController(
    @Value("classpath:/views/content/program/edit_mode/track.fxml") Resource trackFxml,
    @Value("classpath:/views/content/program/edit_mode/track-menu.fxml") Resource trackMenuFxml,
    @Value("classpath:/views/content/program/edit_mode/program-sequence-pattern-event-item.fxml") Resource programSequencePatternEventItem,
    ApplicationContext ac,
    ThemeService themeService,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    this.trackFxml = trackFxml;
    this.trackMenuFxml = trackMenuFxml;
    this.programSequencePatternEventItem = programSequencePatternEventItem;
    this.ac = ac;
    this.themeService = themeService;
    this.projectService = projectService;
    this.uiStateService = uiStateService;
  }

  public void setup(ProgramVoiceTrack track) {
    defaultTrackNameFieldPrefWidth = trackNameField.getPrefWidth();
    this.track = track;
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

    timeLineAnchorpane.setOnMouseClicked(this::addProgramSequencePatternEventItemController);
    timeLineAnchorpane.setCursor(Cursor.CROSSHAIR);
    uiStateService.programEditorZoomProperty().addListener(onZoomChange);
    uiStateService.programEditorGridProperty().addListener(onGridChange);
    Platform.runLater(this::populateTimeline);
  }

  // TODO make sure this is called when the track is removed from the stage
  public void teardown() {
    uiStateService.programEditorZoomProperty().removeListener(onZoomChange);
    uiStateService.programEditorGridProperty().removeListener(onGridChange);
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

  private void adjustWidthWithTextIncrease() {
    double newTextWidth = computeTextWidth(trackNameField.getFont(), trackNameField.getText());
    if (newTextWidth > defaultTrackNameFieldPrefWidth) {
      trackNameField.setPrefWidth(newTextWidth);
      trackControlsContainer.setPrefWidth(newTextWidth + 10);
    }
  }

  @FXML
  protected void handlePressedNewTrack() {
/*
  todo track controller create new track
    try {
      ProgramVoiceTrack newTrack = new ProgramVoiceTrack(UUID.randomUUID(), programEditorController.getProgramId(), voice.getId(), "XXX", 1f);
      projectService.update(newTrack);
      trackItem(trackFxml, ac, voiceController.voiceContainer, LOG, addTrackButton_1, voice, voiceController, newTrack);
    } catch (Exception e) {
      LOG.info("Could not create new Track");
    }
*/
  }

  static void trackItem(Resource trackFxml, ApplicationContext ac, VBox voiceContainer, Logger log, Button addTrackButton_1, ProgramVoice voice, VoiceController voiceController, ProgramVoiceTrack newTrack) {
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
  }

  @FXML
  protected void handlePressedTrackMenu() {
    // todo whatever the purpose of reaching inside the voice controller here, don't do it
    // VoiceController.trackMenu(event, trackMenuFxml, ac, themeService, LOG, voice, voiceController,track, false,trackRoot,addTrackButton_1);
  }

  private void populateTimeline() {
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
  }

  private void greyTheActiveArea() {
/*
  TODO grey the active area
    Rectangle rectangle = new Rectangle();
    //bind the three properties to the width of the rectangle highlighting the active area
    rectangle.widthProperty().bind(
      Bindings.multiply(
        Bindings.multiply(
          voiceController.getBaseSizePerBeat(),
          voiceController.getTotalFloatValue()
        ),
        programEditorController.getZoomFactor()
      )
    );


    rectangle.setHeight(voiceController.getTimelineHeight());
    rectangle.setFill(Color.valueOf("#252525"));
    rectangle.setOpacity(.7);
    timeLineAnchorpane.getChildren().add(0, rectangle);
*/
  }

  private void addProgramSequencePatternEventItemController(MouseEvent event) {
/*
  TODO add program sequence pattern event item controller
    try {
      FXMLLoader loader = new FXMLLoader(programSequencePatternEventItem.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      AnchorPane.setTopAnchor(root, 0.0);
      AnchorPane.setBottomAnchor(root, 0.0);
      ProgramSequencePatternEvent programSequencePatternEvent = new ProgramSequencePatternEvent(UUID.randomUUID(), voiceController.getProgramVoiceTrack().getProgramId(), programEditorController.getSequenceId(), voiceController.getProgramVoiceTrack().getId(), 0.125f, 0.125f, 0.125f, "X");
      ProgramSequencePatternEventItemController patternEventItemController = loader.getController();
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
  }

}
