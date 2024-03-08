package io.xj.gui.controllers.content.program.edit_mode;

import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.hub.tables.pojos.ProgramVoice;
import javafx.fxml.FXML;
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

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class VoiceController {
  static final Logger LOG = LoggerFactory.getLogger(VoiceController.class);
  private final Resource trackFxml;
  private final Resource patternMenuFxml;
  private final Resource trackMenuFxml;
  private final Resource patternSelectorFxml;
  private final int trackHeight;
  private final int voiceControlWidth;
  private final ApplicationContext ac;
  private final ThemeService themeService;
  private final ProjectService projectService;
  private final UIStateService uiStateService;
  private ProgramVoice voice;
  private Runnable deleteVoice;

  @FXML
  public VBox voiceControlContainer;
  @FXML
  public HBox voiceContainer;

  public VoiceController(
    @Value("classpath:/views/content/program/edit_mode/track.fxml") Resource trackFxml,
    @Value("classpath:/views/content/program/edit_mode/pattern-menu.fxml") Resource patternMenuFxml,
    @Value("classpath:/views/content/common/popup-action-menu.fxml") Resource trackMenuFxml,
    @Value("classpath:/views/content/program/edit_mode/pattern-selector.fxml") Resource patternSelectorFxml,
    @Value("${programEditor.trackHeight}") int trackHeight,
    @Value("${programEditor.voiceControlWidth}") int voiceControlWidth,
    ApplicationContext ac,
    ThemeService themeService,
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    this.trackFxml = trackFxml;
    this.patternMenuFxml = patternMenuFxml;
    this.trackMenuFxml = trackMenuFxml;
    this.patternSelectorFxml = patternSelectorFxml;
    this.trackHeight = trackHeight;
    this.voiceControlWidth = voiceControlWidth;
    this.ac = ac;
    this.themeService = themeService;
    this.projectService = projectService;
    this.uiStateService = uiStateService;
  }

  /**
   Setup the voice controller

   @param voice       the voice
   @param deleteVoice callback to delete voice
   */
  protected void setup(ProgramVoice voice, Runnable deleteVoice) {
    this.voice = voice;
    this.deleteVoice = deleteVoice;
  }

  public void teardown() {
    // todo teardown the tracks inside of here
  }


}
