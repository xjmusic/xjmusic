// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.music;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 Chords have different Forms, such as Triad, Seventh, Extended, Added/Omitted, Specific or General.
 */
public interface ChordForms {

  // Regular expression to use mid-word, gluing together form expression parts
  String nExp = "[. ]*";

  // Regular expressions for different utilities
  String majorExp = "(M|maj|major)";
  String minorExp = "([^a-z]|^)(m|min|minor)";

  String flatExp = "(f|flat|b|♭)";
  String sharpExp = "(#|s|sharp)";
  String halfExp = "half";

  String addExp = "(add|\\+)";
  String omitExp = "(omit|\\-)";

  String dominantExp = "(^|dom|dominant)";
  String nonDominantExp = "(non|nondom|nondominant)";
  String diminishedExp = "(dim|dimin|diminished)";
  String augmentedExp = "(aug|augment|augmented)";
  String suspendedExp = "(sus|susp|suspend|suspended)";
  String harmonicExp = "(harm|harmonic)";
  /**
   all is an ordered set of rules to in, and corresponding chord intervals to setup.
   */
  List<ChordForm> all = Lists.newArrayList(

    // Root

    new ChordForm("Basic",
      null,
      ImmutableMap.of(
        Interval.I1, 0, // root
        Interval.I3, 4, // major 3rd
        Interval.I5, 7), // perfect 5th
      null),

    new ChordForm("NonDominant",
      Pattern.compile(nonDominantExp),
      null,
      ImmutableList.of(
        Interval.I1)), // no root


    // Triads

    new ChordForm("Major Triad",
      Pattern.compile("^" + majorExp + "([^a-z]|$|b)"),
      ImmutableMap.of(
        Interval.I3, 4, // major 3rd
        Interval.I5, 7), // perfect 5th
      null),

    new ChordForm("Minor Triad",
      Pattern.compile("^" + minorExp + "([^a-z]|$|b)"),
      ImmutableMap.of(
        Interval.I3, 3, // minor 3rd
        Interval.I5, 7), // perfect 5th
      null),

    new ChordForm("Augmented Triad",
      Pattern.compile("^" + augmentedExp),
      ImmutableMap.of(
        Interval.I3, 4, // major 3rd
        Interval.I5, 8), // augmented 5th
      null),

    new ChordForm("Diminished Triad",
      Pattern.compile("^" + diminishedExp),
      ImmutableMap.of(
        Interval.I3, 3, // diminished (minor) 3rd
        Interval.I5, 6), // diminished 5th
      null),

    new ChordForm("Suspended Triad",
      Pattern.compile("^" + suspendedExp),
      ImmutableMap.of(
        Interval.I4, 5, // 4th
        Interval.I5, 7), // perfect 5th
      ImmutableList.of(
        Interval.I3) // no 3rd
    ),

    // Fifth

    new ChordForm("Omit Fifth",
      Pattern.compile(omitExp + nExp + "5"),
      null,
      ImmutableList.of(
        Interval.I5) // no fifth
    ),

    new ChordForm("Flat Fifth",
      Pattern.compile(flatExp + nExp + "5"),
      ImmutableMap.of(
        Interval.I5, 6), // flat 5th
      null),

    // Sixth

    new ChordForm("Add Sixth",
      Pattern.compile(addExp + nExp + "6"),
      ImmutableMap.of(
        Interval.I6, 9), // 6th
      null),

    new ChordForm("Flat Sixth",
      Pattern.compile(flatExp + nExp + "6"),
      ImmutableMap.of(
        Interval.I6, 8), // flat 6th
      null),

    new ChordForm("Sharp Sixth",
      Pattern.compile(sharpExp + nExp + "6"),
      ImmutableMap.of(
        Interval.I6, 10), // sharp 6th
      null),

    new ChordForm("Augmented Sixth",
      Pattern.compile(augmentedExp + nExp + "6"),
      ImmutableMap.of(
        Interval.I6, 10), // augmented 6th
      null),

    new ChordForm("Omit Sixth",
      Pattern.compile(omitExp + nExp + "6"),
      null,
      ImmutableList.of(
        Interval.I6) // no 6th
    ),

    // Seventh

    new ChordForm("Add Seventh",
      Pattern.compile("7"),
      ImmutableMap.of(
        Interval.I7, 10), // dominant 7th
      null),

    new ChordForm("Dominant Seventh",
      Pattern.compile(dominantExp + nExp + "7"),
      ImmutableMap.of(
        Interval.I7, 10), // dominant 7th
      null),

    new ChordForm("Major Seventh",
      Pattern.compile(majorExp + nExp + "7"),
      ImmutableMap.of(
        Interval.I7, 11), // major 7th
      null),

    new ChordForm("Minor Seventh",
      Pattern.compile(minorExp + nExp + "7"),
      ImmutableMap.of(
        Interval.I7, 10), // minor 7th
      null),

    new ChordForm("Diminished Seventh",
      Pattern.compile(diminishedExp + nExp + "7"),
      ImmutableMap.of(
        Interval.I7, 9), // diminished 7th
      null),

    new ChordForm("Half Diminished Seventh",
      Pattern.compile(halfExp + nExp + diminishedExp + nExp + "7"),
      ImmutableMap.of(
        Interval.I3, 3,  // minor 3rd
        Interval.I5, 6,  // diminished 5th
        Interval.I7, 10), // minor 7th
      null),

    // Diminished Major Seventh Chord
    new ChordForm("Diminished Major Seventh",
      Pattern.compile(diminishedExp + nExp + majorExp + nExp + "7"),
      null,
      null),

    // Augmented Major Seventh Chord
    new ChordForm("Augmented Major Seventh",
      Pattern.compile(augmentedExp + nExp + majorExp + nExp + "7"),
      null,
      null),

    // Augmented Minor Seventh Chord
    new ChordForm("Augmented Minor Seventh",
      Pattern.compile(augmentedExp + nExp + minorExp + nExp + "7"),
      null,
      null),

    new ChordForm("Harmonic Seventh",
      Pattern.compile(harmonicExp + nExp + "7"),
      ImmutableMap.of(
        Interval.I3, 4, // major 3rd
        Interval.I5, 7), // perfect 5th
      null),

    new ChordForm("Omit Seventh",
      Pattern.compile(omitExp + nExp + "7"),
      null,
      ImmutableList.of(
        Interval.I7)), // no 7th

    // Ninth

    new ChordForm("Add Ninth",
      Pattern.compile(addExp + nExp + "9"),
      ImmutableMap.of(
        Interval.I9, 14), // 9th
      null),

    new ChordForm("Dominant Ninth",
      Pattern.compile(dominantExp + nExp + "9"),
      ImmutableMap.of(
        Interval.I7, 10, // minor 7th
        Interval.I9, 14), // dominant 9th
      null),

    new ChordForm("Major Ninth",
      Pattern.compile(majorExp + nExp + "9"),
      ImmutableMap.of(
        Interval.I7, 11, // major 7th
        Interval.I9, 14), // dominant 9th
      null),

    new ChordForm("Minor Ninth",
      Pattern.compile(minorExp + nExp + "9"),
      ImmutableMap.of(
        Interval.I7, 10, // minor 7th
        Interval.I9, 14), // dominant 9th
      null),

    new ChordForm("Flat Ninth",
      Pattern.compile(flatExp + nExp + "9"),
      ImmutableMap.of(
        Interval.I9, 13), // sharp 9th
      null),

    new ChordForm("Sharp Ninth",
      Pattern.compile(sharpExp + nExp + "9"),
      ImmutableMap.of(
        Interval.I9, 15), // sharp 9th
      null),

    new ChordForm("Omit Ninth",
      Pattern.compile(omitExp + nExp + "9"),
      null,
      ImmutableList.of(
        Interval.I9)), // no 9th

    // Eleventh

    new ChordForm("Add Eleventh",
      Pattern.compile("11"),
      ImmutableMap.of(
        Interval.I11, 17), // 11th
      null),

    new ChordForm("Dominant Eleventh",
      Pattern.compile(dominantExp + nExp + "11"),
      ImmutableMap.of(
        Interval.I7, 10, // minor 7th
        Interval.I9, 14, // dominant 9th
        Interval.I11, 17), // dominant 11th
      ImmutableList.of(
        Interval.I3)), // no 3rd

    new ChordForm("Major Eleventh",
      Pattern.compile(majorExp + nExp + "11"),
      ImmutableMap.of(
        Interval.I7, 11, // major 7th
        Interval.I9, 14, // dominant 9th
        Interval.I11, 17), // dominant 11th
      null),

    new ChordForm("Minor Eleventh",
      Pattern.compile(minorExp + nExp + "11"),
      ImmutableMap.of(
        Interval.I3, 3,  // minor 3rd
        Interval.I7, 10, // minor 7th
        Interval.I9, 14, // dominant 9th
        Interval.I11, 17), // dominant 11th
      null),

    new ChordForm("Flat Eleventh",
      Pattern.compile(flatExp + nExp + "11"),
      ImmutableMap.of(
        Interval.I11, 16), // sharp 9th
      null),

    new ChordForm("Sharp Eleventh",
      Pattern.compile(sharpExp + nExp + "11"),
      ImmutableMap.of(
        Interval.I11, 18), // sharp 9th
      null),

    new ChordForm("Omit Eleventh",
      Pattern.compile(omitExp + nExp + "11"),
      null,
      ImmutableList.of(
        Interval.I11)), // no 11th

    // Thirteenth

    new ChordForm("Add Thirteenth",
      Pattern.compile("13"),
      ImmutableMap.of(
        Interval.I13, 21), // dominant 13th
      null),

    new ChordForm("Dominant Thirteenth",
      Pattern.compile(dominantExp + nExp + "13"),
      ImmutableMap.of(
        Interval.I7, 10, // minor 7th
        Interval.I9, 14, // dominant 9th
        Interval.I11, 17, // dominant 11th
        Interval.I13, 21), // dominant 13th
      ImmutableList.of(
        Interval.I3)), // no 3rd

    new ChordForm("Major Thirteenth",
      Pattern.compile(majorExp + nExp + "13"),
      ImmutableMap.of(
        Interval.I3, 4,  // major 3rd
        Interval.I7, 11, // major 7th
        Interval.I9, 14, // dominant 9th
        Interval.I11, 17, // dominant 11th
        Interval.I13, 21), // dominant 13th
      null),

    new ChordForm("Minor Thirteenth",
      Pattern.compile(minorExp + nExp + "13"),
      ImmutableMap.of(
        Interval.I3, 3,  // minor 3rd
        Interval.I7, 10, // minor 7th
        Interval.I9, 14, // dominant 9th
        Interval.I11, 17, // dominant 11th
        Interval.I13, 21), // dominant 13th
      null)

    // Lydian


/*
    new Form(
			Name, "Lydian",
			Pattern.compile("lyd"),
			),

		new Form(
			Name, "Omit Lydian",
			Pattern.compile(omitExp+nExp+"lyd"),
			),
	*//*

	// Specific

	*/
/*	new Form(
      Name, "AlphaSpecific",
			Pattern.compile("alpha"),
			),

		new Form(
			Name, "BridgeSpecific",
			Pattern.compile("bridge"),
			),

		new Form(
			Name, "ComplexeSonoreSpecific",
			Pattern.compile("(complexe|sonore)"),
			),

		new Form(
			Name, "DreamSpecific",
			Pattern.compile("dream"),
			),

		new Form(
			Name, "ElektraSpecific",
			Pattern.compile("elektra"),
			),

		new Form(
			Name, "FarbenSpecific",
			Pattern.compile("farben"),
			),

		new Form(
			Name, "GrandmotherSpecific",
			Pattern.compile("grandmother"),
			),

		new Form(
			Name, "MagicSpecific",
			Pattern.compile("magic"),
			),

		new Form(
			Name, "MµSpecific",
			Pattern.compile("µ"),
			),

		new Form(
			Name, "MysticSpecific",
			Pattern.compile("mystic"),
			),

		new Form(
			Name, "NorthernLightsSpecific",
			Pattern.compile("northern" + nExp + "light"),
			),

		new Form(
			Name, "PetrushkaSpecific",
			Pattern.compile("petrush"),
			),

		new Form(
			Name, "PsalmsSpecific",
			Pattern.compile("psalm"),
			),

		new Form(
			Name, "SoWhatSpecific",
			Pattern.compile("so" + nExp + "what"),
			),

		new Form(
			Name, "TristanSpecific",
			Pattern.compile("tristan"),
			),

		new Form(
			Name, "VienneseTrichordSpecific",
			Pattern.compile("viennese" + nExp + "trichord"),
			),

		// General

		new Form(
			Name, "MixedIntervalGeneral",
			Pattern.compile("mixed" + nExp + "interval"),
			),

		new Form(
			Name, "SecundalGeneral",
			Pattern.compile("secundal"),
			),

		new Form(
			Name, "TertianGeneral",
			Pattern.compile("tertian"),
			),

		new Form(
			Name, "QuartalGeneral",
			Pattern.compile("quartal"),
			),

		new Form(
			Name, "SyntheticChordGeneral",
			Pattern.compile("synthetic"),
			),*/

  );

  Map<String, String> colloquialFormNames =
    ImmutableMap.<String, String>builder()
      .put("Basic", "Major")
      .put("Basic Add Seventh Dominant Seventh Add Ninth", "Major Seventh Add Ninth")
      .put("Basic Add Ninth", "Major Add Ninth")
      .put("Basic Add Ninth Dominant Ninth", "Major Ninth")
      .put("Basic Add Sixth", "Major Sixth")
      .put("Basic Add Seventh Dominant Seventh", "Major Seventh")
      .put("Basic Diminished Triad", "Diminished Major")
      .put("Basic Minor Triad", "Minor")
      .put("Basic Minor Triad Add Sixth", "Minor Sixth")
      .put("Basic Minor Triad Add Seventh Minor Seventh", "Minor Seventh")
      .put("Basic Minor Triad Diminished Triad", "Diminished Minor")
      .put("Basic Minor Triad Omit Fifth Add Seventh Minor Seventh", "Minor Seventh Omit Fifth")
      .build();

  /**
   Run an action on all known forms

   @param action to run on each form
   */
  static void forEach(Consumer<? super ChordForm> action) {
    all.forEach(action);
  }


}

