// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.util;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
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
  Pattern nonSlug = Pattern.compile("[^a-zA-Z]");
  Pattern nonScored = Pattern.compile("[^a-zA-Z0-9_]");
  Pattern nonNote = Pattern.compile("[^#0-9a-zA-Z ]");
  Pattern isInteger = Pattern.compile("[0-9]+");
  String UNDERSCORE = "_";
  String NOTHING = "";
  char SINGLE_QUOTE = '\'';

  /**
   Format a stack trace in carriage-return-separated lines

   @param e exception to format the stack trace of
   @return formatted stack trace
   */
  static String formatSimpleTrace(@Nullable Throwable e) {
    if (Objects.isNull(e)) return "";
    StackTraceElement[] stack = e.getStackTrace();
    return Arrays.stream(stack).map(StackTraceElement::toString).iterator().next();
  }

  /**
   Format a stack trace in carriage-return-separated lines

   @param e exception to format the stack trace of
   @return formatted stack trace
   */
  static String formatStackTrace(@Nullable Throwable e) {
    if (Objects.isNull(e)) return "";
    StackTraceElement[] stack = e.getStackTrace();
    String[] stackLines = Arrays.stream(stack).map(StackTraceElement::toString).toArray(String[]::new);
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
  static String getSimpleName(Class entityClass) {
    if (entityClass.isInterface())
      return entityClass.getSimpleName();
    if (0 < entityClass.getInterfaces().length &&
      "impl".equals(entityClass.getSimpleName().substring(entityClass.getSimpleName().length() - 4).toLowerCase()))
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
   To a thingId style attribute of an object

   @param obj to add Id to
   @return id attribute of key
   */
  static String toIdAttribute(Object obj) {
    return String.format("%sId", Text.toResourceBelongsTo(obj));
  }

  /**
   To a thingId style attribute of an object's class

   @param key to add Id to
   @return id attribute of key
   */
  static String toIdAttribute(Class key) {
    return String.format("%sId", Text.toResourceBelongsTo(key));
  }

  /**
   To an thingId style attribute

   @param key to add Id to
   @return id attribute of key
   */
  static String toIdAttribute(String key) {
    return String.format("%sId", Text.toResourceBelongsTo(key));
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

    // ends in "y" -- remove "y" + add "ies"
    if ("y".equals(lastOne))
      return String.format("%sies", noun.substring(0, noun.length() - 1));

    // ends in "s" -- add "es"
    if ("s".equals(lastOne))
      return String.format("%ses", noun);

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
   get belongs-to relationship name of object, the key to use when this class is the target of a belongs-to relationship
   + Chain.class -> "chain"
   + AccountUser.class -> "accountUser"
   + Library.class -> "library"

   @param belongsTo to get resource belongsTo of
   @return resource belongsTo of object
   */
  static String toResourceBelongsTo(Object belongsTo) {
    return toResourceBelongsTo(Text.getSimpleName(belongsTo));
  }

  /**
   get belongs-to relationship name of class, the key to use when this class is the target of a belongs-to relationship
   + Chain.class -> "chain"
   + AccountUser.class -> "accountUser"
   + Library.class -> "library"

   @param belongsTo to get resource belongsTo of
   @return resource belongsTo of object
   */
  static String toResourceBelongsTo(Class belongsTo) {
    return toResourceBelongsTo(Text.getSimpleName(belongsTo));
  }

  /**
   get belongs-to relationship name, to use when this key is the target of a belongs-to relationship
   + Chain.class -> "chain"
   + AccountUser.class -> "accountUser"
   + Library.class -> "library"

   @param belongsTo to conform
   @return conformed resource belongsTo
   */
  static String toResourceBelongsTo(String belongsTo) {
    return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, belongsTo);
  }

  /**
   get belongs-to relationship name, to use when this key is the target of a belongs-to relationship
   FROM a resource type
   + "chains" -> "chain"
   + "account-users" -> "accountUser"
   + "libraries" -> "library"

   @param type to conform
   @return conformed resource hasMany
   */
  static String toResourceBelongsToFromType(String type) {
    return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, toSingular(type));
  }

  /**
   get has-many relationship name of class, the key to use when this class is the target of a has-many relationship
   + Chain.class -> "chains"
   + AccountUser.class -> "accountUsers"
   + Library.class -> "libraries"

   @param resource to get resource hasMany of
   @return resource hasMany of object
   */
  static String toResourceHasMany(Class resource) {
    return toResourceHasMany(Text.getSimpleName(resource));
  }

  /**
   get has-many relationship name of object, the key to use when this class is the target of a has-many relationship
   + Chain.class -> "chains"
   + AccountUser.class -> "accountUsers"
   + Library.class -> "libraries"

   @param resource to get resource hasMany of
   @return resource hasMany of object
   */
  static String toResourceHasMany(Object resource) {
    return toResourceHasMany(Text.getSimpleName(resource));
  }

  /**
   get has-many relationship name, to use when this key is the target of a has-many relationship
   + Chain.class -> "chains"
   + AccountUser.class -> "accountUsers"
   + Library.class -> "libraries"

   @param hasMany to conform
   @return conformed resource hasMany
   */
  static String toResourceHasMany(String hasMany) {
    return toPlural(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, hasMany));
  }

  /**
   get has-many relationship name, to use when this key is the target of a has-many relationship
   FROM a resource type
   + "chains" -> "chains"
   + "account-users" -> "accountUsers"
   + "libraries" -> "libraries"

   @param type to conform
   @return conformed resource hasMany
   */
  static String toResourceHasManyFromType(String type) {
    return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, type);
  }

  /**
   Get resource type for any class, which is hyphenated lowercase pluralized
   + Chain.class -> "chains"
   + AccountUser.class -> "account-users"
   + Library.class -> "libraries"

   @param resource to get resource type of
   @return resource type of object
   */
  static String toResourceType(Class resource) {
    return toResourceType(Text.getSimpleName(resource));
  }

  /**
   Get resource type for any object, which is hyphenated lowercase pluralized
   + Chain.class -> "chains"
   + AccountUser.class -> "account-users"
   + Library.class -> "libraries"

   @param resource to get resource type of
   @return resource type of object
   */
  static String toResourceType(Object resource) {
    return toResourceType(Text.getSimpleName(resource));
  }

  /**
   Get resource type for any class, which is hyphenated lowercase pluralized
   + Chain.class -> "chains"
   + AccountUser.class -> "account-users"
   + Library.class -> "libraries"

   @param type to conform
   @return conformed resource type
   */
  static String toResourceType(String type) {
    return toPlural(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, type));
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
  static String toSingleQuoted(String value) {
    return String.format("%s%s%s", SINGLE_QUOTE, value, SINGLE_QUOTE);
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
   Format an immutable list of string values of an array of enum

   @param values to include
   @return array of string values
   */
  static List<String> toStrings(Object[] values) {
    ImmutableList.Builder<String> valuesBuilder = ImmutableList.builder();
    for (Object value : values) {
      valuesBuilder.add(value.toString());
    }
    return valuesBuilder.build();
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
   Compute an attribute name based on the name of the getter method,
   by removing the first three letters "get", then lower-casing the new first letter.
   <p>
   e.g., input of "getNewsPaper" results in "newsPaper"

   @param method for which to compute name of attribute
   @return attribute name
   */
  static String toAttributeName(Method method) {
    return String.format("%s%s",
      method.getName().substring(3, 4).toLowerCase(Locale.ENGLISH),
      method.getName().substring(4));
  }

  /**
   Compute a getter method name based on the name of the attribute,
   capitalize the first letter, then prepend "get"
   <p>
   e.g., input of "newsPaper" results in "getNewsPaper"

   @param attributeName for which to get name of getter method
   @return attribute name
   */
  static String toGetterName(String attributeName) {
    return String.format("%s%s%s", "get",
      attributeName.substring(0, 1).toUpperCase(Locale.ENGLISH),
      attributeName.substring(1));
  }

  /**
   Compute a setter method name based on the name of the attribute,
   capitalize the first letter, then prepend "set"
   <p>
   e.g., input of "newsPaper" results in "setNewsPaper"

   @param attributeName for which to get name of setter method
   @return attribute name
   */
  static String toSetterName(String attributeName) {
    return String.format("%s%s%s", "set",
      attributeName.substring(0, 1).toUpperCase(Locale.ENGLISH),
      attributeName.substring(1));
  }

  /**
   Get a string representation of an entity, comprising a key-value map of its properties

   @param name       of entity
   @param properties to map
   @return string representation
   */
  static String toKeyValueString(String name, ImmutableMap<String, String> properties) {
    return String.format("%s{%s}", name, CSV.from(properties));
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
}
