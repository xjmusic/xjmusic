package io.xj.gui.controllers.content.program;


import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import javafx.application.Platform;
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
    private VoiceController voiceController;
    private ProgramVoiceTrack programVoiceTrack;

    public TrackMenuController(ProjectService projectService, ProgramEditorController programEditorController, ApplicationContext ac) {
        this.projectService = projectService;
        this.programEditorController = programEditorController;
        this.ac = ac;
    }

    public void setUp(Parent root, ProgramVoice voice, VoiceController voiceController, ProgramVoiceTrack track, boolean itemIsAttachedToVoiceFxml, Parent trackRoot) {
        this.voice = voice;
        this.voiceController = voiceController;
        this.programVoiceTrack = track;
        createNewTrack();
        deleteTrack(trackRoot,itemIsAttachedToVoiceFxml);
    }


    private void createNewTrack() {
        newTrack.setOnAction(event -> {
            try {
                ProgramVoiceTrack newTrack = new ProgramVoiceTrack(UUID.randomUUID(), programEditorController.getProgramId(), voice.getId(), "XXX", 1f);
                projectService.getContent().put(newTrack);
                trackItem(trackFxml, ac, voiceController.voiceContainer, LOG, voiceController.addTrackButton_1, voice, voiceController, newTrack);
                closeWindow();
            } catch (Exception e) {
                LOG.info("Could not create new Track");
            }
        });
    }

    private void deleteTrack(Parent root, boolean itemIsAttachedToVoiceFxml) {
        deleteTrack.setOnAction(event -> {
            try {
                if (itemIsAttachedToVoiceFxml) {
//                    System.out.println("ppre "+voiceController.getProgramVoiceTrack().getName());
//                    voiceController.setProgramVoiceTrackObjectProperty(voiceController.getProgramVoiceTrackObservableList().get(0));
//                    System.out.println("post "+voiceController.getProgramVoiceTrack().getName());

                } else {
                    Platform.runLater(() -> voiceController.voiceContainer.getChildren().remove(root));
                    voiceController.getProgramVoiceTrackObservableList().remove(programVoiceTrack);

                }
                closeWindow();
            } catch (Exception e) {
//                LOG.info("Could not delete Track");
                e.getStackTrace();
            }
        });
    }

    private void closeWindow() {
        Stage stage = (Stage) newTrack.getScene().getWindow();
        stage.close();
    }
}
