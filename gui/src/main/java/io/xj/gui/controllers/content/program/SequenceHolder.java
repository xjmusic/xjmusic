package io.xj.gui.controllers.content.program;

import io.xj.hub.util.StringUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
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
public class SequenceHolder {
  @FXML
  public VBox sequenceHolder;
  @FXML
  public Label offSet;
  @FXML
  public Button addSequenceButton;
  @Value("classpath:/views/content/program/sequence-item-bind-mode.fxml")
  private Resource sequenceItemBindingFxml;
  private final ProgramEditorController programEditorController;
  private final ApplicationContext applicationContext;
  private final Logger LOG = LoggerFactory.getLogger(SequenceHolder.class);

  public void setUp(int offSetNumber) {
    offSet.setText(String.valueOf(offSetNumber));
    addSequenceButton.setOnAction(e -> addSequenceItem());
  }

  public SequenceHolder(ApplicationContext applicationContext, ProgramEditorController programEditorController) {
    this.programEditorController = programEditorController;
    this.applicationContext = applicationContext;
  }

  private void addSequenceItem() {
    try {
      FXMLLoader loader = new FXMLLoader(sequenceItemBindingFxml.getURL());
      loader.setControllerFactory(applicationContext::getBean);
      Parent root = loader.load();
      sequenceHolder.getChildren().add(sequenceHolder.getChildren().size()-1,root);
      VBox.setMargin(root, new Insets(0, 5, 0, 5));
      SequenceItemBindMode sequenceItemBindMode=loader.getController();
      sequenceItemBindMode.deleteSequence.setOnAction(e->sequenceHolder.getChildren().remove(root));
    } catch (IOException e) {
      LOG.error("Error creating new Sequence \n{}", StringUtils.formatStackTrace(e), e);
    }
  }
}
