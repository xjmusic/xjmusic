package io.xj.gui.controllers.content.program.chord_edit_mode;

import io.xj.gui.controllers.content.common.PopupActionMenuController;
import io.xj.gui.modes.GridChoice;
import io.xj.gui.modes.ZoomChoice;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.UiUtils;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.hub.tables.pojos.ProgramSequenceChordVoicing;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.util.StringUtils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ChordVoiceTimelineController {
  static final Logger LOG = LoggerFactory.getLogger(ChordVoiceTimelineController.class);
  private final Collection<Runnable> unsubscriptions = new HashSet<>();
  private final Collection<SequenceChordVoicingController> voicingControllers = new HashSet<>();
  private final int controlWidth;
  private final ApplicationContext ac;
  private final ProjectService projectService;
  private final UIStateService uiStateService;
  private final Runnable updateVoiceType;
  private final Resource voicingFxml;
  private final int timelineHeight;
  private UUID programVoiceId;
  private Runnable handleDeleteVoice;

  @FXML
  VBox controlContainer;

  @FXML
  HBox container;

  @FXML
  Button voiceActionLauncher;

  @FXML
  ComboBox<InstrumentType> voiceTypeChooser;

  @FXML
  Pane timelineBackground;

  @FXML
  AnchorPane timelineChordVoicingContainer;

  public ChordVoiceTimelineController(
    @Value("classpath:/views/content/program/edit_chord_mode/sequence-chord-voicing.fxml") Resource voicingFxml,
    @Value("${programEditor.voiceControlWidth}") int voiceControlWidth,
    @Value("${programEditor.chordTimelineHeight}") int timelineHeight,
    ApplicationContext ac,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    this.voicingFxml = voicingFxml;
    this.timelineHeight = timelineHeight;
    this.controlWidth = voiceControlWidth;
    this.ac = ac;
    this.projectService = projectService;
    this.uiStateService = uiStateService;

    updateVoiceType = () -> projectService.update(ProgramVoice.class, programVoiceId, "type", voiceTypeChooser.getValue());
  }

  @FXML
  void handlePressedVoiceActionLauncher() {
    uiStateService.launchPopupActionMenu(
      voiceActionLauncher,
      (PopupActionMenuController controller) -> controller.setup(
        "New Voice",
        null,
        handleDeleteVoice,
        null
      )
    );
  }

  /**
   Set up the voice controller

   @param programVoiceId    the voice id
   @param handleDeleteVoice callback to delete voice
   */
  protected void setup(UUID programVoiceId, Runnable handleDeleteVoice) {
    this.programVoiceId = programVoiceId;
    this.handleDeleteVoice = handleDeleteVoice;

    ProgramVoice voice = projectService.getContent().getProgramVoice(programVoiceId).orElseThrow(() -> new RuntimeException("Voice not found!"));

    controlContainer.setMinWidth(controlWidth);
    controlContainer.setMaxWidth(controlWidth);
    timelineBackground.setMinHeight(timelineHeight);
    timelineBackground.setMaxHeight(timelineHeight);

    voiceTypeChooser.setItems(FXCollections.observableArrayList(InstrumentType.values()));
    voiceTypeChooser.setValue(voice.getType());
    unsubscriptions.add(UiUtils.onChange(voiceTypeChooser.valueProperty(), updateVoiceType));
    UiUtils.blurOnSelection(voiceTypeChooser);

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
    unsubscriptions.add(projectService.addProjectUpdateListener(ProgramSequenceChord.class, this::setupTimeline));

    container.setMinHeight(timelineHeight);

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
    setupTimelineChordVoicings();
  }

  /**
   Populate the timeline with sequence chord voicings
   */
  private void setupTimelineChordVoicings() {
    for (SequenceChordVoicingController controller : voicingControllers) controller.teardown();
    timelineChordVoicingContainer.getChildren().clear();
    if (uiStateService.currentProgramSequenceProperty().isNull().get()) return;
    // chord in order of position
    List<ProgramSequenceChord> chords = projectService.getContent().getChordsOfSequence(uiStateService.currentProgramSequenceProperty().get().getId())
      .stream().sorted(Comparator.comparing(ProgramSequenceChord::getPosition)).toList();
    for (int i = 0; i < chords.size(); i++) {
      Double duration = i < chords.size() - 1
        ? chords.get(i + 1).getPosition() - chords.get(i).getPosition()
        : uiStateService.currentProgramSequenceProperty().get().getTotal() - chords.get(i).getPosition();
      addChord(getOrCreateVoicing(chords.get(i)), chords.get(i).getPosition(), duration);
    }
  }

  private ProgramSequenceChordVoicing getOrCreateVoicing(ProgramSequenceChord programSequenceChord) {
    var voicing = projectService.getContent().getVoicingsOfChordAndVoice(programSequenceChord.getId(), programVoiceId).stream().findAny();
    if (voicing.isPresent()) return voicing.get();
    var newVoicing = new ProgramSequenceChordVoicing();
    newVoicing.setId(UUID.randomUUID());
    newVoicing.setProgramId(programSequenceChord.getProgramId());
    newVoicing.setProgramSequenceChordId(programSequenceChord.getId());
    newVoicing.setProgramVoiceId(programVoiceId);
    newVoicing.setNotes("");
    projectService.update(newVoicing);
    return newVoicing;
  }

  /**
   Add an chord to the timeline
   */
  private void addChord(ProgramSequenceChordVoicing voicing, Double position, Double duration) {
    try {
      FXMLLoader loader = new FXMLLoader(voicingFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      SequenceChordVoicingController controller = loader.getController();
      voicingControllers.add(controller);
      controller.setup(voicing.getId(), position, duration);
      timelineChordVoicingContainer.getChildren().add(root);

    } catch (Exception e) {
      LOG.error("Failed to add chord to timeline! {}\n{}", e, StringUtils.formatStackTrace(e));
    }
  }

}
