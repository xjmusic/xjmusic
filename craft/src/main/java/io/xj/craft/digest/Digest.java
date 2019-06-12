// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.math.StatsAccumulator;
import io.xj.core.model.entity.Resource;
import io.xj.core.util.TremendouslyRandom;
import io.xj.core.util.Value;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 [#154350346] Architect wants a universal Ingest Factory, to modularize graph mathematics used during craft to ingest any combination of Library, Sequence, and Instrument for any purpose.
 */
public interface Digest extends Resource {
  String KEY_CHORD_NAME = "chordName";
  String KEY_CHORD_POSITION = "chordPosition";
  String RESOURCE_TYPE = "digests";

  /**
   Get the most popular entry in a histogram

   @param histogram to get most popular entry of
   @param <N>       type of number
   @return most popular entry
   */
  static <N extends Number> N mostPopular(Multiset<N> histogram, N defaultIfZero) {
    N result = null;
    Integer popularity = null;
    for (Multiset.Entry<N> entry : histogram.entrySet()) {
      if (Objects.isNull(popularity) || entry.getCount() > popularity) {
        popularity = entry.getCount();
        result = entry.getElement();
      }
    }
    return Objects.nonNull(result) ? result : defaultIfZero;
  }

  /**
   Selects an integer by random from a histogram; each entry in the histogram is added to the lottery N times, where N is the number of occurrences of the entry in the histogram, such that an entry occurring more often in the histogram is more likely to be selected in the lottery.

   @param <N> type of number
   @return randomly selected integer
   */
  static <N extends Number> N lottery(Multiset<N> histogram, N defaultIfZero) {
    List<N> lottery = Lists.newArrayList();
    lottery.addAll(histogram);
    N winner = lottery.get(TremendouslyRandom.zeroToLimit(lottery.size()));
    return Objects.equals(0, winner) ? defaultIfZero : winner;
  }

  /**
   Selects the max value from a stats object

   @param stats         to select max from
   @param defaultIfZero if max is zero or exception is caught
   @return max value
   */
  static double max(StatsAccumulator stats, double defaultIfZero) {
    try {
      double result = stats.max();
      return Objects.equals(0.0, result) ? defaultIfZero : result;
    } catch (Exception ignored) {
      return defaultIfZero;
    }
  }

  /**
   Get histogram elements all divided by a common divisor, with a default value if none found

   @param histogram     to get elements of, divided
   @param divisor       to divide elements by
   @param defaultIfNone only member of set if none other are found.
   @return resulting elements divided, else set containing default divided by divisor
   */
  static Set<Integer> elementsDividedBy(Multiset<Integer> histogram, double divisor, int defaultIfNone) {
    Set<Integer> result = Value.dividedBy(divisor, histogram.elementSet());
    if (result.isEmpty()) return ImmutableSet.of((int) Math.floor(defaultIfNone / divisor));
    return result;
  }

}
