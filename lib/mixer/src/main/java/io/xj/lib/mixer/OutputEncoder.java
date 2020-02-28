// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;

import java.util.Locale;

public enum OutputEncoder {
  WAV,
  OGG,
  AAC;

  /**
   Parse any object to determine if it's a known output encoding, else use default encoding

   @param obj to parse
   @return output encoding
   */
  public static OutputEncoder parse(Object obj) {
    String encoder = String.valueOf(obj).toLowerCase(Locale.ENGLISH).trim();
    switch (encoder) {

      case "wave":
      case "wav":
        return WAV;

      case "ogg":
        return OGG;

      default:
        return AAC;
    }
  }
}
