// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub;

import io.xj.hub.access.GoogleProvider;
import io.xj.hub.access.HubAccessTokenGenerator;
import io.xj.hub.persistence.HubMigration;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.persistence.kv.HubKvStoreProvider;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HubIntegrationTestFactoryImpl implements HubIntegrationTestFactory {
  final ApiUrlProvider apiUrlProvider;
  final EntityFactory entityFactory;
  final GoogleProvider googleProvider;
  final HubAccessTokenGenerator hubAccessTokenGenerator;
  final HubKvStoreProvider kvStoreProvider;
  final HubMigration hubMigration;
  final HubSqlStoreProvider sqlStoreProvider;
  final JsonapiResponseProvider httpResponseProvider;
  final JsonapiPayloadFactory jsonapiPayloadFactory;

  @Autowired
  public HubIntegrationTestFactoryImpl(
    ApiUrlProvider apiUrlProvider,
    EntityFactory entityFactory,
    GoogleProvider googleProvider,
    HubAccessTokenGenerator hubAccessTokenGenerator,
    HubKvStoreProvider kvStoreProvider,
    HubMigration hubMigration,
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider httpResponseProvider,
    JsonapiPayloadFactory jsonapiPayloadFactory
  ) {
    this.apiUrlProvider = apiUrlProvider;
    this.entityFactory = entityFactory;
    this.googleProvider = googleProvider;
    this.hubAccessTokenGenerator = hubAccessTokenGenerator;
    this.kvStoreProvider = kvStoreProvider;
    this.hubMigration = hubMigration;
    this.sqlStoreProvider = sqlStoreProvider;
    this.httpResponseProvider = httpResponseProvider;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
  }

  @Override
  public HubIntegrationTest build() {
    return new HubIntegrationTest(
      apiUrlProvider,
      entityFactory,
      googleProvider,
      hubAccessTokenGenerator,
      kvStoreProvider,
      hubMigration,
      sqlStoreProvider,
      httpResponseProvider,
      jsonapiPayloadFactory
    );
  }
}
