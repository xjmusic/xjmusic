package io.xj.gui.controllers.content.program.edit_mode;

import io.xj.gui.controllers.content.common.DragZone;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProgramSequencePatternEventItemController {
  static final Logger LOG = LoggerFactory.getLogger(ProgramSequencePatternEventItemController.class);
  private final ObjectProperty<ProgramSequencePatternEvent> programSequencePatternEventObjectProperty = new SimpleObjectProperty<>();
  private final ProjectService projectService;
  private final UIStateService uiStateService;
  protected final DoubleProperty getEventPositionProperty = new SimpleDoubleProperty(0);

  private final DoubleProperty widthProperty = new SimpleDoubleProperty();
  private static final int RESIZE_MARGIN = 10;
  private final DoubleProperty translateDoubleProperty = new SimpleDoubleProperty();

  private double originalXPosition;
  private double originalYPosition;
  private DragZone currentDragZone;
  private double dragStartX;
  private double minWidth = 0;
  
  @FXML
  public AnchorPane timelineEventPropertyParent;
  
  @FXML
  public Button deleteTimelineProperty;
  
  @FXML
  public Label positionLabel;
  
  @FXML
  public Label durationLabel;
  
  @FXML
  public Label velocityLabel;
  
  @FXML
  public Label tonesLabel;
  
  @FXML
  private AnchorPane timeline;
  
  @FXML
  private Parent root;

  public ProgramSequencePatternEventItemController(
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    this.projectService = projectService;
    this.uiStateService = uiStateService;
  }

  ObjectProperty<ProgramSequencePatternEvent> programEvent = new SimpleObjectProperty<>();

  public void setup(Parent root, AnchorPane timeline, ProgramSequencePatternEvent programSequencePatternEvent, VoiceController voiceController) {
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
  }

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

}
