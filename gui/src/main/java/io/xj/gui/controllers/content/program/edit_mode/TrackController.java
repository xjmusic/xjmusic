package io.xj.gui.controllers.content.program.edit_mode;

import io.xj.gui.controllers.content.common.PopupActionMenuController;
import io.xj.gui.modes.GridChoice;
import io.xj.gui.modes.ZoomChoice;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.UiUtils;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
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
  private final ChangeListener<GridChoice> onGridChange;
  private final ChangeListener<ZoomChoice> onZoomChange;
  private final Runnable updateTrackName;
  private UUID programVoiceTrackId;
  private Runnable handleDeleteTrack;
  private Runnable handleCreateTrack;

  @FXML
  HBox trackContainer;

  @FXML
  VBox trackControlContainer;

  @FXML
  AnchorPane trackTimelineContainer;

  @FXML
  Button trackActionLauncher;

  @FXML
  TextField trackNameField;

  @FXML
  AnchorPane trackAddContainer;

  @FXML
  Button addTrackButton;

  public TrackController(
    @Value("classpath:/views/content/program/edit_mode/program-sequence-pattern-event.fxml") Resource programSequencePatternEventFxml,
    @Value("classpath:/views/content/common/popup-action-menu.fxml") Resource popupActionMenuFxml,
    @Value("${programEditor.trackHeight}") int trackHeight,
    @Value("${programEditor.trackControlWidth}") int trackControlWidth,
    ApplicationContext ac,
    ThemeService themeService,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    this.programSequencePatternEventFxml = programSequencePatternEventFxml;
    this.popupActionMenuFxml = popupActionMenuFxml;
    this.trackHeight = trackHeight;
    this.trackControlWidth = trackControlWidth;
    this.ac = ac;
    this.themeService = themeService;
    this.projectService = projectService;
    this.uiStateService = uiStateService;

    onGridChange = (observable, oldValue, newValue) -> Platform.runLater(this::populateTimeline);
    onZoomChange = (observable, oldValue, newValue) -> Platform.runLater(this::populateTimeline);

    updateTrackName = () -> projectService.update(ProgramVoiceTrack.class, programVoiceTrackId, "name", trackNameField.getText());
  }

  public void setup(UUID programVoiceTrackId, Runnable handleCreateTrack, Runnable handleDeleteTrack) {
    this.programVoiceTrackId = programVoiceTrackId;
    this.handleDeleteTrack = handleDeleteTrack;
    this.handleCreateTrack = handleCreateTrack;

    ProgramVoiceTrack track = projectService.getContent().getProgramVoiceTrack(programVoiceTrackId).orElseThrow(() -> new RuntimeException("Track not found!"));

    trackContainer.setMinHeight(trackHeight);
    trackContainer.setMaxHeight(trackHeight);

    trackControlContainer.setMinWidth(trackControlWidth);
    trackControlContainer.setMaxWidth(trackControlWidth);

    uiStateService.programEditorZoomProperty().addListener(onZoomChange);
    uiStateService.programEditorGridProperty().addListener(onGridChange);

    trackNameField.setText(track.getName());
    unsubscriptions.add(UiUtils.onBlur(trackNameField, updateTrackName));
    UiUtils.blurOnEnterKeyPress(trackNameField);

    Platform.runLater(this::populateTimeline);

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

  // TODO make sure this is called when the track is removed from the stage
  public void teardown() {
    for (Runnable unsubscription : unsubscriptions) unsubscription.run();

    uiStateService.programEditorZoomProperty().removeListener(onZoomChange);
    uiStateService.programEditorGridProperty().removeListener(onGridChange);

  }

  @FXML
  void handlePressedTrackActionLauncher() {
    UiUtils.launchModalMenu(trackActionLauncher, popupActionMenuFxml, ac, themeService.getMainScene().getWindow(),
      true, (PopupActionMenuController controller) -> controller.setup(
        "New Track",
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
  void handlePressedTrackMenu() {
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
