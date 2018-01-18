// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.cache.evaluation.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.cache.CacheKey;
import io.xj.core.cache.evaluation.EvaluationCacheProvider;
import io.xj.core.evaluation.Evaluation;
import io.xj.core.evaluation.EvaluationFactory;
import io.xj.core.model.entity.Entity;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Collection;
import java.util.Map;

@Singleton
public class EvaluationCacheProviderImpl implements EvaluationCacheProvider {
  private final Map<String, EvaluationCacheItem> cachedEvaluationMap = Maps.newConcurrentMap();
  private final EvaluationFactory evaluationFactory;

  @Inject
  public EvaluationCacheProviderImpl(
    EvaluationFactory evaluationFactory
  ) {
    this.evaluationFactory = evaluationFactory;
  }

  @Override
  public Evaluation evaluate(Access access, Collection<Entity> entities) throws Exception {
    prune();

    String cacheKey = CacheKey.of(access, entities);

    if (!cachedEvaluationMap.containsKey(cacheKey)) {
      Evaluation evaluation = evaluationFactory.evaluate(access, entities);
      cachedEvaluationMap.put(cacheKey, new EvaluationCacheItem(evaluation));
    }

    return cachedEvaluationMap.get(cacheKey).getEvaluation();
  }

  /**
   Prune expired entries from cache
   */
  private void prune() {
    cachedEvaluationMap.forEach((key, evaluationCacheItem) -> {
      if (!evaluationCacheItem.isValid())
        cachedEvaluationMap.remove(key);
    });
  }

}
