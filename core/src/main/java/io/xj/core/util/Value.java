// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.util;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public interface Value {
  String CHORD_SEPARATOR_DESCRIPTOR = ":";
  String CHORD_SEPARATOR_DESCRIPTOR_UNIT = "|";
  String CHORD_MARKER_NON_CHORD = "---";
  double entityPositionDecimalPlaces = 2.0;

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
  static <N extends Object> void put(Collection<N> ids, N addId) {
    if (!ids.contains(addId)) ids.add(addId);
  }

  /**
   Add an ID if not already added to list

   @param ids    list to which addition will be assured
   @param addIds to ensure in list
   */
  static <N extends Object> void put(Collection<N> ids, Collection<N> addIds) {
    addIds.forEach(addId -> put(ids, addId));
  }

}
