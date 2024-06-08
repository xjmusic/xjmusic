// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.model.util;

import jakarta.annotation.Nullable;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface ValueUtils {
  double entityPositionDecimalPlaces = 2.0;
  double roundPositionMultiplier = StrictMath.pow(10.0, entityPositionDecimalPlaces);
  String K = "k";
  long MILLIS_PER_SECOND = 1000;
  long MICROS_PER_MILLI = 1000;
  long NANOS_PER_MICRO = 1000;
  long MICROS_PER_SECOND = MICROS_PER_MILLI * MILLIS_PER_SECOND;
  long NANOS_PER_SECOND = NANOS_PER_MICRO * MICROS_PER_SECOND;
  long SECONDS_PER_MINUTE = 60;
  long MICROS_PER_MINUTE = SECONDS_PER_MINUTE * MICROS_PER_SECOND;
  long MINUTES_PER_HOUR = 60;
  long HOURS_PER_DAY = 24;
  long SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
  long SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;
  String[] VERSION_ZERO = {"0", "0", "0"};

  /**
   Return the first value if it's non-null, else the second

   @param d1 to check if non-null and return
   @param d2 to default to, if s1 is null
   @return s1 if non-null, else s2
   */
  static Double eitherOr(Double d1, Double d2) {
    if (Objects.nonNull(d1) && !d1.isNaN() && !Objects.equals(d1, 0.0d))
      return d1;
    else
      return d2;
  }

  /**
   Return the first value if it's non-null, else the second

   @param s1 to check if non-null and return
   @param s2 to default to, if s1 is null
   @return s1 if non-null, else s2
   */
  static String eitherOr(String s1, String s2) {
    if (Objects.nonNull(s1) && !s1.isEmpty())
      return s1;
    else
      return s2;
  }

  /**
   Divide a set of integers by a double and return the divided set

   @param divisor   to divide by
   @param originals to divide
   @return divided originals
   */
  static Set<Integer> dividedBy(Double divisor, Set<Integer> originals) {
    return originals.stream().map(original -> (int) Math.floor(original / divisor)).collect(Collectors.toSet());
  }

  /**
   Calculate ratio (of 0 to 1) within a zero-to-N limit

   @param value to calculate radio of
   @param limit N where ratio will be calculated based on zero-to-N
   @return ratio between 0 and 1
   */
  static double ratio(double value, double limit) {
    return Math.max(Math.min(1, value / limit), 0);
  }

  /**
   True if input string is an integer

   @param raw text to check if it's an integer
   @return true if it's an integer
   */
  static Boolean isInteger(String raw) {
    return StringUtils.isInteger.matcher(raw).matches();
  }

  /**
   Add an ID if not already added to list

   @param ids   list to which addition will be assured
   @param addId to ensure in list
   */
  static <N> void put(Collection<N> ids, N addId) {
    if (!ids.contains(addId)) ids.add(addId);
  }

  /**
   Add an ID if not already added to list

   @param ids    list to which addition will be assured
   @param addIds to ensure in list
   */
  static <N> void put(Collection<N> ids, Collection<N> addIds) {
    addIds.forEach(addId -> put(ids, addId));
  }

  /**
   Require a non-null value, or else throw an exception with the specified name

   @param notNull value
   @param name    to describe in exception
   @throws ValueException if null
   */
  static <V> void require(V notNull, String name) throws ValueException {
    if (Objects.isNull(notNull) || String.valueOf(notNull).isEmpty())
      throw new ValueException(String.format("%s is required.", name));
  }

  /**
   Require a minimum value, or else throw an exception with the specified name

   @param minimum threshold minimum
   @param value   value
   @param name    to describe in exception
   @throws ValueException if null
   */
  static void requireMinimum(Double minimum, Double value, String name) throws ValueException {
    if (value < minimum)
      throw new ValueException(String.format("%s must be at least %f", name, minimum));
  }

  /**
   Require a minimum value, or else throw an exception with the specified name

   @param minimum threshold minimum
   @param value   value
   @param name    to describe in exception
   @throws ValueException if null
   */
  static void requireMinimum(Long minimum, Long value, String name) throws ValueException {
    if (value < minimum)
      throw new ValueException(String.format("%s must be at least %d", name, minimum));
  }

  /**
   Require a non-zero value, or else throw an exception with the specified name

   @param value value
   @param name  to describe in exception
   @throws ValueException if null
   */
  static <V> void requireNonZero(V value, String name) throws ValueException {
    if (ValueUtils.isUnsetOrZero(value))
      throw new ValueException(String.format("Non-zero %s is required.", name));
  }

  /**
   allow only the specified values, or else throw an exception with the specified name

   @param value value
   @param name  to describe in exception
   @throws ValueException if null
   */
  static <V> void require(V value, String name, Collection<V> allowed) throws ValueException {
    require(value, name);
    if (!allowed.contains(value))
      throw new ValueException(String.format("%s '%s' is invalid.", name, value));
  }

  /**
   Round a value to N decimal places.
   Limit the floating point precision of chord and event position, in order to limit obsession over the position of things. https://github.com/xjmusic/workstation/issues/223

   @param value to round
   @return rounded position
   */
  static Double limitDecimalPrecision(Double value) {
    return Math.floor(value * roundPositionMultiplier) / roundPositionMultiplier;
  }

  /**
   Definitely not null, or string "null"

   @param obj to ingest for non-nullness
   @return true if non-null
   */
  static boolean isNonNull(Object obj) {
    return Objects.nonNull(obj) &&
        !Objects.equals("null", String.valueOf(obj));
  }

  /**
   Is a value not present?

   @param value to test
   @return true if null or empty
   */
  static boolean isEmpty(Object value) {
    if (Objects.isNull(value)) return true;
    return String.valueOf(value).isBlank();
  }

  /**
   Is a value not present, empty, or equal to zero?

   @param value to test
   @return true if unset, empty, or equals zero
   */
  static <V> boolean isUnsetOrZero(V value) {
    return Objects.isNull(value) || String.valueOf(value).isEmpty() || Double.valueOf(String.valueOf(value)).equals(0.0);
  }

  /**
   Format an Instant as ISO-8601 UTC

   @param instant to format
   @return formatted ISO-8601 UTC from instant
   */
  static String formatIso8601UTC(Instant instant) {
    return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)
        .format(DateTimeFormatter.ISO_DATE_TIME);
  }

  /**
   Format an Instant as ISO-8601 UTC

   @param instant to format
   @return formatted ISO-8601 UTC from instant
   */
  static String formatRfc1123UTC(Instant instant) {
    return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)
        .format(DateTimeFormatter.RFC_1123_DATE_TIME);
  }

  /**
   Whether a value is non-null and non-empty

   @param value to test
   @return true if non-null and non-empty
   */
  static boolean isSet(Object value) {
    if (Objects.isNull(value)) return false;
    return !String.valueOf(value).isBlank();
  }

  /**
   Whether a value is null or empty

   @param value to test
   @return true if non-null and non-empty
   */
  static boolean isUnset(Object value) {
    if (Objects.isNull(value)) return true;
    return StringUtils.isNullOrEmpty(String.valueOf(value));
  }

  /**
   Filter a value out of an array of values

   @param removed value to remove
   @param values  to source and filter
   @param <O>     type
   @return values without removed value
   */
  static <O> O[] without(O removed, O[] values) {
    //noinspection unchecked
    return (O[]) Stream.of(values)
        .filter(v -> !Objects.equals(v, removed))
        .toArray();
  }

  /**
   Get a UUID from the input string, or return null if the input is null or invalid

   @param id from which to compute uuid
   @return uuid or null
   */
  static @Nullable
  UUID uuidOrNull(@Nullable String id) {
    if (StringUtils.isNullOrEmpty(id)) return null;
    try {
      return UUID.fromString(Objects.requireNonNull(id));
    } catch (Exception ignored) {
      return null;
    }
  }

  /**
   Get the epoch micros of a given instant

   @param of instant
   @return epoch micros
   */
  static Long toEpochMicros(Instant of) {
    return of.toEpochMilli() * 1000 + of.getNano() / 1000;
  }

  /**
   Get the "kilos" representation of an integer, as in 128k for 128000

   @param value for which to get kilos
   @return kilos representation
   */
  static String k(int value) {
    return String.format("%d%s", (int) Math.floor((double) value / 1000), K);
  }

  /**
   Get a random string from the collection

   @param from which to get random string
   @return random string from collection
   */
  static String randomFrom(Collection<String> from) {
    return (String) from.toArray()[TremendouslyRandom.zeroToLimit(from.size())];
  }

  /**
   Get N number of the member strings from the collection.
   Don't repeat a choice.

   @param from which to get random strings
   @param num  number of strings to get
   @return random strings from collection
   */
  static List<String> randomFrom(Collection<String> from, int num) {
    if (0 == num || from.isEmpty()) return List.of();
    var working = new ArrayList<>(from);
    while (num < working.size() && !working.isEmpty())
      working.remove((int) TremendouslyRandom.zeroToLimit(working.size()));
    return working;
  }

  /**
   Greatest common denominator of two numbers

   @param a from which to compute
   @param b from which to compute
   @return greatest common denominator
   */
  static long gcd(long a, long b) {
    while (b > 0) {
      long temp = b;
      b = a % b; // % is remainder
      a = temp;
    }
    return a;
  }

  /**
   Get the values (filtered from the given set of test factors) which are factors of the target value

   @param target      which we'll test values against
   @param testFactors values to test
   @return values that are indeed a factor of the target value
   */
  static int[] factors(long target, int[] testFactors) {
    return Arrays.stream(testFactors)
        .filter(tf -> target % tf == 0)
        .toArray();
  }

  /**
   Get the smallest subdivision within the total, e.g.
   --- [12,3] = 4
   --- [12,4] = 3
   --- [16,4] = 4
   --- [24,3] = 4
   --- [24,4] = 3
   --- [48,3] = 4
   --- [48,4] = 3
   --- [64,4] = 4

   @param numerator   from which to compute
   @param denominator from which to compute
   */
  static int subDiv(int numerator, int denominator) {
    if (numerator % denominator != 0 || numerator <= denominator) return numerator;
    var result = numerator;
    while (result % denominator == 0 && result > denominator)
      result = result / denominator;
    while (result % 2 == 0 && result > denominator * 1.38)
      result = result / 2;
    return result;
  }

  /**
   Round down to a multiple of the given factor

   @param factor of which to get a multiple
   @param value  source
   @return floor of value
   */
  static int multipleFloor(int factor, double value) {
    return (int) (Math.floor(value / factor) * factor);
  }

  /**
   Interpolate a value between the floor and ceiling

   @param floor      bottom value
   @param ceiling    top value
   @param position   between 0 and 1 of value to interpolate between the floor and ceiling
   @param multiplier of value (above ceiling)
   @return interpolated value
   */
  static Double interpolate(double floor, double ceiling, double position, double multiplier) {
    return floor + (ceiling - floor) * position * multiplier;
  }

  /**
   Enforce a maximum

   @param value actual
   @throws ValueException if value greater than allowable
   */
  static void enforceMaxStereo(int value) throws ValueException {
    if (value > 2)
      throw new ValueException("more than 2 input audio channels not allowed");
  }

  /**
   Get the key from a map, based on the highest value stored

   @param map of key-value pairs
   @return key of the highest value
   */
  static Optional<UUID> getKeyOfHighestValue(Map<UUID, Integer> map) {
    var max = map.entrySet().stream().max(Map.Entry.comparingByValue());
    return max.map(Map.Entry::getKey);
  }

  /**
   Round value to the nearest positive multiple of N
   */
  static int roundToNearest(int N, int value) {
    return Math.max(0, Math.round((float) value / N)) * N;
  }

  /**
   Remove some number of ids from the list

   @param fromIds to begin with
   @param count   number of ids to add
   @return list including added ids
   */
  static Collection<UUID> withIdsRemoved(Collection<UUID> fromIds, int count) {
    var ids = new ArrayList<>(fromIds);
    for (int i = 0; i < count; i++)
      ids.remove((int) TremendouslyRandom.zeroToLimit(ids.size()));
    return ids;
  }

  /**
   Get string value of int, or empty if zero

   @param value to translate
   @return non-zero value, or empty
   */
  static String emptyZero(int value) {
    return 0 != value ? String.valueOf(value) : "";
  }

  /**
   Get the last N values from a list

   @param num  of entries
   @param list of all entries
   @return last N entries from the list
   */
  static List<String> last(int num, List<String> list) {
    return list.subList(Math.min(list.size(), Math.max(0, list.size() - num)), list.size());
  }

  /**
   Compare two monotonic version strings

   @param a to compare
   @param b to compare
   @return comparison result
   */
  static int compareMonotonicVersion(String a, String b) {
    var aParts = StringUtils.isNullOrEmpty(a) ? VERSION_ZERO : a.split("\\.");
    var bParts = StringUtils.isNullOrEmpty(b) ? VERSION_ZERO : b.split("\\.");
    for (int i = 0; i < Math.min(aParts.length, bParts.length); i++) {
      var aPart = Integer.parseInt(aParts[i]);
      var bPart = Integer.parseInt(bParts[i]);
      if (aPart != bPart) return aPart - bPart;
    }
    return aParts.length - bParts.length;
  }
}
