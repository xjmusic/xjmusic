package io.xj.gui.controllers.content.program.chord_edit_mode;

import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.UiUtils;
import io.xj.hub.pojos.ProgramSequenceChordVoicing;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SequenceChordVoicingController {
  private final int timelineHeight;
  private final int chordSpaceBetween;
  private final ProjectService projectService;
  private final UIStateService uiStateService;
  private final DoubleProperty beatWidth = new SimpleDoubleProperty(0);
  private ProgramSequenceChordVoicing chordVoicing;

  @FXML
  AnchorPane container;

  @FXML
  TextField notesField;

  public SequenceChordVoicingController(
    @Value("${programEditor.chordTimelineHeight}") int timelineHeight,
    @Value("${programEditor.chordSpaceBetween}") int chordSpaceBetween,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    this.timelineHeight = timelineHeight;
    this.chordSpaceBetween = chordSpaceBetween;
    this.projectService = projectService;
    this.uiStateService = uiStateService;
  }

  /**
   Set up the chord voicing controller

   @param chordVoicingId for which to set up the controller
   @param duration       of the chord display (until next chord or end of sequence)
   */
  public void setup(UUID chordVoicingId, Double position, Double duration) {
    this.chordVoicing = projectService.getContent().getProgramSequenceChordVoicing(chordVoicingId).orElseThrow(() -> new RuntimeException("Chord voicing not found"));
    this.beatWidth.set(uiStateService.getProgramEditorBaseSizePerBeat() * uiStateService.programEditorZoomProperty().get().value());
    container.setLayoutX(beatWidth.getValue() * position);
    container.setPrefWidth(beatWidth.getValue() * duration - chordSpaceBetween);
    container.setMinHeight(timelineHeight);
    container.setMaxHeight(timelineHeight);

    notesField.setText(chordVoicing.getNotes());
    notesField.textProperty().addListener((observable, oldValue, newValue) -> {
      chordVoicing.setNotes(newValue);
      projectService.update(ProgramSequenceChordVoicing.class, chordVoicingId, "notes", newValue);
    });
    UiUtils.blurOnEnterKeyPress(notesField);
  }

  /**
   Teardown the chord controller
   */
  public void teardown() {
    // no op
  }
}
