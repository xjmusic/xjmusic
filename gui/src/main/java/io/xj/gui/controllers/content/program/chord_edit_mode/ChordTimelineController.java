package io.xj.gui.controllers.content.program.chord_edit_mode;

import io.xj.gui.ProjectController;
import io.xj.gui.modes.GridChoice;
import io.xj.gui.modes.ZoomChoice;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.hub.tables.pojos.ProgramSequence;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;

@Component
public class ChordTimelineController extends ProjectController {
  private final Collection<Runnable> unsubscriptions = new HashSet<>();
  private final int controlWidth;
  private final ProjectService projectService;
  private final UIStateService uiStateService;
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

  public ChordTimelineController(
    @Value("classpath:/views/content/program/edit_chord_mode/chord-timeline.fxml") Resource fxml,
    @Value("${programEditor.voiceControlWidth}") int voiceControlWidth,
    @Value("${programEditor.chordTimelineHeight}") int timelineHeight,
    ApplicationContext ac,
    ThemeService themeService,
    UIStateService uiStateService,
    ProjectService projectService
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
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
  }

  @Override
  public void onStageClose() {
    // no op
  }

  @FXML
  void handlePressedAddChord() {
    // todo open the add chord modal
  }

  /**
   Set up the voice controller
   */
  protected void setup() {
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

    Platform.runLater(this::setupTimeline);
  }

  /**
   Teardown the voice controller
   */
  public void teardown() {
    for (Runnable unsubscription : unsubscriptions) unsubscription.run();
  }

  /**
   Populate the track timeline
   */
  private void setupTimeline() {
    uiStateService.setupTimelineBackground(timelineBackground, timelineHeight);
    setupTimelineChords();
  }

  /**
   Populate the timeline with sequence chords
   */
  private void setupTimelineChords() {
    // TODO: setup timeline chords
  }

}
