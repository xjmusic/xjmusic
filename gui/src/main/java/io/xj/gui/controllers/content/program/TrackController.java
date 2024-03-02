package io.xj.gui.controllers.content.program;

import io.xj.gui.controllers.content.common.Zoom_Percentage;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import io.xj.hub.util.StringUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
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

import java.io.IOException;
import java.util.UUID;

import static io.xj.gui.controllers.content.program.VoiceController.computeTextWidth;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TrackController {
    private final ProgramEditorController programEditorController;
    @FXML
    public VBox trackControlsContainer;
    @FXML
    public AnchorPane trackContainer;
    @FXML
    public Button trackMenuButton;
    @FXML
    public TextField trackNameField;
    @FXML
    public Button addTrackButton_1;
    @FXML
    public AnchorPane timeLineAnchorpane;

    @Value("classpath:/views/content/program/track.fxml")
    private Resource trackFxml;
    @Value("classpath:/views/content/program/track-menu.fxml")
    private Resource trackMenuFxml;
    @Value("classpath:/views/content/program/program-sequence-pattern-event-item.fxml")
    private Resource programSequencePatternEventItem;
    private final ThemeService themeService;
    static final Logger LOG = LoggerFactory.getLogger(TrackController.class);
    private final ApplicationContext ac;

    private ProgramVoice voice;
    private VoiceController voiceController;
    private final ProjectService projectService;
    private ProgramVoiceTrack track;
    private final SimpleStringProperty trackNameProperty = new SimpleStringProperty();
    private double defaultTrackNameFieldPrefWidth = 0;

    private Parent trackRoot;

    public TrackController(ProgramEditorController programEditorController,
                           ApplicationContext ac, ThemeService themeService, ProjectService projectService) {
        this.programEditorController = programEditorController;
        this.ac = ac;
        this.themeService = themeService;
        this.projectService = projectService;

    }

    public void setUp(Parent root, ProgramVoice voice, VoiceController voiceController, ProgramVoiceTrack track) {
        this.voiceController = voiceController;
        defaultTrackNameFieldPrefWidth = trackNameField.getPrefWidth();
        this.voice = voice;
        this.track = track;
        this.trackRoot=root;
        addTrackButton_1.setOnAction(e -> createNewTrack());
        trackNameField.setText(track.getName());
        trackMenuButton.setOnMouseClicked(this::showTrackMenu);
        Platform.runLater(this::populateTimeline);
        updateTrackName();
        bindGridValueToTimelineGridProperty();
        setTotalChooserSelectionProcessing();
        getZoomValueAndRedrawOnchange();
        bindSequenceTotalValueToTimelineTotalLines();
        timeLineAnchorpane.setOnMouseClicked(this::addProgramSequencePatternEventItemController);
        timeLineAnchorpane.setCursor(Cursor.CROSSHAIR);
    }

    private void getZoomValueAndRedrawOnchange() {
        programEditorController.zoomChooser.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Zoom_Percentage zoomPercentage;
                switch (programEditorController.zoomChooser.getValue()) {
                    case "5%":
                        zoomPercentage = Zoom_Percentage.PERCENT_5;
                        break;
                    case "10%":
                        zoomPercentage = Zoom_Percentage.PERCENT_10;
                        break;
                    case "25%":
                        zoomPercentage = Zoom_Percentage.PERCENT_25;
                        break;
                    case "50%":
                        zoomPercentage = Zoom_Percentage.PERCENT_50;
                        break;
                    case "200%":
                        zoomPercentage = Zoom_Percentage.PERCENT_200;
                        break;
                    case "300%":
                        zoomPercentage = Zoom_Percentage.PERCENT_300;
                        break;
                    case "400%":
                        zoomPercentage = Zoom_Percentage.PERCENT_400;
                        break;
                    default:
                        zoomPercentage = Zoom_Percentage.PERCENT_100;

                }
                programEditorController.setZoomFactorProperty(zoomPercentage.getValue());
                Platform.runLater(this::populateTimeline);
            }
        });
    }

    private void setTotalChooserSelectionProcessing() {
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
    }

    private void bindGridValueToTimelineGridProperty() {
        // Add a listener to the selected item property
        programEditorController.gridChooser.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Parse the new value to update the IntegerProperty
                updateTimelineGridProperty(newValue);
                Platform.runLater(this::populateTimeline);
            }
        });
    }

    private void updateTimelineGridProperty(String selection) {
        // Extract the numeric part from the selection
        updateGrid(selection, programEditorController);
    }

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

    private void updateTrackName() {
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
    }


    private void adjustWidthWithTextIncrease() {
        double newTextWidth = computeTextWidth(trackNameField.getFont(), trackNameField.getText());
        if (newTextWidth > defaultTrackNameFieldPrefWidth) {
            trackNameField.setPrefWidth(newTextWidth);
            trackControlsContainer.setPrefWidth(newTextWidth + 10);
        }
    }

    private void createNewTrack() {
        try {
            ProgramVoiceTrack newTrack = new ProgramVoiceTrack(UUID.randomUUID(), programEditorController.getProgramId(), voice.getId(), "XXX", 1f);
            projectService.update(newTrack);
            trackItem(trackFxml, ac, voiceController.voiceContainer, LOG, addTrackButton_1, voice, voiceController, newTrack);
        } catch (Exception e) {
            LOG.info("Could not create new Track");
        }
    }

    static void trackItem(Resource trackFxml, ApplicationContext ac, VBox voiceContainer, Logger log, Button addTrackButton_1, ProgramVoice voice, VoiceController voiceController, ProgramVoiceTrack newTrack) {
        try {
            FXMLLoader loader = new FXMLLoader(trackFxml.getURL());
            loader.setControllerFactory(ac::getBean);
            Parent root = loader.load();
            addTrackButton_1.setVisible(false);
            TrackController trackController = loader.getController();
            trackController.setUp(root,voice, voiceController, newTrack);
            voiceContainer.getChildren().add(root);
        } catch (IOException e) {
            log.error("Error adding Track item view!\n{}", StringUtils.formatStackTrace(e), e);
        }
    }

    private void showTrackMenu(MouseEvent event) {
        VoiceController.trackMenu(event, trackMenuFxml, ac, themeService, LOG, voice, voiceController,track, false,trackRoot,addTrackButton_1);
    }

    private void populateTimeline() {
        timeLineAnchorpane.getChildren().removeIf(node ->  (node instanceof Line || node instanceof Rectangle));
        if (0 < programEditorController.getSequenceTotal()) {
            for (double b = 0; b <= programEditorController.getSequenceTotal(); b += ((double) 1 / programEditorController.getTimelineGridSize())) {
                double gridLineX = b * voiceController.getBaseSizePerBeat().get() * programEditorController.getZoomFactor();
                VoiceController.drawGridLines(b, gridLineX, timeLineAnchorpane, voiceController.getTimelineHeight(),voiceController.doublePropertyProperty());
            }
            greyTheActiveArea();
        }
    }

    private void greyTheActiveArea() {
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
    }

    private void bindSequenceTotalValueToTimelineTotalLines() {
        // Add a listener to the selected item property
        programEditorController.sequenceTotalChooser.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                programEditorController.setSequenceTotal(programEditorController.sequenceTotalChooser.getValue());
                Platform.runLater(this::populateTimeline);
            }
        });
    }

    private void addProgramSequencePatternEventItemController(MouseEvent event){
        try{
            FXMLLoader loader = new FXMLLoader(programSequencePatternEventItem.getURL());
            loader.setControllerFactory(ac::getBean);
            Parent root = loader.load();
            // Set the layout parameters of the new item to match those of the existing item
            AnchorPane.setLeftAnchor(root, event.getX());
            AnchorPane.setTopAnchor(root,0.0);
            AnchorPane.setBottomAnchor(root,0.0);
            ProgramSequencePatternEvent programSequencePatternEvent=new ProgramSequencePatternEvent(UUID.randomUUID(),voiceController.getProgramVoiceTrack().getProgramId(),programEditorController.getSequenceId(),voiceController.getProgramVoiceTrack().getId(),0.125f,0.125f,0.125f,"X");
            ProgramSequencePatternEventItemController patternEventItemController = loader.getController();
            patternEventItemController.setUp(root, timeLineAnchorpane,programSequencePatternEvent,voiceController);
            // Add the new property item to the AnchorPane
            timeLineAnchorpane.getChildren().add(root);
            projectService.getContent().put(programSequencePatternEvent);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
