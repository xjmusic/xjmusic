// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.ingest;

import io.xj.lib.core.access.Access;
import io.xj.lib.core.model.ChainBinding;

import com.google.inject.assistedinject.Assisted;
import io.xj.lib.core.exception.CoreException;

import java.util.Collection;

/**
 [#154350346] Architect wants a universal Ingest Factory, to modularize graph mathematics used during craft to ingest any combination of Library, Sequence, and Instrument for any purpose.
 Ingest ingest = evaluationFactory.of(...any combination of libraries, instruments, and sequences...);
 */
@FunctionalInterface
public interface IngestFactory {

  /**
   Evaluate any combination of Entities for ingest. Assumes inclusion of child entities of all entities provided

   @param access control
   @return entities to be evaluated
   @throws CoreException on failure to of target entities
   */
  Ingest ingest(
    @Assisted("access") Access access,
    @Assisted("bindings") Collection<ChainBinding> bindings
  ) throws CoreException;
}
