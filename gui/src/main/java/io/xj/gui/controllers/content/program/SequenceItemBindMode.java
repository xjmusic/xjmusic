package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.tables.pojos.ProgramSequenceBindingMeme;
import io.xj.hub.util.StringUtils;
import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
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
  @FXML
  public AnchorPane parentAnchorPane;
  @Value("classpath:/views/content/program/sequence-binding-meme-tag.fxml")
  private Resource memeTagFxml;
  static final Logger LOG = LoggerFactory.getLogger(SequenceItemBindMode.class);
  private int parentPosition;
  private final ApplicationContext ac;
  private final ProjectService projectService;
  private ProgramSequenceBinding programSequenceBinding;
  private final ProgramEditorController programEditorController;
  private VBox container;

  public SequenceItemBindMode(ApplicationContext ac, ProjectService projectService, ProgramEditorController programEditorController) {
    this.ac = ac;
    this.projectService = projectService;
    this.programEditorController = programEditorController;
  }

  public void setUp(VBox container, Parent root, HBox bindViewParentContainer, int parentPosition,
                    ProgramSequenceBinding programSequenceBinding) {
    this.container = container;
    this.parentPosition = parentPosition;
    this.programSequenceBinding = programSequenceBinding;
    deleteSequence(container, root, bindViewParentContainer);
    sequenceName.textProperty().bindBidirectional(programEditorController.sequenceName.textProperty());
    projectService.getContent().getMemesOfSequenceBinding(programSequenceBinding.getId()).forEach(this::addMeme);
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

  private void deleteSequence(VBox container, Parent root, HBox bindViewParentContainer) {
    deleteSequence.setOnAction(e -> {
      container.getChildren().remove(root);
      checkIfNextAndCurrentItemIsEmpty(bindViewParentContainer, container);
    });
  }

  private void addMeme(ProgramSequenceBindingMeme sequenceBindingMeme) {
    try {
      FXMLLoader loader = new FXMLLoader(memeTagFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      ProgramSequenceMemeTagController memeTagController = loader.getController();
      memeTagController.setUp(root, sequenceBindingMeme, programSequenceBinding.getId(), memeHolder,container,parentAnchorPane);

      memeHolder.widthProperty().addListener((o, ov, nv) -> {
        if (!(container.getWidth() >= memeHolder.getWidth())) {
          container.setPrefWidth(memeHolder.getWidth() + 80);
          parentAnchorPane.setPrefWidth(memeHolder.getWidth());
        }
      });
      memeHolder.getChildren().add(root);
    } catch (IOException exception) {
      LOG.error("Error adding Meme!\n{}", StringUtils.formatStackTrace(exception), exception);
    }
  }

  public void checkIfNextAndCurrentItemIsEmpty(HBox bindViewParentContainer, VBox container) {
    if (container.getChildren().size() < 3) {
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
