package io.outright.signal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A stream's catalog, as `signal gen --java` writes it: the digest every
 * payload carries, and each metric as counts. The Dart package's
 * {@code SignalCatalog}, field for field.
 */
public final class SignalCatalog {
  /** Metric types, as generated catalogs carry them. */
  public static final class Type {
    private Type() {
    }

    public static final int COUNTER = 0;
    public static final int LABELED_COUNTER = 1;
    public static final int HISTOGRAM = 2;
    public static final int MILESTONE = 3;
    public static final int PATH = 4;
    public static final int EXIT_NODE = 5;
    public static final int RETURN_NODE = 6;
    public static final int OUTCOME = 7;
  }

  /**
   * One metric, as an SDK needs it at run time: counts, and the names the
   * "What we send" preview says them by.
   */
  public static final class Metric {
    public final int id;
    public final int type;
    /** How many labels it has (an outcome has five), or 0. */
    public final int labels;
    /** How many milestones it has, or 0. */
    public final int bits;
    /** A histogram's bucket scale. */
    public final int scale;
    /** The most one payload may carry in one of its cells. */
    public final long max;
    public final String name;
    public final List<String> labelNames;

    public Metric(int id, int type, int labels, int bits, int scale, long max, String name, List<String> labelNames) {
      this.id = id;
      this.type = type;
      this.labels = labels;
      this.bits = bits;
      this.scale = scale;
      this.max = max;
      this.name = name;
      this.labelNames = List.copyOf(labelNames);
    }
  }

  /** The 8-byte catalog digest, as 16 hex digits. */
  public final String digest;
  /** The stream the catalog is for: its id on the platform. */
  public final String stream;
  public final String name;
  public final List<String> channels;
  /** The declared UI languages; index 0 is always {@code other}. */
  public final List<String> languages;
  public final List<String> nodeNames;
  public final List<String> traitNames;
  /** The id of the stream's path metric, or 0 when it has none. */
  public final int pathId;
  /** By id, in ascending order. */
  public final Map<Integer, Metric> metrics;
  public final Map<String, Object> consent;
  public final Map<String, Object> pulse;
  public final Map<String, Object> review;
  public final Map<String, Object> feedback;
  /** The standard screens' words, by language and key. */
  public final Map<String, Map<String, String>> copy;

  public SignalCatalog(
    String digest,
    String stream,
    String name,
    List<String> channels,
    List<String> languages,
    List<String> nodeNames,
    List<String> traitNames,
    int pathId,
    List<Metric> metrics,
    Map<String, Object> consent,
    Map<String, Object> pulse,
    Map<String, Object> review,
    Map<String, Object> feedback,
    Map<String, Map<String, String>> copy
  ) {
    this.digest = digest;
    this.stream = stream;
    this.name = name;
    this.channels = List.copyOf(channels);
    this.languages = List.copyOf(languages);
    this.nodeNames = List.copyOf(nodeNames);
    this.traitNames = List.copyOf(traitNames);
    this.pathId = pathId;
    TreeMap<Integer, Metric> byId = new TreeMap<>();
    for (Metric metric : metrics) byId.put(metric.id, metric);
    this.metrics = Collections.unmodifiableMap(byId);
    this.consent = consent;
    this.pulse = pulse;
    this.review = review;
    this.feedback = feedback;
    this.copy = copy;
  }

  public int nodes() {
    return nodeNames.size();
  }

  public int traits() {
    return traitNames.size();
  }

  /** A compiled catalog (the JSON `signal compile` prints, parsed by {@link Json}) as an SDK holds one. */
  public static SignalCatalog fromCompiled(Map<String, Object> compiled) {
    Map<String, Integer> types = Map.of(
      "counter", Type.COUNTER,
      "labeled_counter", Type.LABELED_COUNTER,
      "histogram", Type.HISTOGRAM,
      "milestone", Type.MILESTONE,
      "path", Type.PATH,
      "exit_node", Type.EXIT_NODE,
      "return_node", Type.RETURN_NODE,
      "outcome", Type.OUTCOME
    );
    Map<String, Object> wire = Json.asMap(compiled.get("wire"));
    Map<String, Object> meta = orEmpty(Json.asMap(compiled.get("meta")));
    Map<String, Object> sentiment = orEmpty(Json.asMap(meta.get("sentiment")));
    List<Metric> metrics = new ArrayList<>();
    int pathId = 0;
    for (Object entry : Json.asList(wire.get("metrics"))) {
      Map<String, Object> metric = Json.asMap(entry);
      int id = Json.asInt(metric.get("id"), 0);
      int type = types.get((String) metric.get("type"));
      List<String> labels = names(metric.get("labels"));
      List<String> bits = names(metric.get("bits"));
      metrics.add(new Metric(
        id,
        type,
        type == Type.OUTCOME ? 5 : labels.size(),
        bits.size(),
        Json.asInt(metric.get("scale"), 1),
        Json.asLong(metric.get("max"), 100000),
        (String) metric.get("name"),
        !labels.isEmpty() ? labels : bits
      ));
      if (type == Type.PATH) pathId = id;
    }
    Map<String, Map<String, String>> copy = new LinkedHashMap<>();
    Map<String, Object> languages = orEmpty(Json.asMap(meta.get("copy")));
    for (Map.Entry<String, Object> language : languages.entrySet()) {
      Map<String, String> words = new LinkedHashMap<>();
      for (Map.Entry<String, Object> word : orEmpty(Json.asMap(language.getValue())).entrySet()) {
        words.put(word.getKey(), String.valueOf(word.getValue()));
      }
      copy.put(language.getKey(), words);
    }
    return new SignalCatalog(
      (String) compiled.get("digest"),
      (String) wire.get("stream"),
      meta.get("name") instanceof String s ? s : "",
      names(wire.get("channels")),
      names(wire.get("languages")),
      names(wire.get("nodes")),
      names(wire.get("traits")),
      pathId,
      metrics,
      orEmpty(Json.asMap(meta.get("consent"))),
      orEmpty(Json.asMap(sentiment.get("pulse"))),
      orEmpty(Json.asMap(sentiment.get("review"))),
      orEmpty(Json.asMap(sentiment.get("feedback"))),
      copy
    );
  }

  private static Map<String, Object> orEmpty(Map<String, Object> map) {
    return map == null ? new LinkedHashMap<>() : map;
  }

  private static List<String> names(Object list) {
    List<String> out = new ArrayList<>();
    List<Object> entries = Json.asList(list);
    if (entries != null) for (Object entry : entries) out.add((String) entry);
    return out;
  }

  // ── What generated catalogs are written with ───────────────────────────────

  /** A map in the order given, which, unlike {@code Map.of}, may hold a null: `policy_url: null`. */
  public static Map<String, Object> map(Object... keysAndValues) {
    Map<String, Object> out = new LinkedHashMap<>();
    for (int i = 0; i + 1 < keysAndValues.length; i += 2) out.put((String) keysAndValues[i], keysAndValues[i + 1]);
    return Collections.unmodifiableMap(out);
  }

  /** Words by key, from {@code key, words, key, words, …}. */
  public static Map<String, String> words(String... keysAndWords) {
    Map<String, String> out = new LinkedHashMap<>();
    for (int i = 0; i + 1 < keysAndWords.length; i += 2) out.put(keysAndWords[i], keysAndWords[i + 1]);
    return Collections.unmodifiableMap(out);
  }

  /** A list that, unlike {@code List.of}, may hold a null. */
  public static List<Object> list(Object... values) {
    List<Object> out = new ArrayList<>();
    Collections.addAll(out, values);
    return Collections.unmodifiableList(out);
  }
}
