package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.util.StringUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.SearchableComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

@Component
public class CreateBindingItem {
  @FXML
  public VBox container;
  @FXML
  public SearchableComboBox<Label> sequenceSearch;
  private final Logger LOG = LoggerFactory.getLogger(io.xj.gui.controllers.content.program.SearchSequence.class);
  private final ProjectService projectService;
  @Value("classpath:/views/content/program/sequence-item-bind-mode.fxml")
  private Resource sequenceItemBindingFxml;
  private HBox bindViewParentContainer;
  private VBox sequenceHolder;
  private final ApplicationContext applicationContext;
  private final ProgramEditorController programEditorController;
  private int position;
  public CreateBindingItem(ProjectService projectService, ApplicationContext applicationContext
  ,ProgramEditorController programEditorController) {
    this.projectService = projectService;
    this.applicationContext=applicationContext;
    this.programEditorController=programEditorController;
  }

  public void setUp(Collection<ProgramSequence> programSequences,
                    HBox bindViewParentContainer, VBox sequenceHolder,
                    int position,UUID programId,UUID programSequenceId) {
    this.bindViewParentContainer=bindViewParentContainer;
    this.sequenceHolder=sequenceHolder;
    this.position=position;
    programSequences.forEach(sequence -> {
      Label sequenceLabel = new Label();
      sequenceLabel.setId(String.valueOf(sequence.getId()));
      sequenceLabel.setText(sequence.getName());
      sequenceSearch.getItems().add(sequenceLabel);
    });
    // Request focus and show the ComboBox
//    sequenceSearch.getButtonCell().requestFocus();
    sequenceSearch.show();
    addSequenceBinding(position-1,programId,programSequenceId);
  }

  private void addSequenceBinding(int offSet,UUID programId,UUID programSequenceId) {
    sequenceSearch.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        System.out.println("Selected item: " + newValue);
        addSequence(programId,programSequenceId,offSet);
        closeWindow();
      }
    });
  }

  private void addSequence(UUID programId, UUID programSequenceId,int offSet) {
    ProgramSequenceBinding programSequenceBinding = new ProgramSequenceBinding(UUID.randomUUID(), programId, programSequenceId, offSet);
    addSequenceItem(programSequenceBinding);
  }

  private void addSequenceItem(ProgramSequenceBinding programSequenceBinding) {
    try {
      FXMLLoader loader = new FXMLLoader(sequenceItemBindingFxml.getURL());
      loader.setControllerFactory(applicationContext::getBean);
      Parent root = loader.load();
      sequenceHolder.getChildren().add(sequenceHolder.getChildren().size() - 1, root);
      VBox.setMargin(root, new Insets(0, 5, 0, 5));
      SequenceItemBindMode sequenceItemBindMode = loader.getController();
      sequenceItemBindMode.setUp(sequenceHolder, root, bindViewParentContainer, programSequenceBinding.getOffset(), programSequenceBinding);
      projectService.getContent().put(programSequenceBinding);
      checkIfNextItemIsPresent();

    } catch (Exception e) {
      LOG.error("Error creating new Sequence \n{}", StringUtils.formatStackTrace(e), e);
    }
  }

  private void checkIfNextItemIsPresent() {
    if (bindViewParentContainer.getChildren().size() - 1 < position + 1) {
      programEditorController.addBindingView(position + 1);
    }
  }

  private void closeWindow(){
    Stage stage=(Stage) sequenceSearch.getScene().getWindow();
    stage.close();
  }
}
