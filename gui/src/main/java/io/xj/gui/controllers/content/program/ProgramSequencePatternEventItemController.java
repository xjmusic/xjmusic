package io.xj.gui.controllers.content.program;

import io.xj.gui.controllers.content.common.DragResizer;
import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
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
    private double itemWidth = 0;
    @FXML
    private Parent root;
    int id;
    double minWidth = 0;
    private final ObjectProperty<ProgramSequencePatternEvent> programSequencePatternEventObjectProperty = new SimpleObjectProperty<>();
    private final ProjectService projectService;
    static final Logger LOG = LoggerFactory.getLogger(ProgramSequencePatternEventItemController.class);
    private VoiceController voiceController;
    private double previousWidth = 0;
    private final ProgramEditorController programEditorController;
    private boolean isDragging=false;
    public ProgramSequencePatternEventItemController(ProjectService projectService, ProgramEditorController programEditorController) {
        this.projectService = projectService;
        this.programEditorController = programEditorController;
    }

    private double initialX;

    public void setUp(int id, Parent root, AnchorPane timeline, ProgramSequencePatternEvent programSequencePatternEvent, VoiceController voiceController) {
        deleteTimelineProperty.setOnAction(e -> setDeleteTimelineProperty());
        itemWidth = timelineEventPropertyParent.getPrefWidth();
        previousWidth = timelineEventPropertyParent.getPrefWidth();
        DragResizer.makeResizable(timelineEventPropertyParent,timeline);
        this.timeline = timeline;
        this.root = root;
        this.id = id;
        this.voiceController = voiceController;
        minWidth = timelineEventPropertyParent.getPrefWidth();
        programSequencePatternEventObjectProperty.set(programSequencePatternEvent);
        updateLabels(programSequencePatternEvent);
//        timelineEventPropertyParent.setOnMouseMoved(this::handleMouseMove);
        timelineEventPropertyParent.prefWidthProperty().addListener((obs, oldWidth, newWidth) -> {
            // Calculate the change in width
            double widthChange = newWidth.doubleValue() - oldWidth.doubleValue();
            if (widthChange > 0) {
                // Increase the duration when width increases
                programSequencePatternEventObjectProperty.get().setDuration(programSequencePatternEventObjectProperty.get().getDuration() + 0.125f);
                updateLabels(programSequencePatternEventObjectProperty.get());
            } else if (widthChange < 0) {
                // Decrease the duration when width decreases
                programSequencePatternEventObjectProperty.get().setDuration(programSequencePatternEventObjectProperty.get().getDuration() - 0.125f);
                updateLabels(programSequencePatternEventObjectProperty.get());
            }
        });
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

    private void startDrag(MouseEvent event) {
        initialX = event.getSceneX();
        // Check if the drag starts at the desired edge
        if (event.getX() >= timelineEventPropertyParent.getWidth() - 3) {
            dragStartX = event.getSceneX();
            isDragging = true;
        }
    }

    private void handleMouseMove(MouseEvent event) {
        if (event.getSceneX() <= getLeftBorderPosition(timelineEventPropertyParent)) System.out.println("hola");
    }

//    private void handleMouseMove(MouseEvent event) {
//        AnchorPane anchorPane = (AnchorPane) event.getSource();
//        double mouseX = event.getX();
//        if (mouseX >= anchorPane.getWidth() - 3) {
//            anchorPane.setCursor(javafx.scene.Cursor.E_RESIZE);
//        } else if (mouseX <= anchorPane.getLayoutX()) {
//            anchorPane.setCursor(javafx.scene.Cursor.E_RESIZE);
//        } else {
//            anchorPane.setCursor(Cursor.DEFAULT);
//        }
//    }

    public static double getRightBorderPosition(Node node) {
        // Get the local bounds of the AnchorPane
        Bounds localBounds = node.getBoundsInLocal();

        // Transform the local bounds to scene coordinates
        Bounds sceneBounds = node.localToScene(localBounds);

        // Calculate the position of the right border
        return sceneBounds.getMinX() + localBounds.getWidth();
    }


    public static double getLeftBorderPosition(Node node) {
        // Get the local bounds of the AnchorPane
        Bounds localBounds = node.getBoundsInLocal();

        // Transform the local bounds to scene coordinates
        Bounds sceneBounds = node.localToScene(localBounds);

        // Calculate the position of the left border
        return sceneBounds.getMinX();
    }

    private void resize(MouseEvent event) {
        if (isDragging) {
            double mouseX = event.getSceneX();
            double deltaX = mouseX - dragStartX;
            double newWidth = timelineEventPropertyParent.getWidth();

            // Calculate the number of itemWidths to add/subtract based on the deltaX
            int change = (int) Math.round(deltaX / itemWidth);

            // Update the dragStartX to the nearest edge of the next item
            dragStartX += change * itemWidth;
            // Calculate the new width based on the change
            newWidth+= change * newWidth;

            if (newWidth > previousWidth) {
                programSequencePatternEventObjectProperty.get().setDuration(programSequencePatternEventObjectProperty.get().getDuration() + 0.125f);
            } else if (newWidth < previousWidth) {
                programSequencePatternEventObjectProperty.get().setDuration(programSequencePatternEventObjectProperty.get().getDuration() - 0.125f);
            }
            previousWidth = newWidth;
            // Ensure the new width is not less than the minimum width
            if (newWidth < minWidth) {
                newWidth = minWidth;
            }

            // Update the width of the anchorPane
            timelineEventPropertyParent.setPrefWidth(newWidth);
            try {
                projectService.update(ProgramSequencePatternEvent.class, programSequencePatternEventObjectProperty.get().getId(), "duration", programSequencePatternEventObjectProperty.get().getDuration());
            } catch (Exception e) {
                LOG.error("Failed to update ProgramSequencePatternEvent duration");

            }
        } else {
            double x = event.getSceneX() - initialX;
            // Update the item's position
            Platform.runLater(() -> {
                timelineEventPropertyParent.setTranslateX(timelineEventPropertyParent.getTranslateX() + x);
            });

            // Update initial mouse position for the next drag event
            initialX = event.getSceneX();
        }
    }

}
