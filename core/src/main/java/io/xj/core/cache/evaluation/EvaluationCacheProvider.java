// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.cache.evaluation;

import io.xj.core.access.impl.Access;
import io.xj.core.evaluation.digest_chords.DigestChords;
import io.xj.core.evaluation.digest_memes.DigestMemes;

import java.math.BigInteger;

public interface EvaluationCacheProvider {

  /**
   Perform meme evaluation of target library and return resulting JSON Object
   CACHE: results until N seconds have transpired (from system property) *and* library hash changes

   @param access    control
   @param libraryId of target library to evaluate memes of
   @return evaluation results as JSON object
   */
  DigestMemes libraryMemes(Access access, BigInteger libraryId) throws Exception;

  /**
   Perform chord evaluation of target library and return resulting JSON Object
   CACHE: results until N seconds have transpired (from system property) *and* library hash changes

   @param access    control
   @param libraryId of target library to evaluate chords of
   @return evaluation results as JSON object
   */
  DigestChords libraryChords(Access access, BigInteger libraryId) throws Exception;

}
