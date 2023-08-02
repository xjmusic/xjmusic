// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.meme;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Meme Matcher for Strong-Memes
 * <p>
 * Parse any meme to test if it's valid, and extract its features
 * <p>
 * Strong-meme like LEMONS! should always favor LEMONS https://www.pivotaltracker.com/story/show/180468772
 */
class ParseStrong {
  static final Pattern rgx = Pattern.compile("^(.+)!$");
  final String body;
  final boolean isValid;

  ParseStrong(String raw) {
    Matcher matcher = rgx.matcher(raw);

    if (!matcher.find()) {
      body = raw;
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

  public static ParseStrong fromString(String raw) {
    return new ParseStrong(raw);
  }

  public boolean isAllowed(List<ParseStrong> memes) {
    if (!isValid) return true;
    for (var meme : memes)
      if (Objects.equals(body, meme.body)) return true;
    return false;
  }
}
