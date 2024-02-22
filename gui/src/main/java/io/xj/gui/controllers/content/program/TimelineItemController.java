package io.xj.gui.controllers.content.program;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TimelineItemController {
    @FXML
    public AnchorPane timelineParent;
    private final ProgramEditorController programEditorController;
    @FXML
    public Line middleLine;

    public TimelineItemController(ProgramEditorController programEditorController) {
        this.programEditorController = programEditorController;
    }

    public void setUp(int timelineItemId, VoiceController voiceController) {
        middleLine.getStrokeDashArray().addAll(2d, 5d);
        if (voiceController.getTotal() > 0 && timelineItemId <= programEditorController.getTimelineGridProperty().get() * voiceController.getTotal()) {
            Platform.runLater(() -> timelineParent.getStyleClass().add("timeline-item-highlighted"));
        }
    }
}
