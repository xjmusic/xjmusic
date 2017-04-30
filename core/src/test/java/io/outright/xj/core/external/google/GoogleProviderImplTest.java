// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.external.google;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.exception.AccessException;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.services.plus.model.Person;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class GoogleProviderImplTest extends Mockito {
  @Mock private GoogleHttpProvider googleHttpProvider;
  private Injector injector;
  private GoogleProvider googleProvider;

  @Before
  public void setUp() throws Exception {
    System.setProperty("auth.google.id", "12345");
    System.setProperty("auth.google.secret", "abcdefg");
    System.setProperty("app.url.base", "http://shammy/");
    System.setProperty("app.url.api", "api/69/");

    createInjector();
    googleProvider = injector.getInstance(GoogleProvider.class);
  }

  @After
  public void tearDown() throws Exception {
    googleProvider = null;
    System.clearProperty("auth.google.id");
    System.clearProperty("auth.google.secret");
    System.clearProperty("app.url.base");
    System.clearProperty("app.url.api");
  }

  @Test
  public void getAuthCodeRequestUrl() throws Exception {
    String url = googleProvider.getAuthCodeRequestUrl();
    assertEquals(url, "https://accounts.google.com/o/oauth2/auth" +
      "?client_id=12345" +
      "&redirect_uri=http://shammy/api/69/auth/google/callback" +
      "&response_type=code" +
      "&scope=profile%20email" +
      "&state=xj-music");
  }

  @Test
  public void getTokenFromCode() throws Exception {
    String responseJson = "{" +
      "\"access_token\":\"12345\"," +
      "\"refresh_token\":\"abcdef\"" +
      "}";
    MockLowLevelHttpResponse httpResponse = new MockLowLevelHttpResponse();
    httpResponse.setStatusCode(200);
    httpResponse.setContent(responseJson);
    HttpTransport httpTransport = new MockHttpTransport.Builder()
      .setLowLevelHttpResponse(httpResponse).build();
    when(googleHttpProvider.getTransport())
      .thenReturn(httpTransport);

    GoogleTokenResponse tokenResponse = googleProvider.getTokenFromCode("red");

    assertEquals("12345", tokenResponse.getAccessToken());
    assertEquals("abcdef", tokenResponse.getRefreshToken());
  }

  @Test(expected = AccessException.class)
  public void getTokenFromCode_IOFailure() throws Exception {
    String responseJson = "garbage response will cause IO failure";
    MockLowLevelHttpResponse httpResponse = new MockLowLevelHttpResponse();
    httpResponse.setStatusCode(200);
    httpResponse.setContent(responseJson);
    HttpTransport httpTransport = new MockHttpTransport.Builder()
      .setLowLevelHttpResponse(httpResponse).build();
    when(googleHttpProvider.getTransport())
      .thenReturn(httpTransport);

    googleProvider.getTokenFromCode("red");
  }

  @Test(expected = AccessException.class)
  public void getTokenFromCode_TokenResponseFailure() throws Exception {
    String responseJson = "{\"details\":{" +
      "\"error_description\":\"terrible\"," +
      "\"error_uri\":\"http://takealap.com/\"" +
      "}}";
    MockLowLevelHttpResponse httpResponse = new MockLowLevelHttpResponse();
    httpResponse.setStatusCode(500);
    httpResponse.setContent(responseJson.getBytes());
    HttpTransport httpTransport = new MockHttpTransport.Builder()
      .setLowLevelHttpResponse(httpResponse).build();
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
      .setLowLevelHttpResponse(httpResponse).build();
    when(googleHttpProvider.getTransport())
      .thenReturn(httpTransport);

    Person person = googleProvider.getMe("12345");

    assertEquals("189644347861137716193", person.getId());
    assertEquals("Charney Kaye", person.getDisplayName());
    assertEquals("https://lh6.googleusercontent.com/-uVJxoVmL42M/AAAAAAAAAAI/AAAAAAAAACg/kMWD0kJityM/photo.jpg?sz=50", person.getImage().getUrl());
    assertEquals("charneykaye@gmail.com", person.getEmails().get(0).getValue());
  }

  @Test(expected = AccessException.class)
  public void getMe_IOFailure() throws Exception {
    MockLowLevelHttpResponse httpResponse = new MockLowLevelHttpResponse();
    httpResponse.setStatusCode(500);
    HttpTransport httpTransport = new MockHttpTransport.Builder()
      .setLowLevelHttpResponse(httpResponse).build();
    when(googleHttpProvider.getTransport())
      .thenReturn(httpTransport);

    googleProvider.getMe("12345");
  }

  @Test(expected = AccessException.class)
  public void getMe_ResponseJSONFailure() throws Exception {
    String responseJson = "this ain't JSON";
    MockLowLevelHttpResponse httpResponse = new MockLowLevelHttpResponse();
    httpResponse.setStatusCode(200);
    httpResponse.setContent(responseJson);
    HttpTransport httpTransport = new MockHttpTransport.Builder()
      .setLowLevelHttpResponse(httpResponse).build();
    when(googleHttpProvider.getTransport())
      .thenReturn(httpTransport);

    googleProvider.getMe("12345");
  }

  private void createInjector() {
    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(GoogleProvider.class).to(GoogleProviderImpl.class);
          bind(GoogleHttpProvider.class).toInstance(googleHttpProvider);
          bind(JsonFactory.class).to(JacksonFactory.class);
        }
      }));
  }

}
