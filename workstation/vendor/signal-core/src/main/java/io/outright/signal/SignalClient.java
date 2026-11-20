package io.outright.signal;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleSupplier;

/**
 * Signal for a Java desktop app: {@link SignalCore} with an app around it,
 * and no UI of its own.
 *
 * What it does is small and fixed. Calls add integers to a session in
 * memory. When the session ends, the session is folded into one summary of
 * a few dozen bytes and posted. Nothing is sent, and nothing is written to
 * the disk, until the person has said yes; a no is remembered and never
 * asked again; and the person can see the exact bytes ({@link #previewNextPayload})
 * and switch it off ({@link #setConsent}).
 *
 * This is the Flutter package's {@code SignalClient} (and the Godot addon's
 * {@code Signals}), decision for decision, for a desktop app: the states, the
 * spool, the request budget, the backoff, the bounded {@link #finish}. It
 * draws nothing: the app puts up the question and the privacy panel, with
 * {@link #needsConsent}, {@link #consentMode} and {@link #copy}. It has no
 * pulse, review request or feedback form; {@link Rules} has the decisions for
 * an app that wants them.
 *
 * Failure is silent: an app is never slowed, stopped or told about any of it.
 * Every public method may be called from any thread.
 */
public final class SignalClient {
  /**
   * The client's states (README §7.1):
   *
   * <pre>
   *     DISABLED          consent mode off, a kill switch, or no key
   *     AWAITING_CONSENT  recording in memory only; a yes sends this session, a no discards it
   *     ACTIVE            recording, and sending when a session ends
   *     PAUSED            the person said no, or switched sharing off
   *     KILLED            the platform answered 410: silent until its Retry-After, or the next build
   * </pre>
   */
  public enum State { DISABLED, AWAITING_CONSENT, ACTIVE, PAUSED, KILLED }

  /** What {@link #previewNextPayload} shows: the next summary as it would leave now. */
  public record Preview(String hex, int bytes, List<String[]> rows) {
  }

  public static final String DEFAULT_ENDPOINT = "https://in.signal.outright.io";
  public static final int REQUESTS_PER_DAY = 8;
  public static final Duration QUIT_BOUND = Duration.ofSeconds(1);
  public static final int BACKOFF_MIN_S = 5;
  public static final int BACKOFF_MAX_S = 300;
  public static final int IDLE_FLUSH_S = 600;
  public static final int KEPT_SENT = 5;

  public final SignalCatalog catalog;
  public final String key;
  public final String endpoint;
  /** The build's version: a 410 silences the client until a new one. */
  public final String build;
  private final SignalStore store;
  private final SignalTransport transport;
  private final SignalHost host;
  private final DoubleSupplier clock;
  private final Clock wall;
  private final Random random;
  private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

  private State state = State.DISABLED;
  private SignalCore core;
  private Map<String, Object> stored = new LinkedHashMap<>();
  private List<byte[]> spool = new ArrayList<>();
  private int[] dims = {0, 0, 0, 0};
  private boolean started = false;
  private CompletableFuture<Void> inFlight;
  private boolean firstInput = false;
  private int backoffS = 0;
  private int lastNode = -1;

  /**
   * @param catalog   the generated catalog
   * @param key       the stream's public key
   * @param endpoint  the ingest, or null for {@link #DEFAULT_ENDPOINT}
   * @param build     the app's version
   * @param store     where the answer, the ledger and the spool are kept
   * @param transport how a summary leaves, or null for {@link SignalTransport.Http}
   * @param host      the computer and the build, from {@link SignalHost#detect}
   * @param clock     seconds, monotonic; null for the JVM's own
   * @param wall      the wall clock and time zone the local day is read in; null for the system's
   */
  public SignalClient(SignalCatalog catalog, String key, String endpoint, String build, SignalStore store,
                      SignalTransport transport, SignalHost host, DoubleSupplier clock, Clock wall, Random random) {
    this.catalog = catalog;
    this.key = key == null ? "" : key;
    this.endpoint = endpoint == null ? DEFAULT_ENDPOINT : endpoint;
    this.build = build == null ? "dev" : build;
    this.store = store;
    this.transport = transport == null ? new SignalTransport.Http() : transport;
    this.host = host;
    long origin = System.nanoTime();
    this.clock = clock == null ? () -> (System.nanoTime() - origin) / 1e9 : clock;
    this.wall = wall == null ? Clock.systemDefaultZone() : wall;
    this.random = random == null ? new SecureRandom() : random;
  }

  // ── What the app reads ─────────────────────────────────────────────────────

  public synchronized State state() {
    return state;
  }

  /** Whether summaries are being sent. */
  public synchronized boolean hasConsent() {
    return state == State.ACTIVE;
  }

  /** Whether the person has yet to be asked (opt-in) or told (notice and opt-out). */
  public synchronized boolean needsConsent() {
    return unanswered();
  }

  /** {@code opt_in}, {@code notice_and_opt_out} or {@code off}: what the catalog's consent.yaml says. */
  public String consentMode() {
    Object mode = catalog.consent.get("mode");
    return mode == null ? "opt_in" : String.valueOf(mode);
  }

  /** The policy link, or null. */
  public String policyUrl() {
    Object url = catalog.consent.get("policy_url");
    return url == null ? null : String.valueOf(url);
  }

  /** The channel this build counts as, by its catalog name. */
  public synchronized String channel() {
    return catalog.channels.isEmpty() ? "" : catalog.channels.get(dims[0]);
  }

  /** Called whenever the state, the preview or what was sent changes: from any thread. */
  public void addListener(Runnable listener) {
    listeners.add(listener);
  }

  public void removeListener(Runnable listener) {
    listeners.remove(listener);
  }

  private int consentVersion() {
    return Json.asInt(catalog.consent.get("consent_version"), 1);
  }

  // ── Start ──────────────────────────────────────────────────────────────────

  /** Start the client: read what was kept, open a session, send what is waiting. */
  public void start() {
    synchronized (this) {
      if (started) return;
      started = true;
      dims = new int[] {
        Rules.channelOf(catalog.channels, Arrays.asList(host.channel.split("_", -1))::contains),
        host.platform,
        host.deviceClass(),
        Rules.languageOf(catalog.languages, host.locale),
      };
      if (host.killed || "off".equals(consentMode()) || key.isEmpty()) {
        setState(State.DISABLED);
        return;
      }
      try {
        store.open();
      } catch (Exception e) {
        // No storage, no client: nothing may be sent that could not be taken back.
        setState(State.DISABLED);
        return;
      }
      stored = store.loadState();
      spool = store.loadSpool();
      // A 410 silences the client until the platform's Retry-After, or the next build.
      if (build.equals(stored.get("kill_build")) && Json.asLong(stored.get("kill_until"), 0) > unix()) {
        setState(State.KILLED);
        return;
      }

      String answer = stored.get("consent") == null ? "" : String.valueOf(stored.get("consent"));
      // A new data category asks again (README §5.4): an answer to an older consent version is no answer.
      if (!answer.isEmpty() && Json.asInt(stored.get("consent_version"), 0) < consentVersion()) answer = "";
      if (answer.equals("no")) {
        setState(State.PAUSED);
        return;
      }

      core = new SignalCore(catalog, answer.equals("yes") ? Json.asMap(stored.get("ledger")) : null, clock);
      beginSession();
      if (answer.equals("yes") || "notice_and_opt_out".equals(consentMode())) {
        setState(State.ACTIVE);
        persist();
      } else {
        setState(State.AWAITING_CONSENT);
      }
    }
    changed();
    flush();
  }

  // ── The public API (README §7.4) ───────────────────────────────────────────

  /**
   * The person's answer. Yes sends this session when it ends and remembers
   * the install from here on; no discards the session, and what was waiting,
   * and is never asked again.
   */
  public void setConsent(boolean yes) {
    synchronized (this) {
      if (state == State.DISABLED || state == State.KILLED) return;
      if (yes) {
        if (state == State.ACTIVE) return;
        if (core == null) {
          core = new SignalCore(catalog, null, clock);
          beginSession();
        }
        stored.put("consent", "yes");
        stored.put("consent_version", (long) consentVersion());
        core.count(SignalCore.CoreMetric.CONSENT_EVENTS, 1, 0);
        setState(State.ACTIVE);
        persist();
      } else {
        // One click turns it off: what was gathered is dropped, what was waiting is dropped, and nothing more leaves.
        if (core != null) core.abandon();
        core = null;
        spool = new ArrayList<>();
        store.erase();
        stored = new LinkedHashMap<>();
        stored.put("consent", "no");
        stored.put("consent_version", (long) consentVersion());
        store.saveState(stored);
        setState(State.PAUSED);
      }
    }
    changed();
  }

  /** Under notice_and_opt_out: the notice was read and left on. */
  public void acknowledgeNotice() {
    synchronized (this) {
      if (state != State.ACTIVE || stored.get("consent") != null) return;
      stored.put("consent", "yes");
      stored.put("consent_version", (long) consentVersion());
      if (recording()) core.count(SignalCore.CoreMetric.CONSENT_EVENTS, 1, 0);
      persist();
    }
    changed();
  }

  /** Add one to a counter. */
  public void count(int metric) {
    count(metric, 1, -1);
  }

  /** Add one to a label of a labeled counter. */
  public void countLabel(int metric, int label) {
    count(metric, 1, label);
  }

  /** Add to a counter, or to one label of a labeled counter. */
  public synchronized void count(int metric, long amount, int label) {
    if (recording()) core.count(metric, amount, label);
  }

  /** Reach a milestone: {@code milestone(MS.FIRST_GOAL)}, as (metric &lt;&lt; 6) | bit. */
  public void milestone(int ms) {
    boolean atMenu;
    synchronized (this) {
      if (!recording()) return;
      int metric = ms >> 6;
      int bit = ms & 63;
      core.milestone(metric, bit);
      atMenu = metric == SignalCore.CoreMetric.CORE_MILESTONES && bit == SignalCore.CoreMilestone.MENU;
    }
    // The menu is the third flush trigger: if nothing was sent for ten minutes (README §7.1).
    if (atMenu) idleFlush();
  }

  /** Arrive at a node: a screen, a view. */
  public synchronized void node(int index) {
    if (!recording()) return;
    core.node(index);
    if (index >= 0 && index < catalog.nodes()) lastNode = index;
  }

  /** Put a time, in seconds, in a histogram. */
  public void time(int metric, double seconds) {
    time(metric, seconds, -1);
  }

  public synchronized void time(int metric, double seconds, int label) {
    if (recording()) core.observe(metric, seconds, label);
  }

  /** Put a quantity in a histogram. */
  public void observe(int metric, double value, int label) {
    time(metric, value, label);
  }

  /** Say what kind of session this was, where the app knows better. */
  public synchronized void outcome(int cls) {
    if (recording()) core.outcome(cls);
  }

  /** Set a sticky trait bit. Only on the install's first day. */
  public synchronized void setTrait(int bit) {
    if (recording()) core.setTrait(bit);
  }

  /** Note what the session is used with: one of {@link SignalCore.InputKind}. The first input is a milestone. */
  public synchronized void input(int kind) {
    if (!recording()) return;
    core.input(kind);
    if (!firstInput) {
      firstInput = true;
      core.milestone(SignalCore.CoreMetric.CORE_MILESTONES, SignalCore.CoreMilestone.FIRST_INPUT);
    }
  }

  /** The privacy panel was opened: the settings_opened step of the core ladder. */
  public synchronized void privacyOpened() {
    if (recording()) core.milestone(SignalCore.CoreMetric.CORE_MILESTONES, SignalCore.CoreMilestone.SETTINGS_OPENED);
  }

  /** Whole seconds into the open session, or 0. */
  public synchronized int elapsedSeconds() {
    return core == null ? 0 : core.elapsedSeconds();
  }

  /** What would be sent if the session ended now. Null when nothing would be. */
  public synchronized Preview previewNextPayload() {
    if (core == null || !core.isActive()) return null;
    SignalCore.Ended result = core.preview();
    return new Preview(Wire.toHex(result.bytes), result.bytes.length, decoded(result.summary));
  }

  /** The last few summaries sent, newest first, as hex. */
  public synchronized List<String> lastSent() {
    List<String> out = new ArrayList<>();
    List<Object> sent = Json.asList(stored.get("sent"));
    if (sent != null) for (Object hex : sent) out.add(String.valueOf(hex));
    return out;
  }

  /** "Reset local stats": the ledger, the spool and the pacing counters go; the person's answer stays. */
  public void resetLocal() {
    synchronized (this) {
      Object answer = stored.get("consent");
      long version = Json.asLong(stored.get("consent_version"), 0);
      spool = new ArrayList<>();
      store.erase();
      stored = new LinkedHashMap<>();
      if (answer != null) {
        stored.put("consent", answer);
        stored.put("consent_version", version);
      }
      if (core != null) {
        core = new SignalCore(catalog, null, clock);
        beginSession();
      }
      if (answer != null) persist();
    }
    changed();
  }

  /** Send what is waiting. Called for you when a session ends, at start, and at the menu. */
  public void flush() {
    byte[] bytes;
    synchronized (this) {
      if (state != State.ACTIVE || inFlight != null || spool.isEmpty()) return;
      if (unix() < Json.asLong(stored.get("next_try"), 0) || !budget()) return;
      bytes = spool.get(0);
      CompletableFuture<Void> sending = new CompletableFuture<>();
      inFlight = sending;
      transport.post(endpoint + "/v1/s", key, "application/octet-stream", bytes)
        .exceptionally(error -> new SignalTransport.Response(0, 0))
        .thenAccept(response -> {
          synchronized (this) {
            inFlight = null;
          }
          answered(response, bytes);
          sending.complete(null);
        });
    }
  }

  /**
   * End the session, send it, and wait for the answer for at most the bound:
   * {@code client.finish(SignalClient.QUIT_BOUND)} before the app quits.
   */
  public void finish(Duration bound) {
    shutdown();
    long deadline = System.nanoTime() + bound.toNanos();
    // One answer can start the next send: wait for the spool to drain, or the bound, whichever is first.
    while (true) {
      CompletableFuture<Void> waiting;
      synchronized (this) {
        waiting = inFlight;
      }
      if (waiting == null) return;
      long left = deadline - System.nanoTime();
      if (left <= 0) return;
      try {
        waiting.get(left, TimeUnit.NANOSECONDS);
      } catch (Exception e) {
        return;
      }
    }
  }

  /** End the session and start sending it, without waiting. */
  public void shutdown() {
    synchronized (this) {
      if (core == null || !core.isActive()) return;
      if (state != State.ACTIVE) {
        // Never answered: what was gathered in memory goes with the process, and nothing was ever written.
        core.abandon();
        return;
      }
      byte[] bytes = core.end(nonce()).bytes;
      spool.add(bytes);
      persist();
      // The session's end is a flush trigger of its own: a backoff from earlier does not hold it.
      stored.put("next_try", 0L);
    }
    flush();
  }

  /**
   * A line of the standard copy, in the person's language, with the app's
   * name in it: {@code copy("prompt_title")}.
   */
  public String copy(String key) {
    String language = host.locale.replace('_', '-').split("-", -1)[0];
    Map<String, String> words = catalog.copy.get(host.locale);
    if (words == null) words = catalog.copy.get(language);
    String found = words == null ? null : words.get(key);
    if (found == null && catalog.copy.get("en") != null) found = catalog.copy.get("en").get(key);
    if (found == null) found = key;
    return found.replace("{GAME}", catalog.name);
  }

  // ── Inside ─────────────────────────────────────────────────────────────────

  private void setState(State next) {
    if (state == next) return;
    state = next;
  }

  private void changed() {
    for (Runnable listener : listeners) {
      try {
        listener.run();
      } catch (RuntimeException e) {
        // A listener's fault is its own.
      }
    }
  }

  private boolean recording() {
    return (state == State.ACTIVE || state == State.AWAITING_CONSENT) && core != null && core.isActive();
  }

  private boolean unanswered() {
    return state == State.AWAITING_CONSENT || (state == State.ACTIVE && stored.get("consent") == null);
  }

  private long unix() {
    return wall.instant().getEpochSecond();
  }

  private int day() {
    LocalDate date = LocalDate.now(wall);
    return Rules.dayOf(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
  }

  private void beginSession() {
    LocalDate date = LocalDate.now(wall);
    core.begin(Rules.dayOf(date.getYear(), date.getMonthValue(), date.getDayOfMonth()), SignalCore.monthKey(date.getYear(), date.getMonthValue()), dims);
    firstInput = false;
    lastNode = -1;
  }

  private String nonce() {
    byte[] bytes = new byte[8];
    random.nextBytes(bytes);
    return Wire.toHex(bytes);
  }

  /** Write what the person agreed to have kept: the answer, the ledger, the pacing counters, the spool. */
  private void persist() {
    if (state != State.ACTIVE && state != State.KILLED) return;
    if (core != null) stored.put("ledger", core.ledger.toJson());
    store.saveState(stored);
    spool = store.saveSpool(spool);
  }

  /** At most eight requests a local day (README §7.1): past it, what is waiting waits for tomorrow. */
  private boolean budget() {
    int today = day();
    Map<String, Object> sent = Json.asMap(stored.get("requests"));
    long n = sent != null && Json.asLong(sent.get("day"), -1) == today ? Json.asLong(sent.get("n"), 0) : 0;
    if (n >= REQUESTS_PER_DAY) return false;
    Map<String, Object> next = new LinkedHashMap<>();
    next.put("day", (long) today);
    next.put("n", n + 1);
    stored.put("requests", next);
    return true;
  }

  private void answered(SignalTransport.Response response, byte[] bytes) {
    boolean again = false;
    synchronized (this) {
      long now = unix();
      int status = response.status();
      if (status == 204 || status == 400 || status == 413 || status == 422) {
        // Taken, or never going to be: either way it is not sent again.
        if (!spool.isEmpty() && Arrays.equals(spool.get(0), bytes)) spool.remove(0);
        backoffS = 0;
        stored.put("last_send", now);
        if (status == 204) {
          List<Object> sent = new ArrayList<>();
          sent.add(Wire.toHex(bytes));
          for (String hex : lastSent()) if (sent.size() < KEPT_SENT) sent.add(hex);
          stored.put("sent", sent);
        }
        persist();
        again = true;
      } else if (status == 410) {
        // Killed: say nothing more until the platform's Retry-After has passed, or a new build ships.
        spool = new ArrayList<>();
        stored.put("kill_until", now + Math.max(response.retryAfter(), 3600));
        stored.put("kill_build", build);
        if (core != null) core.abandon();
        setState(State.KILLED);
        store.saveState(stored);
        store.saveSpool(spool);
      } else {
        // No answer, a throttle or a fault: back off 5 s to 5 min with full jitter, or as long as the server says.
        backoffS = Math.min(BACKOFF_MAX_S, Math.max(backoffS * 2, BACKOFF_MIN_S));
        stored.put("next_try", now + Math.max(response.retryAfter(), random.nextInt(backoffS + 1)));
        persist();
      }
    }
    changed();
    if (again) flush();
  }

  /** At the menu, if nothing has been sent for ten minutes: the third and last flush trigger (README §7.1). */
  private void idleFlush() {
    boolean due;
    synchronized (this) {
      due = unix() - Json.asLong(stored.get("last_send"), 0) > IDLE_FLUSH_S;
    }
    if (due) flush();
  }

  /** A summary as rows a person can read: what the "What we send" preview shows beside the hex. */
  private List<String[]> decoded(Wire.Summary summary) {
    String[] platforms = {"unknown", "windows", "macos", "linux", "android", "ios", "web"};
    String[] devices = {"low", "mid", "high", "ultra"};
    List<String> flags = new ArrayList<>();
    int[][] flagNames = {{1}, {2}, {4}, {8}};
    String[] flagWords = {"first today", "first this week", "first this month", "new install"};
    for (int i = 0; i < flagNames.length; i++) if ((summary.flags & flagNames[i][0]) != 0) flags.add(flagWords[i]);
    List<String> traits = new ArrayList<>();
    for (int bit = 0; bit < catalog.traitNames.size(); bit++) {
      if ((summary.traits & (1 << bit)) != 0) traits.add(spaced(catalog.traitNames.get(bit)));
    }
    List<String[]> rows = new ArrayList<>();
    rows.add(new String[] {"flags", flags.isEmpty() ? "none" : String.join(", ", flags)});
    rows.add(new String[] {"day", summary.day + " days since 2026-01-01"});
    rows.add(new String[] {"install age", summary.age + " days"});
    rows.add(new String[] {"channel", summary.channel < catalog.channels.size() ? catalog.channels.get(summary.channel) : String.valueOf(summary.channel)});
    rows.add(new String[] {"platform", summary.platform < platforms.length ? platforms[summary.platform] : String.valueOf(summary.platform)});
    rows.add(new String[] {"device class", summary.device < devices.length ? devices[summary.device] : String.valueOf(summary.device)});
    rows.add(new String[] {"language", summary.lang < catalog.languages.size() ? catalog.languages.get(summary.lang) : String.valueOf(summary.lang)});
    rows.add(new String[] {"traits", traits.isEmpty() ? "none" : String.join(", ", traits)});
    for (Wire.Record record : summary.records) {
      SignalCatalog.Metric metric = catalog.metrics.get(record.id);
      String name = spaced(metric == null ? String.valueOf(record.id) : metric.name);
      List<String> labels = metric == null ? List.of() : metric.labelNames;
      long[][] entries = record.entries == null ? new long[0][] : record.entries;
      List<String> parts = new ArrayList<>();
      switch (record.type) {
        case SignalCatalog.Type.COUNTER -> parts.add(String.valueOf(record.value));
        case SignalCatalog.Type.MILESTONE -> {
          for (int bit = 0; bit < labels.size(); bit++) if ((record.value & (1L << bit)) != 0) parts.add(spaced(labels.get(bit)));
        }
        case SignalCatalog.Type.EXIT_NODE, SignalCatalog.Type.RETURN_NODE -> parts.add(nodeName((int) record.value));
        case SignalCatalog.Type.PATH -> {
          for (long[] entry : entries) parts.add(nodeName((int) entry[0]) + " → " + nodeName((int) entry[1]) + " ×" + entry[2]);
        }
        case SignalCatalog.Type.HISTOGRAM -> {
          for (long[] entry : entries) {
            parts.add(entry.length == 3
              ? labelName(labels, (int) entry[0]) + " bucket " + entry[1] + " ×" + entry[2]
              : "bucket " + entry[0] + " ×" + entry[1]);
          }
        }
        default -> {
          for (long[] entry : entries) parts.add(labelName(labels, (int) entry[0]) + " ×" + entry[1]);
        }
      }
      rows.add(new String[] {name, String.join(", ", parts)});
    }
    return rows;
  }

  private static String spaced(String name) {
    return name.replace('_', ' ');
  }

  private String nodeName(int index) {
    return index >= 0 && index < catalog.nodeNames.size() ? spaced(catalog.nodeNames.get(index)) : String.valueOf(index);
  }

  private static String labelName(List<String> labels, int index) {
    return index >= 0 && index < labels.size() ? spaced(labels.get(index)) : String.valueOf(index);
  }
}
