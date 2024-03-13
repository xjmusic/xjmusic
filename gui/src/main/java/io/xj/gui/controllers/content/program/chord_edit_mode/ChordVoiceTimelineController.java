package io.xj.gui.controllers.content.program.chord_edit_mode;

import io.xj.gui.controllers.content.common.PopupActionMenuController;
import io.xj.gui.modes.GridChoice;
import io.xj.gui.modes.ZoomChoice;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.UiUtils;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramVoice;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ChordVoiceTimelineController {
  private final Collection<Runnable> unsubscriptions = new HashSet<>();
  private final int controlWidth;
  private final ProjectService projectService;
  private final UIStateService uiStateService;
  private final Runnable updateVoiceType;
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

  public ChordVoiceTimelineController(
    @Value("${programEditor.voiceControlWidth}") int voiceControlWidth,
    @Value("${programEditor.chordTimelineHeight}") int timelineHeight,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    this.timelineHeight = timelineHeight;
    this.controlWidth = voiceControlWidth;
    this.projectService = projectService;
    this.uiStateService = uiStateService;

    updateVoiceType = () -> projectService.update(ProgramVoice.class, programVoiceId, "type", voiceTypeChooser.getValue());
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
   Populate the timeline with sequence chord voicings for the given voice
   */
  private void setupTimelineChordVoicings() {
    // TODO: setup timeline chord voicings
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

}
