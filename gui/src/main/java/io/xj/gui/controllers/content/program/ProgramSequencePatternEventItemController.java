package io.xj.gui.controllers.content.program;

import io.xj.gui.controllers.content.common.DragZone;
import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
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
    private double dragStartX;
    @FXML
    private AnchorPane timeline;
    @FXML
    private Parent root;
    int id;
    double minWidth = 0;
    private final ObjectProperty<ProgramSequencePatternEvent> programSequencePatternEventObjectProperty = new SimpleObjectProperty<>();
    private final ProjectService projectService;
    static final Logger LOG = LoggerFactory.getLogger(ProgramSequencePatternEventItemController.class);
    private VoiceController voiceController;
    private final ProgramEditorController programEditorController;

    private final DoubleProperty eventPositionProperty = new SimpleDoubleProperty();

    private final DoubleProperty widthProperty = new SimpleDoubleProperty(0);


    private double offsetX = 0;

    public ProgramSequencePatternEventItemController(ProjectService projectService, ProgramEditorController programEditorController) {
        this.projectService = projectService;
        this.programEditorController = programEditorController;
    }

    public void setUp(Parent root, AnchorPane timeline, ProgramSequencePatternEvent programSequencePatternEvent, VoiceController voiceController) {
        this.voiceController = voiceController;
        deleteTimelineProperty.setOnAction(e -> setDeleteTimelineProperty());
        //allows the width to go to the lowest value
        timelineEventPropertyParent.setMinWidth((voiceController.getBaseSizePerBeat().doubleValue() * programEditorController.getSequenceTotal() * programEditorController.getZoomFactor()) /
                (programEditorController.getSequenceTotal() * programEditorController.getTimelineGridSize()));
        // Bind the minWidthProperty of timelineEventPropertyParent
        timelineEventPropertyParent.minWidthProperty().bind(
                Bindings.createDoubleBinding(
                        () -> {
                            double sequenceTotal = programEditorController.getSequenceTotal();
                            double timelineGridSize = programEditorController.getTimelineGridSize();
                            double baseSizePerBeat = voiceController.getBaseSizePerBeat().doubleValue();
                            double zoomFactor = programEditorController.getZoomFactor();
                            return (baseSizePerBeat * sequenceTotal * zoomFactor / (sequenceTotal * timelineGridSize)) + widthProperty.doubleValue();
                        }
                        ,
                        programEditorController.getSequenceTotalProperty(),
                        programEditorController.getZoomFactorProperty(),
                        widthProperty
                )
        );
        this.timeline = timeline;
        this.root = root;
        this.voiceController = voiceController;
        minWidth = timelineEventPropertyParent.getPrefWidth();
        programSequencePatternEventObjectProperty.set(programSequencePatternEvent);
        updateLabels(programSequencePatternEvent);

        timelineEventPropertyParent.translateXProperty().bind(eventPositionProperty);
        timelineEventPropertyParent.setOnMousePressed(event -> dragStartX = event.getX());
        timelineEventPropertyParent.setOnMouseDragged(this::dragItem);
        timelineEventPropertyParent.setOnMouseMoved(this::onMouseOver);
        timelineEventPropertyParent.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            // Calculate the distance moved from the initial mouse press
            double deltaX = event.getX() - dragStartX;
            // Define a threshold for distinguishing clicks and drags (e.g., 5 pixels)
            double dragThreshold = 5.0;
            if (Math.abs(deltaX) < dragThreshold) {
                event.consume(); // Consume the event to prevent it from reaching the timeLineAnchorpane
            }
        });

    }

    private static final int RESIZE_MARGIN = 10;


    protected DragZone computeDragZone(MouseEvent event) {
        if (event.getY() > (timelineEventPropertyParent.getHeight() - RESIZE_MARGIN))
            return DragZone.BOTTOM;
        if (event.getX() > (timelineEventPropertyParent.getWidth() - RESIZE_MARGIN))
            return DragZone.LEFT;
        if (event.getSceneX() <= (getLeftBorderPosition(timelineEventPropertyParent) + RESIZE_MARGIN))
            return DragZone.RIGHT;
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

    private DragZone currentDragZone;


    private void dragItem(MouseEvent event){
         currentDragZone = computeDragZone(event);
        switch (currentDragZone){
            case CENTRE -> {
                timelineEventPropertyParent.setCursor(Cursor.CLOSED_HAND);
                position(event);
            }
            case RIGHT -> increaseWidthToRight(event);
        }

    }

    private void onMouseOver(MouseEvent event){
        currentDragZone = computeDragZone(event);
        switch (currentDragZone){
            case CENTRE -> timelineEventPropertyParent.setCursor(Cursor.OPEN_HAND);
            case RIGHT -> timelineEventPropertyParent.setCursor(Cursor.E_RESIZE);
            case LEFT -> timelineEventPropertyParent.setCursor(Cursor.W_RESIZE);
            case TOP -> timelineEventPropertyParent.setCursor(Cursor.N_RESIZE);
            case BOTTOM -> timelineEventPropertyParent.setCursor(Cursor.S_RESIZE);
        }
    }

    private void increaseWidthToRight(MouseEvent event){
        // Calculate the difference in X position from the initial drag start
        double deltaX = event.getX() - dragStartX;
//        // Update the width of the timelineParentAnchorPane by adjusting its prefWidth
        double newWidth = timelineEventPropertyParent.getMinWidth() + deltaX;
        widthProperty.set(deltaX);
        // Calculate the difference in X position from the initial drag start
        dragStartX = event.getX();

    }


    private void position(MouseEvent event) {
        //prevents going beyond the grid lines to the left
        if (!(getLeftBorderPosition(timelineEventPropertyParent) <= getLeftBorderPosition(timeline))
        ) {
            //prevents going beyond the grid lines to the right
            if (!(getRightBorderPosition(timelineEventPropertyParent) <= voiceController.getDoubleProperty())) {
                if (event.getX() <= getRightBorderPosition(timelineEventPropertyParent)) {
                    offsetX = 0;
                    eventPositionProperty.set(eventPositionProperty.get() - 5);
                    dragStartX = event.getX();
                }
            } else {
                // Calculate the difference in X position from the initial drag start
                double deltaOffsetX = event.getX() - dragStartX;
                // Update the offsetX variable
                offsetX += deltaOffsetX;

                // Update the translation by adjusting the bound property
                double newEventPosition = eventPositionProperty.get() + offsetX;
                eventPositionProperty.set(newEventPosition);
                // Update the drag start position for the next mouse move event
                dragStartX = event.getX();
            }

        } else {
            //returns the dragging the desirable container bounds
            if (event.getX() >= 0) {
                offsetX = 0;
                eventPositionProperty.set(eventPositionProperty.get() + 5);
                dragStartX = event.getX();
            }
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
