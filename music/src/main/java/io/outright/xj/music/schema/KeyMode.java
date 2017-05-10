// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.music.schema;

import java.util.regex.Pattern;

/**
 Key has a Mode, e.g. Major or Minor
 <p>
 Mode is the mode of a key, e.g. Major or Minor
 */
public enum KeyMode {
  None,
  Major,
  Minor;

  // Default mode, when not specified
  static final KeyMode Default = Major;

  // regular expression to in major key
  static final Pattern rgxMajor = Pattern.compile("^(M|maj|major)");

  // regular expression to in minor key
  static final Pattern rgxMinor = Pattern.compile("^(m\\b|min|minor|Minor)");

  /**
   get a named mode

   @param name of mode
   @return mode
   */
  public static KeyMode of(String name) {
    if (rgxMinor.matcher(name.trim()).find())
      return Minor;
    else if (rgxMajor.matcher(name.trim()).find())
      return Major;
    else
      return Default;
  }

}

