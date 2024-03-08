package io.xj.gui.controllers.content.program.bind_mode;

import io.xj.gui.controllers.content.common.EntityMemesController;
import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.util.StringUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
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
  Label sequenceName;
  @FXML
  StackPane sequenceBindingMemeContainer;

  public AnchorPane mainBorderPane;

  @Value("classpath:/views/content/common/entity-memes.fxml")
  private Resource entityMemesFxml;

  static final Logger LOG = LoggerFactory.getLogger(SequenceBindingItemController.class);
  private final ApplicationContext ac;
  private final ProjectService projectService;
  private ProgramSequenceBinding programSequenceBinding;
  private Runnable deleteSequenceBinding;
  private Runnable unsubscribe; // unsubscribe from sequence listener

  public SequenceBindingItemController(
    ApplicationContext ac,
    ProjectService projectService
  ) {
    this.ac = ac;
    this.projectService = projectService;
  }

  /**
   Set up the controller with the program sequence binding

   @param programSequenceBinding the program sequence binding
   @param deleteSequenceBinding  the action to delete the sequence binding
   */
  public void setup(
    ProgramSequenceBinding programSequenceBinding,
    Runnable deleteSequenceBinding
  ) {
    this.programSequenceBinding = programSequenceBinding;
    this.deleteSequenceBinding = deleteSequenceBinding;

    setupSequenceName();

    unsubscribe = projectService.addProjectUpdateListener(ProgramSequence.class, this::setupSequenceName);

    setupSequenceBindingMemeContainer();
  }

  /**
   Called before this controller is removed from the stage
   */
  public void teardown() {
    unsubscribe.run();
  }

  /**
   Set up the sequence name
   */
  private void setupSequenceName() {
    var sequence = projectService.getContent().getProgramSequence(programSequenceBinding.getProgramSequenceId())
      .orElseThrow(() -> new RuntimeException("Could not find sequence for sequence binding!"));
    sequenceName.setText(sequence.getName());
  }

  @FXML
  void handleDeleteSequenceBinding() {
    deleteSequenceBinding.run();
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
        false,
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
      LOG.error("Error loading Entity Memes window! {}\n{}", e, StringUtils.formatStackTrace(e));
    }
  }
}
