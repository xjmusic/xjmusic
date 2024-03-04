package io.xj.gui.controllers.content.program;

import io.xj.gui.controllers.content.common.EntityMemesController;
import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.util.StringUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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
public class SequenceBindingItemController {
  @FXML
  public Button deleteSequence;
  @FXML
  public Label sequenceName;
  @FXML
  public Pane sequenceBindingMemeContainer;

  public AnchorPane mainBorderPane;

  @Value("classpath:/views/content/common/entity-memes.fxml")
  private Resource entityMemesFxml;

  static final Logger LOG = LoggerFactory.getLogger(SequenceBindingItemController.class);
  private int parentPosition;
  private final ApplicationContext ac;
  private final ProjectService projectService;
  private ProgramSequenceBinding programSequenceBinding;

  public SequenceBindingItemController(
    ApplicationContext ac,
    ProjectService projectService
  ) {
    this.ac = ac;
    this.projectService = projectService;
  }

  public void setup(
    VBox sequenceSelector,
    Parent root, HBox bindViewParentContainer,
    int parentPosition,
    ProgramSequenceBinding programSequenceBinding
  ) {
    this.parentPosition = parentPosition;
    this.programSequenceBinding = programSequenceBinding;
    deleteSequence.setOnAction(event -> {
      try {
        if (!projectService.getContent().getMemesOfSequenceBinding(programSequenceBinding.getId()).isEmpty()) {
          projectService.showWarningAlert("Failure", "Found Meme on Sequence Binding", "Cannot delete sequence binding because it contains a meme.");
        } else {
          sequenceSelector.getChildren().remove(root);
          projectService.deleteContent(programSequenceBinding);
          checkIfNextAndCurrentItemIsEmpty(bindViewParentContainer, sequenceSelector);
        }
      } catch (Exception e) {
        LOG.error("Failed to delete ProgramSequenceBinding at " + programSequenceBinding.getOffset(), e);
      }
    });

    setupSequenceBindingMemeContainer();
  }

  /**
   Set up the Program Meme Container FXML and its controller
   */
  private void setupSequenceBindingMemeContainer() {
    try {
      FXMLLoader loader = new FXMLLoader(entityMemesFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      sequenceBindingMemeContainer.getChildren().clear();
      sequenceBindingMemeContainer.getChildren().add(root);
      EntityMemesController entityMemesController = loader.getController();
      entityMemesController.setup(
        () -> projectService.getContent().getMemesOfSequenceBinding(programSequenceBinding),
        () -> projectService.createProgramSequenceBindingMeme(programSequenceBinding.getId()),
        (Object meme) -> {
          try {
            projectService.update(meme);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      );
    } catch (IOException e) {
      LOG.error("Error loading Entity Memes window!\n{}", StringUtils.formatStackTrace(e), e);
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
