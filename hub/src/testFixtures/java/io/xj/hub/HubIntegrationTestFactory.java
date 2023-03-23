package io.xj.hub;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.services.plus.model.Person;
import io.xj.hub.access.GoogleProvider;
import io.xj.hub.access.HubAccessException;
import io.xj.hub.access.HubAccessTokenGenerator;
import io.xj.hub.access.HubAccessTokenGeneratorImpl;
import io.xj.hub.persistence.HubKvStoreProvider;
import io.xj.hub.persistence.HubKvStoreProviderRedisImpl;
import io.xj.hub.persistence.HubMigration;
import io.xj.hub.persistence.HubMigrationImpl;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.persistence.HubSqlStoreProviderImpl;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import io.xj.lib.jsonapi.JsonapiResponseProviderImpl;

public class HubIntegrationTestFactory {
  public static HubIntegrationTest build(AppEnvironment env) {
    JsonProviderImpl jsonProvider = new JsonProviderImpl();
    EntityFactory entityFactory = new EntityFactoryImpl(jsonProvider);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    ApiUrlProvider apiUrlProvider = new ApiUrlProvider(env);
    JsonapiResponseProvider httpResponseProvider = new JsonapiResponseProviderImpl(apiUrlProvider);
    GoogleProvider googleProvider = new FakeGoogleProvider();
    HubAccessTokenGenerator hubAccessTokenGenerator = new HubAccessTokenGeneratorImpl();
    HubKvStoreProvider kvStoreProvider = new HubKvStoreProviderRedisImpl(env, entityFactory);
    HubSqlStoreProvider sqlStoreProvider = new HubSqlStoreProviderImpl(env);
    HubMigration hubMigration = new HubMigrationImpl(sqlStoreProvider);
    return new HubIntegrationTest(
      env,
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

  private static class FakeGoogleProvider implements GoogleProvider {
    @Override
    public String getAuthCodeRequestUrl() throws HubAccessException {
      throw new HubAccessException("Not implemented");
    }

    @Override
    public String getCallbackUrl() throws HubAccessException {
      throw new HubAccessException("Not implemented");
    }

    @Override
    public GoogleTokenResponse getTokenFromCode(String code) throws HubAccessException {
      throw new HubAccessException("Not implemented");
    }

    @Override
    public Person getMe(String access_token) throws HubAccessException {
      throw new HubAccessException("Not implemented");
    }
  }
}

