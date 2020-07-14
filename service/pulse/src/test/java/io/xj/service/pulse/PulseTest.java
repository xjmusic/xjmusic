// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.pulse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PulseTest {
  @Mock
  public HttpClient httpClient;
  @Mock
  public Context context;
  @Mock
  public LambdaLogger lambdaLogger;
  @Mock
  public HttpResponse httpResponse;
  @Mock
  public StatusLine statusLine;
  @Mock
  public LambdaExecutionConfiguration config;
  private Pulse subject;

  @Before
  public void setUp() {
    subject = new Pulse(config, httpClient);
  }

  @Test
  public void handleRequest() throws Exception {
    when(context.getLogger()).thenReturn(lambdaLogger);
    when(config.getHeartbeatKey()).thenReturn("1234");
    when(config.getHeartbeatURL()).thenReturn("https://test.com");
    when(httpClient.execute(any())).thenReturn(httpResponse);
    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    when(statusLine.getStatusCode()).thenReturn(202); // HttpStatus.SC_ACCEPTED

    JSONObject result = subject.handleRequest(new JSONObject("{\"input\":\"test\"}"), context);

    ArgumentCaptor<HttpPost> argument = ArgumentCaptor.forClass(HttpPost.class);
    verify(httpClient).execute(argument.capture());
    verify(lambdaLogger, times(1)).log("Input: {\"input\":\"test\"}");
    assertEquals(new URI("https://test.com"), argument.getValue().getURI());
    assertEquals("POST", argument.getValue().getMethod());
    assertEquals("key=1234", IOUtils.toString(argument.getValue().getEntity().getContent(), "utf8"));
    assertEquals("Pulse success, code 202", result.get("message"));
  }

}
