package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import io.xj.hub.util.StringUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TimelineItemController {
    @FXML
    public AnchorPane timelineParent;
    private final ProgramEditorController programEditorController;
    @FXML
    public Line middleLine;

    @Value("classpath:/views/content/program/event-grid-property-item.fxml")
    private Resource propertyFxml;
    private final ApplicationContext ac;
    private final ProjectService projectService;
    private ProgramVoiceTrack track;
    static final Logger LOG = LoggerFactory.getLogger(TimelineItemController.class);
    private VoiceController voiceController;

    public TimelineItemController(ProgramEditorController programEditorController, ApplicationContext ac, ProjectService projectService) {
        this.programEditorController = programEditorController;
        this.ac=ac;
        this.projectService=projectService;
    }

    public void setUp(int timelineItemId, VoiceController voiceController, AnchorPane timeline, Parent root, ProgramVoiceTrack track) {
        middleLine.getStrokeDashArray().addAll(2d, 5d);
        if (voiceController.getTotal() > 0 && timelineItemId < programEditorController.getTimelineGridProperty().get() * voiceController.getTotal()) {
            Platform.runLater(() -> {
                timelineParent.getStyleClass().add("timeline-item-highlighted");
                timelineParent.setCursor(Cursor.CROSSHAIR);
                timelineParent.setOnMouseClicked(e->addProgramSequencePatternEventItem(timeline, timelineItemId));
            });
            this.track=track;
            this.voiceController=voiceController;
        }
    }

    private void addProgramSequencePatternEventItem(AnchorPane timeline, int id) {
        try {
            // Load the FXML for the property item
            FXMLLoader loader = new FXMLLoader(propertyFxml.getURL());
            loader.setControllerFactory(ac::getBean);
            Parent root = loader.load();

            // Retrieve the layout parameters of the existing item at the specified index
            Node existingItem = timeline.getChildren().get(id);
            double x = AnchorPane.getLeftAnchor(existingItem);
            double y = AnchorPane.getTopAnchor(existingItem);
            double z = AnchorPane.getBottomAnchor(existingItem);

            // Set the layout parameters of the new item to match those of the existing item
            AnchorPane.setLeftAnchor(root, x);
            AnchorPane.setTopAnchor(root, y);
            AnchorPane.setBottomAnchor(root, z);

            // Add the new property item to the AnchorPane
            timeline.getChildren().add(root);
            //create a ProgramSequencePatternEvent object
            ProgramSequencePatternEvent programSequencePatternEvent=new ProgramSequencePatternEvent(UUID.randomUUID(),track.getProgramId(),programEditorController.getSequenceId(),track.getId(),0.125f,id*0.125f,0.125f,"X");
            // Initialize the controller for the new property item
            EventGridPropertyItemController eventGridPropertyItemController = loader.getController();
            eventGridPropertyItemController.setUp(id, root, timeline,programSequencePatternEvent,voiceController);
            //save the ProgramSequencePatternEvent
            projectService.getContent().put(programSequencePatternEvent);
        } catch (Exception e) {
            LOG.error("Error adding Timeline Property item \n{}", StringUtils.formatStackTrace(e), e);
        }
    }

}
