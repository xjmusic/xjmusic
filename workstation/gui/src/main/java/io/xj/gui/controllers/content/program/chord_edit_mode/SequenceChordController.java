package io.xj.gui.controllers.content.program.chord_edit_mode;

import io.xj.gui.controllers.content.common.PopupActionMenuController;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.LaunchMenuPosition;
import io.xj.hub.pojos.ProgramSequenceChord;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SequenceChordController {
  private final Resource propertiesFxml;
  private final int timelineHeight;
  private final int chordSpaceBetween;
  private final ProjectService projectService;
  private final UIStateService uiStateService;
  private final DoubleProperty beatWidth = new SimpleDoubleProperty(0);
  private Runnable handleUpdate;
  private Runnable handleDelete;
  private ProgramSequenceChord chord;

  @FXML
  AnchorPane container;

  @FXML
  Label nameField;

  public SequenceChordController(
    @Value("classpath:/views/content/program/edit_chord_mode/sequence-chord-properties.fxml") Resource propertiesFxml,
    @Value("${programEditor.chordTimelineHeight}") int timelineHeight,
    @Value("${programEditor.chordSpaceBetween}") int chordSpaceBetween,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    this.propertiesFxml = propertiesFxml;
    this.timelineHeight = timelineHeight;
    this.chordSpaceBetween = chordSpaceBetween;
    this.projectService = projectService;
    this.uiStateService = uiStateService;
  }

  /**
   Set up the chord controller

   @param chordId      for which to set up the controller
   @param duration     of the chord display (until next chord or end of sequence)
   @param handleUpdate to call when the chord is updated
   @param handleDelete to call when the chord is deleted
   */
  public void setup(UUID chordId, Double duration, Runnable handleUpdate, Runnable handleDelete) {
    this.handleUpdate = handleUpdate;
    this.handleDelete = handleDelete;

    this.chord = projectService.getContent().getProgramSequenceChord(chordId).orElseThrow(() -> new RuntimeException("Chord not found"));
    this.beatWidth.set(uiStateService.getProgramEditorBaseSizePerBeat() * uiStateService.programEditorZoomProperty().get().value());
    container.setLayoutX(beatWidth.getValue() * chord.getPosition());
    container.setPrefWidth(beatWidth.getValue() * duration - chordSpaceBetween);
    container.setMinHeight(timelineHeight);
    container.setMaxHeight(timelineHeight);

    nameField.setText(chord.getName());
  }

  /**
   Teardown the chord controller
   */
  public void teardown() {
    // no op
  }

  @FXML
  void handleCenterClicked(MouseEvent mouse) {
    if (mouse.getButton().equals(MouseButton.SECONDARY) || mouse.isControlDown()) {
      uiStateService.launchQuickActionMenu(
        container,
        mouse,
        (PopupActionMenuController controller) -> controller.setup(
          null,
          null,
          handleDelete,
          null
        )
      );

    } else if (mouse.getClickCount() == 2) {
      uiStateService.launchModalMenu(
        propertiesFxml,
        container,
        (SequenceChordPropertiesController controller) -> controller.setupEditing(chord.getId()),
        LaunchMenuPosition.from(mouse),
        true,
        handleUpdate);
    }
  }
}
