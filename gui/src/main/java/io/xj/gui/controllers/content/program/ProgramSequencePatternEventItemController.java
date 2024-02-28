package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
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

    private DoubleProperty eventPositionProperty=new SimpleDoubleProperty();

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
                            double baseSizePerBeat=voiceController.getBaseSizePerBeat().doubleValue();
                            double zoomFactor = programEditorController.getZoomFactor();
                            return baseSizePerBeat * sequenceTotal * zoomFactor /(sequenceTotal * timelineGridSize);
                        }
                        ,
                        programEditorController.getSequenceTotalProperty(),
                        programEditorController.getZoomFactorProperty()
                )
        );
        this.timeline = timeline;
        this.root = root;
        this.voiceController = voiceController;
        minWidth = timelineEventPropertyParent.getPrefWidth();
        programSequencePatternEventObjectProperty.set(programSequencePatternEvent);
        updateLabels(programSequencePatternEvent);
        // Bind the translateX property of timelineEventPropertyParent directly to the eventPositionProperty
        timelineEventPropertyParent.translateXProperty().bind(eventPositionProperty);
        // Bind the translateX property of timelineEventPropertyParent to the specified calculation
        // this was the code you shared, I modified it as the syntax had an issue
//        timelineEventPropertyParent.translateXProperty().bind(Bindings.createIntegerBinding(() -> (int) (eventPositionProperty.get() * programEditorController.getZoomFactorProperty().get() * voiceController.getBaseSizePerBeat().get()), eventPositionProperty, programEditorController.getZoomFactorProperty()));
        //this code below is what i tried to add extra modification from yours
//        timelineEventPropertyParent.translateXProperty().bind(
//                Bindings.createDoubleBinding(
//                        () -> {
//                            double baseSizePerBeat=voiceController.getBaseSizePerBeat().doubleValue();
//                            double zoomFactor = programEditorController.getZoomFactor();
//                            return baseSizePerBeat * eventPositionProperty.get() * zoomFactor ;
//                        }
//                        ,
//                        eventPositionProperty,
//                        programEditorController.getZoomFactorProperty()
//                )
//        );
        timelineEventPropertyParent.setOnMousePressed(event -> dragStartX = event.getX());
        timelineEventPropertyParent.setOnMouseDragged(this::position);
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

    private double offsetX = 0;

    private void position(MouseEvent event) {
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

}
