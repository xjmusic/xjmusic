package io.xj.gui.controllers.content.program;

import io.xj.gui.services.ProjectService;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.util.StringUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.controlsfx.control.SearchableComboBox;
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
public class SequenceBindingItemCreationController {
    @FXML
    public VBox container;
    @FXML
    public SearchableComboBox<ProgramSequence> sequenceSearch;
    private final Logger LOG = LoggerFactory.getLogger(SequenceSearchController.class);
    private final ProjectService projectService;
    @Value("classpath:/views/content/program/sequence-selector.fxml")
    private Resource sequenceHolderFxml;
    @Value("classpath:/views/content/program/sequence-binding-item.fxml")
    private Resource sequenceItemBindingFxml;
    private HBox bindViewParentContainer;
    private VBox sequenceSelector;
    private final ApplicationContext applicationContext;
    private final ProgramEditorController programEditorController;
    private int position;

    public SequenceBindingItemCreationController(ProjectService projectService, ApplicationContext applicationContext
            , ProgramEditorController programEditorController) {
        this.projectService = projectService;
        this.applicationContext = applicationContext;
        this.programEditorController = programEditorController;
    }

    public void setUp(HBox bindViewParentContainer, VBox sequenceSelector,
                      int position, UUID programId) {
        this.bindViewParentContainer = bindViewParentContainer;
        this.sequenceSelector = sequenceSelector;
        this.position = position;
        setCombobox();
        addSequenceBinding(position - 1, programId);
    }

    private void addSequenceBinding(int offSet, UUID programId) {
        sequenceSearch.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                addSequence(programId, newValue, offSet);
                closeWindow();
            }
        });
    }

    private void setCombobox() {
        // Clear existing items
        sequenceSearch.getItems().clear();

        sequenceSearch.getItems().addAll(programEditorController.programSequenceObservableList);

        // Set the cell factory to display the name of ProgramSequence
        sequenceSearch.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ProgramSequence item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        // Set the string converter to get ProgramSequence from its name
        sequenceSearch.setConverter(new StringConverter<>() {
            @Override
            public String toString(ProgramSequence object) {
                return object != null ? object.getName() : "";
            }

            @Override
            public ProgramSequence fromString(String string) {
                return programEditorController.programSequenceObservableList.stream()
                        .filter(sequence -> sequence.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    private void addSequence(UUID programId, ProgramSequence programSequence, int offSet) {
        ProgramSequenceBinding programSequenceBinding = new ProgramSequenceBinding(UUID.randomUUID(), programId, programSequence.getId(), offSet);
        addSequenceItem(programSequenceBinding, programSequence);
    }

    public void addSequenceItem(ProgramSequenceBinding programSequenceBinding, ProgramSequence programSequence) {
        try {
            ProgramEditorController.createProgramSequenceBindingItem(programSequenceBinding, sequenceSelector, position, programSequence, sequenceItemBindingFxml, applicationContext, bindViewParentContainer, projectService);
            checkIfNextItemIsPresent();
        } catch (Exception e) {
            LOG.error("Error creating new Sequence \n{}", StringUtils.formatStackTrace(e), e);
        }
    }

    private void checkIfNextItemIsPresent() {
        if (bindViewParentContainer.getChildren().size() - 1 < position + 1) {
            addBindingView(position + 1);
        }
    }

    protected void addBindingView(int position) {
        try {
            FXMLLoader loader = new FXMLLoader(sequenceHolderFxml.getURL());
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();
            bindViewParentContainer.getChildren().add(bindViewParentContainer.getChildren().size(), root);
            SequenceSelectorController sequenceSelector = loader.getController();
            sequenceSelector.setUp(bindViewParentContainer, position, programEditorController.getProgramId());
            HBox.setHgrow(root, javafx.scene.layout.Priority.ALWAYS);
        } catch (IOException e) {
            LOG.error("Error loading Sequence Holder view!\n{}", StringUtils.formatStackTrace(e), e);
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) sequenceSearch.getScene().getWindow();
        stage.close();
    }
}
