// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.music;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Expression of the "accidental notes" as either Sharps or Flats
 <p>
 Note has an adjustment symbol (Sharp or Flat) to render the "accidental notes for a given name (e.g. of a chord, scale or key)
 */
public enum AdjSymbol {
  None,
  Sharp,
  Flat;

  private static final Pattern rgxSharpIn = Pattern.compile("[♯#]|major");
  private static final Pattern rgxFlatIn = Pattern.compile("[♭b]");
  private static final Pattern rgxSharpBegin = Pattern.compile("^[♯#]");
  private static final Pattern rgxFlatBegin = Pattern.compile("^[♭b]");
  private static final Pattern rgxSharpishIn = Pattern.compile("(M|maj|major|aug)");
  private static final Pattern rgxFlattishIn = Pattern.compile("([^a-z]|^)(m|min|minor|dim)");

  /**
   AdjSymbol the adjustment symbol (Sharp or Flat) for a given name (e.g. of a chord, scale or key)

   @param name to get adjustment symbol of
   @return adjustment symbol
   */
  public static AdjSymbol of(String name) {
    int numSharps = countMatches(rgxSharpIn.matcher(name));
    int numFlats = countMatches(rgxFlatIn.matcher(name));
    int numSharpish = countMatches(rgxSharpishIn.matcher(name));
    int numFlattish = countMatches(rgxFlattishIn.matcher(name));

    // sharp/flat has precedent over sharpish/flattish; overall default is sharp
    if (numSharps > 0 && numSharps > numFlats)
      return Sharp;
    else if (numFlats > 0)
      return Flat;
    else if (numSharpish > 0 && numSharpish > numFlattish)
      return Sharp;
    else if (numFlattish > 0)
      return Flat;
    else
      return Sharp;
  }

  /**
   Count number of matches of a regex pattern matcher

   @param matcher to count
   @return number of matches
   */
  private static int countMatches(Matcher matcher) {
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
  public static AdjSymbol firstOf(String name) {
    if (rgxSharpBegin.matcher(name).find())
      return Sharp;
    else if (rgxFlatBegin.matcher(name).find())
      return Flat;
    else
      return None;

  }

}
