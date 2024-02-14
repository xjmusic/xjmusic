package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.util.StringUtils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.SearchableComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
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
    , ProgramEditorController programEditorController) {
    this.projectService = projectService;
    this.applicationContext = applicationContext;
    this.programEditorController = programEditorController;
  }

  public void setUp(Collection<ProgramSequence> programSequences,
                    HBox bindViewParentContainer, VBox sequenceHolder,
                    int position, UUID programId, UUID programSequenceId) {
    this.bindViewParentContainer = bindViewParentContainer;
    this.sequenceHolder = sequenceHolder;
    this.position = position;
    programSequences.forEach(sequence -> {
      Label sequenceLabel = new Label();
      sequenceLabel.setId(String.valueOf(sequence.getId()));
      sequenceLabel.setText(sequence.getName());
      sequenceSearch.getItems().add(sequenceLabel);
    });
    addSequenceBinding(position - 1, programId, programSequenceId);
  }

  private void addSequenceBinding(int offSet, UUID programId, UUID programSequenceId) {
    sequenceSearch.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        addSequence(programId, programSequenceId, offSet);
        closeWindow();
      }
    });
  }

  private void addSequence(UUID programId, UUID programSequenceId, int offSet) {
    ProgramSequenceBinding programSequenceBinding = new ProgramSequenceBinding(UUID.randomUUID(), programId, programSequenceId, offSet);
    addSequenceItem(programSequenceBinding);
  }

  public void addSequenceItem(ProgramSequenceBinding programSequenceBinding) {
    try {
      FXMLLoader loader = new FXMLLoader(sequenceItemBindingFxml.getURL());
      loader.setControllerFactory(applicationContext::getBean);
      Parent root = loader.load();
      HBox.setHgrow(sequenceHolder, Priority.ALWAYS);
      SequenceItemBindMode sequenceItemBindMode = loader.getController();
      sequenceItemBindMode.setUp(sequenceHolder, root, bindViewParentContainer, position, programSequenceBinding);
      sequenceHolder.getChildren().add(root);
      HBox.setHgrow(root,Priority.ALWAYS);
      projectService.putContent(programSequenceBinding);
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

  private void closeWindow() {
    Stage stage = (Stage) sequenceSearch.getScene().getWindow();
    stage.close();
  }
}
