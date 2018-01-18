// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.cache.evaluation;

import io.xj.core.access.impl.Access;
import io.xj.core.evaluation.Evaluation;
import io.xj.core.model.entity.Entity;

import java.util.Collection;

/**
 [#154350346] Architect wants a universal Evaluation Factory, to modularize graph mathematics used during craft to evaluate any combination of Library, Pattern, and Instrument for any purpose.
 Evaluation evaluation = evaluationFactory.of(...any combination of libraries, instruments, and patterns...);
 */
@FunctionalInterface
public interface EvaluationCacheProvider {

  /**
   Evaluate any combination of Entities for evaluation. Assumes inclusion of child entities of all entities provided
   CACHES the result for any access+entities signature, for N seconds.
   Where N is configurable in system properties `evaluation.cache.seconds`

   @param access control
   @return entities to be evaluated
   @throws Exception on failure to of target entities
   */
  Evaluation evaluate(Access access, Collection<Entity> entities) throws Exception;
}
