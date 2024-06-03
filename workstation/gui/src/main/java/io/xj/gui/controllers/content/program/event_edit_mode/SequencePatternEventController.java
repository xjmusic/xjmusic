package io.xj.gui.controllers.content.program.event_edit_mode;

import io.xj.gui.controllers.content.common.PopupActionMenuController;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.LaunchMenuPosition;
import io.xj.model.pojos.ProgramSequencePattern;
import io.xj.model.pojos.ProgramSequencePatternEvent;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SequencePatternEventController {
  private static final double UNSNAPPED_POSITION_GRAIN = 0.001;
  private final Resource propertiesFxml;
  private final int timelineHeight;
  private final ProjectService projectService;
  private final UIStateService uiStateService;
  private final IntegerProperty dragStartMouseX = new SimpleIntegerProperty();
  private final IntegerProperty dragStartMouseY = new SimpleIntegerProperty();
  private final DoubleProperty dragStartPosition = new SimpleDoubleProperty(0);
  private final DoubleProperty dragStartDuration = new SimpleDoubleProperty(0);
  private final DoubleProperty roundPositionToNearest = new SimpleDoubleProperty(0);
  private final DoubleProperty beatWidth = new SimpleDoubleProperty(0);
  private Runnable handleDelete;
  private ProgramSequencePatternEvent event;
  private ProgramSequencePattern pattern;

  @FXML
  AnchorPane container;

  @FXML
  Pane velocityShader;

  @FXML
  Label tonesLabel;

  public SequencePatternEventController(
    @Value("classpath:/views/content/program/edit_event_mode/sequence-pattern-event-properties.fxml") Resource propertiesFxml,
    @Value("${programEditor.eventTimelineHeight}") int timelineHeight,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    this.propertiesFxml = propertiesFxml;
    this.timelineHeight = timelineHeight;
    this.projectService = projectService;
    this.uiStateService = uiStateService;
  }

  /**
   Set up the event controller

   @param eventId for which to set up the controller
   */
  public void setup(UUID eventId, Runnable handleDelete) {
    this.handleDelete = handleDelete;

    this.event = projectService.getContent().getProgramSequencePatternEvent(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
    this.pattern = projectService.getContent().getProgramSequencePattern(event.getProgramSequencePatternId()).orElseThrow(() -> new RuntimeException("Pattern not found"));
    this.beatWidth.set(uiStateService.getProgramEditorBaseSizePerBeat() * uiStateService.programEditorZoomProperty().get().value());
    container.setLayoutX(beatWidth.getValue() * event.getPosition());
    container.setPrefWidth(beatWidth.getValue() * event.getDuration());
    container.setMinHeight(timelineHeight);
    container.setMaxHeight(timelineHeight);
    tonesLabel.setText(event.getTones());
    velocityShader.setPrefHeight(timelineHeight * event.getVelocity());
  }

  /**
   Teardown the event controller
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
        (SequencePatternEventPropertiesController controller) -> controller.setup(event.getId()),
        LaunchMenuPosition.from(container),
        true,
        () -> setup(event.getId(), handleDelete));
    }

  }

  @FXML
  void handleCenterPressed(MouseEvent mouse) {
    startDrag(mouse);
  }

  @FXML
  void handleCenterDragged(MouseEvent mouse) {
    double position = (double)
      Math.round(
        Math.min(pattern.getTotal() - roundPositionToNearest.get(),
          Math.max(0,
            dragStartPosition.get() + (mouse.getScreenX() - dragStartMouseX.get()) / beatWidth.getValue()
          )
        ) / roundPositionToNearest.get()
      ) * roundPositionToNearest.get();
    event.setPosition((float) position);
    projectService.update(event);
    container.setLayoutX(beatWidth.getValue() * event.getPosition());
  }

  @FXML
  void handleLeftPressed(MouseEvent mouse) {
    startDrag(mouse);
  }

  @FXML
  void handleLeftDragged(MouseEvent mouse) {
    double position = (double)
      Math.round(
        Math.min(event.getPosition() + event.getDuration(),
          Math.max(0,
            dragStartPosition.get() + (mouse.getScreenX() - dragStartMouseX.get()) / beatWidth.getValue()
          )
        ) / roundPositionToNearest.get()
      ) * roundPositionToNearest.get();
    double duration = dragStartDuration.get() - (position - dragStartPosition.get());
    event.setPosition((float) position);
    event.setDuration((float) duration);
    projectService.update(event);
    container.setLayoutX(beatWidth.getValue() * event.getPosition());
    container.setPrefWidth(beatWidth.getValue() * event.getDuration());
  }

  @FXML
  void handleRightPressed(MouseEvent mouse) {
    startDrag(mouse);
  }

  @FXML
  void handleRightDragged(MouseEvent mouse) {
    double endPosition = (double)
      Math.round(
        Math.max(event.getPosition(),
          dragStartPosition.get() + dragStartDuration.get() + (mouse.getScreenX() - dragStartMouseX.get()) / beatWidth.getValue()
        ) / roundPositionToNearest.get()
      ) * roundPositionToNearest.get();
    double duration = endPosition - event.getPosition();
    event.setDuration((float) duration);
    projectService.update(event);
    container.setPrefWidth(beatWidth.getValue() * event.getDuration());
  }

  /**
   Start mouse drag by setting the initial values
   */
  private void startDrag(MouseEvent mouse) {
    dragStartMouseX.set((int) mouse.getScreenX());
    dragStartMouseY.set((int) mouse.getScreenY());
    dragStartPosition.set(event.getPosition());
    dragStartDuration.set(event.getDuration());
    roundPositionToNearest.set(uiStateService.programEditorSnapProperty().get() ? uiStateService.programEditorGridProperty().get().value() : UNSNAPPED_POSITION_GRAIN);
    beatWidth.set(uiStateService.getProgramEditorBaseSizePerBeat() * uiStateService.programEditorZoomProperty().get().value());
  }
}
