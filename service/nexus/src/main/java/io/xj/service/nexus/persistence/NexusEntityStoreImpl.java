// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.nexus.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityStoreImpl;

/**
 Implementation of Nexus service record store
 <p>
 [#171553408] XJ Mk3 Distributed Architecture
 Chains, ChainBindings, ChainConfigs, Segments and all Segment content sub-entities persisted in JSON:API record stored keyed by chain or segment id in memory
 */
@Singleton
public class NexusEntityStoreImpl extends EntityStoreImpl implements NexusEntityStore {
  @Inject
  public NexusEntityStoreImpl(
    EntityFactory entityFactory
  ) {
    super();
  }
}
