package io.xj.gui.controllers.content.program;

import io.xj.gui.controllers.content.common.Zoom_Percentage;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import io.xj.hub.util.StringUtils;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
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
import java.util.UUID;

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
    public TextField trackNameField;
    private final ProgramEditorController programEditorController;
    static final Logger LOG = LoggerFactory.getLogger(VoiceController.class);
    @FXML
    public VBox voiceContainer;
    @FXML
    public Label noSequenceLabel;
    @FXML
    public HBox totalHbox;
    @FXML
    public TextField programVoiceNameTextField;
    @FXML
    public Spinner<Integer> patternTotalCountChooser;
    @FXML
    public AnchorPane timeLineAnchorpane;

    @Value("classpath:/views/content/program/track.fxml")
    private Resource trackFxml;
    @Value("classpath:/views/content/program/pattern-menu.fxml")
    private Resource patternMenuFxml;
    @Value("classpath:/views/content/program/track-menu.fxml")
    private Resource trackMenuFxml;
    @Value("classpath:/views/content/program/pattern-selector.fxml")
    private Resource patternSelectorFxml;
    @Value("classpath:/views/content/program/program-sequence-pattern-event-item.fxml")
    private Resource programSequencePatternEventItem;
    private final ApplicationContext ac;
    private final ThemeService themeService;
    private final ProjectService projectService;
    private ProgramVoice voice;

    public SimpleIntegerProperty getBaseSizePerBeat() {
        return baseSizePerBeat;
    }

    private final SimpleIntegerProperty baseSizePerBeat = new SimpleIntegerProperty(300);
    private final ObjectProperty<ProgramSequencePattern> selectedProgramSequencePattern = new SimpleObjectProperty<>();
    private final ObservableList<ProgramSequencePattern> programSequencePatternsOfThisVoice = FXCollections.observableArrayList();
    private final SimpleStringProperty patternNameProperty = new SimpleStringProperty();
    private double previousPatternNameFieldPrefWidth = 0;
    private double defaultTrackNameFieldPrefWidth = 0;
    private final SimpleStringProperty trackNameProperty = new SimpleStringProperty();

    private final ObservableList<ProgramVoiceTrack> programVoiceTrackObservableList = FXCollections.observableArrayList();

    private final ObjectProperty<ProgramVoiceTrack> programVoiceTrackObjectProperty = new SimpleObjectProperty<>();
    private final SpinnerValueFactory<Integer> totalValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1300, 0);
    private final FloatProperty totalFloatValue = new SimpleFloatProperty(totalValueFactory.getValue());

    private final DoubleProperty timelineHeightProperty = new SimpleDoubleProperty(0);

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
        previousPatternNameFieldPrefWidth = patternNameField.getPrefWidth();
        defaultTrackNameFieldPrefWidth = trackNameField.getPrefWidth();
        programSequencePatternsOfThisVoice.addAll(projectService.getContent().getProgramSequencePatternsOfVoice(voice));
        setSelectedProgramSequencePattern(programSequencePatternsOfThisVoice.isEmpty() ?
                null : programSequencePatternsOfThisVoice.get(0));
        deleteVoice(root);
        hideItemsBeforeTrackIsCreated();
        totalHbox.visibleProperty().bind(selectedProgramSequencePattern.isNotNull());
        if (selectedProgramSequencePattern.get() != null) {
            updatePatternUI(selectedProgramSequencePattern.get());
            patternNameField.toFront();
        }
        patternNameField.textProperty().bindBidirectional(patternNameProperty);
        programVoiceNameTextField.setText(voice.getName());
        setCombobox();
        timelineHeightProperty.set(timeLineAnchorpane.getPrefHeight());
        patternMenuButton.setOnMouseClicked(this::showPatternMenu);
        trackMenuButton.setOnMouseClicked(this::showTrackMenu);
        searchPattern.setOnMouseClicked(this::showPatternSearch);
        updateProgramSequencePatternName();
        updateTrackName();
        updateProgramSequencePatternInstrumentType();
        updateProgramVoiceName();
        bindGridValueToTimelineGridProperty();
        loadProgramVoiceTracks();
        patternTotalCountChooser.valueProperty().addListener((observable, oldValue, newValue) -> totalFloatValue.set(newValue));
        patternTotalCountChooser.setValueFactory(totalValueFactory);
        setTotalChooserSelectionProcessing();
        Platform.runLater(this::populateTimeline);
        getZoomValueAndRedrawOnchange();
        bindSequenceTotalValueToTimelineTotalLines();
        trackNameField.textProperty().bindBidirectional(trackNameProperty);
        timeLineAnchorpane.setOnMouseClicked(this::addProgramSequencePatternEventItemController);
        timeLineAnchorpane.setCursor(Cursor.CROSSHAIR);
    }


    public ObservableList<ProgramVoiceTrack> getProgramVoiceTrackObservableList() {
        return programVoiceTrackObservableList;
    }

    public void setTrackNameProperty(String trackNameProperty) {
        this.trackNameProperty.set(trackNameProperty);
    }


    public void setProgramVoiceTrackObjectProperty(ProgramVoiceTrack programVoiceTrack) {
        this.programVoiceTrackObjectProperty.set(programVoiceTrack);
    }

    public ProgramVoiceTrack getProgramVoiceTrack() {
        return programVoiceTrackObjectProperty.get();
    }

    public FloatProperty getTotalFloatValue() {
        return totalFloatValue;
    }

    public double getTimelineHeight() {
        return timelineHeightProperty.get();
    }

    public SpinnerValueFactory<Integer> getTotalValueFactory() {
        return totalValueFactory;
    }

    private void setTotalChooserSelectionProcessing() {
        patternTotalCountChooser.focusedProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue) {
                    totalValueFactory.setValue(patternTotalCountChooser.getValue());
                    projectService.update(ProgramSequencePattern.class, selectedProgramSequencePattern.get().getId(), "total",
                            totalValueFactory.getValue());
                }
            } catch (Exception e) {
                LOG.info("Failed to update ProgramSequencePattern total");
            }
        });
    }

    private void loadProgramVoiceTracks() {
        programVoiceTrackObservableList.addAll(projectService.getContent().getTracksOfVoice(voice));
        for (int i = 0; i < programVoiceTrackObservableList.size() && programVoiceTrackObservableList.size() > 0; i++) {
            if (i == 0) {
                programVoiceTrackObjectProperty.set(programVoiceTrackObservableList.get(i));
                trackNameProperty.set(programVoiceTrackObjectProperty.get().getName());
                showItemsAfterTrackIsCreated();
            } else
                trackItem(programVoiceTrackObservableList.get(i),programVoiceTrackObservableList.get(i)==programVoiceTrackObservableList.get(programVoiceTrackObservableList.size()-1));
        }

    }

    private void trackItem( ProgramVoiceTrack newTrack,Boolean isLastItem) {
        try {
            FXMLLoader loader = new FXMLLoader(trackFxml.getURL());
            loader.setControllerFactory(ac::getBean);
            Parent root = loader.load();
            addTrackButton_1.setVisible(false);
            TrackController trackController = loader.getController();
            if (!isLastItem) trackController.addTrackButton_1.setVisible(false);
            trackController.setUp(root,voice, this, newTrack);
            voiceContainer.getChildren().add(root);
        } catch (IOException e) {
           LOG.error("Error adding Track item view!\n{}", StringUtils.formatStackTrace(e), e);
        }
    }

    private void getZoomValueAndRedrawOnchange() {
        programEditorController.zoomChooser.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Zoom_Percentage zoomPercentage = switch (programEditorController.zoomChooser.getValue()) {
                    case "5%" -> Zoom_Percentage.PERCENT_5;
                    case "10%" -> Zoom_Percentage.PERCENT_10;
                    case "25%" -> Zoom_Percentage.PERCENT_25;
                    case "50%" -> Zoom_Percentage.PERCENT_50;
                    case "200%" -> Zoom_Percentage.PERCENT_200;
                    case "300%" -> Zoom_Percentage.PERCENT_300;
                    case "400%" -> Zoom_Percentage.PERCENT_400;
                    default -> Zoom_Percentage.PERCENT_100;
                };
                programEditorController.setZoomFactorProperty(zoomPercentage.getValue());
                Platform.runLater(this::populateTimeline);
            }
        });
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
        TrackController.updateGrid(selection, programEditorController);
    }


    protected void updatePatternUI(ProgramSequencePattern programSequencePattern) {
        if (programSequencePattern != null) {
            patternNameField.toFront();
            patternNameProperty.set(programSequencePattern.getName());
            totalValueFactory.setValue(Integer.valueOf(programSequencePattern.getTotal()));
        } else {
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
                LOG.info("Failed to update ProgramSequencePattern name");
            }
        });
    }

    private void updateTrackName() {
        trackNameField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue) {
                    adjustTrackNameFieldWidthWithTextIncrease();
                    trackNameProperty.set(trackNameField.getText());
                    projectService.update(ProgramVoiceTrack.class, programVoiceTrackObjectProperty.get().getId(), "name",
                            trackNameProperty.get());
                }
            } catch (Exception e) {
                LOG.info("Failed to update ProgramVoiceTrack name");
            }
        });
    }

    private void adjustTrackNameFieldWidthWithTextIncrease() {
        double newTextWidth = computeTextWidth(trackNameField.getFont(), trackNameField.getText());
        if (newTextWidth > defaultTrackNameFieldPrefWidth) {
            trackNameField.setPrefWidth(newTextWidth);
            trackContainer.setPrefWidth(newTextWidth + 10);
        }
    }

    private void updateProgramVoiceName() {
        programVoiceNameTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue) {
                    projectService.update(ProgramVoice.class, voice.getId(), "name",
                            programVoiceNameTextField.getText());
                }
            } catch (Exception e) {
                LOG.info("Failed to update ProgramVoice name ");
            }
        });
    }

    private void updateProgramSequencePatternInstrumentType() {
        instrumentTypeCombobox.focusedProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue) {
                    projectService.update(ProgramVoice.class, voice.getId(), "type",
                            instrumentTypeCombobox.getValue());

                }
            } catch (Exception e) {
                LOG.info("Failed to update ProgramVoice type ");
            }
        });
    }

    private void adjustWidthWithTextIncrease() {
        // Compute the new preferred width based on the text content
        double newTextWidth = computeTextWidth(patternNameField.getFont(), patternNameField.getText());
        if (newTextWidth > previousPatternNameFieldPrefWidth) {
            instrumentTypeCombobox.setPrefWidth(newTextWidth);
            voiceControlsContainer.setPrefWidth(newTextWidth + 10);
            patternNameField.setPrefWidth(newTextWidth);
            noSequenceLabel.setPrefWidth(newTextWidth);
            noPatternLabel.setPrefWidth(newTextWidth);
        }
    }

    public static double computeTextWidth(javafx.scene.text.Font font, String text) {
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
        timeLineAnchorpane.getChildren().removeIf(node ->  (node instanceof Line || node instanceof Rectangle));
        if (0 < programEditorController.getSequenceTotal()) {
            for (double b = 0; b <= programEditorController.getSequenceTotal(); b += ((double) 1 / programEditorController.getTimelineGridSize())) {
                double gridLineX = b * baseSizePerBeat.get() * programEditorController.getZoomFactor();
                drawGridLines(b, gridLineX, timeLineAnchorpane, timelineHeightProperty.get(),doubleProperty);
            }
            greyTheActiveArea();
        }
    }

    public double getDoubleProperty() {
        return doubleProperty.get();
    }

    public DoubleProperty doublePropertyProperty() {
        return doubleProperty;
    }

    public void setDoubleProperty(double doubleProperty) {
        this.doubleProperty.set(doubleProperty);
    }

    private final DoubleProperty doubleProperty=new SimpleDoubleProperty();

    static void drawGridLines(double b, double gridLineX, AnchorPane timeLineAnchorpane, double rectangleHeight, DoubleProperty doubleProperty) {
        boolean isMajorLine = (b % 1) == 0;
        Line line = new Line();
        line.setStartY(0);
        line.setEndY(rectangleHeight);
        line.setStartX(gridLineX);
        line.setEndX(gridLineX);
        if (isMajorLine) {
            doubleProperty.set(gridLineX);
            line.setStroke(Color.GREY);
        } else {
            line.setStroke(Color.valueOf("#3F3F3F"));
        }
        AnchorPane.setLeftAnchor(line, gridLineX);
        AnchorPane.setTopAnchor(line, 0.0); // Adjust the top position as needed
        AnchorPane.setBottomAnchor(line, 0.0); // Adjust the bottom position as needed
        timeLineAnchorpane.getChildren().add(line);
    }

    private void greyTheActiveArea() {
        Rectangle rectangle = new Rectangle();
        // Bind the width of the rectangle to the product of the two integer properties
        rectangle.widthProperty().bind(
                Bindings.multiply(
                        Bindings.multiply(
                                baseSizePerBeat,
                                totalFloatValue
                        ),
                        programEditorController.getZoomFactor()
                ));
        rectangle.setHeight(timelineHeightProperty.get());
        rectangle.setFill(Color.valueOf("#252525"));
        rectangle.setOpacity(.7);
        timeLineAnchorpane.getChildren().add(0, rectangle);
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

    protected void hideItemsBeforeTrackIsCreated() {
        addTrackButton.toFront();
        addTrackButton_1.setVisible(false);
        trackNameField.setVisible(false);
        timeLineAnchorpane.setVisible(false);
        addNewTrackToCurrentVoiceLine();
        addNewTrackToNewLine();

    }

    private void addNewTrackToCurrentVoiceLine() {
        addTrackButton.setOnAction(e -> {
            try {
                ProgramVoiceTrack newTrack = new ProgramVoiceTrack(UUID.randomUUID(), programEditorController.getProgramId(), voice.getId(), "XXX", 1f);
                projectService.update(newTrack);
                trackNameProperty.set(newTrack.getName());
                programVoiceTrackObjectProperty.set(newTrack);
                showItemsAfterTrackIsCreated();
            } catch (Exception ex) {
                LOG.info("Could not create new Track to the current voice line");
            }
        });
    }

    private void addNewTrackToNewLine() {
        addTrackButton_1.setOnAction(e -> createNewTrack());
    }


    protected void createNewTrack() {
        try {
            ProgramVoiceTrack newTrack = new ProgramVoiceTrack(UUID.randomUUID(), programEditorController.getProgramId(), voice.getId(), "XXX", 1f);
            projectService.update(newTrack);
            TrackController.trackItem(trackFxml, ac, voiceContainer,LOG ,addTrackButton_1, voice, this, newTrack);
        } catch (Exception e) {
            LOG.info("Could not create new Track");
        }
    }

    protected void showItemsAfterTrackIsCreated() {
        trackContainer.getStyleClass().add("track-container");
        trackMenuButton.toFront();
        addTrackButton_1.setVisible(true);
        trackNameField.setVisible(true);
        timeLineAnchorpane.setVisible(true);
        populateTimeline();
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
            patternMenuController.setUp(root, voice, selectedProgramSequencePattern.get(), this);
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
        trackMenu(event, trackMenuFxml, ac, themeService, LOG, voice, this,programVoiceTrackObjectProperty.get(), true, null, addTrackButton_1);
    }

    static void trackMenu(MouseEvent event, Resource trackMenuFxml, ApplicationContext ac, ThemeService themeService, Logger log, ProgramVoice voice, VoiceController voiceController, ProgramVoiceTrack track, boolean itemIsAttachedToVoiceFxml, Parent trackRoot, Button addTrackButton) {
        try {
            Stage stage = new Stage(StageStyle.TRANSPARENT);
            FXMLLoader loader = new FXMLLoader(trackMenuFxml.getURL());
            loader.setControllerFactory(ac::getBean);
            Parent root = loader.load();
            TrackMenuController trackMenuController = loader.getController();
            trackMenuController.setUp(root, voice, voiceController,track,itemIsAttachedToVoiceFxml,trackRoot,addTrackButton);
            stage.setScene(new Scene(root));
            stage.initOwner(themeService.getMainScene().getWindow());
            stage.show();
            positionUIAtLocation(stage, event, 0, 30);
            closeWindowOnClickingAway(stage);
        } catch (IOException e) {
            log.error("Error loading Track Menu view!\n{}", StringUtils.formatStackTrace(e), e);
        }
    }



    private void addProgramSequencePatternEventItemController(MouseEvent event) {
        try {
            event.consume();
            FXMLLoader loader = new FXMLLoader(programSequencePatternEventItem.getURL());
            loader.setControllerFactory(ac::getBean);
            Parent root = loader.load();
            ProgramSequencePatternEvent programSequencePatternEvent = new ProgramSequencePatternEvent(UUID.randomUUID(), programVoiceTrackObjectProperty.get().getProgramId(), programEditorController.getSequenceId(), programVoiceTrackObjectProperty.get().getId(), 0.125f, 0.125f, 0.125f, "X");
            ProgramSequencePatternEventItemController patternEventItemController = loader.getController();
            patternEventItemController.setUp(root, timeLineAnchorpane, programSequencePatternEvent, this);
            timeLineAnchorpane.getChildren().add(root);
            patternEventItemController.getEventPositionProperty.set(event.getX() - ((this.getBaseSizePerBeat().doubleValue() * programEditorController.getZoomFactor())  +
                    patternEventItemController.getEventPositionProperty.get()));
            AnchorPane.setBottomAnchor(root,0.0);
            AnchorPane.setTopAnchor(root,0.0);
            projectService.getContent().put(programSequencePatternEvent);

       } catch (Exception e) {
           throw new RuntimeException(e);
       }
    }
}
