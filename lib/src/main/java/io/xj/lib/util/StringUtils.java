// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.util;

import com.typesafe.config.Config;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public interface StringUtils {
  Pattern spaces = Pattern.compile(" +");
  Pattern underscores = Pattern.compile("_+");
  Pattern leadingScores = Pattern.compile("^_+");
  Pattern tailingScores = Pattern.compile("_+$");
  Pattern nonAlphabetical = Pattern.compile("[^a-zA-Z]");
  Pattern nonAlphaSlug = Pattern.compile("[^a-zA-Z_]");
  Pattern nonAlphanumeric = Pattern.compile("[^a-zA-Z0-9.\\-]"); // include decimal and sign
  Pattern nonNumeric = Pattern.compile("[^0-9.\\-]"); // include decimal and sign
  Pattern nonSlug = Pattern.compile("[^a-zA-Z0-9]");
  Pattern nonHyphenatedSlug = Pattern.compile("[^a-zA-Z0-9\\-]");
  Pattern nonMeme = Pattern.compile("[^a-zA-Z0-9!$]");
  Pattern nonEvent = Pattern.compile("[^a-zA-Z]");
  Pattern nonScored = Pattern.compile("[^a-zA-Z0-9_]");
  Pattern nonNote = Pattern.compile("[^#0-9a-zA-Z ]");
  Pattern isInteger = Pattern.compile("[0-9]+");
  Pattern integerSuffix = Pattern.compile(".*([0-9]+)$");
  String UNDERSCORE = "_";
  String SPACE = " ";
  String NOTHING = "";
  char SINGLE_QUOTE = '\'';
  char DOUBLE_QUOTE = '"';

  /**
   * Format a stack trace in carriage-return-separated lines
   *
   * @param e exception to format the stack trace of
   * @return formatted stack trace
   */
  static String formatStackTrace(@Nullable Throwable e) {
    if (Objects.isNull(e)) return "";
    return formatMultiline(e.getStackTrace());
  }

  /**
   * Format multiline text in carriage-return-separated lines
   *
   * @param stack of strings to format as multiline
   * @return formatted stack trace
   */
  static String formatMultiline(Object[] stack) {
    String[] stackLines = Arrays.stream(stack).map(String::valueOf).toArray(String[]::new);
    return String.join(System.lineSeparator(), stackLines) + System.lineSeparator();
  }

  /**
   * Get class simple name, or interface class simple name if it exists
   *
   * @param entity to get name of
   * @return entity name
   */
  static String getSimpleName(Object entity) {
    return getSimpleName(entity.getClass());
  }

  /**
   * Get class simple name, or interface class simple name if it exists
   *
   * @param entityClass to get name of
   * @return entity name
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
   * Alphabetical characters only, no case modification
   *
   * @param raw text to restrict to alphabetical
   * @return alphabetical-only string
   */
  static String toAlphabetical(String raw) {
    return
      nonAlphabetical.matcher(raw)
        .replaceAll("");
  }

  /**
   * Alphanumeric characters only, no case modification
   *
   * @param raw text to restrict to alphanumeric
   * @return alphanumeric-only string
   */
  static String toAlphanumeric(String raw) {
    return
      nonAlphanumeric.matcher(raw)
        .replaceAll("");
  }

  /**
   * Alphanumeric characters only, no case modification
   *
   * @param raw text to restrict to alphanumeric
   * @return alphanumeric-only string
   */
  static String toAlphanumericHyphenated(String raw) {
    return
      nonAlphanumeric.matcher(raw)
        .replaceAll("-");
  }

  /**
   * Alphabetical characters and underscore only, no case modification
   *
   * @param raw text to restrict to alphabetical and underscore
   * @return alphabetical-and-underscore-only string
   */
  static String toAlphaSlug(String raw) {
    return
      nonAlphaSlug.matcher(raw)
        .replaceAll("");
  }

  /**
   * Conform to Lower-scored (e.g. "buns_and_jams")
   *
   * @param raw input
   * @return purified
   */
  static String toLowerScored(String raw) {
    return toScored(raw).toLowerCase(Locale.ENGLISH);
  }

  /**
   * Conform to Lower-scored (e.g. "buns_and_jams"), else default value
   *
   * @param raw          input
   * @param defaultValue if no input
   * @return purified
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
   * Conform to Lowercase hyphenated-slug (e.g. "mush-mush")
   *
   * @param raw input
   * @return purified
   */
  static String toLowerHyphenatedSlug(String raw) {
    return nonHyphenatedSlug.matcher(raw.replace(" ", "-")).replaceAll(NOTHING).toLowerCase(Locale.ENGLISH);
  }

  /**
   * Conform to Lowercase slug (e.g. "mush")
   *
   * @param raw input
   * @return purified
   */
  static String toLowerSlug(String raw) {
    return toSlug(raw).toLowerCase(Locale.ENGLISH);
  }

  /**
   * Conform to Lowercase slug (e.g. "mush"), else default value
   *
   * @param raw          input
   * @param defaultValue if no input
   * @return purified
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
   * Conform to Note (e.g. "C# major")
   *
   * @param raw input
   * @return purified
   */
  static String toNote(String raw) {
    return nonNote.matcher(raw)
      .replaceAll("").trim();
  }

  /**
   * Number characters only
   *
   * @param raw text to restrict to numeric
   * @return numeric-only string
   */
  static String toNumeric(String raw) {
    return
      nonNumeric.matcher(raw)
        .replaceAll("");
  }

  /**
   * Make plural
   *
   * @param noun to make plural
   * @return plural noun
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
   * Make plural
   *
   * @param noun to make plural
   * @return plural noun
   */
  static String toSingular(String noun) {
    // too short to make singular
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
   * Conform to Proper (e.g. "Jam")
   *
   * @param raw input
   * @return purified
   */
  static String toProper(String raw) {
    if (1 < raw.length()) {
      return raw.substring(0, 1).toUpperCase(Locale.ENGLISH) + raw.substring(1);

    } else if (!raw.isEmpty())
      return raw.toUpperCase(Locale.ENGLISH);

    return "";
  }

  /**
   * Conform to Proper-slug (e.g. "Jam")
   *
   * @param raw input
   * @return purified
   */
  static String toProperSlug(String raw) {
    return toProper(toSlug(raw));
  }

  /**
   * Conform to Proper-slug (e.g. "Jam"), else default value
   *
   * @param raw          input
   * @param defaultValue if no input
   * @return purified
   */
  static String toProperSlug(String raw, String defaultValue) {
    return toProper(toSlug(raw, defaultValue));
  }

  /**
   * Conform to toScored (e.g. "mush_bun")
   *
   * @param raw input
   * @return purified
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
   * Return single-quoted version of string
   *
   * @param value to single-quote
   * @return single-quoted value
   */
  static String singleQuoted(String value) {
    return String.format("%s%s%s", SINGLE_QUOTE, value, SINGLE_QUOTE);
  }

  /**
   * Return double-quoted version of string
   *
   * @param value to single-quote
   * @return single-quoted value
   */
  static String doubleQuoted(String value) {
    return String.format("%s%s%s", DOUBLE_QUOTE, value, DOUBLE_QUOTE);
  }

  /**
   * Conform to Slug (e.g. "jim")
   *
   * @param raw input
   * @return purified
   */
  static String toSlug(String raw) {
    return nonSlug.matcher(raw)
      .replaceAll(NOTHING);
  }

  /**
   * Conform to Slug (e.g. "jim"), else default value
   *
   * @param raw          input
   * @param defaultValue if no input
   * @return purified
   */
  static String toSlug(String raw, String defaultValue) {
    String slug = toSlug(raw);
    if (slug.isEmpty()) return defaultValue;
    else return slug;
  }

  /**
   * Conform to Upper-scored (e.g. "BUNS_AND_JAMS")
   *
   * @param raw input
   * @return purified
   */
  static String toUpperScored(String raw) {
    return toScored(raw).toUpperCase(Locale.ENGLISH);
  }

  /**
   * Conform to Upper-scored (e.g. "BUNS_AND_JAMS"), else default value
   *
   * @param raw          input
   * @param defaultValue if no input
   * @return purified
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
   * Conform to Upper-slug non-numeric and strip special characters, e.g. "BUN"
   *
   * @param raw input
   * @return purified
   */
  static String toEvent(String raw) {
    return nonEvent.matcher(raw)
      .replaceAll(NOTHING)
      .toUpperCase(Locale.ENGLISH);
  }

  /**
   * Conform to Upper-slug including some special characters, e.g. "BUN!"
   *
   * @param raw input
   * @return purified
   */
  static String toMeme(String raw) {
    return nonMeme.matcher(raw)
      .replaceAll(NOTHING)
      .toUpperCase(Locale.ENGLISH);
  }

  /**
   * Conform to Upper-slug (e.g. "BUN"), else default value
   *
   * @param raw          input
   * @param defaultValue if no input
   * @return purified
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
   * Format a TypeSafe config as K=V lines
   *
   * @param config to format
   * @return formatted config
   */
  static String format(Config config) {
    return config.entrySet().stream()
      .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue().render()))
      .sorted()
      .collect(Collectors.joining("\n"));
  }

  /**
   * Format af ship key
   *
   * @param shipKey to format
   * @return formatted ship key
   */
  static String toShipKey(String shipKey) {
    return toLowerScored(shipKey);
  }

  /**
   * Parse a block of text containing environment variable key-value pairs, into a kev-value map
   *
   * @param secretValue to parse
   * @return key-value map of environment variables
   */
  static Map<String, String> parseEnvironmentVariableKeyPairs(String secretValue) {
    return Arrays.stream(secretValue.split(System.lineSeparator()))
      .map(pair -> pair.split("="))
      .collect(Collectors.toMap(p -> p[0], p -> p[1]));
  }

  /**
   * @param value to quote if empty
   * @return value, or empty quotes
   */
  static String orEmptyQuotes(@Nullable String value) {
    return isNullOrEmpty(value) ? "\"\"" : value;
  }

  /**
   * Split multiline text into an array of lines
   *
   * @param content to split
   * @return split content
   */
  static String[] splitLines(String content) {
    return content.split(System.lineSeparator());
  }

  /**
   * Get the percentage of a ratio, e.g. "53%" for 0.53f
   *
   * @param ratio input
   * @return formatted percentage
   */
  static String percentage(float ratio) {
    return String.format("%d%%", (int) Math.floor(100 * ratio));
  }

  /**
   * Increment the integer suffix of a given string, or start at 2
   *
   * @param value of which to scan suffix and increment
   * @return value with incremented integer suffix
   */
  static String incrementIntegerSuffix(String value) {
    if (isNullOrEmpty(value)) return "2";
    var m = integerSuffix.matcher(value);
    return String.format("%s%d",
      value.substring(0, value.length() - (m.matches() ? m.group(1).length() : 0)),
      m.matches() ? Integer.parseInt(m.group(1)) + 1 : 2);
  }

  /**
   * Replace more than one space with one space, and strip leading and trailing spaces
   *
   * @param value to strip
   * @return stripped value
   */
  static String stripExtraSpaces(String value) {
    return value.trim().replaceAll(" +", " ").strip();
  }

  /**
   * First group matching pattern in text, else null
   *
   * @param pattern to use
   * @param text    to search
   * @return match if found
   */
  static Optional<String> match(Pattern pattern, String text) {
    Matcher matcher = pattern.matcher(text);
    if (!matcher.find())
      return Optional.empty();

    if (0 == matcher.groupCount())
      return Optional.empty();

    String match = matcher.group(1);
    if (Objects.isNull(match) || match.length() == 0)
      return Optional.empty();

    return Optional.of(match);
  }

  /**
   * First group matching pattern in text, else null
   *
   * @param pattern to use
   * @param text    to search
   * @return true if found
   */
  static Boolean find(Pattern pattern, String text) {
    Matcher matcher = pattern.matcher(text);
    return matcher.find();
  }

  /**
   * Reverse the lines of a multi-line text value
   *
   * @param text lines to reverse
   * @return text reversed lines
   */
  static String reverseLines(String text) {
    ArrayList<String> lines = new ArrayList<>(Arrays.asList(text.split("\n")));
    Collections.reverse(lines);
    return String.join("\n", lines);
  }

  /**
   * Whether the text begins with the given prefix
   *
   * @param text   to test
   * @param prefix search at beginning only
   * @return true if text begins with prefix
   */
  static boolean beginsWith(String text, String prefix) {
    return text.length() >= prefix.length()
      && Objects.equals(text.substring(0, prefix.length()), prefix);
  }

  /**
   * Pad the value with zeros to the given number of digits
   *
   * @param value  to pad
   * @param digits total after padding
   * @return padded value
   */
  static String zeroPadded(int value, int digits) {
    return String.format("%0" + digits + "d", value);
  }

  /**
   * Whether a value is null or empty
   *
   * @param value to test
   * @return true if non-null and non-empty
   */
  static boolean isNullOrEmpty(@Nullable String value) {
    return Objects.isNull(value) || value.isEmpty();
  }

  /**
   * String representation of value, or pass through null as empty string
   *
   * @param value to parse
   * @return string or empty
   */
  static String stringOrEmpty(@Nullable Object value) {
    return stringOrDefault(value, "");
  }

  /**
   * String representation of value, or pass through a default value
   *
   * @param value        to parse
   * @param defaultValue to return if value is null
   * @return string or default value
   */
  static String stringOrDefault(@Nullable Object value, String defaultValue) {
    return Objects.nonNull(value) ? String.valueOf(value) : defaultValue;
  }

  /**
   * Convert snake_case to UpperCamelCase
   *
   * @param source snake_case text
   * @return UpperCamelCase text
   */
  static String snakeToUpperCamelCase(String source) {
    return Arrays.stream(source.split("_"))
      .map(StringUtils::firstLetterToUpperCase)
      .collect(Collectors.joining());
  }

  /**
   * Convert snake_case to lowerCamelCase
   *
   * @param source snake_case text
   * @return lowerCamelCase text
   */
  static String snakeToLowerCamelCase(String source) {
    return firstLetterToLowerCase(snakeToUpperCamelCase(source));
  }

  /**
   * Change the first letter of a string to lower case
   *
   * @param source text
   * @return lowerCase text
   */
  static String firstLetterToLowerCase(String source) {
    return source.substring(0, 1).toLowerCase() + source.substring(1);
  }

  /**
   * Change the first letter of a string to upper case
   *
   * @param source text
   * @return upperCase text
   */
  static String firstLetterToUpperCase(String source) {
    return source.substring(0, 1).toUpperCase() + source.substring(1);
  }

  /**
   * Convert CamelCase or camelCase to snake_case
   *
   * @param source CamelCase or camelCase text
   * @return snake_case text
   */
  static String camelToSnakeCase(String source) {
    return Arrays.stream(splitCamelCase(source))
      .map(String::toLowerCase)
      .collect(Collectors.joining("_"));
  }

  /**
   * Convert kebab-case to lowerCamelCase
   *
   * @param source kebab-case text
   * @return lowerCamelCase text
   */
  static String kebabToLowerCamelCase(String source) {
    return firstLetterToLowerCase(kebabToUpperCamelCase(source));
  }

  /**
   * Convert kebab-case to upperCamelCase
   *
   * @param source kebab-case text
   * @return upperCamelCase text
   */
  static String kebabToUpperCamelCase(String source) {
    return Arrays.stream(source.split("-"))
      .map(StringUtils::firstLetterToUpperCase)
      .collect(Collectors.joining());
  }

  /**
   * Convert camelCase to kebab-case
   *
   * @param source camelCase text
   * @return kebab-case text
   */
  static String camelToKebabCase(String source) {
    return Arrays.stream(splitCamelCase(source))
      .map(String::toLowerCase)
      .collect(Collectors.joining("-"));
  }

  /**
   * Split camelCase or CamelCase into words
   *
   * @param source camelCase or CamelCase text
   * @return words
   */
  static String[] splitCamelCase(String source) {
    return source.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
  }
}
