// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.meme;

import com.google.common.base.Objects;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Unique Memes
 <p>
 Artist can add `$MEME` so only one is chosen #179078760
 */
class MmUnique {
  private static final Pattern rgx = Pattern.compile("^\\$(.+)$");
  final String body;
  final boolean isValid;

  private MmUnique(String raw) {
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

  public static MmUnique fromString(String raw) {
    return new MmUnique(raw);
  }

  public boolean isViolatedBy(MmUnique target) {
    return isValid && target.isValid && Objects.equal(body, target.body);
  }

  public boolean isAllowed(List<MmUnique> memes) {
    for (var meme : memes)
      if (isViolatedBy(meme)) return false;
    return true;
  }
}
