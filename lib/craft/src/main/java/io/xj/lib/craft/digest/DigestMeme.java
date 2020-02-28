// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.craft.digest;

import java.util.Map;

public interface DigestMeme extends Digest {

  /**
   Get a map of all digested memes

   @return map of digested memes
   */
  Map<String, DigestMemeImpl.DigestMemesItem> getMemes();

}
