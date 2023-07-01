package io.xj.hub;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.services.plus.model.Person;
import io.xj.hub.access.GoogleProvider;
import io.xj.hub.access.HubAccessException;
import io.xj.hub.access.HubAccessTokenGenerator;
import io.xj.hub.access.HubAccessTokenGeneratorImpl;
import io.xj.hub.persistence.HubMigration;
import io.xj.hub.persistence.HubMigrationImpl;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.persistence.HubSqlStoreProviderImpl;
import io.xj.hub.persistence.kv.HubKvStoreProvider;
import io.xj.hub.persistence.kv.HubKvStoreProviderImpl;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import io.xj.lib.jsonapi.JsonapiResponseProviderImpl;

public interface HubIntegrationTestFactory {
  HubIntegrationTest build();
}

