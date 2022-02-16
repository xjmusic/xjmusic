// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.meme;

import com.google.common.base.Objects;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Parse any meme to test if it's a numeric meme, and extract its features
 <p>
 Numeric memes with common letters and different integer prefix (e.g. 2STEP vs 4STEP) are known to be exclusive #180125852
 */
class MmNumeric {
  private static final Pattern rgx = Pattern.compile("^([0-9]+)(.+)$");
  final String body;
  final Integer prefix;
  final boolean isValid;

  private MmNumeric(String raw) {
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

  public static MmNumeric fromString(String raw) {
    return new MmNumeric(raw);
  }

  public boolean isViolatedBy(MmNumeric target) {
    return
      isValid && target.isValid
        && Objects.equal(body, target.body)
        && !Objects.equal(prefix, target.prefix);
  }

  public boolean isAllowed(List<MmNumeric> memes) {
    for (var meme : memes)
      if (isViolatedBy(meme)) return false;
    return true;
  }
}
