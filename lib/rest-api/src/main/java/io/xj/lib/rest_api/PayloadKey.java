// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.rest_api;

import com.google.common.base.CaseFormat;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 Text utilities for Payload sent/received to/from a XJ Music REST JSON:API service
 <p>
 Created by Charney Kaye on 2020/03/05
 */
public enum PayloadKey {
  ;
  private static final String SIMPLE_NAME_OF_NULL = "null";
  static Pattern spaces = Pattern.compile("[ ]+");
  static Pattern underscores = Pattern.compile("_+");
  static Pattern leadingScores = Pattern.compile("^_+");
  static Pattern tailingScores = Pattern.compile("_+$");
  static Pattern nonAlphabetical = Pattern.compile("[^a-zA-Z]");
  static Pattern nonAlphaSlug = Pattern.compile("[^a-zA-Z_]");
  static Pattern nonAlphanumeric = Pattern.compile("[^a-zA-Z0-9.\\-]"); // include decimal and sign
  static Pattern nonNumeric = Pattern.compile("[^0-9.\\-]"); // include decimal and sign
  static Pattern nonSlug = Pattern.compile("[^a-zA-Z]");
  static Pattern nonScored = Pattern.compile("[^a-zA-Z0-9_]");
  static String UNDERSCORE = "_";
  static String NOTHING = "";
  static char SINGLE_QUOTE = '\'';

  /**
   Get class simple name, or interface class simple name if it exists

   @param entity to get name of
   @return entity name
   */
  public static String getSimpleName(Object entity) {
    return Objects.nonNull(entity) ? getSimpleName(entity.getClass()) : SIMPLE_NAME_OF_NULL;
  }

  /**
   Get class simple name, or interface class simple name if it exists

   @param entityClass to get name of
   @return entity name
   */
  public static String getSimpleName(Class<?> entityClass) {
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
  public static String toAlphabetical(String raw) {
    return
      nonAlphabetical.matcher(raw)
        .replaceAll("");
  }

  /**
   Alphanumeric characters only, no case modification

   @param raw text to restrict to alphanumeric
   @return alphanumeric-only string
   */
  public static String toAlphanumeric(String raw) {
    return
      nonAlphanumeric.matcher(raw)
        .replaceAll("");
  }

  /**
   Alphabetical characters and underscore only, no case modification

   @param raw text to restrict to alphabetical and underscore
   @return alphabetical-and-underscore-only string
   */
  public static String toAlphaSlug(String raw) {
    return
      nonAlphaSlug.matcher(raw)
        .replaceAll("");
  }

  /**
   To a thingId style attribute of an object

   @param obj to add Id to
   @return id attribute of key
   */
  public static String toIdAttribute(Object obj) {
    return String.format("%sId", PayloadKey.toResourceBelongsTo(obj));
  }

  /**
   To a thingId style attribute of an object's class

   @param key to add Id to
   @return id attribute of key
   */
  public static String toIdAttribute(Class<?> key) {
    return String.format("%sId", PayloadKey.toResourceBelongsTo(key));
  }

  /**
   To an thingId style attribute

   @param key to add Id to
   @return id attribute of key
   */
  public static String toIdAttribute(String key) {
    return String.format("%sId", PayloadKey.toResourceBelongsTo(key));
  }

  /**
   Conform to Lower-scored (e.g. "buns_and_jams")

   @param raw input
   @return purified
   */
  public static String toLowerScored(String raw) {
    return toScored(raw).toLowerCase(Locale.ENGLISH);
  }

  /**
   Conform to Lower-scored (e.g. "buns_and_jams"), else default value

   @param raw          input
   @param defaultValue if no input
   @return purified
   */
  public static String toLowerScored(String raw, String defaultValue) {
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
  public static String toLowerSlug(String raw) {
    return toSlug(raw).toLowerCase(Locale.ENGLISH);
  }

  /**
   Conform to Lower-slug (e.g. "mush"), else default value

   @param raw          input
   @param defaultValue if no input
   @return purified
   */
  public static String toLowerSlug(String raw, String defaultValue) {
    if (Objects.isNull(raw))
      return defaultValue.toLowerCase(Locale.ENGLISH);

    String out = toSlug(raw).toLowerCase(Locale.ENGLISH);
    if (out.isEmpty())
      return defaultValue.toLowerCase(Locale.ENGLISH);

    return out;
  }

  /**
   Number characters only

   @param raw text to restrict to numeric
   @return numeric-only string
   */
  public static String toNumeric(String raw) {
    return
      nonNumeric.matcher(raw)
        .replaceAll("");
  }

  /**
   Make plural

   @param noun to make plural
   @return plural noun
   */
  public static String toPlural(String noun) {
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
  public static String toSingular(String noun) {
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
  public static String toProper(String raw) {
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
  public static String toProperSlug(String raw) {
    return toProper(toSlug(raw));
  }

  /**
   Conform to Proper-slug (e.g. "Jam"), else default value

   @param raw          input
   @param defaultValue if no input
   @return purified
   */
  public static String toProperSlug(String raw, String defaultValue) {
    return toProper(toSlug(raw, defaultValue));
  }

  /**
   get belongs-to relationship name of object, the key to use when this class is the target of a belongs-to relationship
   + Chain.class -> "chain"
   + AccountUser.class -> "accountUser"
   + Entity.class -> "entity"

   @param belongsTo to get resource belongsTo of
   @return resource belongsTo of object
   */
  public static String toResourceBelongsTo(Object belongsTo) {
    return toResourceBelongsTo(PayloadKey.getSimpleName(belongsTo));
  }

  /**
   get belongs-to relationship name of class, the key to use when this class is the target of a belongs-to relationship
   + Chain.class -> "chain"
   + AccountUser.class -> "accountUser"
   + Entity.class -> "entity"

   @param belongsTo to get resource belongsTo of
   @return resource belongsTo of object
   */
  public static String toResourceBelongsTo(Class<?> belongsTo) {
    return toResourceBelongsTo(PayloadKey.getSimpleName(belongsTo));
  }

  /**
   get belongs-to relationship name, to use when this key is the target of a belongs-to relationship
   + Chain.class -> "chain"
   + AccountUser.class -> "accountUser"
   + Entity.class -> "entity"

   @param belongsTo to conform
   @return conformed resource belongsTo
   */
  public static String toResourceBelongsTo(String belongsTo) {
    return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, toSingular(belongsTo));
  }

  /**
   get belongs-to relationship name, to use when this key is the target of a belongs-to relationship
   FROM a resource type
   + "chains" -> "chain"
   + "account-users" -> "accountUser"
   + "entities" -> "entity"

   @param type to conform
   @return conformed resource hasMany
   */
  public static String toResourceBelongsToFromType(String type) {
    return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, toSingular(type));
  }

  /**
   get has-many relationship name of class, the key to use when this class is the target of a has-many relationship
   + Chain.class -> "chains"
   + AccountUser.class -> "accountUsers"
   + Entity.class -> "entities"

   @param resource to get resource hasMany of
   @return resource hasMany of object
   */
  public static String toResourceHasMany(Class<?> resource) {
    return toResourceHasMany(PayloadKey.getSimpleName(resource));
  }

  /**
   get has-many relationship name of object, the key to use when this class is the target of a has-many relationship
   + Chain.class -> "chains"
   + AccountUser.class -> "accountUsers"
   + Entity.class -> "entities"

   @param resource to get resource hasMany of
   @return resource hasMany of object
   */
  public static String toResourceHasMany(Object resource) {
    return toResourceHasMany(PayloadKey.getSimpleName(resource));
  }

  /**
   get has-many relationship name, to use when this key is the target of a has-many relationship
   + Chain.class -> "chains"
   + AccountUser.class -> "accountUsers"
   + Entity.class -> "entities"

   @param hasMany to conform
   @return conformed resource hasMany
   */
  public static String toResourceHasMany(String hasMany) {
    return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, toPlural(hasMany));
  }

  /**
   get has-many relationship name, to use when this key is the target of a has-many relationship
   FROM a resource type
   + "chains" -> "chains"
   + "account-users" -> "accountUsers"
   + "entities" -> "entities"

   @param type to conform
   @return conformed resource hasMany
   */
  public static String toResourceHasManyFromType(String type) {
    return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, toPlural(type));
  }

  /**
   Get resource type for any class, which is hyphenated lowercase pluralized
   + Chain.class -> "chains"
   + AccountUser.class -> "account-users"
   + Entity.class -> "entities"

   @param resource to get resource type of
   @return resource type of object
   */
  public static String toResourceType(Class<?> resource) {
    return toResourceType(PayloadKey.getSimpleName(resource));
  }

  /**
   Get resource type for any object, which is hyphenated lowercase pluralized
   + Chain.class -> "chains"
   + AccountUser.class -> "account-users"
   + Entity.class -> "entities"

   @param resource to get resource type of
   @return resource type of object
   */
  public static String toResourceType(Object resource) {
    return toResourceType(PayloadKey.getSimpleName(resource));
  }

  /**
   Get resource type for any class, which is hyphenated lowercase pluralized
   + Chain.class -> "chains"
   + AccountUser.class -> "account-users"
   + Entity.class -> "entities"

   @param type to conform
   @return conformed resource type
   */
  public static String toResourceType(String type) {
    return toPlural(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, type));
  }

  /**
   Conform to toScored (e.g. "mush_bun")

   @param raw input
   @return purified
   */
  public static String toScored(String raw) {
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
  public static String toSingleQuoted(String value) {
    return String.format("%s%s%s", SINGLE_QUOTE, value, SINGLE_QUOTE);
  }

  /**
   Conform to Slug (e.g. "jim")

   @param raw input
   @return purified
   */
  public static String toSlug(String raw) {
    return nonSlug.matcher(raw)
      .replaceAll(NOTHING);
  }

  /**
   Conform to Slug (e.g. "jim"), else default value

   @param raw          input
   @param defaultValue if no input
   @return purified
   */
  public static String toSlug(String raw, String defaultValue) {
    String slug = toSlug(raw);
    if (slug.isEmpty()) return defaultValue;
    else return slug;
  }

  /**
   Conform to Upper-scored (e.g. "BUNS_AND_JAMS")

   @param raw input
   @return purified
   */
  public static String toUpperScored(String raw) {
    return toScored(raw).toUpperCase(Locale.ENGLISH);
  }

  /**
   Conform to Upper-scored (e.g. "BUNS_AND_JAMS"), else default value

   @param raw          input
   @param defaultValue if no input
   @return purified
   */
  public static String toUpperScored(String raw, String defaultValue) {
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
  public static String toUpperSlug(String raw) {
    return toSlug(raw).toUpperCase(Locale.ENGLISH);
  }

  /**
   Conform to Upper-slug (e.g. "BUN"), else default value

   @param raw          input
   @param defaultValue if no input
   @return purified
   */
  public static String toUpperSlug(String raw, String defaultValue) {
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
  public static String toAttributeName(Method method) {
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
  public static String toGetterName(String attributeName) {
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
  public static String toSetterName(String attributeName) {
    return String.format("%s%s%s", "set",
      attributeName.substring(0, 1).toUpperCase(Locale.ENGLISH),
      attributeName.substring(1));
  }

  /**
   Compute an attribute name, by just lower-casing the first better
   <p>
   e.g., input of "NewsPaper" results in "newsPaper"

   @param name for conversion to attribute name
   @return attribute name
   */
  public static String toAttributeName(String name) {
    return String.format("%s%s",
      name.substring(0, 1).toLowerCase(Locale.ENGLISH),
      name.substring(1));
  }
}
