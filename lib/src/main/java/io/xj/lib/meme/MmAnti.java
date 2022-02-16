// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.meme;

import com.google.common.base.Objects;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Anti-Memes
 <p>
 Artist can add !MEME values into Programs #176474073
 */
class MmAnti {
  private static final Pattern rgx = Pattern.compile("^!(.+)$");
  final String body;
  final boolean isValid;

  private MmAnti(String raw) {
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

  public static MmAnti fromString(String raw) {
    return new MmAnti(raw);
  }

  public boolean isViolatedBy(MmAnti target) {
    return (isValid && !target.isValid && Objects.equal(body, target.body)) ||
      (!isValid && target.isValid && Objects.equal(body, target.body));
  }

  public boolean isAllowed(List<MmAnti> memes) {
    for (var meme : memes)
      if (isViolatedBy(meme)) return false;
    return true;
  }
}
