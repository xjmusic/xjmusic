package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.util.StringUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

import static io.xj.gui.controllers.content.program.ProgramEditorController.closeWindowOnClickingAway;
import static io.xj.gui.controllers.content.program.ProgramEditorController.positionUIAtLocation;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VoiceController {
    @FXML
    public VBox voiceControlsContainer;
    @FXML
    public Button deleteButton;
    @FXML
    public Text voiceName;
    @FXML
    public ComboBox<InstrumentType> voiceCombobox;
    @FXML
    public Button searchPattern;
    @FXML
    public Button patternMenuButton;
    @FXML
    public TextField patternNameField;
    @FXML
    public Label noPatternLabel;
    @FXML
    public Label patternTotalCountLabel;
    @FXML
    public AnchorPane trackContainer;
    @FXML
    public Group trackButtonGroup;
    @FXML
    public Button addTrackButton;
    @FXML
    public Button trackMenuButton;
    @FXML
    public Button addTrackButton_1;
    @FXML
    public Text trackName;
    private final ProgramEditorController programEditorController;
    @FXML
    public HBox timeline;
    static final Logger LOG = LoggerFactory.getLogger(VoiceController.class);
    @FXML
    public VBox voiceContainer;

    @Value("classpath:/views/content/program/track.fxml")
    private Resource trackFxml;
    @Value("classpath:/views/content/program/pattern-menu.fxml")
    private Resource patternMenuFxml;
    @Value("classpath:/views/content/program/track-menu.fxml")
    private Resource trackMenuFxml;
    @Value("classpath:/views/content/program/pattern-search.fxml")
    private Resource patternSearchFxml;
    @Value("classpath:/views/content/program/timeline-Item.fxml")
    private Resource timelineItemFxml;
    private final ApplicationContext ac;
    private final ThemeService themeService;
    private final ProjectService projectService;
    private ProgramVoice voice;

    public VoiceController(ProgramEditorController programEditorController,
                           ApplicationContext ac, ThemeService themeService,
                           ProjectService projectService
    ) {
        this.programEditorController = programEditorController;
        this.ac = ac;
        this.themeService = themeService;
        this.projectService = projectService;
    }

    protected void setUp(Parent root, ProgramVoice voice) {
        this.voice = voice;
        deleteVoice(root);
        populateTimeline();
        hideItemsBeforeTrackIsCreated();
        trackName.setText(voice.getName());
        setCombobox();
        patternMenuButton.setOnMouseClicked(this::showPatternMenu);
        trackMenuButton.setOnMouseClicked(this::showTrackMenu);
        searchPattern.setOnMouseClicked(this::showPatternSearch);
    }

    private void populateTimeline() {
        for (int i = 0; i < programEditorController.getTimelineGridSize() * 16; i++) {
            loadTimelineItem(i);
        }
    }


    private void loadTimelineItem(int id) {
        TrackController.timelineItem(id, timelineItemFxml, ac, timeline, LOG);
    }

    private void setCombobox() {
        // Clear existing items
        voiceCombobox.getItems().clear();
        // Add items from InstrumentType enum
        voiceCombobox.getItems().addAll(InstrumentType.values());
        voiceCombobox.setValue(voice.getType());
    }


    private void deleteVoice(Parent root) {
        deleteButton.setOnAction(e -> {
            programEditorController.editModeContainer.getChildren().remove(root);
            projectService.deleteContent(voice);
            projectService.showAlert(Alert.AlertType.INFORMATION, "It Was Done", "", "Deleted voice");
        });
    }

    private void hideItemsBeforeTrackIsCreated() {
        addTrackButton.toFront();
        addTrackButton_1.setVisible(false);
        trackName.setVisible(false);
        timeline.setVisible(false);
        addNewTrackToCurrentVoiceLine();
        addNewTrackToNewLine();

    }

    private void addNewTrackToCurrentVoiceLine() {
        addTrackButton.setOnAction(e -> showItemsAfterTrackIsCreated());
    }

    private void addNewTrackToNewLine() {
        addTrackButton_1.setOnAction(e -> addTrackItemToNewLine());
    }

    protected void addTrackItemToNewLine() {
        TrackController.trackItem(trackFxml, ac, voiceContainer, LOG, addTrackButton_1);
    }

    private void showItemsAfterTrackIsCreated() {
        trackContainer.getStyleClass().add("track-container");
        trackMenuButton.toFront();
        addTrackButton_1.setVisible(true);
        trackName.setVisible(true);
        timeline.setVisible(true);
    }


    private void showPatternSearch(MouseEvent event) {
        try {
            Stage stage = new Stage(StageStyle.TRANSPARENT);
            FXMLLoader loader = new FXMLLoader(patternSearchFxml.getURL());
            loader.setControllerFactory(ac::getBean);
            Parent root = loader.load();
            Optional<ProgramSequencePattern> programSequencePattern = projectService.getContent().getProgramSequencePattern(programEditorController.getProgramId());
            PatternSearchController patternSearchController = loader.getController();
            patternSearchController.setUp(programSequencePattern.orElse(null), voice);
            stage.setScene(new Scene(root));
            stage.initOwner(themeService.getMainScene().getWindow());
            stage.show();
            positionUIAtLocation(stage, event, 0, 30);
            closeWindowOnClickingAway(stage);
        } catch (IOException e) {
            LOG.error("Error loading Pattern Menu view!\n{}", StringUtils.formatStackTrace(e), e);
        }
    }

    private void showPatternMenu(MouseEvent event) {
        try {
            Stage stage = new Stage(StageStyle.TRANSPARENT);
            FXMLLoader loader = new FXMLLoader(patternMenuFxml.getURL());
            loader.setControllerFactory(ac::getBean);
            Parent root = loader.load();
            PatternMenuController patternMenuController = loader.getController();
            patternMenuController.setUp(root, voice);
            stage.setScene(new Scene(root));
            stage.initOwner(themeService.getMainScene().getWindow());
            stage.show();
            positionUIAtLocation(stage, event, 0, 30);
            closeWindowOnClickingAway(stage);
        } catch (IOException e) {
            LOG.error("Error loading Pattern Search view!\n{}", StringUtils.formatStackTrace(e), e);
        }
    }

    private void showTrackMenu(MouseEvent event) {
        trackMenu(event, trackMenuFxml, ac, themeService, LOG);
    }

    static void trackMenu(MouseEvent event, Resource trackMenuFxml, ApplicationContext ac, ThemeService themeService, Logger log) {
        try {
            Stage stage = new Stage(StageStyle.TRANSPARENT);
            FXMLLoader loader = new FXMLLoader(trackMenuFxml.getURL());
            loader.setControllerFactory(ac::getBean);
            Parent root = loader.load();
            TrackMenuController trackMenuController = loader.getController();
            trackMenuController.setUp(root);
            stage.setScene(new Scene(root));
            stage.initOwner(themeService.getMainScene().getWindow());
            stage.show();
            positionUIAtLocation(stage, event, 0, 30);
            closeWindowOnClickingAway(stage);
        } catch (IOException e) {
            log.error("Error loading Track Menu view!\n{}", StringUtils.formatStackTrace(e), e);
        }
    }
}
