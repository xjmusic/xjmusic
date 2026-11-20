package io.outright.signal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.DoubleSupplier;

/**
 * The SDK pattern's core (README §7.1), in Java: the part of an SDK with no
 * app in it.
 *
 * This is the Dart package's {@code SignalCore} (and {@code src/sdk/core.mjs}),
 * name for name. Two things, both plain data: the {@link SignalLedger}, which
 * is what an install remembers between sessions, and the session, an
 * accumulator of integers where recording is an add into a cell. And one
 * operation that matters: {@link #end}, which folds the session into a
 * summary, the summary into bytes, and the session into the ledger.
 *
 * The host gives it the clock, the local day and month, and the dimensions,
 * so the same calls give the same bytes as the reference implementation. The
 * golden test holds it to that with {@code fixtures/golden.json}.
 *
 * Not thread-safe: a host records from one thread, or holds a lock.
 */
public final class SignalCore {
  /** The universal core's metric ids (README §2.3). */
  public static final class CoreMetric {
    private CoreMetric() {
    }

    public static final int SESSIONS = 1;
    public static final int PLAYTIME_S = 2;
    public static final int SESSION_LENGTH = 3;
    public static final int CORE_MILESTONES = 4;
    public static final int TIME_TO_CORE_LOOP = 5;
    public static final int EXIT_NODE = 6;
    public static final int RETURN_FROM_NODE = 7;
    public static final int RETURN_GAP = 8;
    public static final int UNCLEAN_PREV_EXIT = 9;
    public static final int FPS_BUCKET = 10;
    public static final int INPUT_DEVICE = 11;
    public static final int SENTIMENT = 12;
    public static final int REVIEW_FLOW = 13;
    public static final int FEEDBACK_SENT = 14;
    public static final int CONSENT_EVENTS = 15;
    public static final int SESSION_OUTCOME = 16;
  }

  /** The bits of the core ladder (metric 4). */
  public static final class CoreMilestone {
    private CoreMilestone() {
    }

    public static final int BOOT = 0;
    public static final int FIRST_INPUT = 1;
    public static final int MENU = 2;
    public static final int CORE_LOOP_START = 3;
    public static final int CORE_LOOP_COMPLETE = 4;
    public static final int SECOND_CORE_LOOP = 5;
    public static final int SETTINGS_OPENED = 6;
    public static final int QUIT_CLEAN = 7;
  }

  /** Session classes (metric 16). */
  public static final class Outcome {
    private Outcome() {
    }

    public static final int BOUNCED = 0;
    public static final int EXPLORED = 1;
    public static final int PLAYED = 2;
    public static final int DEEP = 3;
    public static final int STUCK = 4;
  }

  /** What a session was used with (metric 11's labels; mixed is derived). */
  public static final class InputKind {
    private InputKind() {
    }

    public static final int KBM = 0;
    public static final int GAMEPAD = 1;
    public static final int TOUCH = 2;
    public static final int MIXED = 3;
  }

  public static final int BOUNCE_SECONDS = 60;
  public static final int DEEP_LOOPS = 3;
  public static final int FPS_SAMPLES = 64;
  public static final int MAX_SESSION_SECONDS = 86400;
  public static final int MAX_INSTALL_AGE = 400;
  public static final int MAX_PAYLOAD_BYTES = 1024;
  public static final int HISTOGRAM_BUCKETS = 16;
  private static final int PATH_STRIDE = 32;
  private static final double BUCKET_VALUE_CAP = 1000000;

  /**
   * Sixteen half-octave buckets from {@code scale}, found by comparing
   * squares: no logarithm, no square root, the same boundary in every
   * language. Values are capped at a million first.
   */
  public static int bucketOf(double value, int scale) {
    double capped = value > BUCKET_VALUE_CAP ? BUCKET_VALUE_CAP : value;
    long v = (long) Math.floor(capped);
    if (v < 0 || Double.isNaN(capped)) v = 0;
    long squared = v * v;
    long edge = (long) scale * scale * 2;
    int bucket = 0;
    while (bucket < HISTOGRAM_BUCKETS - 1 && squared >= edge) {
      bucket += 1;
      edge *= 2;
    }
    return bucket;
  }

  public static int bucketOf(double value) {
    return bucketOf(value, 1);
  }

  /** The week a day is in, as a number that changes every Monday. Day 0 (2026-01-01) is a Thursday. */
  public static int weekOf(int day) {
    return Math.floorDiv(day + 3, 7);
  }

  /** The month key a host passes: {@code year * 12 + (month - 1)}. */
  public static int monthKey(int year, int month) {
    return year * 12 + (month - 1);
  }

  /** A session's end: the bytes to send, and the summary they are of. */
  public static final class Ended {
    public final byte[] bytes;
    public final Wire.Summary summary;

    Ended(byte[] bytes, Wire.Summary summary) {
      this.bytes = bytes;
      this.summary = summary;
    }
  }

  /** A copy of the core's state: {@link #restore} puts the core back as it was. */
  public static final class Snapshot {
    private final SignalLedger ledger;
    private final Session session;

    private Snapshot(SignalLedger ledger, Session session) {
      this.ledger = ledger;
      this.session = session;
    }
  }

  private static final class Session {
    final int day;
    final int month;
    final int[] dims;
    final double started;
    /** Whether this is the install's first session ever: only then is the time to the first core loop recorded. */
    final boolean first;
    /** Seconds spent paused, taken out of the time used. */
    double paused = 0;
    double pausedAt = -1;
    /** {@code cells[id]} is a metric's sparse cells: index → count. */
    Map<Integer, TreeMap<Integer, Long>> cells = new HashMap<>();
    /** Milestone bits reached this session, by metric id. */
    Map<Integer, Long> reached = new HashMap<>();
    int lastNode = -1;
    int returnNode = -1;
    int outcome = -1;
    int inputs = 0;
    List<Integer> fps = new ArrayList<>();
    int fpsSeen = 0;
    long loopsStarted = 0;
    long loopsCompleted = 0;
    boolean timedFirstLoop = false;

    Session(int day, int month, int[] dims, double started, boolean first) {
      this.day = day;
      this.month = month;
      this.dims = dims;
      this.started = started;
      this.first = first;
    }

    Session copy() {
      Session s = new Session(day, month, dims.clone(), started, first);
      s.paused = paused;
      s.pausedAt = pausedAt;
      s.cells = new HashMap<>();
      for (Map.Entry<Integer, TreeMap<Integer, Long>> entry : cells.entrySet()) s.cells.put(entry.getKey(), new TreeMap<>(entry.getValue()));
      s.reached = new HashMap<>(reached);
      s.lastNode = lastNode;
      s.returnNode = returnNode;
      s.outcome = outcome;
      s.inputs = inputs;
      s.fps = new ArrayList<>(fps);
      s.fpsSeen = fpsSeen;
      s.loopsStarted = loopsStarted;
      s.loopsCompleted = loopsCompleted;
      s.timedFirstLoop = timedFirstLoop;
      return s;
    }
  }

  public final SignalCatalog catalog;
  public SignalLedger ledger;
  /** Seconds, monotonic. */
  private final DoubleSupplier clock;
  private final int[] ids;
  private Session session;

  /**
   * @param catalog the stream's catalog
   * @param ledger  what was kept (null for a new install)
   * @param clock   seconds, monotonic
   */
  public SignalCore(SignalCatalog catalog, Map<String, Object> ledger, DoubleSupplier clock) {
    this.catalog = catalog;
    this.ledger = SignalLedger.fromJson(ledger);
    this.clock = clock;
    this.ids = catalog.metrics.keySet().stream().mapToInt(Integer::intValue).sorted().toArray();
  }

  /** Whether a session is open. */
  public boolean isActive() {
    return session != null;
  }

  /** The open session's local day, or null. */
  public Integer sessionDay() {
    return session == null ? null : session.day;
  }

  // ── A session's start ──────────────────────────────────────────────────────

  /**
   * Open a session on a local {@code day} (whole days since 2026-01-01) and
   * {@code month} (see {@link #monthKey}), with the dimensions
   * {@code [channel, platform, device, lang]}.
   */
  public void begin(int day, int month, int[] dims) {
    // The local day never decreases: a clock set back is the day it was.
    int today = day;
    if (today < ledger.maxDay) today = ledger.maxDay;
    ledger.maxDay = today;
    if (ledger.installDay < 0) ledger.installDay = today;

    Session s = new Session(today, month, new int[] {dims[0], dims[1], dims[2], dims[3]}, clock.getAsDouble(), ledger.sessions == 0);
    session = s;

    // What the last session left behind.
    if (ledger.open == 1) add(s.cells, CoreMetric.UNCLEAN_PREV_EXIT, 0, 1);
    if (ledger.prevExitDay >= 0) {
      add(s.cells, CoreMetric.RETURN_GAP, bucketOf(today - ledger.prevExitDay), 1);
      if (ledger.prevExitNode >= 0 && ledger.prevExitNode < catalog.nodes()) s.returnNode = ledger.prevExitNode;
    }
    // Paired once: a session that does not end cleanly leaves nothing to pair with.
    ledger.prevExitDay = -1;
    ledger.prevExitNode = -1;
    ledger.open = 1;

    milestone(CoreMetric.CORE_MILESTONES, CoreMilestone.BOOT);
  }

  // ── Recording ──────────────────────────────────────────────────────────────

  /** Add to a counter. */
  public void count(int id, long amount) {
    count(id, amount, -1);
  }

  /** Add to a counter, or to one label of a labeled counter. */
  public void count(int id, long amount, int label) {
    Session s = session;
    SignalCatalog.Metric metric = catalog.metrics.get(id);
    if (s == null || metric == null || amount <= 0) return;
    if (metric.type == SignalCatalog.Type.COUNTER) {
      add(s.cells, id, 0, amount);
    } else if (metric.type == SignalCatalog.Type.LABELED_COUNTER && label >= 0 && label < metric.labels) {
      add(s.cells, id, label, amount);
    }
  }

  /** Put a value in a histogram: a time in seconds, or a quantity. */
  public void observe(int id, double value) {
    observe(id, value, -1);
  }

  public void observe(int id, double value, int label) {
    Session s = session;
    SignalCatalog.Metric metric = catalog.metrics.get(id);
    if (s == null || metric == null || metric.type != SignalCatalog.Type.HISTOGRAM) return;
    int bucket = bucketOf(value, metric.scale);
    if (metric.labels == 0) {
      add(s.cells, id, bucket, 1);
    } else if (label >= 0 && label < metric.labels) {
      add(s.cells, id, label * HISTOGRAM_BUCKETS + bucket, 1);
    }
  }

  /** Reach a milestone. Reaching it again says nothing new. */
  public void milestone(int id, int bit) {
    Session s = session;
    SignalCatalog.Metric metric = catalog.metrics.get(id);
    if (s == null || metric == null || metric.type != SignalCatalog.Type.MILESTONE || bit < 0 || bit >= metric.bits) return;
    s.reached.put(id, setBit(s.reached.getOrDefault(id, 0L), bit));
    if (id != CoreMetric.CORE_MILESTONES) return;
    if (bit == CoreMilestone.CORE_LOOP_START) {
      s.loopsStarted += 1;
      ledger.loopsStarted += 1;
      if (s.first && !s.timedFirstLoop) {
        s.timedFirstLoop = true;
        observe(CoreMetric.TIME_TO_CORE_LOOP, elapsed(s));
      }
      if (ledger.loopsStarted >= 2) s.reached.put(id, setBit(s.reached.get(id), CoreMilestone.SECOND_CORE_LOOP));
    } else if (bit == CoreMilestone.CORE_LOOP_COMPLETE) {
      s.loopsCompleted += 1;
      ledger.loopsCompleted += 1;
    }
  }

  /** Arrive at a node: a transition from the last one, and the exit node so far. */
  public void node(int index) {
    Session s = session;
    if (s == null || index < 0 || index >= catalog.nodes()) return;
    int last = s.lastNode;
    if (last >= 0 && last != index && catalog.pathId != 0) {
      add(s.cells, catalog.pathId, last * PATH_STRIDE + index, 1);
    }
    s.lastNode = index;
  }

  /** Set a sticky trait bit. Only on the install's first day: after it, the traits are what they are. */
  public void setTrait(int bit) {
    Session s = session;
    if (s == null || bit < 0 || bit >= catalog.traits()) return;
    if (s.day != ledger.installDay) return;
    ledger.traits = (int) setBit(ledger.traits, bit);
  }

  /** Say what kind of session this was, in place of what the core would work out. */
  public void outcome(int cls) {
    Session s = session;
    if (s == null || cls < 0 || cls > Outcome.STUCK) return;
    s.outcome = cls;
  }

  /** Note what the session is being used with. */
  public void input(int kind) {
    Session s = session;
    if (s == null || kind < InputKind.KBM || kind > InputKind.TOUCH) return;
    s.inputs = (int) setBit(s.inputs, kind);
  }

  /** Take a frame-rate sample. The session's median of the most recent ones is what is sent. */
  public void fps(double sample) {
    Session s = session;
    if (s == null || !(sample > 0)) return;
    long floor = (long) Math.floor(sample);
    int value = floor > 1000 ? 1000 : (int) floor;
    if (s.fps.size() < FPS_SAMPLES) {
      s.fps.add(value);
    } else {
      s.fps.set(s.fpsSeen % FPS_SAMPLES, value);
    }
    s.fpsSeen += 1;
  }

  /** The app went to the background: the time until {@link #resume} is not time used. */
  public void pause() {
    Session s = session;
    if (s != null && s.pausedAt < 0) s.pausedAt = clock.getAsDouble();
  }

  public void resume() {
    Session s = session;
    if (s == null || s.pausedAt < 0) return;
    double away = clock.getAsDouble() - s.pausedAt;
    s.paused += away > 0 ? away : 0;
    s.pausedAt = -1;
  }

  // ── A session's end ────────────────────────────────────────────────────────

  /** What {@link #end} would send if the session ended now, without ending it: the "What we send" preview. */
  public Ended preview() {
    return preview("0000000000000000");
  }

  public Ended preview(String nonce) {
    Session s = session;
    return s == null ? null : summary(s, nonce, false);
  }

  /**
   * Close the session: its bytes, its summary, and the ledger brought up to
   * date. Null when no session is open.
   *
   * A session that does not end cleanly never gets here: nothing is sent for
   * it, and the next session's start counts it (unclean_prev_exit).
   */
  public Ended end(String nonce) {
    Session s = session;
    if (s == null) return null;
    Ended result = summary(s, nonce, true);
    session = null;
    return result;
  }

  /** The ledger and the open session, copied. */
  public Snapshot snapshot() {
    return new Snapshot(ledger.copy(), session == null ? null : session.copy());
  }

  public void restore(Snapshot snapshot) {
    ledger = snapshot.ledger.copy();
    session = snapshot.session == null ? null : snapshot.session.copy();
  }

  /** Abandon the open session without sending anything: what a crash does. */
  public void abandon() {
    session = null;
  }

  /** Whole seconds used so far in the open session, less the time paused. */
  public int elapsedSeconds() {
    return session == null ? 0 : elapsed(session);
  }

  // ── Inside ─────────────────────────────────────────────────────────────────

  private static void add(Map<Integer, TreeMap<Integer, Long>> cells, int id, int cell, long amount) {
    cells.computeIfAbsent(id, k -> new TreeMap<>()).merge(cell, amount, Long::sum);
  }

  private int elapsed(Session s) {
    double until = s.pausedAt >= 0 ? s.pausedAt : clock.getAsDouble();
    long seconds = (long) Math.floor(until - s.started - s.paused);
    if (seconds < 0) return 0;
    return seconds > MAX_SESSION_SECONDS ? MAX_SESSION_SECONDS : (int) seconds;
  }

  private Ended summary(Session source, String nonce, boolean commit) {
    // The core's own cells, worked out at the end. A preview works on a copy.
    Session s = commit ? source : source.copy();
    int duration = elapsed(source);
    Map<Integer, TreeMap<Integer, Long>> cells = s.cells;
    Map<Integer, Long> reached = s.reached;

    add(cells, CoreMetric.SESSIONS, 0, 1);
    if (duration > 0) add(cells, CoreMetric.PLAYTIME_S, 0, duration);
    add(cells, CoreMetric.SESSION_LENGTH, bucketOf(duration, catalog.metrics.get(CoreMetric.SESSION_LENGTH).scale), 1);
    reached.put(CoreMetric.CORE_MILESTONES, setBit(reached.getOrDefault(CoreMetric.CORE_MILESTONES, 0L), CoreMilestone.QUIT_CLEAN));
    if (!s.fps.isEmpty()) {
      int[] sorted = s.fps.stream().mapToInt(Integer::intValue).toArray();
      Arrays.sort(sorted);
      add(cells, CoreMetric.FPS_BUCKET, bucketOf(sorted[(sorted.length - 1) >> 1]), 1);
    }
    if (s.inputs != 0) {
      int device = InputKind.MIXED;
      if (s.inputs == 1) device = InputKind.KBM;
      else if (s.inputs == 2) device = InputKind.GAMEPAD;
      else if (s.inputs == 4) device = InputKind.TOUCH;
      add(cells, CoreMetric.INPUT_DEVICE, device, 1);
    }
    int cls = s.outcome;
    if (cls < 0) {
      if (duration < BOUNCE_SECONDS && s.loopsStarted == 0) cls = Outcome.BOUNCED;
      else if (s.loopsCompleted >= DEEP_LOOPS) cls = Outcome.DEEP;
      else if (s.loopsCompleted >= 1) cls = Outcome.PLAYED;
      else cls = Outcome.EXPLORED;
    }
    add(cells, CoreMetric.SESSION_OUTCOME, cls, 1);

    int flags = 0;
    if (ledger.lastDay != s.day) flags |= Wire.Flag.FIRST_TODAY;
    if (ledger.lastWeek != weekOf(s.day)) flags |= Wire.Flag.FIRST_WEEK;
    if (ledger.lastMonth != s.month) flags |= Wire.Flag.FIRST_MONTH;
    if (ledger.sentAny == 0) flags |= Wire.Flag.NEW_INSTALL;

    List<Wire.Record> records = new ArrayList<>();
    for (int id : ids) {
      Wire.Record record = record(id, catalog.metrics.get(id), cells.get(id), reached.getOrDefault(id, 0L), s);
      if (record != null) records.add(record);
    }

    int age = s.day - ledger.installDay;
    if (age < 0) age = 0;
    if (age > MAX_INSTALL_AGE) age = MAX_INSTALL_AGE;
    Wire.Summary summary = new Wire.Summary(flags, catalog.digest, nonce, s.day, age, s.dims[0], s.dims[1], s.dims[2], s.dims[3], ledger.traits, records);

    // A kilobyte is the hard cap: over it, the highest metrics are left out until it fits.
    byte[] bytes = Wire.encode(summary);
    while (bytes.length > MAX_PAYLOAD_BYTES && !records.isEmpty()) {
      records.remove(records.size() - 1);
      bytes = Wire.encode(summary);
    }

    if (commit) {
      for (Map.Entry<Integer, Long> entry : reached.entrySet()) {
        ledger.ever.put(entry.getKey(), ledger.ever.getOrDefault(entry.getKey(), 0L) | entry.getValue());
      }
      ledger.lastDay = s.day;
      ledger.lastWeek = weekOf(s.day);
      ledger.lastMonth = s.month;
      ledger.sentAny = 1;
      ledger.sessions += 1;
      ledger.playtime += duration;
      if (ledger.lastPlayedDay != s.day) {
        ledger.days += 1;
        ledger.lastPlayedDay = s.day;
      }
      ledger.prevExitDay = s.day;
      ledger.prevExitNode = s.lastNode;
      ledger.open = 0;
    }
    return new Ended(bytes, summary);
  }

  /** One metric's record, or null when it has nothing to say. */
  private Wire.Record record(int id, SignalCatalog.Metric metric, TreeMap<Integer, Long> cells, long reached, Session s) {
    switch (metric.type) {
      case SignalCatalog.Type.COUNTER: {
        long raw = cells == null ? 0 : cells.getOrDefault(0, 0L);
        long value = Math.min(raw, metric.max);
        return value > 0 ? Wire.Record.value(id, metric.type, value) : null;
      }
      case SignalCatalog.Type.MILESTONE: {
        // Newly reached: the bits this session reached that the install never had.
        long bits = reached & ~ledger.ever.getOrDefault(id, 0L) & 0xFFFFFFFFL;
        return bits > 0 ? Wire.Record.value(id, metric.type, bits) : null;
      }
      case SignalCatalog.Type.EXIT_NODE:
        return s.lastNode >= 0 ? Wire.Record.value(id, metric.type, s.lastNode) : null;
      case SignalCatalog.Type.RETURN_NODE:
        return s.returnNode >= 0 ? Wire.Record.value(id, metric.type, s.returnNode) : null;
      case SignalCatalog.Type.LABELED_COUNTER:
      case SignalCatalog.Type.OUTCOME:
        return entries(id, metric, cells, 0);
      case SignalCatalog.Type.HISTOGRAM:
        return entries(id, metric, cells, metric.labels > 0 ? HISTOGRAM_BUCKETS : 0);
      case SignalCatalog.Type.PATH:
        return entries(id, metric, cells, PATH_STRIDE);
      default:
        return null;
    }
  }

  /**
   * A metric's cells as sorted entries: the keys a cell index stands for,
   * then its count. {@code stride} splits an index in two (label and bucket,
   * or from and to).
   */
  private static Wire.Record entries(int id, SignalCatalog.Metric metric, TreeMap<Integer, Long> cells, int stride) {
    if (cells == null) return null;
    List<long[]> entries = new ArrayList<>();
    for (Map.Entry<Integer, Long> cell : cells.entrySet()) {
      long value = Math.min(cell.getValue(), metric.max);
      if (value <= 0) continue;
      int index = cell.getKey();
      entries.add(stride > 0 ? new long[] {index / stride, index % stride, value} : new long[] {index, value});
    }
    return entries.isEmpty() ? null : Wire.Record.entries(id, metric.type, entries.toArray(new long[0][]));
  }

  /** Bit sets up to 32 bits, as non-negative longs: bit 31 is a milestone like any other. */
  private static long setBit(long bits, int bit) {
    return bits | (1L << bit);
  }
}
