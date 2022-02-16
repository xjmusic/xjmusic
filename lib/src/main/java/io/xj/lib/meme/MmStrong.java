// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.meme;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Parse any meme to test if it's a strong meme, and extract its features
 <p>
 Strong-meme like LEMONS! should always favor LEMONS #180468772
 */
class MmStrong {
  private static final Pattern rgx = Pattern.compile("^(.+)!$");
  final String body;
  final boolean isValid;

  private MmStrong(String raw) {
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

  public static MmStrong fromString(String raw) {
    return new MmStrong(raw);
  }

  public boolean isAllowed(List<MmStrong> memes) {
    if (!isValid) return true;
    for (var meme : memes)
      if (Objects.equals(body, meme.body)) return true;
    return false;
  }
}
