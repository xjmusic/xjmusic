// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.cache.evaluation.impl;

import io.xj.core.config.Config;
import io.xj.core.evaluation.Evaluation;
import io.xj.core.timestamp.TimestampUTC;

import java.sql.Timestamp;

/**
 Evaluate any combination of Entities for evaluation. Assumes inclusion of child entities of all entities provided
 CACHES the result for any access+entities signature, for N seconds.
 Where N is configurable in system properties `evaluation.cache.seconds`
 */
public class EvaluationCacheItem {
  private final Timestamp createdAt;
  private final Evaluation evaluation;

  /**
   Create a new cached evaluation.

   @param evaluation to cache
   */
  public EvaluationCacheItem(Evaluation evaluation) {
    this.evaluation = evaluation;
    createdAt = TimestampUTC.now();
  }

  /**
   Whether this cached evaluation is valid (NOT expired) because N seconds have not yet transpired since it was cached.
   Where N is configurable in system properties `evaluation.cache.seconds`

   @return true if expired
   */
  public Boolean isValid() {
    return TimestampUTC.now().toInstant().getEpochSecond() <
      createdAt.toInstant().getEpochSecond() + Config.evaluationCacheSeconds();
  }

  /**
   Get the evaluation

   @return evaluation
   */
  public Evaluation getEvaluation() {
    return evaluation;
  }

}
