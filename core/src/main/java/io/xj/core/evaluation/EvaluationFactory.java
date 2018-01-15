// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.evaluation;

import io.xj.core.access.impl.Access;
import io.xj.core.model.Entity;

import com.google.inject.assistedinject.Assisted;

import java.util.Collection;

/**
 Evaluation evaluation = evaluationFactory.of(...any combination of libraries, instruments, and patterns...);
 */
@FunctionalInterface
public interface EvaluationFactory {

  /**
   Of any combination of Entities for evaluation. Assumes inclusion of child entities of all entities provided

   @param access control
   @return entities to be evaluated
   @throws Exception on failure to of target entities
   */
  Evaluation of(
    @Assisted("access") Access access,
    @Assisted("entities") Collection<Entity> entities
  ) throws Exception;
}
