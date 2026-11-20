package io.outright.signal;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * The decisions that are not arithmetic on a payload: what day it is, which
 * of the catalog's dimensions this build is, and when the pulse and the
 * store's review request may be shown (README §6.2). All of it pure, so all
 * of it tested without a device. The Dart package's {@code rules.dart}.
 */
public final class Rules {
  private Rules() {
  }

  private static final int DAY_ZERO_YEAR = 2026;

  /** The wire's platform numbers. */
  public static final class Platform {
    private Platform() {
    }

    public static final int UNKNOWN = 0;
    public static final int WINDOWS = 1;
    public static final int MACOS = 2;
    public static final int LINUX = 3;
    public static final int ANDROID = 4;
    public static final int IOS = 5;
    public static final int WEB = 6;
  }

  /** Whole days from 2026-01-01 to a civil date: the wire's day number. */
  public static int dayOf(int year, int month, int day) {
    return daysFromCivil(year, month, day) - daysFromCivil(DAY_ZERO_YEAR, 1, 1);
  }

  /** Days since 1970-01-01 of a civil date (Howard Hinnant's algorithm): integers only. */
  private static int daysFromCivil(int year, int month, int day) {
    int y = year - (month <= 2 ? 1 : 0);
    int era = Math.floorDiv(y, 400);
    int yoe = y - era * 400;
    int doy = (153 * (month + (month > 2 ? -3 : 9)) + 2) / 5 + day - 1;
    int doe = yoe * 365 + yoe / 4 - yoe / 100 + doy;
    return era * 146097 + doe - 719468;
  }

  /** The device class: four tiers from cores and memory, never a model or a GPU string. */
  public static int deviceClass(int cores, long memoryBytes) {
    double gb = memoryBytes / 1073741824.0;
    if (cores >= 12 && gb >= 24) return 3;
    if (cores >= 6 && gb >= 12) return 2;
    if (cores >= 4 && gb >= 6) return 1;
    return 0;
  }

  /**
   * The catalog's channel for this build: the declared channel with the most
   * words, every one of which is a tag of the build, so {@code steam_demo}
   * beats {@code steam}. None matching is the first channel.
   */
  public static int channelOf(List<String> channels, Predicate<String> hasTag) {
    int best = 0;
    int bestWords = 0;
    for (int index = 0; index < channels.size(); index++) {
      String[] words = channels.get(index).split("_", -1);
      boolean all = true;
      for (String word : words) all &= hasTag.test(word);
      if (all && words.length > bestWords) {
        best = index;
        bestWords = words.length;
      }
    }
    return best;
  }

  /** The catalog's language index for a locale: its own code, else its language, else {@code other} (0). */
  public static int languageOf(List<String> languages, String locale) {
    String normal = locale.replace('_', '-');
    for (String candidate : new String[] {normal, normal.split("-", -1)[0]}) {
      int index = languages.indexOf(candidate);
      if (index > 0) return index;
    }
    return 0;
  }

  private static int num(Object value, int fallback) {
    return Json.asInt(value, fallback);
  }

  /** What the pulse and the review request are decided on, at a win moment. */
  public static final class WinMoment {
    public final int day;
    /** Sessions so far, this one included. */
    public final int sessions;
    /** Distinct days played, today included. */
    public final int days;
    public final long playtimeS;
    public final long loopsCompleted;
    public final boolean firstSession;
    public final boolean afterUncleanExit;
    public final boolean kiosk;
    /** Whether this build's channel is a store with a review API. */
    public final boolean store;
    public final boolean pulseThisSession;

    public WinMoment(int day, int sessions, int days, long playtimeS, long loopsCompleted, boolean firstSession,
                     boolean afterUncleanExit, boolean kiosk, boolean store, boolean pulseThisSession) {
      this.day = day;
      this.sessions = sessions;
      this.days = days;
      this.playtimeS = playtimeS;
      this.loopsCompleted = loopsCompleted;
      this.firstSession = firstSession;
      this.afterUncleanExit = afterUncleanExit;
      this.kiosk = kiosk;
      this.store = store;
      this.pulseThisSession = pulseThisSession;
    }
  }

  /**
   * Whether the pulse may be shown at this win moment. {@code seen} is what
   * the SDK remembers of pulses past: {@code last_day}, {@code year_start},
   * {@code in_year}, {@code skips}.
   */
  public static boolean pulseDue(Map<String, Object> config, WinMoment now, Map<String, Object> seen) {
    if (!Boolean.TRUE.equals(config.get("enabled")) || now.kiosk) return false;
    // Never in the first session, and never after a session that did not end cleanly.
    if (now.firstSession || now.afterUncleanExit) return false;
    if (now.sessions < num(config.get("min_sessions"), 3)) return false;
    if (now.playtimeS < num(config.get("min_minutes"), 20) * 60L) return false;
    if (now.loopsCompleted < 1) return false;
    // Retired after two skips: "not now" twice is an answer.
    if (num(seen.get("skips"), 0) >= num(config.get("retire_after_skips"), 2)) return false;
    int last = num(seen.get("last_day"), -1);
    if (last >= 0 && now.day - last < num(config.get("cooldown_days"), 30)) return false;
    int inYear = now.day - num(seen.get("year_start"), -100000) < 365 ? num(seen.get("in_year"), 0) : 0;
    return inYear < num(config.get("max_per_year"), 3);
  }

  /**
   * Whether the store's review request may be made at this win moment: never
   * on a channel with no store API, never beside a pulse, whatever the pulse said.
   */
  public static boolean reviewDue(Map<String, Object> config, WinMoment now, Map<String, Object> seen) {
    if (!Boolean.TRUE.equals(config.get("enabled")) || now.kiosk || !now.store) return false;
    if (now.sessions < num(config.get("min_sessions"), 5) || now.days < num(config.get("min_days"), 3)) return false;
    // No pulse in this session or the last one.
    if (now.pulseThisSession || num(seen.get("pulse_session"), -10) >= now.sessions - 1) return false;
    int last = num(seen.get("last_day"), -1);
    return last < 0 || now.day - last >= num(config.get("cooldown_days"), 120);
  }

  private static final Set<String> STORES_WITH_REVIEW = Set.of("appstore", "macappstore", "playstore", "msstore");

  /** The channels a store's own review API exists on (README §6.3): not Steam, not Snap, not the web. */
  public static boolean hasStoreReview(String channel) {
    return STORES_WITH_REVIEW.contains(channel);
  }
}
