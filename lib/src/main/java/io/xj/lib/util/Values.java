// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.util;

import com.google.api.client.util.Lists;
import com.google.common.base.Strings;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Values {
  double entityPositionDecimalPlaces = 2.0;
  double roundPositionMultiplier = StrictMath.pow(10.0, entityPositionDecimalPlaces);
  String K = "k";
  String EMPTY = "";
  double MICROS_PER_SECOND = 1000000.0F;
  double NANOS_PER_SECOND = 1000.0F * MICROS_PER_SECOND;

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
    return Text.isInteger.matcher(raw).matches();
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
   Require a non-zero value, or else throw an exception with the specified name

   @param value value
   @param name  to describe in exception
   @throws ValueException if null
   */
  static <V> void requireNonZero(V value, String name) throws ValueException {
    if (Values.isUnsetOrZero(value))
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
   [#154976066] Architect wants to limit the floating point precision of chord and event position, in order to limit obsession over the position of things.

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
    return Strings.isNullOrEmpty(String.valueOf(value));
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
   String representation of value, or pass through null as empty string

   @param value to parse
   @return string or empty
   */
  static String stringOrEmpty(@Nullable Object value) {
    return stringOrDefault(value, EMPTY);
  }

  /**
   String representation of value, or pass through a default value

   @param value        to parse
   @param defaultValue to return if value is null
   @return string or default value
   */
  static String stringOrDefault(@Nullable Object value, String defaultValue) {
    return Objects.nonNull(value) ? String.valueOf(value) : defaultValue;
  }

  /**
   Get a UUID from the input string, or return null if the input is null or invalid

   @param id from which to compute uuid
   @return uuid or null
   */
  static @Nullable
  UUID uuidOrNull(@Nullable String id) {
    if (Strings.isNullOrEmpty(id)) return null;
    try {
      return UUID.fromString(id);
    } catch (Exception ignored) {
      return null;
    }
  }

  /**
   Get the epoch micros of a given instant

   @param of instant
   @return epoch micros
   */
  static long toEpochMicros(Instant of) {
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
    var working = Lists.newArrayList(from);
    while (num < working.size())
      working.remove((int) TremendouslyRandom.zeroToLimit(working.size()));
    return working;
  }
}
