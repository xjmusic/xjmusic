package io.xj.gui.controllers.content.program.edit_mode;

import io.xj.gui.controllers.content.common.PopupActionMenuController;
import io.xj.gui.controllers.content.common.PopupSelectorMenuController;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.UiUtils;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramVoice;
import jakarta.annotation.Nullable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VoiceController {
  static final Logger LOG = LoggerFactory.getLogger(VoiceController.class);
  private final Collection<Runnable> subscriptions = new HashSet<>();
  private final Resource trackFxml;
  private final Resource popupSelectorMenuFxml;
  private final Resource popupActionMenuFxml;
  private final int trackHeight;
  private final int voiceControlWidth;
  private final ApplicationContext ac;
  private final ThemeService themeService;
  private final ProjectService projectService;
  private final UIStateService uiStateService;
  private final Runnable updateVoiceName;
  private final Runnable updatePatternName;
  private final Runnable updateVoiceType;
  private final Runnable updatePatternTotal;
  private final ObjectProperty<UUID> patternId = new SimpleObjectProperty<>();
  private final SpinnerValueFactory<Integer> patternTotalValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 0);
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
  Spinner<Integer> patternTotalChooser;

  public VoiceController(
    @Value("classpath:/views/content/program/edit_mode/track.fxml") Resource trackFxml,
    @Value("classpath:/views/content/common/popup-selector-menu.fxml") Resource popupSelectorMenuFxml,
    @Value("classpath:/views/content/common/popup-action-menu.fxml") Resource popupActionMenuFxml,
    @Value("${programEditor.trackHeight}") int trackHeight,
    @Value("${programEditor.voiceControlWidth}") int voiceControlWidth,
    ApplicationContext ac,
    ThemeService themeService,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    this.trackFxml = trackFxml;
    this.popupSelectorMenuFxml = popupSelectorMenuFxml;
    this.popupActionMenuFxml = popupActionMenuFxml;
    this.trackHeight = trackHeight;
    this.voiceControlWidth = voiceControlWidth;
    this.ac = ac;
    this.themeService = themeService;
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
        projectService.update(ProgramSequencePattern.class, patternId.get(), "total", patternTotalValueFactory.getValue());
    };
  }

  /**
   Setup the voice controller

   @param programVoiceId the voice id
   @param deleteVoice    callback to delete voice
   */
  protected void setup(UUID programVoiceId, Runnable deleteVoice) {
    this.programVoiceId = programVoiceId;
    this.handleDeleteVoice = deleteVoice;

    voiceContainer.setMinHeight(trackHeight);
    voiceControlContainer.setMinWidth(voiceControlWidth);
    voiceControlContainer.setMaxWidth(voiceControlWidth);

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

    ProgramVoice voice = projectService.getContent().getProgramVoice(programVoiceId).orElseThrow(() -> new RuntimeException("Voice not found!"));

    voiceNameField.setText(voice.getName());
    subscriptions.add(UiUtils.onBlur(voiceNameField, updateVoiceName));
    UiUtils.blurOnEnterKeyPress(voiceNameField);

    voiceTypeChooser.setItems(FXCollections.observableArrayList(InstrumentType.values()));
    voiceTypeChooser.setValue(voice.getType());
    subscriptions.add(UiUtils.onBlur(voiceTypeChooser, updateVoiceType));
    UiUtils.blurOnSelection(voiceTypeChooser);

    patternTotalChooser.setValueFactory(patternTotalValueFactory);
    subscriptions.add(UiUtils.onChange(patternTotalValueFactory.valueProperty(), updatePatternTotal));
    subscriptions.add(UiUtils.onBlur(patternTotalChooser, updatePatternTotal));
    UiUtils.blurOnEnterKeyPress(patternTotalChooser);

    subscriptions.add(UiUtils.onBlur(patternNameField, updatePatternName));
    UiUtils.blurOnEnterKeyPress(patternNameField);

    subscriptions.add(UiUtils.onChange(uiStateService.currentProgramSequenceProperty(), this::selectFirstPattern));
    selectFirstPattern();
  }

  /**
   Teardown the voice controller
   */
  public void teardown() {
    for (Runnable subscription : subscriptions) subscription.run();
    // todo teardown the tracks inside of here
    // todo teardown the patterns inside of here
    // todo teardown listeners

    // todo teardown listener to pattern updates
  }

  @FXML
  void handlePressedVoiceActionLauncher() {
    UiUtils.launchModalMenu(voiceActionLauncher, popupActionMenuFxml, ac, themeService.getMainScene().getWindow(),
      true, (PopupActionMenuController controller) -> controller.setup(
        "New Voice",
        null,
        handleDeleteVoice,
        null
      )
    );
  }

  @FXML
  void handlePressedPatternSelectorLauncher() {
    UiUtils.launchModalMenu(patternSelectorLauncher, popupSelectorMenuFxml, ac, themeService.getMainScene().getWindow(),
      true, (PopupSelectorMenuController controller) -> controller.setup(
        projectService.getContent().getPatternsOfSequenceAndVoice(uiStateService.currentProgramSequenceProperty().get().getId(), programVoiceId),
        this::handleSelectPattern
      )
    );
  }

  @FXML
  void handlePressedPatternActionLauncher() {
    UiUtils.launchModalMenu(patternActionLauncher, popupActionMenuFxml, ac, themeService.getMainScene().getWindow(),
      true, (PopupActionMenuController controller) -> controller.setup(
        "New Pattern",
        this::handleCreatePattern,
        patternId.isNotNull().get() ? this::handleDeletePattern : null,
        patternId.isNotNull().get() ? this::handleClonePattern : null
      )
    );
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
      patternTotalValueFactory.setValue(pattern.getTotal().intValue());
    } else {
      this.patternId.set(null);
      patternNameField.clear();
      patternTotalValueFactory.setValue(0);
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
    projectService.deleteContent(ProgramSequencePattern.class, patternId.get());
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

}
