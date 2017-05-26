// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.util;

import java.util.regex.Pattern;

public interface Text {
  Pattern hyphensAndSlugs = Pattern.compile("[\\-_]+");
  Pattern fileExtension = Pattern.compile("\\.[a-zA-z0-9]+$");
  Pattern space = Pattern.compile("[ ]+");
  Pattern nonAlphabetical = Pattern.compile("[^a-zA-Z]");
  Pattern nonSlug = Pattern.compile("[^a-zA-Z]");
  Pattern nonScored = Pattern.compile("[^a-zA-Z_]");
  Pattern nonNote = Pattern.compile("[^#0-9a-zA-Z ]");
  Pattern nonDocKey = Pattern.compile("[^0-9a-zA-Z_\\-.]");
  Pattern oneOrMorePeriod = Pattern.compile("\\.+");
  String UNDERSCORE = "_";
  String NOTHING = "";

  /**
   Alphabetical characters only, no case modification

   @param raw text to restrict to alphabetical
   @return alphabetical-only string
   */
  static String alphabetical(String raw) {
    return
      nonAlphabetical.matcher(raw)
        .replaceAll("");
  }

  /**
   Conform to DocKey key (e.g. "chain-info.md")

   @param raw input
   @return doc key
   */
  static String DocKey(String raw) {
    return
      oneOrMorePeriod.matcher(
        nonDocKey.matcher(raw)
          .replaceAll("").trim()
      ).replaceAll(".");
  }

  /**
   Generate DocName from DocKey (e.g. "Chain Info")

   @param key to generate name for
   @return doc name
   */
  static String DocNameForKey(String key) {
    String[] words =
      hyphensAndSlugs.matcher(
        fileExtension.matcher(key)
          .replaceAll("")
      ).replaceAll(" ")
        .trim()
        .split(" ");
    for (int i = 0; i < words.length; i++) {
      words[i] = toProper(words[i]);
    }
    return String.join(" ", words);
  }

  /**
   Conform to Note (e.g. "C# major")

   @param raw input
   @return purified
   */
  static String Note(String raw) {
    return nonNote.matcher(raw)
      .replaceAll("").trim();
  }

  /**
   Conform to Slug (e.g. "jim")

   @param raw input
   @return purified
   */
  static String Slug(String raw) {
    return nonSlug.matcher(raw)
      .replaceAll(NOTHING);
  }

  /**
   Conform to Slug (e.g. "jim"), else default value

   @param raw          input
   @param defaultValue if no input
   @return purified
   */
  static String Slug(String raw, String defaultValue) {
    String slug = Slug(raw);
    return slug.length() > 0 ? slug : defaultValue;
  }

  /**
   Conform to Proper (e.g. "Jam")

   @param raw input
   @return purified
   */
  static String toProper(String raw) {
    if (raw.length() > 1) {
      String lower = raw.toLowerCase();
      return lower.substring(0, 1).toUpperCase() + lower.substring(1);
    } else if (raw.length() > 0) {
      return raw.toUpperCase();
    } else {
      return "";
    }
  }

  /**
   Conform to Proper-slug (e.g. "Jam")

   @param raw input
   @return purified
   */
  static String ProperSlug(String raw) {
    return toProper(Slug(raw));
  }

  /**
   Conform to Proper-slug (e.g. "Jam"), else default value

   @param raw          input
   @param defaultValue if no input
   @return purified
   */
  static String ProperSlug(String raw, String defaultValue) {
    return toProper(Slug(raw, defaultValue));
  }

  /**
   Conform to Upper-slug (e.g. "BUN")

   @param raw input
   @return purified
   */
  static String UpperSlug(String raw) {
    return Slug(raw).toUpperCase();
  }

  /**
   Conform to Upper-slug (e.g. "BUN"), else default value

   @param raw          input
   @param defaultValue if no input
   @return purified
   */
  static String UpperSlug(String raw, String defaultValue) {
    if (raw == null) {
      return defaultValue.toUpperCase();
    }
    String out = Slug(raw).toUpperCase();
    if (out.length() > 0) {
      return out;
    } else {
      return defaultValue.toUpperCase();
    }
  }

  /**
   Conform to Lower-slug (e.g. "mush")

   @param raw input
   @return purified
   */
  static String LowerSlug(String raw) {
    return Slug(raw).toLowerCase();
  }

  /**
   Conform to Lower-slug (e.g. "mush"), else default value

   @param raw          input
   @param defaultValue if no input
   @return purified
   */
  static String LowerSlug(String raw, String defaultValue) {
    if (raw == null) {
      return defaultValue.toLowerCase();
    }
    String out = Slug(raw).toLowerCase();
    if (out.length() > 0) {
      return out;
    } else {
      return defaultValue.toLowerCase();
    }
  }

  /**
   Conform to Scored (e.g. "mush_bun")

   @param raw input
   @return purified
   */
  static String Scored(String raw) {
    return
      nonScored.matcher(
        space.matcher(
          raw.trim()
        ).replaceAll(UNDERSCORE)
      ).replaceAll(NOTHING);
  }

  /**
   Conform to Scored (e.g. "bUN_jam"), else default value

   @param raw          input
   @param defaultValue if no input
   @return purified
   */
  static String Scored(String raw, String defaultValue) {
    String scored = Scored(raw);
    return scored.length() > 0 ? scored : defaultValue;
  }

  /**
   Conform to Upper-scored (e.g. "BUNS_AND_JAMS")

   @param raw input
   @return purified
   */
  static String UpperScored(String raw) {
    return Scored(raw).toUpperCase();
  }

  /**
   Conform to Upper-scored (e.g. "BUNS_AND_JAMS"), else default value

   @param raw          input
   @param defaultValue if no input
   @return purified
   */
  static String UpperScored(String raw, String defaultValue) {
    if (raw == null) {
      return defaultValue.toUpperCase();
    }
    String out = Scored(raw).toUpperCase();
    if (out.length() > 0) {
      return out;
    } else {
      return defaultValue.toUpperCase();
    }
  }

  /**
   Conform to Lower-scored (e.g. "buns_and_jams")

   @param raw input
   @return purified
   */
  static String LowerScored(String raw) {
    return Scored(raw).toLowerCase();
  }

  /**
   Conform to Lower-scored (e.g. "buns_and_jams"), else default value

   @param raw          input
   @param defaultValue if no input
   @return purified
   */
  static String LowerScored(String raw, String defaultValue) {
    if (raw == null) {
      return defaultValue.toLowerCase();
    }
    String out = Scored(raw).toLowerCase();
    if (out.length() > 0) {
      return out;
    } else {
      return defaultValue.toLowerCase();
    }
  }

  /**
   Format a stack trace in carriage-return-separated lines

   @param e exception to format the stack trace of
   @return formatted stack trace
   */
  static String formatStackTrace(Exception e) {
    StackTraceElement[] stack = e.getStackTrace();
    String[] stackLines = new String[stack.length];
    for (int i = 0; i < stack.length; i++)
      stackLines[i] = stack[i].toString();
    return String.join("\n", stackLines);
  }

}
