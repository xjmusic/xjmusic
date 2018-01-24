// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.digest.meme;

import io.xj.core.digest.Digest;
import io.xj.core.digest.meme.impl.DigestMemesItem;

import java.util.Map;

public interface DigestMeme extends Digest {

  /**
   Get a map of all digested memes
   @return map of digested memes
   */
  Map<String, DigestMemesItem> getMemes();

}
