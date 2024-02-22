package io.xj.gui.controllers.content.program;


import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static io.xj.gui.controllers.content.program.TrackController.trackItem;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TrackMenuController {
    @FXML
    public Button newTrack;
    @FXML
    public Button deleteTrack;
    private final ProjectService projectService;
    private final ProgramEditorController programEditorController;
    static final Logger LOG = LoggerFactory.getLogger(TrackMenuController.class);

    @Value("classpath:/views/content/program/track.fxml")
    private Resource trackFxml;
    private ProgramVoice voice;
    private final ApplicationContext ac;
    private  VoiceController voiceController;

    public TrackMenuController(ProjectService projectService, ProgramEditorController programEditorController, ApplicationContext ac) {
        this.projectService = projectService;
        this.programEditorController = programEditorController;
        this.ac=ac;
    }

    public void setUp(Parent root, ProgramVoice voice, VoiceController voiceController) {
        this.voice = voice;
        this.voiceController=voiceController;
        createNewTrack();
    }


    private void createNewTrack() {
        newTrack.setOnAction(event -> {
            try {
                ProgramVoiceTrack newTrack = new ProgramVoiceTrack(UUID.randomUUID(), programEditorController.getProgramId(), voice.getId(), "XXX", 1f);
                projectService.getContent().put(newTrack);
                trackItem(trackFxml,ac,voiceController.voiceContainer,LOG,voiceController.addTrackButton_1,voice, voiceController, newTrack);
                closeWindow();
            } catch (Exception e) {
                LOG.info("Could not create new Track");
            }
        });
    }

    private void closeWindow(){
        Stage stage=(Stage) newTrack.getScene().getWindow();
        stage.close();
    }
}
