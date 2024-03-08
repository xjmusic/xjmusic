package io.xj.gui.controllers.content.program.bind_mode;

import io.xj.gui.ProjectController;
import io.xj.gui.modes.ProgramEditorMode;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.util.StringUtils;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
public class BindModeController extends ProjectController {
  static final Logger LOG = LoggerFactory.getLogger(BindModeController.class);
  private final Resource sequenceBindingColumnFxml;
  private final List<SequenceBindingColumnController> sequenceBindingColumnControllers = new ArrayList<>();
  private final ObjectProperty<UUID> programId = new SimpleObjectProperty<>();

  private final BooleanBinding active;

  @FXML
  HBox container;

  @FXML
  HBox sequenceBindingsContainer;

  /**
   Program Edit Bind-mode Controller

   @param fxml           FXML resource
   @param ac             application context
   @param themeService   common theme service
   @param uiStateService common UI state service
   @param projectService common project service
   */
  protected BindModeController(
    @Value("classpath:/views/content/program/bind_mode/bind-mode.fxml") Resource fxml,
    @Value("classpath:/views/content/program/bind_mode/sequence-binding-column.fxml") Resource sequenceBindingColumnFxml,
    ApplicationContext ac,
    ThemeService themeService,
    UIStateService uiStateService,
    ProjectService projectService
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.sequenceBindingColumnFxml = sequenceBindingColumnFxml;

    active = uiStateService.programEditorModeProperty().isEqualTo(ProgramEditorMode.Bind);
  }

  @Override
  public void onStageReady() {
    container.visibleProperty().bind(active);
    container.managedProperty().bind(active);

    projectService.addProjectUpdateListener(ProgramSequenceBinding.class, this::addOrRemoveSequenceBindingColumnsAsNeeded);
  }

  @Override
  public void onStageClose() {
    // no op
  }

  /**
   Set up the Sequence Binding View
   */
  public void setup(UUID programId) {
    this.programId.set(programId);
    addOrRemoveSequenceBindingColumnsAsNeeded();
  }

  /**
   Teardown the controller before it is removed
   */
  public void teardown() {
    for (SequenceBindingColumnController controller : sequenceBindingColumnControllers) controller.teardown();
    sequenceBindingColumnControllers.clear();
    sequenceBindingsContainer.getChildren().clear();
  }

  /**
   find the highest offset in the current sequenceBindingsOfProgram group and create the offset holders  with an extra button
   if sequence bindings number is zero, add the two buttons that appear when empty
   */
  private void addOrRemoveSequenceBindingColumnsAsNeeded() {
    Collection<ProgramSequenceBinding> bindings = projectService.getContent().getSequenceBindingsOfProgram(programId.get());

    int highestOffset = bindings.stream().map(ProgramSequenceBinding::getOffset).max(Integer::compareTo).orElse(-1);
    for (int i = 0; i <= highestOffset + 1; i++) {
      addSequenceBindingColumnIfNotPresent(i);
    }

    while (sequenceBindingColumnControllers.size() > highestOffset + 2) {
      sequenceBindingColumnControllers.remove(sequenceBindingColumnControllers.size() - 1).teardown();
      sequenceBindingsContainer.getChildren().remove(sequenceBindingsContainer.getChildren().size() - 1);
    }
  }

  /**
   Add a sequence binding column if it doesn't already exist

   @param offset the offset of the sequence binding column
   */
  private void addSequenceBindingColumnIfNotPresent(int offset) {
    try {
      // skip if we already have a column at this offset
      if (sequenceBindingColumnControllers.size() > offset) return;

      FXMLLoader loader = new FXMLLoader(sequenceBindingColumnFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      sequenceBindingsContainer.getChildren().add(root);
      SequenceBindingColumnController controller = loader.getController();
      sequenceBindingColumnControllers.add(offset, controller);
      controller.setup(offset, programId.get());
    } catch (IOException e) {
      LOG.error("Error loading Sequence Selector view! {}\n{}", e, StringUtils.formatStackTrace(e));
    }
  }
}
