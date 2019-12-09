// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.persistence;

import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.inject.AbstractModule;
import io.xj.core.persistence.redis.RedisDatabaseProvider;
import io.xj.core.persistence.redis.impl.RedisDatabaseProviderImpl;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLDatabaseProviderImpl;
import io.xj.core.persistence.sql.migration.Migration;
import io.xj.core.persistence.sql.migration.impl.MigrationImpl;

public class PersistenceModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(DataStoreFactory.class).to(MemoryDataStoreFactory.class);
    bind(Migration.class).to(MigrationImpl.class);
    bind(RedisDatabaseProvider.class).to(RedisDatabaseProviderImpl.class);
    bind(SQLDatabaseProvider.class).to(SQLDatabaseProviderImpl.class);
  }
}
