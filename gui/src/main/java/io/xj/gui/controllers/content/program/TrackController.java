package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import io.xj.hub.util.StringUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
    public HBox timeline;

    @Value("classpath:/views/content/program/track.fxml")
    private Resource trackFxml;
    @Value("classpath:/views/content/program/track-menu.fxml")
    private Resource trackMenuFxml;
    @Value("classpath:/views/content/program/timeline-Item.fxml")
    private Resource timelineItemFxml;
    private final ThemeService themeService;
    static final Logger LOG = LoggerFactory.getLogger(TrackController.class);
    private final ApplicationContext ac;

    private ProgramVoice voice;
    private VoiceController voiceController;
    private final ProjectService projectService;
    private ProgramVoiceTrack track;
    private final SimpleStringProperty trackNameProperty = new SimpleStringProperty();
    private double defaultTrackNameFieldPrefWidth = 0;

    public TrackController(ProgramEditorController programEditorController,
                           ApplicationContext ac, ThemeService themeService, ProjectService projectService){
        this.programEditorController=programEditorController;
        this.ac=ac;
        this.themeService=themeService;
        this.projectService=projectService;
    }
    public void setUp(ProgramVoice voice, VoiceController voiceController, ProgramVoiceTrack track) {
        this.voiceController=voiceController;
        defaultTrackNameFieldPrefWidth=trackNameField.getPrefWidth();
        this.voice=voice;
        this.track=track;
        this.addTrackButton_1.setOnAction(e->createNewTrack());
        trackNameField.setText(track.getName());
        trackMenuButton.setOnMouseClicked(this::showTrackMenu);
        Platform.runLater(this::populateTimeline);
        updateTrackName();
        bindGridValueToTimelineGridProperty();
        setTotalChooserSelectionProcessing();

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

    private void updateTrackName(){
        trackNameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue) {
                    adjustWidthWithTextIncrease();
                    trackNameProperty.set(trackNameField.getText());
                    projectService.update(ProgramVoiceTrack.class,track.getId(),"name",trackNameProperty.get());
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
                projectService.getContent().put(newTrack);
                Platform.runLater(()->trackItem(trackFxml,ac,voiceController.voiceContainer,LOG,voiceController.addTrackButton_1,voice, voiceController,newTrack));
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
            trackController.setUp(voice,voiceController,newTrack);
            voiceContainer.getChildren().add(root);
        } catch (IOException e) {
            log.error("Error adding Track item view!\n{}", StringUtils.formatStackTrace(e), e);
        }
    }

    private void showTrackMenu(MouseEvent event) {
        VoiceController.trackMenu(event, trackMenuFxml, ac, themeService, LOG,voice, voiceController);
    }

    private void populateTimeline(){
        timeline.getChildren().clear();
        for (int i=0;i< programEditorController.getTimelineGridSize() * 16;i++){
            loadTimelineItem(i);
        }
    }


    private void loadTimelineItem(int id){
        timelineItem(id, timelineItemFxml, ac, timeline, LOG,voiceController);
    }

    static void timelineItem(int id, Resource timelineItemFxml, ApplicationContext ac, HBox timeline, Logger log, VoiceController voiceController) {
        try {
            FXMLLoader loader = new FXMLLoader(timelineItemFxml.getURL());
            loader.setControllerFactory(ac::getBean);
            Parent root = loader.load();
            TimelineItemController timelineItemController = loader.getController();
            timeline.getChildren().add(root);
            timelineItemController.setUp(id,voiceController);
        } catch (IOException e) {
            log.error("Error loading Pattern Menu view!\n{}", StringUtils.formatStackTrace(e), e);
        }
    }
}
