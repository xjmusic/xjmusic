// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.model.music;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Expression of the "accidental notes" as either Sharps or Flats
 <p>
 Note has an adjustment symbol (Sharp or Flat) to render the "accidental notes for a given name (e.g. of a chord, scale or key)
 */
public enum Accidental {
  None,
  Sharp,
  Flat;

  static final Pattern rgxSharpIn = Pattern.compile("[♯#]|major");
  static final Pattern rgxFlatIn = Pattern.compile("[♭b]");
  static final Pattern rgxSharpBegin = Pattern.compile("^[♯#]");
  static final Pattern rgxFlatBegin = Pattern.compile("^[♭b]");
  static final Pattern rgxSharpishIn = Pattern.compile("(M|maj|Major|major|aug)");
  static final Pattern rgxFlattishIn = Pattern.compile("([^a-z]|^)(m|min|Minor|minor|dim)");
  static final Pattern rgxSharp = Pattern.compile("[♯#]");
  static final Pattern rgxFlat = Pattern.compile("[♭b]");
  static final String EXPLICIT_SHARP = "s";
  static final String EXPLICIT_FLAT = "b";

  /**
   AdjSymbol the adjustment symbol (Sharp or Flat) for a given name (e.g. of a chord, scale or key)

   @param name to get adjustment symbol of
   @return adjustment symbol
   */
  public static Accidental of(CharSequence name) {
    int numSharps = countMatches(rgxSharpIn.matcher(name));
    int numFlats = countMatches(rgxFlatIn.matcher(name));
    int numSharpish = countMatches(rgxSharpishIn.matcher(name));
    int numFlattish = countMatches(rgxFlattishIn.matcher(name));

    // sharp/flat has precedent over sharpish/flattish; overall default is sharp
    if (0 < numSharps && numSharps > numFlats)
      return Sharp;
    else if (0 < numFlats)
      return Flat;
    else if (0 < numSharpish && numSharpish > numFlattish)
      return Sharp;
    else if (0 < numFlattish)
      return Flat;
    else
      return Sharp;
  }

  /**
   Count number of matches of a regex pattern matcher

   @param matcher to count
   @return number of matches
   */
  static int countMatches(Matcher matcher) {
    int count = 0;
    while (matcher.find())
      count++;
    return count;
  }

  /**
   the adjustment symbol (Sharp or Flat) that begins a given name (e.g. the Root of a chord, scale or key)

   @param name to get adjustment symbol from the beginning of
   @return adjustment symbol
   */
  public static Accidental firstOf(CharSequence name) {
    if (rgxSharpBegin.matcher(name).find())
      return Sharp;
    else if (rgxFlatBegin.matcher(name).find())
      return Flat;
    else
      return None;
  }

  /**
   Replace any accidentals with the explicit text "sharp" or "flat"

   @param name within which to replace text
   */
  public static String replaceWithExplicit(String name) {
    return rgxSharp.matcher(rgxFlat.matcher(name).replaceAll(EXPLICIT_FLAT)).replaceAll(EXPLICIT_SHARP);
  }

}
