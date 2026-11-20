package io.outright.signal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * What an install remembers between sessions. Every field is an integer, or
 * a map of integers; a host persists it as JSON ({@link #toJson}), and only
 * once the person has said yes. The Dart package's {@code SignalLedger}.
 */
public final class SignalLedger {
  /** The first local day the app ran; -1 until it has. */
  public int installDay = -1;
  /** The latest local day seen: the day never goes back. */
  public int maxDay = -1;
  /** The day, week and month of the last summary produced: what decides the first_* flags. */
  public int lastDay = -1;
  public int lastWeek = -1;
  public int lastMonth = -1;
  /** 1 once any summary has been produced: the next is not a new install's. */
  public int sentAny = 0;
  /** Sticky milestone bits, by metric id. */
  public final Map<Integer, Long> ever = new TreeMap<>();
  /** Sticky first-day trait bits. */
  public int traits = 0;
  /** What the last clean session left, for the next to pair with: -1 when there is nothing. */
  public int prevExitNode = -1;
  public int prevExitDay = -1;
  /** 1 while a session is open: found still 1 at the next start, the last one did not end cleanly. */
  public int open = 0;
  /** Lifetime totals: what the pulse and the review request are eligible on. */
  public int sessions = 0;
  public long playtime = 0;
  public int days = 0;
  public int lastPlayedDay = -1;
  public long loopsStarted = 0;
  public long loopsCompleted = 0;

  public SignalLedger() {
  }

  /** A stored ledger, with anything missing filled in: an older build's still loads. Null is a new one. */
  public static SignalLedger fromJson(Map<String, Object> stored) {
    SignalLedger ledger = new SignalLedger();
    if (stored == null) return ledger;
    ledger.installDay = (int) read(stored, "install_day", -1);
    ledger.maxDay = (int) read(stored, "max_day", -1);
    ledger.lastDay = (int) read(stored, "last_day", -1);
    ledger.lastWeek = (int) read(stored, "last_week", -1);
    ledger.lastMonth = (int) read(stored, "last_month", -1);
    ledger.sentAny = (int) read(stored, "sent_any", 0);
    ledger.traits = (int) read(stored, "traits", 0);
    ledger.prevExitNode = (int) read(stored, "prev_exit_node", -1);
    ledger.prevExitDay = (int) read(stored, "prev_exit_day", -1);
    ledger.open = (int) read(stored, "open", 0);
    ledger.sessions = (int) read(stored, "sessions", 0);
    ledger.playtime = read(stored, "playtime", 0);
    ledger.days = (int) read(stored, "days", 0);
    ledger.lastPlayedDay = (int) read(stored, "last_played_day", -1);
    ledger.loopsStarted = read(stored, "loops_started", 0);
    ledger.loopsCompleted = read(stored, "loops_completed", 0);
    Object ever = stored.get("ever");
    if (ever instanceof Map<?, ?> map) {
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        try {
          int id = Integer.parseInt(String.valueOf(entry.getKey()));
          if (entry.getValue() instanceof Number bits) ledger.ever.put(id, bits.longValue());
        } catch (NumberFormatException ignored) {
          // Not a metric id: left out.
        }
      }
    }
    return ledger;
  }

  /** A whole number, as Dart's `value is num && value == value.truncate()` reads one. */
  private static long read(Map<String, Object> stored, String key, long fallback) {
    Object value = stored.get(key);
    if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
      return ((Number) value).longValue();
    }
    if (value instanceof Number n) {
      double d = n.doubleValue();
      return d == Math.floor(d) && !Double.isInfinite(d) ? (long) d : fallback;
    }
    return fallback;
  }

  public Map<String, Object> toJson() {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("v", 1L);
    out.put("install_day", (long) installDay);
    out.put("max_day", (long) maxDay);
    out.put("last_day", (long) lastDay);
    out.put("last_week", (long) lastWeek);
    out.put("last_month", (long) lastMonth);
    out.put("sent_any", (long) sentAny);
    Map<String, Object> bits = new LinkedHashMap<>();
    for (Map.Entry<Integer, Long> entry : ever.entrySet()) bits.put(String.valueOf(entry.getKey()), entry.getValue());
    out.put("ever", bits);
    out.put("traits", (long) traits);
    out.put("prev_exit_node", (long) prevExitNode);
    out.put("prev_exit_day", (long) prevExitDay);
    out.put("open", (long) open);
    out.put("sessions", (long) sessions);
    out.put("playtime", playtime);
    out.put("days", (long) days);
    out.put("last_played_day", (long) lastPlayedDay);
    out.put("loops_started", loopsStarted);
    out.put("loops_completed", loopsCompleted);
    return out;
  }

  public SignalLedger copy() {
    return fromJson(toJson());
  }
}
