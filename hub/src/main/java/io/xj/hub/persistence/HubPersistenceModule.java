// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.persistence;

import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.inject.AbstractModule;

public class HubPersistenceModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(DataStoreFactory.class).to(MemoryDataStoreFactory.class); // required by Google OAuth flow
    bind(HubMigration.class).to(HubMigrationImpl.class);
    bind(HubRedisProvider.class).to(HubRedisProviderImpl.class);
    bind(HubDatabaseProvider.class).to(HubDatabaseProviderImpl.class);
  }
}
