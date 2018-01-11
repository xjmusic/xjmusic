// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.pulse;


import io.xj.pulse.util.EnvironmentProvider;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PulseTest {
  @Mock private HttpClient httpClient;
  @Mock private Context context;
  @Mock private LambdaLogger lambdaLogger;
  @Mock private EnvironmentProvider environmentProdiver;
  @Mock private HttpResponse httpResponse;
  @Mock private StatusLine statusLine;
  private Pulse subject;

  @Before
  public void setUp() throws Exception {
    subject = new Pulse();
    subject.mock(httpClient, environmentProdiver);
  }

  @After
  public void tearDown() throws Exception {
    subject = null;
  }

  @Test
  public void handleRequest() throws Exception {
    when(context.getLogger()).thenReturn(lambdaLogger);
    when(environmentProdiver.getRequired("platform_heartbeat_key")).thenReturn("1234");
    when(environmentProdiver.getRequired("platform_heartbeat_url")).thenReturn("https://test.com");
    ArgumentCaptor<HttpPost> argument = ArgumentCaptor.forClass(HttpPost.class);
    when(httpClient.execute(argument.capture())).thenReturn(httpResponse);
    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    when(statusLine.getStatusCode()).thenReturn(202); // HttpStatus.SC_ACCEPTED

    JSONObject result = subject.handleRequest(new JSONObject("{\"input\":\"test\"}"), context);

    verify(lambdaLogger, times(1)).log("Input: {\"input\":\"test\"}");
    assertEquals(new URI("https://test.com"), argument.getValue().getURI());
    assertEquals("POST", argument.getValue().getMethod());
    assertEquals("key=1234", IOUtils.toString(argument.getValue().getEntity().getContent(), "utf8"));
    assertEquals("Pulse success, code 202", result.get("message"));
  }

}
