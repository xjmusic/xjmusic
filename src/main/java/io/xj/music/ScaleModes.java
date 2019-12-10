// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.music;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 Scales have different Modes, such as Locrian, Ionian, Natural, Melodic Minor or Augmented Phrygian.
 */
public interface ScaleModes {

  String nExp = "[. ]*";

  String majorExp = "(M|maj|major)";
  String minorStrictExp = "([^a-z ]|^)(m|min|minor)";
  String minorExp = "(m|min|minor)";

  // String flatExp  = "(f|flat|b|♭)";
  // String sharpExp = "(#|s|sharp)";
  // String halfExp  = "half";

  // String omitExp = "(omit|\\-)";

  String naturalExp = "(nat|natural)";
  String melodicExp = "(mel|melodic)";
  String ascendExp = "(asc|ascend)";
  String descendExp = "(desc|descend)";
  String diminishedExp = "(dim|dimin|diminished)";
  String augmentedExp = "(aug|augment|augmented)";
  String harmonicExp = "(harm|harmonic)";
  // String dominantExp    = "(^|dom|dominant)";
  // String nonDominantExp = "(non|nondom|nondominant)";
  // String suspendedExp   = "(sus|susp|suspend|suspended)";

  String locrianExp = "(loc|locrian)";
  String ionianExp = "(ion|ionian)";
  String dorianExp = "(dor|dorian)";
  String phrygianExp = "(phr|phrygian)";
  String lydianExp = "(lyd|lydian)";
  String mixolydianExp = "(mix|mixolydian)";
  String aeolianExp = "(aeo|aeolian)";

  List<Integer> ionianIntervals = ImmutableList.of(2, 2, 1, 2, 2, 2);
  List<Integer> dorianIntervals = ImmutableList.of(2, 1, 2, 2, 2, 1);
  List<Integer> phrygianIntervals = ImmutableList.of(1, 2, 2, 2, 1, 2);
  List<Integer> lydianIntervals = ImmutableList.of(2, 2, 2, 1, 2, 2);
  List<Integer> mixolydianIntervals = ImmutableList.of(2, 2, 1, 2, 2, 1);
  List<Integer> aeolianIntervals = ImmutableList.of(2, 1, 2, 2, 1, 2);
  List<Integer> locrianIntervals = ImmutableList.of(1, 2, 2, 1, 2, 2);
  /**
   all is an ordered set of rules to in, and corresponding chord intervals to setup.
   */
  List<ScaleMode> all = Lists.newArrayList(

    // Root

    // Basic

    new ScaleMode(
      "Default (Major)",
      null,
      ionianIntervals,
      null),

    new ScaleMode(
      "Minor",
      Pattern.compile(minorStrictExp),
      aeolianIntervals,
      null),

    new ScaleMode(
      "Major",
      Pattern.compile(majorExp),
      ionianIntervals,
      null),

    new ScaleMode(
      "Natural Minor",
      Pattern.compile(naturalExp + nExp + minorExp),
      aeolianIntervals,
      null),

    new ScaleMode(
      "Diminished",
      Pattern.compile(diminishedExp),
      ImmutableList.of(2, 1, 2, 1, 2, 1, 2),
      null
    ),

    new ScaleMode(
      "Augmented",
      Pattern.compile(augmentedExp),
      ImmutableList.of(3, 1, 3, 1, 3),
      ImmutableList.of(Interval.I7)
    ),

    new ScaleMode(
      "Melodic Minor Ascend",
      Pattern.compile(melodicExp + nExp + minorExp + nExp + ascendExp),
      ImmutableList.of(2, 1, 2, 2, 2, 2),
      null),

    new ScaleMode(
      "Melodic Minor Descend",
      Pattern.compile(melodicExp + nExp + minorExp + nExp + descendExp),
      ImmutableList.of(2, 1, 2, 2, 1, 2),
      null),

    new ScaleMode(
      "Harmonic Minor",
      Pattern.compile(harmonicExp + nExp + minorExp),
      ImmutableList.of(2, 1, 2, 2, 1, 3),
      null),

    new ScaleMode(
      "Ionian",
      Pattern.compile(ionianExp),
      ionianIntervals,
      null),

    new ScaleMode(
      "Dorian",
      Pattern.compile(dorianExp),
      dorianIntervals,
      null),

    new ScaleMode(
      "Phrygian",
      Pattern.compile(phrygianExp),
      phrygianIntervals,
      null),

    new ScaleMode(
      "Lydian",
      Pattern.compile(lydianExp),
      lydianIntervals,
      null),

    new ScaleMode(
      "Mixolydian",
      Pattern.compile(mixolydianExp),
      mixolydianIntervals,
      null),

    new ScaleMode(
      "Aeolian",
      Pattern.compile(aeolianExp),
      aeolianIntervals,
      null),

    new ScaleMode(
      "Locrian",
      Pattern.compile(locrianExp),
      locrianIntervals,
      null)

  );

  /**
   Run an action on all known modes

   @param action to run on each mode
   */
  static void forEach(Consumer<? super ScaleMode> action) {
    all.forEach(action);
  }


}

