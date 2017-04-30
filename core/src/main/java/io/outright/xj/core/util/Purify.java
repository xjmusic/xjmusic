// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.util;

import java.util.regex.Pattern;

public interface Purify {
  Pattern space = Pattern.compile("[ ]+");
  Pattern nonSlug = Pattern.compile("[^a-zA-Z]");
  Pattern nonScored = Pattern.compile("[^a-zA-Z_]");
  Pattern nonNote = Pattern.compile("[^#0-9a-zA-Z ]");
  String UNDERSCORE = "_";
  String NOTHING = "";

  static String Note(String raw) {
    return nonNote.matcher(raw)
      .replaceAll("").trim();
  }

  static String Slug(String raw) {
    return nonSlug.matcher(raw)
      .replaceAll(NOTHING);
  }

  static String Slug(String raw, String defaultValue) {
    String slug = Slug(raw);
    return slug.length() > 0 ? slug : defaultValue;
  }

  static String toProper(String from) {
    if (from.length() > 1) {
      String lower = from.toLowerCase();
      return lower.substring(0, 1).toUpperCase() + lower.substring(1);
    } else if (from.length() > 0) {
      return from.toUpperCase();
    } else {
      return "";
    }
  }

  static String ProperSlug(String raw) {
    return toProper(Slug(raw));
  }

  static String ProperSlug(String raw, String defaultValue) {
    return toProper(Slug(raw, defaultValue));
  }

  static String UpperSlug(String raw) {
    return Slug(raw).toUpperCase();
  }

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

  static String LowerSlug(String raw) {
    return Slug(raw).toLowerCase();
  }

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

  static String Scored(String raw) {
    return
      nonScored.matcher(
        space.matcher(
          raw.trim()
        ).replaceAll(UNDERSCORE)
      ).replaceAll(NOTHING);
  }

  static String Scored(String raw, String defaultValue) {
    String scored = Scored(raw);
    return scored.length() > 0 ? scored : defaultValue;
  }

  static String UpperScored(String raw) {
    return Scored(raw).toUpperCase();
  }

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

  static String LowerScored(String raw) {
    return Scored(raw).toLowerCase();
  }

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
}
