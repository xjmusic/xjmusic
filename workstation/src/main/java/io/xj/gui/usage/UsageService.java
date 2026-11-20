// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.usage;

import io.outright.signal.SignalClient;
import io.outright.signal.SignalHost;
import io.outright.signal.SignalStore;
import io.xj.gui.services.VersionService;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 Usage reporting: the workstation's anonymous usage numbers, sent to Signal, Outright Mental's own
 first-party, aggregate-only analytics. See PRIVACY.md at the root of the repository for exactly what is
 sent, metric by metric, and how to turn it off.
 <p>
 Nothing is sent unless the person says yes to the first-run question, or switches on "Share anonymous
 usage numbers" in Settings › Privacy. Before an answer, the session is counted in memory only, and is
 thrown away at quit unless the answer was yes. Nothing is written to disk before an answer.
 <p>
 Every value is a number: there is no field that could carry a name, a project, a file, music or text.
 The vendored Signal core (workstation/vendor/signal-core) aggregates the session into one summary of a
 few dozen bytes, which is posted once, when the workstation quits.
 <p>
 Hard off, for CI and anyone who wants it: {@code XJ_SIGNAL=off} in the environment, or
 {@code -Dxj.signal=off}. Signal's own switches work too: {@code --no-telemetry}, {@code SIGNAL_DISABLE=1},
 or a file named {@code signal-disable} in the user's configuration folder. With any of them, the client
 never starts: nothing is recorded, written or sent, and the question is never asked.
 */
@Service
public class UsageService {
  static final Logger LOG = LoggerFactory.getLogger(UsageService.class);

  /**
   The environment variable and system property that turn usage reporting off, whatever was answered.
   */
  public static final String HARD_OFF_ENV = "XJ_SIGNAL";
  public static final String HARD_OFF_PROPERTY = "xj.signal";

  private final SignalClient client;
  private final boolean hardOff;
  private final Path folder;
  private final List<Runnable> beforeEnd = new CopyOnWriteArrayList<>();

  public UsageService(
    @Value("${signal.key}") String key,
    @Value("${signal.endpoint}") String endpoint,
    VersionService versionService,
    ApplicationArguments arguments
  ) {
    String[] args = arguments.getSourceArgs();
    hardOff = isHardOff(System.getenv(), System.getProperty(HARD_OFF_PROPERTY))
      || SignalHost.killSwitch(args, System.getenv());
    folder = dataFolder(System.getProperty("os.name", ""), System.getProperty("user.home", "."), System.getenv());
    SignalHost host = SignalHost.detect(args, UsageChannel.resolve()).withKilled(hardOff);
    client = new SignalClient(
      XjSignalCatalog.CATALOG,
      key,
      endpoint,
      versionService.getVersion(),
      new SignalStore.Folder(folder),
      null,
      host,
      null,
      null,
      null
    );
    client.start();
    LOG.info("Usage reporting is {}", describe());
  }

  /**
   @return the Signal client, for the privacy panel and the first-run question
   */
  public SignalClient client() {
    return client;
  }

  /**
   @return true if usage reporting was turned off by XJ_SIGNAL=off, -Dxj.signal=off or one of Signal's own switches
   */
  public boolean isHardOff() {
    return hardOff;
  }

  /**
   @return the folder where the answer, and (only after a yes) the summaries waiting to be sent, are kept
   */
  public Path folder() {
    return folder;
  }

  /**
   @return the state of usage reporting, in a few words, for the log and the privacy panel
   */
  public String describe() {
    if (hardOff) return "off: turned off on this computer (" + HARD_OFF_ENV + "=off, -D" + HARD_OFF_PROPERTY + "=off or a Signal switch)";
    return switch (client.state()) {
      case DISABLED -> "off";
      case AWAITING_CONSENT -> "waiting for an answer: nothing is sent unless you say yes";
      case ACTIVE -> client.needsConsent() ? "on, until you turn it off" : "on: you said yes";
      case PAUSED -> "off: you said no";
      case KILLED -> "paused by Signal: nothing is sent";
    };
  }

  /**
   Run this just before the session's summary is made at quit: for numbers that are only known at the end.
   */
  public void beforeEnd(Runnable task) {
    beforeEnd.add(task);
  }

  // ── Recording: each a few integer adds in memory, and nothing if reporting is off ──────────────────────

  /** Count one use of a major feature: one of {@link XjSignalCatalog.L}'s FEATURES_* labels. */
  public void feature(int label) {
    client.countLabel(XjSignalCatalog.M.FEATURES, label);
  }

  /** Reach a step: one of {@link XjSignalCatalog.MS}. */
  public void milestone(int ms) {
    client.milestone(ms);
  }

  /** Arrive at a view: one of {@link XjSignalCatalog.N}. */
  public void node(int node) {
    client.node(node);
  }

  /** Count one failure: one of {@link XjSignalCatalog.L}'s FAILURES_* labels. */
  public void failure(int label) {
    client.countLabel(XjSignalCatalog.M.FAILURES, label);
  }

  /** Count one error dialog shown. */
  public void errorShown() {
    client.count(XjSignalCatalog.M.ERRORS_SHOWN);
  }

  /** Count one under a label of a labeled counter. */
  public void countLabel(int metric, int label) {
    client.countLabel(metric, label);
  }

  /** Put a number of seconds in a histogram. */
  public void time(int metric, double seconds, int label) {
    client.time(metric, seconds, label);
  }

  /** Set a first-day trait. */
  public void trait(int bit) {
    client.setTrait(bit);
  }

  /** Something was clicked or typed: the session is used with a mouse and keyboard. */
  public void input() {
    client.input(io.outright.signal.SignalCore.InputKind.KBM);
  }

  /** Whole seconds since the workstation was launched, while a session is being counted. */
  public int elapsedSeconds() {
    return client.elapsedSeconds();
  }

  /**
   At quit: finish the numbers only known at the end, make the session's one summary, and send it if the
   answer was yes, waiting at most about a second for the answer. A summary that cannot be sent in time
   waits on disk for the next launch.
   */
  @PreDestroy
  public void finish() {
    for (Runnable task : beforeEnd) {
      try {
        task.run();
      } catch (RuntimeException e) {
        LOG.debug("Usage reporting: a task before the end failed", e);
      }
    }
    beforeEnd.clear();
    client.finish(SignalClient.QUIT_BOUND);
  }

  // ── Inside ─────────────────────────────────────────────────────────────────────────────────────────────

  /**
   @return true if XJ_SIGNAL or -Dxj.signal says off (off, 0, false or no, in any case)
   */
  static boolean isHardOff(Map<String, String> environment, String property) {
    return isOff(environment.get(HARD_OFF_ENV)) || isOff(property);
  }

  private static boolean isOff(String value) {
    if (value == null) return false;
    return List.of("off", "0", "false", "no").contains(value.trim().toLowerCase(Locale.ROOT));
  }

  /**
   The folder of the workstation's own where usage reporting keeps its two small files. It is made only
   when something is first written, which is never before an answer.

   @return %APPDATA%\XJ Music\workstation\signal on Windows, ~/Library/Application Support/XJ Music/workstation/signal
   on macOS, and $XDG_DATA_HOME/xjmusic/workstation/signal (~/.local/share/...) elsewhere
   */
  static Path dataFolder(String osName, String home, Map<String, String> environment) {
    String os = osName.toLowerCase(Locale.ROOT);
    if (os.contains("win")) {
      String appData = environment.get("APPDATA");
      Path base = appData != null && !appData.isBlank() ? Path.of(appData) : Path.of(home, "AppData", "Roaming");
      return base.resolve("XJ Music").resolve("workstation").resolve("signal");
    }
    if (os.contains("mac") || os.contains("darwin")) {
      return Path.of(home, "Library", "Application Support", "XJ Music", "workstation", "signal");
    }
    String xdg = environment.get("XDG_DATA_HOME");
    Path base = xdg != null && !xdg.isBlank() ? Path.of(xdg) : Path.of(home, ".local", "share");
    return base.resolve("xjmusic").resolve("workstation").resolve("signal");
  }
}
