// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest.meme;

import io.xj.craft.digest.Digest;
import io.xj.craft.digest.meme.impl.DigestMemesItem;

import java.util.Map;

public interface DigestMeme extends Digest {

  /**
   Get a map of all digested memes
   @return map of digested memes
   */
  Map<String, DigestMemesItem> getMemes();

}
