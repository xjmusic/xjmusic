// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.usage;

import io.outright.signal.SignalClient;
import io.xj.gui.controllers.SettingsModalController;
import io.xj.gui.services.SupportService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.utils.UiUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.springframework.stereotype.Service;

/**
 The first-run question (opt-in): "Help improve the XJ music workstation?", asked once, when the main window is
 first up.
 <p>
 Its rules, from Signal's: the two answers are the same size and the same weight, and neither is the default;
 nothing is ticked for the person, nothing is offered for a yes and nothing is said about a no; "What exactly is
 sent?" opens Settings › Privacy, with the live summary. Nothing is sent unless the answer is yes. A no is
 remembered and never asked again. Closing the window without an answer is no answer: nothing is sent, and the
 question is asked at the next launch.
 <p>
 Under notice_and_opt_out (a change to .signal/consent.yaml, once counsel signs it off), the same window is a
 notice, with "OK" and an equal "Turn off".
 */
@Service
public class UsageQuestion {
  static final double ANSWER_WIDTH = 160;
  private final UsageService usage;
  private final SupportService supportService;
  private final ThemeService themeService;
  private final SettingsModalController settingsModalController;

  public UsageQuestion(
    UsageService usage,
    SupportService supportService,
    ThemeService themeService,
    SettingsModalController settingsModalController
  ) {
    this.usage = usage;
    this.supportService = supportService;
    this.themeService = themeService;
    this.settingsModalController = settingsModalController;
  }

  /**
   Ask, if the person has not answered yet and usage reporting is not turned off.

   @param owner the main window
   */
  public void askIfNeeded(Window owner) {
    SignalClient client = usage.client();
    if (!client.needsConsent()) return;
    boolean notice = "notice_and_opt_out".equals(client.consentMode());

    var stage = new Stage();
    UiUtils.setupIcon(stage);
    stage.setTitle(UiUtils.computeTitle("Usage reporting"));
    stage.initOwner(owner);
    stage.initModality(Modality.APPLICATION_MODAL);
    stage.initStyle(StageStyle.UTILITY);

    var title = new Label(client.copy(notice ? "notice_title" : "prompt_title"));
    title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
    title.setWrapText(true);
    var body = new Label(client.copy(notice ? "notice_body" : "prompt_body"));
    body.setWrapText(true);
    body.setMaxWidth(520);

    Button first;
    Button second;
    if (notice) {
      first = answer(client.copy("notice_ok"), stage, client::acknowledgeNotice);
      second = answer(client.copy("notice_off"), stage, () -> client.setConsent(false));
    } else {
      first = answer(client.copy("prompt_yes"), stage, () -> client.setConsent(true));
      second = answer(client.copy("prompt_no"), stage, () -> client.setConsent(false));
    }
    var answers = new HBox(12, first, second);
    answers.setAlignment(Pos.CENTER);

    var what = new Hyperlink(client.copy("prompt_what"));
    what.setOnAction(event -> settingsModalController.launchModalWithPrivacySettings());
    var policy = new Hyperlink(client.copy("prompt_policy"));
    policy.setOnAction(event -> supportService.launchPrivacyPolicyInBrowser());
    var links = new HBox(12, what, policy);
    links.setAlignment(Pos.CENTER);

    var root = new VBox(14, title, body, answers, links);
    root.setPadding(new Insets(20));
    root.setMaxWidth(560);
    // Focus rests on the words: reaching an answer is a choice, not a default.
    root.setFocusTraversable(true);

    var scene = new Scene(root);
    themeService.setup(scene);
    stage.setScene(scene);
    stage.setOnShown(event -> root.requestFocus());
    stage.showAndWait();
  }

  /** One of the two answers: the same width, the same style, and neither the default nor the cancel button. */
  private static Button answer(String text, Stage stage, Runnable action) {
    var button = new Button(text);
    button.setMinWidth(ANSWER_WIDTH);
    button.setPrefWidth(ANSWER_WIDTH);
    button.setDefaultButton(false);
    button.setCancelButton(false);
    button.setOnAction(event -> {
      action.run();
      stage.close();
    });
    return button;
  }
}
