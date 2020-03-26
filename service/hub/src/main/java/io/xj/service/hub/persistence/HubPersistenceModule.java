// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.persistence;

import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.inject.AbstractModule;

public class HubPersistenceModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(AmazonProvider.class).to(AmazonProviderImpl.class);
    bind(DataStoreFactory.class).to(MemoryDataStoreFactory.class);
    bind(Migration.class).to(MigrationImpl.class);
    bind(RedisDatabaseProvider.class).to(RedisDatabaseProviderImpl.class);
    bind(SQLDatabaseProvider.class).to(SQLDatabaseProviderImpl.class);
  }
}
