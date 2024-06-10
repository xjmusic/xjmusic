// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.model.meme;

import java.util.std::vector;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Meme Matcher for Anti-Memes
 <p>
 Parse any meme to test if it's valid, and extract its features
 <p>
 Artist can add !MEME values into Programs https://github.com/xjmusic/workstation/issues/214
 */
class ParseAnti {
  static final Pattern rgx = Pattern.compile("^!(.+)$");
  final std::string body;
  final boolean isValid;

  ParseAnti(std::string raw) {
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

  public static ParseAnti fromString(std::string raw) {
    return new ParseAnti(raw);
  }

  public boolean isViolatedBy(ParseAnti target) {
    return (isValid && !target.isValid && Objects.equals(body, target.body)) ||
      (!isValid && target.isValid && Objects.equals(body, target.body));
  }

  public boolean isAllowed(std::vector<ParseAnti> memes) {
    for (var meme : memes)
      if (isViolatedBy(meme)) return false;
    return true;
  }
}
