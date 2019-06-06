// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.util;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import io.xj.core.config.Config;
import io.xj.core.model.entity.Entity;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
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
  char SINGLE_QUOTE = '\'';
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
    if (slug.isEmpty()) return defaultValue;
    else return slug;
  }

  /**
   Conform to Proper (e.g. "Jam")

   @param raw input
   @return purified
   */
  static String toProper(String raw) {
    if (1 < raw.length()) {
      String lower = raw.toLowerCase(Locale.ENGLISH);
      return lower.substring(0, 1).toUpperCase(Locale.ENGLISH) + lower.substring(1);

    } else if (!raw.isEmpty())
      return raw.toUpperCase(Locale.ENGLISH);

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
    return toSlug(raw).toUpperCase(Locale.ENGLISH);
  }

  /**
   Conform to Upper-slug (e.g. "BUN"), else default value

   @param raw          input
   @param defaultValue if no input
   @return purified
   */
  static String toUpperSlug(String raw, String defaultValue) {
    if (Objects.isNull(raw))
      return defaultValue.toUpperCase(Locale.ENGLISH);

    String out = toSlug(raw).toUpperCase(Locale.ENGLISH);
    if (out.isEmpty())
      return defaultValue.toUpperCase(Locale.ENGLISH);

    return out;
  }

  /**
   Conform to Lower-slug (e.g. "mush")

   @param raw input
   @return purified
   */
  static String toLowerSlug(String raw) {
    return toSlug(raw).toLowerCase(Locale.ENGLISH);
  }

  /**
   Conform to Lower-slug (e.g. "mush"), else default value

   @param raw          input
   @param defaultValue if no input
   @return purified
   */
  static String toLowerSlug(String raw, String defaultValue) {
    if (Objects.isNull(raw))
      return defaultValue.toLowerCase(Locale.ENGLISH);

    String out = toSlug(raw).toLowerCase(Locale.ENGLISH);
    if (out.isEmpty())
      return defaultValue.toLowerCase(Locale.ENGLISH);

    return out;
  }

  /**
   Conform to toScored (e.g. "mush_bun")

   @param raw input
   @return purified
   */
  static String toScored(String raw) {
    if (Objects.isNull(raw)) return "";
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
    return toScored(raw).toUpperCase(Locale.ENGLISH);
  }

  /**
   Conform to Upper-scored (e.g. "BUNS_AND_JAMS"), else default value

   @param raw          input
   @param defaultValue if no input
   @return purified
   */
  static String toUpperScored(String raw, String defaultValue) {
    if (Objects.isNull(raw))
      return defaultValue.toUpperCase(Locale.ENGLISH);

    String out = toScored(raw).toUpperCase(Locale.ENGLISH);
    if (out.isEmpty())
      return defaultValue.toUpperCase(Locale.ENGLISH);

    return out;
  }

  /**
   Conform to Lower-scored (e.g. "buns_and_jams")

   @param raw input
   @return purified
   */
  static String toLowerScored(String raw) {
    return toScored(raw).toLowerCase(Locale.ENGLISH);
  }

  /**
   Conform to Lower-scored (e.g. "buns_and_jams"), else default value

   @param raw          input
   @param defaultValue if no input
   @return purified
   */
  static String toLowerScored(String raw, String defaultValue) {
    if (Objects.isNull(raw))
      return defaultValue.toLowerCase(Locale.ENGLISH);

    String out = toScored(raw).toLowerCase(Locale.ENGLISH);
    if (out.isEmpty())
      return defaultValue.toLowerCase(Locale.ENGLISH);

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
  static List<String> stringValues(Object[] values) {
    ImmutableList.Builder<String> valuesBuilder = ImmutableList.builder();
    for (Object value : values) {
      valuesBuilder.add(value.toString());
    }
    return valuesBuilder.build();
  }

  /**
   Format a comma-separated list of entity counts from a collection of entities

   @param entities for format a comma-separated list of the # occurences of each class
   @return comma-separated list in text
   */
  static <E extends Entity> String entityHistogram(Collection<E> entities) {
    Multiset<String> entityHistogram = ConcurrentHashMultiset.create();
    entities.forEach((E entity) -> entityHistogram.add(entity.getClass().getSimpleName()));
    List<String> descriptors = Lists.newArrayList();
    entityHistogram.elementSet().forEach((String name) -> descriptors.add(String.format("%d %s", entityHistogram.count(name), name)));
    return String.join(", ", descriptors);
  }

  /**
   Format a comma-separated list of entities from a collection of entities

   @param entities to format a comma-separated list of
   @return comma-separated list in text
   */
  static <E extends Entity> String entities(List<E> entities) {
    List<String> descriptors = Lists.newArrayList();
    entities.forEach((E entity) -> descriptors.add(entity.toString()));
    return String.join(", ", descriptors);
  }

  /**
   Return single-quoted version of string
   @param value to single-quote
   @return single-quoted value
   */
  static String singleQuoted(String value) {
    return String.format("%s%s%s", SINGLE_QUOTE, value, SINGLE_QUOTE);
  }
}
