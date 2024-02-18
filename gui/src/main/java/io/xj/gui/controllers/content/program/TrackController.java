package io.xj.gui.controllers.content.program;

import io.xj.hub.util.StringUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TrackController {
    private final ProgramEditorController programEditorController;
    @FXML
    public VBox trackControlsContainer;
    @FXML
    public AnchorPane trackContainer;
    @FXML
    public Button trackMenuButton;
    @FXML
    public Text trackName;
    @FXML
    public Button addTrackButton_1;

    @Value("classpath:/views/content/program/track.fxml")
    private Resource trackFxml;
    static final Logger LOG = LoggerFactory.getLogger(TrackController.class);

    private final ApplicationContext ac;
    private VBox voiceContainer;
    public TrackController(ProgramEditorController programEditorController,
                           ApplicationContext ac){
        this.programEditorController=programEditorController;
        this.ac=ac;
    }
    public void setUp(Parent root, VBox voiceContainer) {
        this.voiceContainer=voiceContainer;
        addTrackButton_1.setOnAction(e->addTrackItemToNewLine());
    }

    private void addTrackItemToNewLine(){
        trackItem(trackFxml, ac, voiceContainer, LOG);
    }

    static void trackItem(Resource trackFxml, ApplicationContext ac, VBox voiceContainer, Logger log) {
        try {
            FXMLLoader loader = new FXMLLoader(trackFxml.getURL());
            loader.setControllerFactory(ac::getBean);
            Parent root = loader.load();
            TrackController trackController = loader.getController();
            trackController.setUp(root, voiceContainer);
            voiceContainer.getChildren().add(root);
        } catch (IOException e) {
            log.error("Error adding Voice item view!\n{}", StringUtils.formatStackTrace(e), e);
        }
    }
}
