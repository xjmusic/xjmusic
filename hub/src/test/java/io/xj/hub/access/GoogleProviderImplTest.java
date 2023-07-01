// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.services.plus.model.Person;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.notification.NotificationProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class GoogleProviderImplTest extends Mockito {
  @MockBean
  NotificationProvider notificationProvider;

  @MockBean
  FileStoreProvider fileStoreProvider;

  @MockBean
  HttpClientProvider httpClientProvider;

  @MockBean
  GoogleHttpProvider googleHttpProvider;

  GoogleProvider googleProvider;

  @BeforeEach
  public void setUp() throws Exception {
    ApiUrlProvider apiUrlProvider = new ApiUrlProvider("http://shammy/");
    googleProvider = new GoogleProviderImpl(googleHttpProvider, apiUrlProvider, "12345", "");
  }

  @Test
  public void getAuthCodeRequestUrl() throws Exception {
    String url = googleProvider.getAuthCodeRequestUrl("xj-music");
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

  @Test
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

    assertThrows(HubAccessException.class, () -> googleProvider.getTokenFromCode("red"));
  }

  @Test
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

    assertThrows(HubAccessException.class, () -> googleProvider.getTokenFromCode("red"));
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

  @Test
  public void getMe_IOFailure() throws Exception {
    MockLowLevelHttpResponse httpResponse = new MockLowLevelHttpResponse();
    httpResponse.setStatusCode(500);
    HttpTransport httpTransport = new MockHttpTransport.Builder()
      .setLowLevelHttpResponse(httpResponse)
      .build();
    when(googleHttpProvider.getTransport())
      .thenReturn(httpTransport);

    assertThrows(HubAccessException.class, () -> googleProvider.getMe("12345"));
  }

  @Test
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

    assertThrows(HubAccessException.class, () -> googleProvider.getMe("12345"));
  }

}
