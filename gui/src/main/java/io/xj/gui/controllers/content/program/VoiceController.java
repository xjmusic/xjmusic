package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.util.StringUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

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
    public ComboBox<InstrumentType> instrumentTypeCombobox;
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
    @FXML
    public Label noSequenceLabel;
    @FXML
    public HBox totalHbox;

    @Value("classpath:/views/content/program/track.fxml")
    private Resource trackFxml;
    @Value("classpath:/views/content/program/pattern-menu.fxml")
    private Resource patternMenuFxml;
    @Value("classpath:/views/content/program/track-menu.fxml")
    private Resource trackMenuFxml;
    @Value("classpath:/views/content/program/pattern-selector.fxml")
    private Resource patternSelectorFxml;
    @Value("classpath:/views/content/program/timeline-Item.fxml")
    private Resource timelineItemFxml;
    private final ApplicationContext ac;
    private final ThemeService themeService;
    private final ProjectService projectService;
    private ProgramVoice voice;

    private final ObjectProperty<ProgramSequencePattern> selectedProgramSequencePattern = new SimpleObjectProperty<>();

    private final ObservableList<ProgramSequencePattern> programSequencePatternsOfThisVoice = FXCollections.observableArrayList();

    private final SimpleStringProperty patternNameProperty = new SimpleStringProperty();
    private final SimpleStringProperty patternTotalProperty = new SimpleStringProperty();

    private double previousPatternNameFieldPrefWidth=0;


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
        previousPatternNameFieldPrefWidth=patternNameField.getPrefWidth();
        programSequencePatternsOfThisVoice.addAll(projectService.getContent().getProgramSequencePatternsOfVoice(voice));
        setSelectedProgramSequencePattern(programSequencePatternsOfThisVoice.isEmpty() ?
                null : programSequencePatternsOfThisVoice.get(0));
        deleteVoice(root);
        populateTimeline();
        hideItemsBeforeTrackIsCreated();
        trackName.setText(voice.getName());
        totalHbox.visibleProperty().bind(selectedProgramSequencePattern.isNotNull());
        if (selectedProgramSequencePattern.get() != null) {
            updatePatternUI(selectedProgramSequencePattern.get());
            patternNameField.toFront();
        }
        patternNameField.textProperty().bindBidirectional(patternNameProperty);
        patternTotalCountLabel.textProperty().bindBidirectional(patternTotalProperty);
        setCombobox();
        patternMenuButton.setOnMouseClicked(this::showPatternMenu);
        trackMenuButton.setOnMouseClicked(this::showTrackMenu);
        searchPattern.setOnMouseClicked(this::showPatternSearch);
        updateProgramSequencePatternName();
        updateProgramSequencePatternInstrumentType();
    }


    protected void updatePatternUI(ProgramSequencePattern programSequencePattern) {
        if (programSequencePattern!=null){
            patternNameField.toFront();
            patternNameProperty.set(programSequencePattern.getName());
            patternTotalProperty.set(String.valueOf(programSequencePattern.getTotal()));
        }else {
            //return widths to normal
            noPatternLabel.setPrefWidth(previousPatternNameFieldPrefWidth);
            patternNameField.setPrefWidth(previousPatternNameFieldPrefWidth);
            noSequenceLabel.setPrefWidth(previousPatternNameFieldPrefWidth);
            noPatternLabel.toFront();
        }
    }

    private void updateProgramSequencePatternName() {
        patternNameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue) {
                    adjustWidthWithTextIncrease();
                    patternNameProperty.set(patternNameField.getText());
                    projectService.update(ProgramSequencePattern.class, selectedProgramSequencePattern.get().getId(), "name",
                            patternNameProperty.get());
                }
            } catch (Exception e) {
                LOG.info("Failed to update program sequence ");
            }
        });
    }

    private void updateProgramSequencePatternInstrumentType() {
        instrumentTypeCombobox.focusedProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue) {
                    adjustWidthWithTextIncrease();
                    patternNameProperty.set(patternNameField.getText());
                    projectService.update(ProgramVoice.class, voice.getId(), "type",
                            instrumentTypeCombobox.getValue());
                }
            } catch (Exception e) {
                LOG.info("Failed to update program sequence ");
            }
        });
    }

    private void adjustWidthWithTextIncrease() {
        // Compute the new preferred width based on the text content
        double newTextWidth = computeTextWidth(patternNameField.getFont(), patternNameField.getText());
        if (newTextWidth > previousPatternNameFieldPrefWidth) {
            instrumentTypeCombobox.setPrefWidth(newTextWidth);
            voiceControlsContainer.setPrefWidth(newTextWidth+10);
            patternNameField.setPrefWidth(newTextWidth);
            noSequenceLabel.setPrefWidth(newTextWidth);
            noPatternLabel.setPrefWidth(newTextWidth);
        }
    }

    private double computeTextWidth(javafx.scene.text.Font font, String text) {
        javafx.scene.text.Text helper = new javafx.scene.text.Text();
        helper.setFont(font);
        helper.setText(text);
        return helper.getBoundsInLocal().getWidth();
    }


    public ObservableList<ProgramSequencePattern> getProgramSequencePatternsOfThisVoice() {
        return programSequencePatternsOfThisVoice;
    }

    public ProgramSequencePattern getSelectedProgramSequencePattern() {
        return selectedProgramSequencePattern.get();
    }

    public void setSelectedProgramSequencePattern(ProgramSequencePattern programSequencePattern) {
        selectedProgramSequencePattern.set(programSequencePattern);
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
        instrumentTypeCombobox.getItems().clear();
        // Add items from InstrumentType enum
        instrumentTypeCombobox.getItems().addAll(InstrumentType.values());
        instrumentTypeCombobox.setValue(voice.getType());
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
            FXMLLoader loader = new FXMLLoader(patternSelectorFxml.getURL());
            loader.setControllerFactory(ac::getBean);
            Parent root = loader.load();
            PatternSelectorController patternSearchController = loader.getController();
            patternSearchController.setUp(programSequencePatternsOfThisVoice, voice, this, stage);
            stage.setScene(new Scene(root));
            stage.setOnShown(e -> patternSearchController.patternSelector.show());
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
            patternMenuController.setUp(root, voice, selectedProgramSequencePattern.get(),this);
            stage.setScene(new Scene(root));
            stage.initOwner(themeService.getMainScene().getWindow());
            stage.show();
            positionUIAtLocation(stage, event, 0, 30);
            closeWindowOnClickingAway(stage);
        } catch (IOException e) {
            LOG.error("Error loading Pattern menu view!\n{}", StringUtils.formatStackTrace(e), e);
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
