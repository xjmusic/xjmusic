// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.meme;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Meme Matcher for Numeric Memes
 * <p>
 * Parse any meme to test if it's valid, and extract its features
 * <p>
 * Numeric memes with common letters and different integer prefix (e.g. 2STEP vs 4STEP) are known to be exclusive https://www.pivotaltracker.com/story/show/180125852
 */
class ParseNumeric {
  static final Pattern rgx = Pattern.compile("^([0-9]+)(.+)$");
  final String body;
  final Integer prefix;
  final boolean isValid;

  ParseNumeric(String raw) {
    Matcher matcher = rgx.matcher(raw);

    if (!matcher.find()) {
      prefix = null;
      body = null;
      isValid = false;
      return;
    }

    String pfx = matcher.group(1);
    if (java.util.Objects.isNull(pfx) || pfx.length() == 0) {
      prefix = null;
      body = null;
      isValid = false;
      return;
    }
    prefix = Integer.valueOf(pfx);

    body = matcher.group(2);
    if (java.util.Objects.isNull(body) || body.length() == 0) {
      isValid = false;
      return;
    }

    isValid = true;
  }

  public static ParseNumeric fromString(String raw) {
    return new ParseNumeric(raw);
  }

  public boolean isViolatedBy(ParseNumeric target) {
    return
      isValid && target.isValid
        && Objects.equals(body, target.body)
        && !Objects.equals(prefix, target.prefix);
  }

  public boolean isAllowed(List<ParseNumeric> memes) {
    for (var meme : memes)
      if (isViolatedBy(meme)) return false;
    return true;
  }
}
