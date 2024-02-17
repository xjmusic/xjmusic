package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.tables.pojos.ProgramSequenceBindingMeme;
import io.xj.hub.util.StringUtils;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SequenceItemBindMode {
  @FXML
  public Button deleteSequence;
  @FXML
  public Button addMemeButton;
  @FXML
  public Text sequenceName;
  @FXML
  public HBox memeHolder;
  public BorderPane mainBorderPane;
  @Value("classpath:/views/content/program/sequence-binding-meme-tag.fxml")
  private Resource memeTagFxml;
  @Value("classpath:/views/content/program/alert.fxml")
  private Resource alertFxml;
  static final Logger LOG = LoggerFactory.getLogger(SequenceItemBindMode.class);
  private int parentPosition;
  private final ApplicationContext ac;
  private final ProjectService projectService;
  private ProgramSequenceBinding programSequenceBinding;

  private final ThemeService themeService;
  private VBox sequenceHolder;
  private final ProgramEditorController programEditorController;
  public SequenceItemBindMode(ApplicationContext ac, ProjectService projectService, ProgramEditorController programEditorController,
                              ThemeService themeService) {
    this.ac = ac;
    this.projectService = projectService;
    this.themeService = themeService;
    this.programEditorController=programEditorController;
  }

  public void setUp(VBox sequenceHolder, Parent root, HBox bindViewParentContainer, int parentPosition,
                    ProgramSequenceBinding programSequenceBinding, ProgramSequence programSequence) {
    this.sequenceHolder = sequenceHolder;
    this.parentPosition = parentPosition;
    this.programSequenceBinding = programSequenceBinding;
    sequenceName.setText(programSequence.getName());
    deleteSequence(sequenceHolder, root, bindViewParentContainer);
    if (programEditorController.activeProgramSequenceItem.get().equals(programSequence)) sequenceName.textProperty().bind(programEditorController.sequencePropertyName);
    projectService.getContent().getMemesOfSequenceBinding(programSequenceBinding.getId()).forEach(this::addMeme);
    mainBorderPane.widthProperty().addListener((o, ov, nv) -> {
      if (!(sequenceHolder.getWidth() >= nv.doubleValue())) {
        sequenceHolder.setPrefWidth(nv.doubleValue());
      }
    });

    setAddMemeButton();
  }

  private void setAddMemeButton() {
    addMemeButton.setOnAction(e -> {
      try {
        ProgramSequenceBindingMeme programSequenceBindingMeme =
          new ProgramSequenceBindingMeme(UUID.randomUUID(), programSequenceBinding.getProgramId(), programSequenceBinding.getId(), "XXX");
        projectService.getContent().put(programSequenceBindingMeme);
        addMeme(programSequenceBindingMeme);
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    });
  }

  private void deleteSequence(VBox sequenceHolder, Parent root, HBox bindViewParentContainer) {
    deleteSequence.setOnAction(e -> {
      try {
        if (!hasMemes()) {
          sequenceHolder.getChildren().remove(root);
          projectService.deleteContent(programSequenceBinding);
          checkIfNextAndCurrentItemIsEmpty(bindViewParentContainer, sequenceHolder);
        } else {
          showTimedAlert("Failure", "Found Meme on Sequence Binding", Duration.seconds(4), alertFxml, themeService,"#DB6A64");
        }
      } catch (Exception ex) {
        LOG.info("Failed to delete ProgramSequenceBinding at " + programSequenceBinding.getOffset());
        ex.printStackTrace();
      }
    });
  }


  public static void showTimedAlert(String title, String message, Duration duration, Resource alertFxml, ThemeService themeService,
                                    String color) {
    try {
      Stage stage = new Stage(StageStyle.TRANSPARENT);
      FXMLLoader loader = new FXMLLoader(alertFxml.getURL());
      Parent root = loader.load();
      AlertController alertController = loader.getController();
      alertController.setUp(title, message, color);
      stage.setScene(new Scene(root));
      // Set the owner of the stage
      stage.initOwner(themeService.getMainScene().getWindow());

      // Get the screen dimensions
      Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
      double screenWidth = screenBounds.getWidth();
      double screenHeight = screenBounds.getHeight();

      // Position the stage at the bottom-right corner
      stage.setX(screenWidth - root.prefWidth(-1));  // Right-most position
      stage.setY(screenHeight - root.prefHeight(-1)); // Bottom position

      // Create a FadeTransition for the stage
      FadeTransition fadeTransition = new FadeTransition(Duration.seconds(2), stage.getScene().getRoot());
      fadeTransition.setFromValue(.1);
      fadeTransition.setToValue(1);

      // Create fade-out transition
      FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), root);
      fadeOut.setFromValue(1);
      fadeOut.setToValue(0);
      fadeTransition.play();

      stage.show();

      // Create a PauseTransition and set its onFinished event
      PauseTransition pauseTransition = new PauseTransition(duration);
      pauseTransition.setOnFinished(e -> stage.close());
      pauseTransition.play();
    } catch (IOException e) {
      LOG.error("Error loading EditConfig window!\n{}", StringUtils.formatStackTrace(e), e);
    }
  }

  private boolean hasMemes() {
    return projectService.getContent().getMemesOfSequenceBinding(programSequenceBinding.getId()).size() > 0;
  }

  private void addMeme(ProgramSequenceBindingMeme sequenceBindingMeme) {
    try {
      FXMLLoader loader = new FXMLLoader(memeTagFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      ProgramSequenceMemeTagController memeTagController = loader.getController();
      memeTagController.setUp(root, sequenceBindingMeme, programSequenceBinding.getId(), memeHolder, sequenceHolder, mainBorderPane);
      memeHolder.getChildren().add(root);
    } catch (IOException exception) {
      LOG.error("Error adding Meme!\n{}", StringUtils.formatStackTrace(exception), exception);
    }
  }

  private void checkIfNextAndCurrentItemIsEmpty(HBox bindViewParentContainer, VBox sequenceHolder) {
    if (sequenceHolder.getChildren().size() < 3) {

      //get last position
      int lastPosition = bindViewParentContainer.getChildren().size() - 1;
      //store current position
      int current = parentPosition;
      //if an empty item is ahead of the current empty item
      if (parentPosition == lastPosition-1) {
        if (bindViewParentContainer.getChildren().size() > 3) {
          bindViewParentContainer.getChildren().remove(lastPosition);
        }
        //continue removing the empty elements while moving the current position back
        while (((VBox) bindViewParentContainer.getChildren().get(current - 1)).getChildren().size() < 3) {
          if (bindViewParentContainer.getChildren().size() <= 3) {
            return;
          }
          //break the deletion process if there only remains two items, plus the first label
          bindViewParentContainer.getChildren().remove(current);
          current = current - 1;
        }
      }
    }
  }
}
