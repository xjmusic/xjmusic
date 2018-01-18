// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.evaluation.digest.memes;

import io.xj.core.evaluation.digest.Digest;
import io.xj.core.evaluation.digest.memes.impl.DigestMemesItem;

import java.util.Map;

public interface DigestMemes extends Digest {

  /**
   Get a map of all digested memes
   @return map of digested memes
   */
  Map<String, DigestMemesItem> getMemes();

}
