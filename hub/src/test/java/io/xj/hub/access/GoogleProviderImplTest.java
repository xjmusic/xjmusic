// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.services.plus.model.Person;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.hub.dao.DAOModule;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.HubTestConfiguration;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class GoogleProviderImplTest extends Mockito {
  @Mock
  private GoogleHttpProvider googleHttpProvider;
  private GoogleProvider googleProvider;

  @Before
  public void setUp() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    var env = Environment.from(ImmutableMap.of(
      "GOOGLE_CLIENT_ID","12345",
      "GOOGLE_CLIENT_SECRET","ab1cd2ef3",
      "APP_BASE_URL","http://shammy/"
    ));
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(),
      new FileStoreModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Config.class).toInstance(config);
        bind(Environment.class).toInstance(env);
        bind(GoogleProvider.class).to(GoogleProviderImpl.class);
        bind(GoogleHttpProvider.class).toInstance(googleHttpProvider);
        bind(JsonFactory.class).to(JacksonFactory.class);
      }
    }));
    googleProvider = injector.getInstance(GoogleProvider.class);
  }

  @Test
  public void getAuthCodeRequestUrl() throws Exception {
    String url = googleProvider.getAuthCodeRequestUrl();
    assertEquals("https://accounts.google.com/o/oauth2/auth" +
      "?client_id=12345" +
      "&redirect_uri=http://shammy/auth/google/callback" +
      "&response_type=code" +
      "&scope=profile%20email" +
      "&state=xj-music", url);
  }

  @Test
  public void getTokenFromCode() throws Exception {
    String responseJson = "{" +
      "\"access_token\":\"12345\"," +
      "\"refresh_token\":\"ab1cd2ef3\"" +
      "}";
    MockLowLevelHttpResponse httpResponse = new MockLowLevelHttpResponse();
    httpResponse.setStatusCode(200);
    httpResponse.setContent(responseJson);
    HttpTransport httpTransport = new MockHttpTransport.Builder()
      .setLowLevelHttpResponse(httpResponse)
      .build();
    when(googleHttpProvider.getTransport())
      .thenReturn(httpTransport);

    GoogleTokenResponse tokenResponse = googleProvider.getTokenFromCode("red");

    assertEquals("12345", tokenResponse.getAccessToken());
    assertEquals("ab1cd2ef3", tokenResponse.getRefreshToken());
  }

  @Test(expected = HubAccessException.class)
  public void getTokenFromCode_IOFailure() throws Exception {
    String responseJson = "garbage response will cause IO failure";
    MockLowLevelHttpResponse httpResponse = new MockLowLevelHttpResponse();
    httpResponse.setStatusCode(200);
    httpResponse.setContent(responseJson);
    HttpTransport httpTransport = new MockHttpTransport.Builder()
      .setLowLevelHttpResponse(httpResponse)
      .build();
    when(googleHttpProvider.getTransport())
      .thenReturn(httpTransport);

    googleProvider.getTokenFromCode("red");
  }

  @Test(expected = HubAccessException.class)
  public void getTokenFromCode_TokenResponseFailure() throws Exception {
    String responseJson = "{\"details\":{" +
      "\"error_description\":\"terrible\"," +
      "\"error_uri\":\"http://takealap.com/\"" +
      "}}";
    MockLowLevelHttpResponse httpResponse = new MockLowLevelHttpResponse();
    httpResponse.setStatusCode(500);
    httpResponse.setContent(responseJson.getBytes());
    HttpTransport httpTransport = new MockHttpTransport.Builder()
      .setLowLevelHttpResponse(httpResponse)
      .build();
    when(googleHttpProvider.getTransport())
      .thenReturn(httpTransport);

    googleProvider.getTokenFromCode("red");
  }

  @Test
  public void getMe() throws Exception {
    String responseJson = "{" +
      "  \"kind\": \"plus#person\"," +
      "  \"etag\": \"\\\"FT7X6cYw9BSnPtIywEFNNGVVdio/VUQf6-pycKq5jJriA2orbVoK42g\\\"\"," +
      "  \"emails\": [" +
      "    {" +
      "      \"value\": \"charneykaye@gmail.com\"," +
      "      \"type\": \"account\"" +
      "    }" +
      "  ]," +
      "  \"objectType\": \"person\"," +
      "  \"id\": \"189644347861137716193\"," +
      "  \"displayName\": \"Charney Kaye\"," +
      "  \"name\": {" +
      "    \"familyName\": \"Kaye\"," +
      "    \"givenName\": \"Charney\"" +
      "  }," +
      "  \"url\": \"https://plus.google.com/189644347861137716193\"," +
      "  \"image\": {" +
      "    \"url\": \"https://lh6.googleusercontent.com/-uVJxoVmL42M/AAAAAAAAAAI/AAAAAAAAACg/kMWD0kJityM/photo.jpg?sz=50\"," +
      "    \"isDefault\": false" +
      "  }," +
      "  \"isPlusUser\": true," +
      "  \"language\": \"en\"," +
      "  \"circledByCount\": 0," +
      "  \"verified\": false," +
      "  \"cover\": {" +
      "    \"layout\": \"banner\"," +
      "    \"coverPhoto\": {" +
      "      \"url\": \"https://lh3.googleusercontent.com/-bk0tJruxTGxQYwSPPN_Mub70bYsSbnsRqnJvza3WhV-k9B81D0zAyeeshiyW40StSQjfls=s630-fcrop64=1,00000000ffffffff\"," +
      "      \"height\": 528," +
      "      \"width\": 940" +
      "    }," +
      "    \"coverInfo\": {" +
      "      \"topImageOffset\": 0," +
      "      \"leftImageOffset\": 0" +
      "    }" +
      "  }" +
      "}";
    MockLowLevelHttpResponse httpResponse = new MockLowLevelHttpResponse();
    httpResponse.setStatusCode(200);
    httpResponse.setContent(responseJson);
    HttpTransport httpTransport = new MockHttpTransport.Builder()
      .setLowLevelHttpResponse(httpResponse)
      .build();
    when(googleHttpProvider.getTransport())
      .thenReturn(httpTransport);

    Person person = googleProvider.getMe("12345");

    assertEquals("189644347861137716193", person.getId());
    assertEquals("Charney Kaye", person.getDisplayName());
    assertEquals("https://lh6.googleusercontent.com/-uVJxoVmL42M/AAAAAAAAAAI/AAAAAAAAACg/kMWD0kJityM/photo.jpg?sz=50", person.getImage().getUrl());
    assertEquals("charneykaye@gmail.com", person.getEmails().get(0).getValue());
  }

  @Test(expected = HubAccessException.class)
  public void getMe_IOFailure() throws Exception {
    MockLowLevelHttpResponse httpResponse = new MockLowLevelHttpResponse();
    httpResponse.setStatusCode(500);
    HttpTransport httpTransport = new MockHttpTransport.Builder()
      .setLowLevelHttpResponse(httpResponse)
      .build();
    when(googleHttpProvider.getTransport())
      .thenReturn(httpTransport);

    googleProvider.getMe("12345");
  }

  @Test(expected = HubAccessException.class)
  public void getMe_ResponseJSONFailure() throws Exception {
    String responseJson = "this ain't JSON";
    MockLowLevelHttpResponse httpResponse = new MockLowLevelHttpResponse();
    httpResponse.setStatusCode(200);
    httpResponse.setContent(responseJson);
    HttpTransport httpTransport = new MockHttpTransport.Builder()
      .setLowLevelHttpResponse(httpResponse)
      .build();
    when(googleHttpProvider.getTransport())
      .thenReturn(httpTransport);

    googleProvider.getMe("12345");
  }

}
