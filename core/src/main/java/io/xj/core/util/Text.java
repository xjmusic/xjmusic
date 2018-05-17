// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.util;

import io.xj.core.config.Config;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public interface Text {
  //  Pattern hyphensAndSlugs = Pattern.compile("[\\-_]+");
//  Pattern fileExtension = Pattern.compile("\\.[a-zA-z0-9]+$");
  Pattern spaces = Pattern.compile("[ ]+");
  Pattern underscores = Pattern.compile("_+");
  Pattern leadingScores = Pattern.compile("^_+");
  Pattern tailingScores = Pattern.compile("_+$");
  Pattern nonAlphabetical = Pattern.compile("[^a-zA-Z]");
  Pattern nonSlug = Pattern.compile("[^a-zA-Z]");
  Pattern nonScored = Pattern.compile("[^a-zA-Z0-9_]");
  Pattern nonNote = Pattern.compile("[^#0-9a-zA-Z ]");
  Pattern isInteger = Pattern.compile("[0-9]+");
  //  Pattern nonDocKey = Pattern.compile("[^0-9a-zA-Z_\\-.]");
//  Pattern oneOrMorePeriod = Pattern.compile("\\.+");
  String UNDERSCORE = "_";
  String NOTHING = "";
//  Comparator<? super String> byStringLengthDescending = (o1, o2) -> -Integer.compare(o2.length(), o1.length());

  /**
   Alphabetical characters only, no case modification

   @param raw text to restrict to alphabetical
   @return alphabetical-only string
   */
  static String toAlphabetical(String raw) {
    return
      nonAlphabetical.matcher(raw)
        .replaceAll("");
  }

  /**
   Conform to Note (e.g. "C# major")

   @param raw input
   @return purified
   */
  static String toNote(String raw) {
    return nonNote.matcher(raw)
      .replaceAll("").trim();
  }

  /**
   Conform to Slug (e.g. "jim")

   @param raw input
   @return purified
   */
  static String toSlug(String raw) {
    return nonSlug.matcher(raw)
      .replaceAll(NOTHING);
  }

  /**
   Conform to Slug (e.g. "jim"), else default value

   @param raw          input
   @param defaultValue if no input
   @return purified
   */
  static String toSlug(String raw, String defaultValue) {
    String slug = toSlug(raw);
    return slug.isEmpty() ? defaultValue : slug;
  }

  /**
   Conform to Proper (e.g. "Jam")

   @param raw input
   @return purified
   */
  static String toProper(String raw) {
    if (1 < raw.length()) {
      String lower = raw.toLowerCase();
      return lower.substring(0, 1).toUpperCase() + lower.substring(1);

    } else if (!raw.isEmpty())
      return raw.toUpperCase();

    return "";
  }

  /**
   Conform to Proper-slug (e.g. "Jam")

   @param raw input
   @return purified
   */
  static String toProperSlug(String raw) {
    return toProper(toSlug(raw));
  }

  /**
   Conform to Proper-slug (e.g. "Jam"), else default value

   @param raw          input
   @param defaultValue if no input
   @return purified
   */
  static String toProperSlug(String raw, String defaultValue) {
    return toProper(toSlug(raw, defaultValue));
  }

  /**
   Conform to Upper-slug (e.g. "BUN")

   @param raw input
   @return purified
   */
  static String toUpperSlug(String raw) {
    return toSlug(raw).toUpperCase();
  }

  /**
   Conform to Upper-slug (e.g. "BUN"), else default value

   @param raw          input
   @param defaultValue if no input
   @return purified
   */
  static String toUpperSlug(String raw, String defaultValue) {
    if (Objects.isNull(raw))
      return defaultValue.toUpperCase();

    String out = toSlug(raw).toUpperCase();
    if (out.isEmpty())
      return defaultValue.toUpperCase();

    return out;
  }

  /**
   Conform to Lower-slug (e.g. "mush")

   @param raw input
   @return purified
   */
  static String toLowerSlug(String raw) {
    return toSlug(raw).toLowerCase();
  }

  /**
   Conform to Lower-slug (e.g. "mush"), else default value

   @param raw          input
   @param defaultValue if no input
   @return purified
   */
  static String toLowerSlug(String raw, String defaultValue) {
    if (Objects.isNull(raw))
      return defaultValue.toLowerCase();

    String out = toSlug(raw).toLowerCase();
    if (out.isEmpty())
      return defaultValue.toLowerCase();

    return out;
  }

  /**
   Conform to toScored (e.g. "mush_bun")

   @param raw input
   @return purified
   */
  static String toScored(String raw) {
    return
      leadingScores.matcher(
        tailingScores.matcher(
          underscores.matcher(
            nonScored.matcher(
              spaces.matcher(
                raw.trim()
              ).replaceAll(UNDERSCORE)
            ).replaceAll(NOTHING)
          ).replaceAll(UNDERSCORE)
        ).replaceAll(NOTHING)
      ).replaceAll(NOTHING);
  }

  /**
   Conform to Upper-scored (e.g. "BUNS_AND_JAMS")

   @param raw input
   @return purified
   */
  static String toUpperScored(String raw) {
    return toScored(raw).toUpperCase();
  }

  /**
   Conform to Upper-scored (e.g. "BUNS_AND_JAMS"), else default value

   @param raw          input
   @param defaultValue if no input
   @return purified
   */
  static String toUpperScored(String raw, String defaultValue) {
    if (Objects.isNull(raw))
      return defaultValue.toUpperCase();

    String out = toScored(raw).toUpperCase();
    if (out.isEmpty())
      return defaultValue.toUpperCase();

    return out;
  }

  /**
   Conform to Lower-scored (e.g. "buns_and_jams")

   @param raw input
   @return purified
   */
  static String toLowerScored(String raw) {
    return toScored(raw).toLowerCase();
  }

  /**
   Conform to Lower-scored (e.g. "buns_and_jams"), else default value

   @param raw          input
   @param defaultValue if no input
   @return purified
   */
  static String toLowerScored(String raw, String defaultValue) {
    if (Objects.isNull(raw))
      return defaultValue.toLowerCase();

    String out = toScored(raw).toLowerCase();
    if (out.isEmpty())
      return defaultValue.toLowerCase();

    return out;
  }

  /**
   True if input string is an integer
   @param raw text to check if it's an integer
   @return true if it's an integer
   */
  static Boolean isInteger(String raw) {
    return isInteger.matcher(raw).matches();
  }

  /**
   Format a stack trace in carriage-return-separated lines

   @param e exception to format the stack trace of
   @return formatted stack trace
   */
  static String formatStackTrace(@Nullable Exception e) {
    if (Objects.isNull(e)) return "";
    StackTraceElement[] stack = e.getStackTrace();
    String[] stackLines = Arrays.stream(stack).map(StackTraceElement::toString).toArray(String[]::new);
    return String.join(Config.lineSeparator(), stackLines);
  }

  /**
   Format an immutable list of string values from an array of enum

   @param values to include
   @return array of string values
   */
  static List<String> stringValues(Object... values) {
    ImmutableList.Builder<String> valuesBuilder = ImmutableList.builder();
    for (Object value : values) {
      valuesBuilder.add(value.toString());
    }
    return valuesBuilder.build();
  }
}
