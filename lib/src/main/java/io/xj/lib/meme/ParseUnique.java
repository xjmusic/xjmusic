// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.meme;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Meme Matcher for Unique-Memes
 * <p>
 * Parse any meme to test if it's valid, and extract its features
 * <p>
 * Artist can add `$MEME` so only one is chosen https://www.pivotaltracker.com/story/show/179078760
 */
class ParseUnique {
  static final Pattern rgx = Pattern.compile("^\\$(.+)$");
  final String body;
  final boolean isValid;

  ParseUnique(String raw) {
    Matcher matcher = rgx.matcher(raw);

    if (!matcher.find()) {
      body = null;
      isValid = false;
      return;
    }

    body = matcher.group(1);
    if (java.util.Objects.isNull(body) || body.length() == 0) {
      isValid = false;
      return;
    }

    isValid = true;
  }

  public static ParseUnique fromString(String raw) {
    return new ParseUnique(raw);
  }

  public boolean isViolatedBy(ParseUnique target) {
    return isValid && target.isValid && Objects.equals(body, target.body);
  }

  public boolean isAllowed(List<ParseUnique> memes) {
    for (var meme : memes)
      if (isViolatedBy(meme)) return false;
    return true;
  }
}
