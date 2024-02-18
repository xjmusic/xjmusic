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
import javafx.scene.layout.BorderPane;
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

import java.io.IOException;
import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SequenceBindingItemController {
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

  static final Logger LOG = LoggerFactory.getLogger(SequenceBindingItemController.class);
  private int parentPosition;
  private final ApplicationContext ac;
  private final ProjectService projectService;
  private ProgramSequenceBinding programSequenceBinding;

  private final ThemeService themeService;
  private VBox sequenceSelector;
  private final ProgramEditorController programEditorController;

  public SequenceBindingItemController(
    ApplicationContext ac,
    ProjectService projectService,
    ProgramEditorController programEditorController,
    ThemeService themeService
  ) {
    this.ac = ac;
    this.projectService = projectService;
    this.themeService = themeService;
    this.programEditorController = programEditorController;
  }

  public void setUp(VBox sequenceSelector, Parent root, HBox bindViewParentContainer, int parentPosition,
                    ProgramSequenceBinding programSequenceBinding, ProgramSequence programSequence) {
    this.sequenceSelector = sequenceSelector;
    this.parentPosition = parentPosition;
    this.programSequenceBinding = programSequenceBinding;
    sequenceName.setText(programSequence.getName());
    deleteSequence(sequenceSelector, root, bindViewParentContainer);
    if (programEditorController.activeProgramSequenceItem.get().equals(programSequence))
      sequenceName.textProperty().bind(programEditorController.sequencePropertyName);
    projectService.getContent().getMemesOfSequenceBinding(programSequenceBinding.getId()).forEach(this::addMeme);
    mainBorderPane.widthProperty().addListener((o, ov, nv) -> {
      if (!(sequenceSelector.getWidth() >= nv.doubleValue())) {
        sequenceSelector.setPrefWidth(nv.doubleValue());
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

  private void deleteSequence(VBox sequenceSelector, Parent root, HBox bindViewParentContainer) {
    deleteSequence.setOnAction(event -> {
      try {
        if (!hasMemes()) {
          sequenceSelector.getChildren().remove(root);
          projectService.deleteContent(programSequenceBinding);
          checkIfNextAndCurrentItemIsEmpty(bindViewParentContainer, sequenceSelector);
        } else {
          projectService.showWarningAlert("Failure", "Found Meme on Sequence Binding", "Cannot delete sequence binding because it contains a meme.");
        }
      } catch (Exception e) {
        LOG.error("Failed to delete ProgramSequenceBinding at " + programSequenceBinding.getOffset(), e);
      }
    });
  }

  private boolean hasMemes() {
    return !projectService.getContent().getMemesOfSequenceBinding(programSequenceBinding.getId()).isEmpty();
  }

  private void addMeme(ProgramSequenceBindingMeme sequenceBindingMeme) {
    try {
      FXMLLoader loader = new FXMLLoader(memeTagFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      SequenceBindingMemeTagController memeTagController = loader.getController();
      memeTagController.setUp(root, sequenceBindingMeme, programSequenceBinding.getId(), memeHolder, sequenceSelector, mainBorderPane);
      memeHolder.getChildren().add(root);
    } catch (IOException exception) {
      LOG.error("Error adding Meme!\n{}", StringUtils.formatStackTrace(exception), exception);
    }
  }

  private void checkIfNextAndCurrentItemIsEmpty(HBox bindViewParentContainer, VBox sequenceSelector) {
    if (sequenceSelector.getChildren().size() < 3) {

      //get last position
      int lastPosition = bindViewParentContainer.getChildren().size() - 1;
      //store current position
      int current = parentPosition;
      //if an empty item is ahead of the current empty item
      if (parentPosition == lastPosition - 1) {
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
