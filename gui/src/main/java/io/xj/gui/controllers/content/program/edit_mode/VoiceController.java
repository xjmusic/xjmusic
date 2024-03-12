package io.xj.gui.controllers.content.program.edit_mode;

import io.xj.gui.controllers.content.common.PopupActionMenuController;
import io.xj.gui.controllers.content.common.PopupSelectorMenuController;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.UiUtils;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import io.xj.hub.util.StringUtils;
import jakarta.annotation.Nullable;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VoiceController {
  static final Logger LOG = LoggerFactory.getLogger(VoiceController.class);
  private final Collection<Runnable> unsubscriptions = new HashSet<>();
  private final Resource trackFxml;
  private final int trackHeight;
  private final int trackSpaceBetween;
  private final int voiceControlWidth;
  private final int trackControlWidth;
  private final ApplicationContext ac;
  private final ProjectService projectService;
  private final UIStateService uiStateService;
  private final Runnable updateVoiceName;
  private final Runnable updatePatternName;
  private final Runnable updateVoiceType;
  private final Runnable updatePatternTotal;
  private final ObjectProperty<UUID> patternId = new SimpleObjectProperty<>();
  private final ObservableList<VoiceTrackController> trackControllers = FXCollections.observableArrayList();
  private final InvalidationListener trackControllersChange = (observable) -> setupVoiceHeight();
  private UUID programVoiceId;
  private Runnable handleDeleteVoice;

  @FXML
  VBox voiceControlContainer;

  @FXML
  HBox voiceContainer;

  @FXML
  Button voiceActionLauncher;

  @FXML
  TextField voiceNameField;

  @FXML
  TextField patternNameField;

  @FXML
  ComboBox<InstrumentType> voiceTypeChooser;

  @FXML
  Label noSequencesLabel;

  @FXML
  Label noPatternsLabel;

  @FXML
  VBox voicePatternControlContainer;

  @FXML
  Button patternSelectorLauncher;

  @FXML
  Button patternActionLauncher;

  @FXML
  HBox patternTotalContainer;

  @FXML
  TextField patternTotalField;

  @FXML
  VBox tracksContainer;

  @FXML
  AnchorPane trackAddContainer;

  @FXML
  Button addTrackButton;

  public VoiceController(
    @Value("classpath:/views/content/program/edit_mode/voice-track.fxml") Resource trackFxml,
    @Value("${programEditor.trackHeight}") int trackHeight,
    @Value("${programEditor.trackSpaceBetween}") int trackSpaceBetween,
    @Value("${programEditor.voiceControlWidth}") int voiceControlWidth,
    @Value("${programEditor.trackControlWidth}") int trackControlWidth,
    ApplicationContext ac,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    this.trackFxml = trackFxml;
    this.trackHeight = trackHeight;
    this.trackSpaceBetween = trackSpaceBetween;
    this.voiceControlWidth = voiceControlWidth;
    this.trackControlWidth = trackControlWidth;
    this.ac = ac;
    this.projectService = projectService;
    this.uiStateService = uiStateService;

    updateVoiceName = () -> projectService.update(ProgramVoice.class, programVoiceId, "name", voiceNameField.getText());
    updateVoiceType = () -> projectService.update(ProgramVoice.class, programVoiceId, "type", voiceTypeChooser.getValue());
    updatePatternName = () -> {
      if (patternId.isNotNull().get())
        projectService.update(ProgramSequencePattern.class, patternId.get(), "name", patternNameField.getText());
    };
    updatePatternTotal = () -> {
      if (patternId.isNotNull().get())
        projectService.update(ProgramSequencePattern.class, patternId.get(), "total", patternTotalField.getText());
    };
  }

  /**
   Set up the voice controller

   @param programVoiceId    the voice id
   @param handleDeleteVoice callback to delete voice
   */
  protected void setup(UUID programVoiceId, Runnable handleDeleteVoice) {
    this.programVoiceId = programVoiceId;
    this.handleDeleteVoice = handleDeleteVoice;

    ProgramVoice voice = projectService.getContent().getProgramVoice(programVoiceId).orElseThrow(() -> new RuntimeException("Voice not found!"));

    voiceControlContainer.setMinWidth(voiceControlWidth);
    voiceControlContainer.setMaxWidth(voiceControlWidth);

    trackAddContainer.setMinWidth(trackControlWidth);
    trackAddContainer.setMaxWidth(trackControlWidth);
    var trackAddVisible = Bindings.createBooleanBinding(trackControllers::isEmpty, trackControllers);
    trackAddContainer.visibleProperty().bind(trackAddVisible);
    trackAddContainer.managedProperty().bind(trackAddVisible);

    noSequencesLabel.visibleProperty().bind(uiStateService.currentProgramSequenceProperty().isNull());
    noSequencesLabel.managedProperty().bind(uiStateService.currentProgramSequenceProperty().isNull());
    voicePatternControlContainer.visibleProperty().bind(uiStateService.currentProgramSequenceProperty().isNotNull());
    voicePatternControlContainer.managedProperty().bind(uiStateService.currentProgramSequenceProperty().isNotNull());
    noPatternsLabel.visibleProperty().bind(patternId.isNull());
    noPatternsLabel.managedProperty().bind(patternId.isNull());
    patternSelectorLauncher.visibleProperty().bind(patternId.isNotNull());
    patternSelectorLauncher.managedProperty().bind(patternId.isNotNull());
    patternNameField.visibleProperty().bind(patternId.isNotNull());
    patternNameField.managedProperty().bind(patternId.isNotNull());
    patternTotalContainer.visibleProperty().bind(patternId.isNotNull());
    patternTotalContainer.managedProperty().bind(patternId.isNotNull());

    voiceNameField.setText(voice.getName());
    unsubscriptions.add(UiUtils.onBlur(voiceNameField, updateVoiceName));
    UiUtils.blurOnEnterKeyPress(voiceNameField);

    voiceTypeChooser.setItems(FXCollections.observableArrayList(InstrumentType.values()));
    voiceTypeChooser.setValue(voice.getType());
    unsubscriptions.add(UiUtils.onChange(voiceTypeChooser.valueProperty(), updateVoiceType));
    UiUtils.blurOnSelection(voiceTypeChooser);

    unsubscriptions.add(UiUtils.onBlur(patternTotalField, updatePatternTotal));
    UiUtils.blurOnEnterKeyPress(patternTotalField);

    unsubscriptions.add(UiUtils.onBlur(patternNameField, updatePatternName));
    UiUtils.blurOnEnterKeyPress(patternNameField);

    unsubscriptions.add(UiUtils.onChange(uiStateService.currentProgramSequenceProperty(), this::selectFirstPattern));
    selectFirstPattern();

    for (ProgramVoiceTrack programTrack :
      projectService.getContent().getTracksOfVoice(programVoiceId).stream()
        .sorted(Comparator.comparing(ProgramVoiceTrack::getOrder))
        .toList()) {
      addTrack(programTrack);
    }

    tracksContainer.setSpacing(trackSpaceBetween);
    trackControllers.addListener(trackControllersChange);
    unsubscriptions.add(() -> trackControllers.removeListener(trackControllersChange));
    setupVoiceHeight();
  }

  /**
   Teardown the voice controller
   */
  public void teardown() {
    for (Runnable unsubscription : unsubscriptions) unsubscription.run();

    for (VoiceTrackController controller : trackControllers) controller.teardown();
    tracksContainer.getChildren().clear();
    trackControllers.clear();
  }

  @FXML
  void handlePressedVoiceActionLauncher() {
    uiStateService.launchPopupActionMenu(
      voiceActionLauncher,
      (PopupActionMenuController controller) -> controller.setup(
        "New Voice",
        null,
        handleDeleteVoice,
        null
      )
    );
  }

  @FXML
  void handlePressedPatternSelectorLauncher() {
    uiStateService.launchPopupSelectorMenu(
      patternSelectorLauncher,
      (PopupSelectorMenuController controller) -> controller.setup(
        projectService.getContent().getPatternsOfSequenceAndVoice(uiStateService.currentProgramSequenceProperty().get().getId(), programVoiceId),
        this::handleSelectPattern
      )
    );
  }

  @FXML
  void handlePressedPatternActionLauncher() {
    uiStateService.launchPopupActionMenu(
      patternActionLauncher,
      (PopupActionMenuController controller) -> controller.setup(
        "New Pattern",
        this::handleCreatePattern,
        patternId.isNotNull().get() ? this::handleDeletePattern : null,
        patternId.isNotNull().get() ? this::handleClonePattern : null
      )
    );
  }

  @FXML
  void handlePressedAddTrack() {
    try {
      ProgramVoiceTrack programTrack = projectService.createProgramVoiceTrack(programVoiceId);
      addTrack(programTrack);
    } catch (Exception e) {
      LOG.error("Could not create new Track! {}\n{}", e, StringUtils.formatStackTrace(e));
    }
  }

  /**
   Set up the height of the voice based on its # of tracks
   Total voice height is the height of each track + the height of the gaps between tracks
   */
  private void setupVoiceHeight() {
    voiceContainer.setMinHeight(trackHeight * (Math.max(1, trackControllers.size())) + trackSpaceBetween * Math.max(0, trackControllers.size() - 1));
  }

  /**
   Select the first pattern found for this voice, or clear the selection if none are found
   */
  private void selectFirstPattern() {
    if (uiStateService.currentProgramSequenceProperty().isNotNull().get()) {
      handleSelectPattern(projectService.getContent().getPatternsOfSequenceAndVoice(uiStateService.currentProgramSequenceProperty().get().getId(), programVoiceId).stream().findFirst().map(ProgramSequencePattern::getId).orElse(null));
    } else {
      handleSelectPattern(null);
    }
  }

  /**
   Select a pattern

   @param patternId that was selected
   */
  private void handleSelectPattern(@Nullable UUID patternId) {
    if (Objects.nonNull(patternId)) {
      var pattern = projectService.getContent().getProgramSequencePattern(patternId).orElseThrow(() -> new RuntimeException("Pattern not found!"));
      this.patternId.set(patternId);
      patternNameField.setText(pattern.getName());
      patternTotalField.setText(pattern.getTotal().toString());
    } else {
      this.patternId.set(null);
      patternNameField.clear();
      patternTotalField.clear();
    }
  }

  /**
   Create a new pattern and select it
   */
  private void handleCreatePattern() {
    try {
      var pattern = projectService.createProgramSequencePattern(
        uiStateService.currentProgramProperty().get().getId(),
        uiStateService.currentProgramSequenceProperty().get().getId(),
        programVoiceId
      );
      handleSelectPattern(pattern.getId());
    } catch (Exception e) {
      LOG.error("Could not create new Pattern", e);
    }
  }

  /**
   Delete a new pattern and select it
   */
  private void handleDeletePattern() {
    if (!projectService.deleteProgramSequencePattern(patternId.get())) {
      return;
    }
    selectFirstPattern();
  }

  /**
   Clone a new pattern and select it
   */
  private void handleClonePattern() {
    try {
      var pattern = projectService.cloneProgramSequencePattern(patternId.get());
      handleSelectPattern(pattern.getId());
    } catch (Exception e) {
      LOG.error("Could not clone Pattern", e);
    }
  }

  /**
   Add a program sequence binding item to the sequence binding column.

   @param programTrack to add
   */
  private void addTrack(ProgramVoiceTrack programTrack) {
    try {
      FXMLLoader loader = new FXMLLoader(trackFxml.getURL());
      loader.setControllerFactory(ac::getBean);
      Parent root = loader.load();
      VoiceTrackController controller = loader.getController();
      trackControllers.add(controller);
      controller.setup(
        programTrack.getId(),
        this::handlePressedAddTrack,
        () -> {
          if (!projectService.deleteProgramVoiceTrack(programTrack.getId())) {
            return;
          }
          controller.teardown();
          trackControllers.remove(controller);
          tracksContainer.getChildren().remove(root);
        },
        patternId
      );
      tracksContainer.getChildren().add(root);
    } catch (IOException e) {
      LOG.error("Error adding Track! {}\n{}", e, StringUtils.formatStackTrace(e));
    }
  }

}
