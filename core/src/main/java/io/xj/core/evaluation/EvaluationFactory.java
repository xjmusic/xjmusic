// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.evaluation;

import io.xj.core.access.impl.Access;
import io.xj.core.model.entity.Entity;

import com.google.inject.assistedinject.Assisted;

import java.util.Collection;

/**
 [#154350346] Architect wants a universal Evaluation Factory, to modularize graph mathematics used during craft to evaluate any combination of Library, Pattern, and Instrument for any purpose.
 Evaluation evaluation = evaluationFactory.of(...any combination of libraries, instruments, and patterns...);
 */
@FunctionalInterface
public interface EvaluationFactory {

  /**
   Evaluate any combination of Entities for evaluation. Assumes inclusion of child entities of all entities provided

   @param access control
   @return entities to be evaluated
   @throws Exception on failure to of target entities
   */
  Evaluation evaluate(
    @Assisted("access") Access access,
    @Assisted("entities") Collection<Entity> entities
  ) throws Exception;
}
