package io.xj.gui.controllers.content.program.edit_mode;

import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VoiceTrackEventController {
  static final Logger LOG = LoggerFactory.getLogger(VoiceTrackEventController.class);
  private final int trackHeight;
  private final ProjectService projectService;
  private final UIStateService uiStateService;
  private UUID eventId;
  private Runnable handleDelete;

  /*
TODO cleanup unused
  protected final DoubleProperty getEventPositionProperty = new SimpleDoubleProperty(0);
  private final DoubleProperty widthProperty = new SimpleDoubleProperty();
  private static final int RESIZE_MARGIN = 10;
  private final DoubleProperty translateDoubleProperty = new SimpleDoubleProperty();
  private double originalXPosition;
  private double originalYPosition;
  private DragZone currentDragZone;
  private double dragStartX;
  private double minWidth = 0;
*/

  @FXML
  AnchorPane container;

  @FXML
  Label tonesLabel;

  public VoiceTrackEventController(
    @Value("${programEditor.trackHeight}") int trackHeight,
    @Value("classpath:/views/content/common/popup-action-menu.fxml") Resource popupActionMenuFxml,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    this.trackHeight = trackHeight;
    this.projectService = projectService;
    this.uiStateService = uiStateService;
  }

  /**
   Set up the event controller

   @param eventId for which to set up the controller
   */
  public void setup(UUID eventId, Runnable handleDelete) {
    this.eventId = eventId;
    this.handleDelete = handleDelete;

    var event = projectService.getContent().getProgramSequencePatternEvent(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
    var zoom = uiStateService.programEditorZoomProperty().get().value();
    var baseSizePerBeat = uiStateService.getProgramEditorBaseSizePerBeat();
    container.setLayoutX(baseSizePerBeat * event.getPosition() * zoom);
    container.setPrefWidth(baseSizePerBeat * event.getDuration() * zoom);
    container.setPrefHeight(trackHeight);

    tonesLabel.setText(event.getTones());
  }

  /**
   Teardown the event controller
   */
  public void teardown() {
    // TODO teardown
  }

  /**
   Handle pressed delete
   */
  @FXML
  public void handlePressedDelete() {
    handleDelete.run();
  }

  public void handleCenterClicked(MouseEvent mouseEvent) {
    if (mouseEvent.isSecondaryButtonDown()) {
      LOG.info("Right Clicked");

    } else if (mouseEvent.getClickCount() == 2) {
      LOG.info("Double Clicked");
    }

  }

  public void handleCenterDragEntered(MouseDragEvent mouseDragEvent) {
    LOG.info("Center: Mouse Drag Entered");
    // TODO enter event move state
  }

  public void handleCenterDragged(MouseEvent mouseEvent) {
    LOG.info("Center: Mouse Dragged");
    // TODO move event
  }

  public void handleCenterDragExited(MouseDragEvent mouseDragEvent) {
    LOG.info("Center: Mouse Drag Exited");
    // TODO exit event move state
  }

  public void handleLeftDragEntered(MouseDragEvent mouseDragEvent) {
    LOG.info("Left: Mouse Drag Entered");
    // TODO enter event resize left side state
  }

  public void handleLeftDragged(MouseEvent mouseEvent) {
    LOG.info("Left: Mouse Dragged");
    // TODO resize event left side
  }

  public void handleLeftDragExited(MouseDragEvent mouseDragEvent) {
    LOG.info("Left: Mouse Drag Exited");
    // TODO exit event resize left side state
  }

  public void handleRightDragEntered(MouseDragEvent mouseDragEvent) {
    LOG.info("Right: Mouse Drag Entered");
    // TODO enter event resize right side state
  }

  public void handleRightDragged(MouseEvent mouseEvent) {
    LOG.info("Right: Mouse Dragged");
    // TODO resize event right side
  }

  public void handleRightDragExited(MouseDragEvent mouseDragEvent) {
    LOG.info("Right: Mouse Drag Exited");
    // TODO exit event resize right side state
  }

  /*
 TODO
    this.timeline = timeline;
    this.root = root;
    minWidth = timelineEventPropertyParent.getMinWidth();
    programEvent.set(programSequencePatternEvent);
    deleteTimelineProperty.setOnAction(e -> setDeleteTimelineProperty());
    //allows the width to go to the lowest value
    timelineEventPropertyParent.setMinWidth((uiStateService.getProgramEditorBaseSizePerBeat() * programEditorController.getSequenceTotal() * programEditorController.getZoomFactor()) /
      (programEditorController.getSequenceTotal() * programEditorController.getTimelineGridSize()));
    translateDoubleProperty.bind(Bindings.createDoubleBinding(
      () -> (uiStateService.getProgramEditorBaseSizePerBeat() * programEditorController.getSequenceTotal() *
        programEditorController.getZoomFactor() / (programEditorController.getSequenceTotal() * programEditorController.getTimelineGridSize()))
      ,
      programEditorController.getSequenceTotalProperty(),
      programEditorController.getZoomFactorProperty()
    ));
    // Bind the minWidthProperty of timelineEventPropertyParent
    timelineEventPropertyParent.minWidthProperty().bind(
      Bindings.createDoubleBinding(
        () -> {
          double sequenceTotal = programEditorController.getSequenceTotal();
          double timelineGridSize = programEditorController.getTimelineGridSize();
          double baseSizePerBeat = uiStateService.getProgramEditorBaseSizePerBeat();
          double zoomFactor = programEditorController.getZoomFactor();
          return (baseSizePerBeat * sequenceTotal * zoomFactor / (sequenceTotal * timelineGridSize)) + widthProperty.doubleValue();
        }
        ,
        programEditorController.getSequenceTotalProperty(),
        programEditorController.getZoomFactorProperty(),
        widthProperty
      )
    );

    programSequencePatternEventObjectProperty.set(programSequencePatternEvent);
    bindTranslateXToPositionProperty();
    updateLabels(programSequencePatternEvent);
    timelineEventPropertyParent.setOnMouseMoved(this::onMouseOver);
    timelineEventPropertyParent.setOnMousePressed(this::onMouseDown);
    timelineEventPropertyParent.setOnMouseReleased(this::onMouseReleased);
*/

/*

TODO


  protected void updatePosition(double position) {
    ProgramSequencePatternEvent newEvent = programEvent.get();
    newEvent.setPosition((float) position);
    projectService.update(newEvent);
    programEvent.set(newEvent);
  }


  private void onMouseDown(MouseEvent event) {
    timeline.setMouseTransparent(true);
    originalXPosition = event.getX();
    originalYPosition = event.getY();
    currentDragZone = computeDragZone(event);
  }

  private void onMouseReleased(MouseEvent event) {
    double topAnchor = AnchorPane.getTopAnchor(timelineEventPropertyParent);
    double bottomAnchor = AnchorPane.getBottomAnchor(timelineEventPropertyParent);

    if (currentDragZone.equals(DragZone.CENTRE)) {
      double newPosition = event.getX() - originalXPosition;
      updatePosition(originalXPosition);
      getEventPositionProperty.set(getEventPositionProperty.get() + newPosition);
    } else if (currentDragZone.equals(DragZone.LEFT)) {
      double newWidth = event.getX() + originalXPosition;
      if (newWidth > 0) {
        widthProperty.set(widthProperty.get() - translateDoubleProperty.get());
      } else {
        widthProperty.set(widthProperty.get() + translateDoubleProperty.get());
      }
    } else if (currentDragZone.equals(DragZone.RIGHT)) {
      double newWidth = event.getX() - originalXPosition;
      if (newWidth > 0) {
        widthProperty.set(widthProperty.get() + translateDoubleProperty.get());
      } else {
        widthProperty.set(widthProperty.get() - translateDoubleProperty.get());
      }

    } else if (currentDragZone.equals(DragZone.TOP)) {

      double newDifference = event.getY() - originalYPosition;
      AnchorPane.setTopAnchor(timelineEventPropertyParent, topAnchor + (newDifference / 2));
      AnchorPane.setBottomAnchor(timelineEventPropertyParent, bottomAnchor + (newDifference / 2));

    } else if (currentDragZone.equals(DragZone.BOTTOM)) {
      double newDifference = event.getY() - originalYPosition;
      AnchorPane.setTopAnchor(timelineEventPropertyParent, topAnchor - (newDifference / 2));
      AnchorPane.setBottomAnchor(timelineEventPropertyParent, bottomAnchor - (newDifference / 2));
    }
    timeline.setMouseTransparent(false);
  }

*/
/*
TODO
  public void bindTranslateXToPositionProperty() {
    timelineEventPropertyParent.translateXProperty().bind(Bindings.createDoubleBinding(
      () -> ((uiStateService.getProgramEditorBaseSizePerBeat() * programEditorController.getZoomFactor()) + getEventPositionProperty.get())
      ,
      programEditorController.getZoomFactorProperty(),
      uiStateService.getProgramEditorBaseSizePerBeat(),
      getEventPositionProperty
    ));
  }
*/

/*

TODO

  protected DragZone computeDragZone(MouseEvent event) {
    if (event.getY() > (timelineEventPropertyParent.getHeight() - RESIZE_MARGIN))
      return DragZone.BOTTOM;
    if (event.getX() > (timelineEventPropertyParent.getWidth() - RESIZE_MARGIN))
      return DragZone.RIGHT;
    if (event.getSceneX() <= (getLeftBorderPosition(timelineEventPropertyParent) + RESIZE_MARGIN))
      return DragZone.LEFT;
    if (event.getSceneY() <= (getTopBorderPosition(timelineEventPropertyParent) + RESIZE_MARGIN))
      return DragZone.TOP;
    return DragZone.CENTRE;
  }

  public static double getTopBorderPosition(Node node) {
    // Get the local bounds of the AnchorPane
    Bounds localBounds = node.getBoundsInLocal();

    // Transform the local bounds to scene coordinates
    Bounds sceneBounds = node.localToScene(localBounds);

    // Calculate the position of the left border
    return sceneBounds.getMinY();
  }


  private void updateLabels(ProgramSequencePatternEvent event) {
    // Update the labels with the values from the ProgramSequencePatternEvent object

    Platform.runLater(() -> {
      positionLabel.setText(event.getPosition().toString());
      durationLabel.setText(event.getDuration().toString());
      velocityLabel.setText(event.getVelocity().toString());
      tonesLabel.setText(event.getTones());
    });
  }

  private void setDeleteTimelineProperty() {
    timeline.getChildren().remove(root);
    projectService.deleteContent(programSequencePatternEventObjectProperty.get());
  }

  private void onMouseOver(MouseEvent event) {
    currentDragZone = computeDragZone(event);
    if (currentDragZone != DragZone.CENTRE) {
      if (currentDragZone == DragZone.BOTTOM) {
        timelineEventPropertyParent.setCursor(Cursor.S_RESIZE);
      }

      if (currentDragZone == DragZone.RIGHT) {
        timelineEventPropertyParent.setCursor(Cursor.E_RESIZE);
      }
      if (currentDragZone == DragZone.LEFT) {
        timelineEventPropertyParent.setCursor(Cursor.W_RESIZE);
      }
      if (currentDragZone == DragZone.TOP) {
        timelineEventPropertyParent.setCursor(Cursor.N_RESIZE);
      }
    } else {
      timelineEventPropertyParent.setCursor(Cursor.OPEN_HAND);
    }
  }

  public static double getLeftBorderPosition(Node node) {
    // Get the local bounds of the AnchorPane
    Bounds localBounds = node.getBoundsInLocal();

    // Transform the local bounds to scene coordinates
    Bounds sceneBounds = node.localToScene(localBounds);

    // Calculate the position of the left border
    return sceneBounds.getMinX();
  }

  public static double getRightBorderPosition(Node node) {
    // Get the local bounds of the node
    Bounds localBounds = node.getBoundsInLocal();

    // Transform the local bounds to parent's coordinate system
    Bounds parentBounds = node.localToParent(localBounds);

    // Calculate the position of the right border
    return parentBounds.getMaxX();
  }
*/

}
