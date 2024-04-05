package io.xj.gui.controllers.content.program.chord_edit_mode;

import io.xj.gui.ProjectController;
import io.xj.gui.types.GridChoice;
import io.xj.gui.types.ZoomChoice;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.LaunchMenuPosition;
import io.xj.hub.pojos.ProgramSequence;
import io.xj.hub.pojos.ProgramSequenceChord;
import io.xj.hub.util.StringUtils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class ChordTimelineController extends ProjectController {
  static final Logger LOG = LoggerFactory.getLogger(ChordTimelineController.class);
  private final ObservableList<SequenceChordController> sequenceChordControllers = FXCollections.observableArrayList();
  private final int controlWidth;
  private final ProjectService projectService;
  private final UIStateService uiStateService;
  private final Resource chordFxml;
  private final Resource chordPropertiesFxml;
  private final int timelineHeight;

  @FXML
  HBox controlContainer;

  @FXML
  HBox container;

  @FXML
  Pane timelineBackground;

  @FXML
  Pane chordAddContainer;

  @FXML
  Button addChordButton;

  @FXML
  AnchorPane timelineChordContainer;

  public ChordTimelineController(
    @Value("classpath:/views/content/program/edit_chord_mode/chord-timeline.fxml") Resource fxml,
    @Value("classpath:/views/content/program/edit_chord_mode/sequence-chord.fxml") Resource chordFxml,
    @Value("classpath:/views/content/program/edit_chord_mode/sequence-chord-properties.fxml") Resource chordPropertiesFxml,
    @Value("${programEditor.voiceControlWidth}") int voiceControlWidth,
    @Value("${programEditor.chordTimelineHeight}") int timelineHeight,
    ApplicationContext ac,
    ThemeService themeService,
    UIStateService uiStateService,
    ProjectService projectService
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.chordFxml = chordFxml;
    this.chordPropertiesFxml = chordPropertiesFxml;
    this.timelineHeight = timelineHeight;
    this.controlWidth = voiceControlWidth;
    this.projectService = projectService;
    this.uiStateService = uiStateService;
  }

  @Override
  public void onStageReady() {
    container.setMinHeight(timelineHeight);
    container.setMaxHeight(timelineHeight);
    controlContainer.setMinWidth(controlWidth);
    controlContainer.setMaxWidth(controlWidth);
    timelineBackground.setMinHeight(timelineHeight);
    timelineBackground.setMaxHeight(timelineHeight);
    chordAddContainer.setMinWidth(timelineHeight);
    chordAddContainer.setMaxWidth(timelineHeight);

    ChangeListener<ZoomChoice> onZoomChange = (o, ov, v) -> Platform.runLater(this::setup);
    uiStateService.programEditorZoomProperty().addListener(onZoomChange);

    ChangeListener<GridChoice> onGridChange = (o, ov, v) -> Platform.runLater(this::setup);
    uiStateService.programEditorGridProperty().addListener(onGridChange);

    ChangeListener<ProgramSequence> onSequenceChange = (o, ov, v) -> Platform.runLater(this::setup);
    uiStateService.currentProgramSequenceProperty().addListener(onSequenceChange);

    projectService.addProjectUpdateListener(ProgramSequence.class, this::setup);
    projectService.addProjectUpdateListener(ProgramSequenceChord.class, this::setup);
  }

  @Override
  public void onStageClose() {
    // no op
  }

  @FXML
  void handlePressedAddChord() {
    if (uiStateService.currentProgramSequenceProperty().isNull().get()) return;
    var sequence = uiStateService.currentProgramSequenceProperty().get();
    uiStateService.launchModalMenu(
      chordPropertiesFxml,
      addChordButton,
      (SequenceChordPropertiesController controller) -> controller.setupCreating(sequence.getId()),
      LaunchMenuPosition.from(addChordButton),
      true,
      () -> {/* no op*/});
  }

  /**
   Set up the voice controller
   */
  protected void setup() {
    uiStateService.setupTimelineBackground(timelineBackground, timelineHeight);
    setupTimelineChords();
  }

  /**
   Populate the timeline with sequence chords
   */
  private void setupTimelineChords() {
    for (SequenceChordController controller : sequenceChordControllers) controller.teardown();
    timelineChordContainer.getChildren().clear();
    if (uiStateService.currentProgramSequenceProperty().isNull().get()) return;
    // chord in order of position
    List<ProgramSequenceChord> chords = projectService.getContent().getChordsOfSequence(uiStateService.currentProgramSequenceProperty().get().getId())
      .stream().sorted(Comparator.comparing(ProgramSequenceChord::getPosition)).toList();
    for (int i = 0; i < chords.size(); i++) {
      Double duration = i < chords.size() - 1
        ? chords.get(i + 1).getPosition() - chords.get(i).getPosition()
        : uiStateService.currentProgramSequenceProperty().get().getTotal() - chords.get(i).getPosition();
      addChord(chords.get(i), duration);
    }
  }

  /**
   Add an chord to the timeline
   */
  private void addChord(ProgramSequenceChord chord, Double duration) {
    try {
      FXMLLoader loader = new FXMLLoader(chordFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      SequenceChordController controller = loader.getController();
      sequenceChordControllers.add(controller);
      controller.setup(chord.getId(), duration,
        this::setupTimelineChords,
        () -> {
          controller.teardown();
          sequenceChordControllers.remove(controller);
          timelineChordContainer.getChildren().remove(root);
          projectService.deleteContent(ProgramSequenceChord.class, chord.getId());
        });
      timelineChordContainer.getChildren().add(root);

    } catch (Exception e) {
      LOG.error("Failed to add chord to timeline! {}\n{}", e, StringUtils.formatStackTrace(e));
    }
  }
}
