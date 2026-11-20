// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.usage;

import io.outright.signal.SignalClient;
import io.xj.gui.services.SupportService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

/**
 Settings › Privacy: usage reporting, in the open.
 <ul>
 <li>"Share anonymous usage numbers": the switch. Off drops what was counted and what was waiting, at once.</li>
 <li>"What we send": the next summary, exactly as it would be sent if the workstation quit now, as bytes and as
 a table of what each part means, made by the same code that sends it. Then the last five that were sent.</li>
 <li>"Reset local stats", the privacy policy, PRIVACY.md, and where the two small files are kept.</li>
 </ul>
 */
public final class UsagePrivacyPanel {
  private final UsageService usage;
  private final SupportService supportService;
  private final SignalClient client;
  private final CheckBox share = new CheckBox();
  private final Label status = new Label();
  private final TextArea preview = new TextArea();
  private final Label note = new Label();
  private final Timeline refresher;
  private final Runnable listener = () -> Platform.runLater(this::refresh);
  private boolean live = false;

  private UsagePrivacyPanel(UsageService usage, SupportService supportService) {
    this.usage = usage;
    this.supportService = supportService;
    this.client = usage.client();
    refresher = new Timeline(new KeyFrame(Duration.seconds(1), event -> refresh()));
    refresher.setCycleCount(Timeline.INDEFINITE);
  }

  /**
   Fill a container with the privacy panel, and keep its "What we send" live while the container is showing.

   @param container      the Settings modal's privacy container
   @param usage          usage reporting
   @param supportService for the links
   */
  public static void fill(VBox container, UsageService usage, SupportService supportService) {
    var panel = new UsagePrivacyPanel(usage, supportService);
    container.getChildren().setAll(panel.build());
    // Live only while the tab is selected and its window is up: closing Settings stops the refresh.
    Runnable update = () -> {
      var scene = container.getScene();
      var window = scene == null ? null : scene.getWindow();
      panel.showing(container.isVisible() && window != null && window.isShowing());
    };
    container.visibleProperty().addListener((o, ov, visible) -> update.run());
    javafx.beans.value.ChangeListener<Boolean> onShowing = (o, ov, showing) -> update.run();
    javafx.beans.value.ChangeListener<javafx.stage.Window> onWindow = (o, ov, window) -> {
      if (ov != null) ov.showingProperty().removeListener(onShowing);
      if (window != null) window.showingProperty().addListener(onShowing);
      update.run();
    };
    container.sceneProperty().addListener((o, ov, scene) -> {
      if (ov != null) ov.windowProperty().removeListener(onWindow);
      if (scene != null) {
        scene.windowProperty().addListener(onWindow);
        onWindow.changed(null, null, scene.getWindow());
      }
      update.run();
    });
    if (container.getScene() != null) {
      container.getScene().windowProperty().addListener(onWindow);
      onWindow.changed(null, null, container.getScene().getWindow());
    }
    update.run();
  }

  private List<javafx.scene.Node> build() {
    var title = new Label("Usage reporting");
    title.setStyle("-fx-font-weight: bold;");

    var about = new Label(client.copy("prompt_body"));
    about.setWrapText(true);

    share.setText(client.copy("privacy_share"));
    share.setOnAction(event -> {
      client.setConsent(share.isSelected());
      note.setText("");
      refresh();
    });

    var heading = new Label(client.copy("privacy_preview"));
    heading.setStyle("-fx-font-weight: bold;");
    var hint = new Label("Below is the next summary, exactly as it would be sent if you quit now: its bytes, then what each part means. It updates as you work. Every value is a number.");
    hint.setWrapText(true);

    preview.setEditable(false);
    preview.setWrapText(true);
    preview.setPrefRowCount(14);
    preview.setStyle("-fx-font-family: 'Roboto Mono', monospace; -fx-font-size: 11px;");
    VBox.setVgrow(preview, Priority.ALWAYS);

    var reset = new Button(client.copy("privacy_reset"));
    reset.setOnAction(event -> {
      client.resetLocal();
      note.setText(client.copy("privacy_reset_done"));
      refresh();
    });

    var policy = new Hyperlink(client.copy("privacy_policy"));
    policy.setOnAction(event -> supportService.launchPrivacyPolicyInBrowser());
    var doc = new Hyperlink("What is sent, metric by metric (PRIVACY.md)");
    doc.setOnAction(event -> supportService.launchUsageReportingDocInBrowser());
    var actions = new HBox(10, reset, policy, doc);
    actions.setStyle("-fx-alignment: center-left;");

    var kept = new Label("Kept on this computer, only once you have answered: your answer, and after a yes, the summaries waiting to be sent, in "
      + usage.folder() + ". To turn usage reporting off for good on this computer, whatever was answered, set the environment variable "
      + UsageService.HARD_OFF_ENV + "=off.");
    kept.setWrapText(true);
    kept.setStyle("-fx-font-size: 11px;");
    note.setWrapText(true);

    var box = new VBox(8, title, about, share, status, heading, hint, preview, actions, note, kept);
    box.setPadding(new Insets(0));
    refresh();
    return List.of(box);
  }

  private void showing(boolean visible) {
    if (visible == live) return;
    live = visible;
    if (visible) {
      client.privacyOpened();
      client.addListener(listener);
      refresh();
      refresher.play();
    } else {
      client.removeListener(listener);
      refresher.stop();
    }
  }

  private void refresh() {
    var state = client.state();
    share.setSelected(client.hasConsent());
    share.setDisable(usage.isHardOff() || state == SignalClient.State.DISABLED || state == SignalClient.State.KILLED);
    status.setText("Usage reporting is " + usage.describe() + ". Channel: " + client.channel() + ".");
    status.setWrapText(true);

    var text = new StringBuilder();
    var next = client.previewNextPayload();
    if (next == null) {
      text.append(client.copy("privacy_preview_none")).append('\n');
    } else {
      text.append(client.copy("privacy_preview_next")).append(" · ").append(next.bytes()).append(" bytes\n");
      text.append(next.hex()).append("\n\n");
      for (String[] row : next.rows()) text.append(row[0]).append(": ").append(row[1]).append('\n');
    }
    var sent = client.lastSent();
    if (!sent.isEmpty()) {
      text.append('\n').append(client.copy("privacy_preview_sent")).append('\n');
      for (String hex : sent) text.append(hex).append('\n');
    }
    if (!text.toString().equals(preview.getText())) {
      double scroll = preview.getScrollTop();
      preview.setText(text.toString());
      preview.setScrollTop(scroll);
    }
  }
}
