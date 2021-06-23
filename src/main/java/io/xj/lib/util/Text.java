// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.util;

import com.google.common.collect.Ordering;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public interface Text {
  Pattern spaces = Pattern.compile("[ ]+");
  Pattern underscores = Pattern.compile("_+");
  Pattern leadingScores = Pattern.compile("^_+");
  Pattern tailingScores = Pattern.compile("_+$");
  Pattern nonAlphabetical = Pattern.compile("[^a-zA-Z]");
  Pattern nonAlphaSlug = Pattern.compile("[^a-zA-Z_]");
  Pattern nonAlphanumeric = Pattern.compile("[^a-zA-Z0-9.\\-]"); // include decimal and sign
  Pattern nonNumeric = Pattern.compile("[^0-9.\\-]"); // include decimal and sign
  Pattern nonSlug = Pattern.compile("[^a-zA-Z0-9]");
  Pattern nonMeme = Pattern.compile("[^a-zA-Z0-9!]");
  Pattern nonScored = Pattern.compile("[^a-zA-Z0-9_]");
  Pattern nonNote = Pattern.compile("[^#0-9a-zA-Z ]");
  Pattern isInteger = Pattern.compile("[0-9]+");
  String UNDERSCORE = "_";
  String NOTHING = "";
  char SINGLE_QUOTE = '\'';
  char DOUBLE_QUOTE = '"';

  /**
   Format a stack trace in carriage-return-separated lines

   @param e exception to format the stack trace of
   @return formatted stack trace
   */
  static String formatStackTrace(@Nullable Throwable e) {
    if (Objects.isNull(e)) return "";
    return formatMultiline(e.getStackTrace());
  }

  /**
   Format multiline text in carriage-return-separated lines

   @param stack of strings to format as multiline
   @return formatted stack trace
   */
  static String formatMultiline(Object[] stack) {
    String[] stackLines = Arrays.stream(stack).map(String::valueOf).toArray(String[]::new);
    return String.join(System.getProperty("line.separator"), stackLines);
  }

  /**
   Get class simple name, or interface class simple name if it exists

   @param entity to get name of
   @return entity name
   */
  static String getSimpleName(Object entity) {
    return getSimpleName(entity.getClass());
  }

  /**
   Get class simple name, or interface class simple name if it exists

   @param entityClass to get name of
   @return entity name
   */
  static String getSimpleName(Class<?> entityClass) {
    if (entityClass.isInterface())
      return entityClass.getSimpleName();
    if (0 < entityClass.getInterfaces().length &&
      "impl".equalsIgnoreCase(entityClass.getSimpleName().substring(entityClass.getSimpleName().length() - 4)))
      return entityClass.getInterfaces()[0].getSimpleName();
    else
      return entityClass.getSimpleName();
  }

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
   Alphanumeric characters only, no case modification

   @param raw text to restrict to alphanumeric
   @return alphanumeric-only string
   */
  static String toAlphanumeric(String raw) {
    return
      nonAlphanumeric.matcher(raw)
        .replaceAll("");
  }

  /**
   Alphabetical characters and underscore only, no case modification

   @param raw text to restrict to alphabetical and underscore
   @return alphabetical-and-underscore-only string
   */
  static String toAlphaSlug(String raw) {
    return
      nonAlphaSlug.matcher(raw)
        .replaceAll("");
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
   Conform to Note (e.g. "C# major")

   @param raw input
   @return purified
   */
  static String toNote(String raw) {
    return nonNote.matcher(raw)
      .replaceAll("").trim();
  }

  /**
   Number characters only

   @param raw text to restrict to numeric
   @return numeric-only string
   */
  static String toNumeric(String raw) {
    return
      nonNumeric.matcher(raw)
        .replaceAll("");
  }

  /**
   Make plural

   @param noun to make plural
   @return plural noun
   */
  static String toPlural(String noun) {
    // too short to pluralize
    if (2 > noun.length())
      return noun;

    // cache last letters
    String lastTwo = noun.substring(noun.length() - 2).toLowerCase();
    String lastOne = noun.substring(noun.length() - 1).toLowerCase();

    // ends in "se" -- add "s"
    if ("se".equals(lastTwo))
      return String.format("%ss", noun);

    // ends in "ss" -- add "es"
    if ("ss".equals(lastTwo))
      return String.format("%ses", noun);

    // ends in "y" -- remove "y" + add "ies"
    if ("y".equals(lastOne))
      return String.format("%sies", noun.substring(0, noun.length() - 1));

    // ends in "s" -- skip
    if ("s".equals(lastOne))
      return noun;

    // add "s"
    return String.format("%ss", noun);
  }

  /**
   Make plural

   @param noun to make plural
   @return plural noun
   */
  static String toSingular(String noun) {
    // too short to singularize
    if (2 > noun.length())
      return noun;

    // cache last letters
    String lastThree = noun.substring(noun.length() - 3).toLowerCase();
    String lastOne = noun.substring(noun.length() - 1).toLowerCase();

    // ends in "ies" -- change to y
    if ("ies".equals(lastThree))
      return String.format("%sy", noun.substring(0, noun.length() - 3));

    // ends in "s" -- remove s
    if ("s".equals(lastOne))
      return noun.substring(0, noun.length() - 1);

    // no op
    return noun;
  }

  /**
   Conform to Proper (e.g. "Jam")

   @param raw input
   @return purified
   */
  static String toProper(String raw) {
    if (1 < raw.length()) {
      return raw.substring(0, 1).toUpperCase(Locale.ENGLISH) + raw.substring(1);

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
   Return single-quoted version of string

   @param value to single-quote
   @return single-quoted value
   */
  static String singleQuoted(String value) {
    return String.format("%s%s%s", SINGLE_QUOTE, value, SINGLE_QUOTE);
  }

  /**
   Return double-quoted version of string

   @param value to single-quote
   @return single-quoted value
   */
  static String doubleQuoted(String value) {
    return String.format("%s%s%s", DOUBLE_QUOTE, value, DOUBLE_QUOTE);
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
   Conform to Upper-slug (e.g. "BUN")

   @param raw input
   @return purified
   */
  static String toMeme(String raw) {
    return nonMeme.matcher(raw)
      .replaceAll(NOTHING)
      .toUpperCase(Locale.ENGLISH);
  }

  /**
   Conform to Upper-slug (e.g. "BUN"), else default value

   @param raw          input
   @param defaultValue if no input
   @return purified
   */
  static String toMeme(String raw, String defaultValue) {
    if (Objects.isNull(raw))
      return defaultValue.toUpperCase(Locale.ENGLISH);

    String out = toSlug(raw).toUpperCase(Locale.ENGLISH);
    if (out.isEmpty())
      return defaultValue.toUpperCase(Locale.ENGLISH);

    return out;
  }

  /**
   Format a config into multiline key => value, padded to align into two columns, sorted alphabetically by key name

   @param config to format
   @return multiline formatted config
   */
  static String toReport(Config config) {
    Set<Map.Entry<String, ConfigValue>> entries = config.entrySet();

    // there must be one longest entry, and we'll use its length as the column width for printing the whole list
    Optional<String> longest = entries.stream().map(Map.Entry::getKey).max(Comparator.comparingInt(String::length));
    if (longest.isEmpty()) return "";
    int padding = longest.get().length();

    // each line in the entry is padded to align into two columns, sorted alphabetically by key name
    List<String> lines = entries.stream()
      .map(c -> String.format("    %-" + padding + "s => %s", c.getKey(), c.getValue().unwrapped()))
      .sorted(Ordering.natural())
      .collect(Collectors.toList());

    // join lines into one multiline output
    return String.join("\n", lines);
  }

  /**
   Format a TypeSafe config as K=V lines

   @param config to format
   @return formatted config
   */
  static String format(Config config) {
    return config.entrySet().stream()
      .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue().render()))
      .sorted()
      .collect(Collectors.joining("\n"));
  }

  /**
   Format an embed key

   @param embedKey to format
   @return formatted embed key
   */
  static String toEmbedKey(String embedKey) {
    return toLowerScored(embedKey);
  }

  /**
   Parse a block of text containing environment variable key-value pairs, into a kev-value map

   @param secretValue to parse
   @return key-value map of environment variables
   */
  static Map<String, String> parseEnvironmentVariableKeyPairs(String secretValue) {
    return Arrays.stream(secretValue.split("\n"))
      .map(pair -> pair.split("="))
      .collect(Collectors.toMap(p -> p[0], p -> p[1]));
  }
}
