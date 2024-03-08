package io.xj.gui.controllers.content.program.edit_mode;

import io.xj.gui.controllers.content.common.PopupActionMenuController;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.UiUtils;
import io.xj.hub.tables.pojos.ProgramVoice;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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

import java.util.UUID;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VoiceController {
  static final Logger LOG = LoggerFactory.getLogger(VoiceController.class);
  private final Resource trackFxml;
  private final Resource popupSelectorMenuFxml;
  private final Resource popupActionMenuFxml;
  private final int trackHeight;
  private final int voiceControlWidth;
  private final ApplicationContext ac;
  private final ThemeService themeService;
  private final ProjectService projectService;
  private final UIStateService uiStateService;
  private UUID programVoiceId;
  private Runnable deleteVoice;

  @FXML
  VBox voiceControlContainer;

  @FXML
  HBox voiceContainer;

  @FXML
  Button voiceActionLauncher;

  @FXML
  TextField nameField;

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
  }

  /**
   Setup the voice controller

   @param programVoiceId     the voice id
   @param deleteVoice callback to delete voice
   */
  protected void setup(UUID programVoiceId, Runnable deleteVoice) {
    this.programVoiceId = programVoiceId;
    this.deleteVoice = deleteVoice;

    voiceContainer.setMinHeight(trackHeight);
    voiceControlContainer.setMinWidth(voiceControlWidth);
    voiceControlContainer.setMaxWidth(voiceControlWidth);

    ProgramVoice voice = projectService.getContent().getProgramVoice(programVoiceId).orElseThrow(() -> new RuntimeException("Voice not found!"));

    nameField.setText(voice.getName());
    UiUtils.onBlur(nameField, () -> projectService.update(ProgramVoice.class, programVoiceId, "name", nameField.getText()));
    UiUtils.transferFocusOnEnterKeyPress(nameField);
  }

  /**
   Teardown the voice controller
   */
  public void teardown() {
    // todo teardown the tracks inside of here
    // todo teardown listeners
  }

  @FXML
  void handlePressedVoiceActionLauncher() {
    UiUtils.launchModalMenu(voiceActionLauncher, popupActionMenuFxml, ac, themeService.getMainScene().getWindow(),
      true, (PopupActionMenuController controller) -> controller.setup(
        null,
        deleteVoice,
        null
      )
    );
  }

}
