// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.pulse;

import io.xj.pulse.util.EnvironmentProvider;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Pulse implements RequestHandler<JSONObject, JSONObject> {
  public static final int CODE_SUCCESS = HttpStatus.SC_ACCEPTED;
  HttpClient httpClient = HttpClients.createDefault();
  EnvironmentProvider environmentProvider = new EnvironmentProvider();
  String heartbeatKey;
  String requestURL;

  void mock(HttpClient httpClient, EnvironmentProvider environmentProvider) {
    this.httpClient = httpClient;
    this.environmentProvider = environmentProvider;
  }

  /**
   Handles a Lambda Function request

   @param input   The Lambda Function input
   @param context The Lambda execution environment context object.
   @return The Lambda Function output
   */
  @Override
  public JSONObject handleRequest(JSONObject input, Context context) {
    context.getLogger().log("Input: " + input);

    try {
      heartbeatKey = environmentProvider.getRequired("platform_heartbeat_key");
      requestURL = environmentProvider.getRequired("platform_heartbeat_url");
    } catch (Exception e) {
      return resultJSON(String.format("Configuration failure: %s", e));
    }

    HttpPost httpRequest = new HttpPost(requestURL);

    // Request parameters and other properties.
    List<NameValuePair> params = new ArrayList<>(1);
    params.add(new BasicNameValuePair("key", heartbeatKey));
    try {
      httpRequest.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      return resultJSON(String.format("URL encoding failure: %s", e));
    }

    //Execute and get the response
    HttpResponse response;
    try {
      response = httpClient.execute(httpRequest);
    } catch (IOException e) {
      return resultJSON(String.format("HTTP request failure: %s", e));
    }

    int code = response.getStatusLine().getStatusCode();
    switch (code) {
      case CODE_SUCCESS:
        return resultJSON(String.format("Pulse success, code %d", code));

      default:
        return resultJSON(String.format("Pulse failure, code %d", code));
    }
  }

  /**
   Build JSON of result

   @param msg to encapsulate
   @return string of JSON object
   */
  private static JSONObject resultJSON(String msg) {
    JSONObject json = new JSONObject();
    json.put("message", msg);
    return json;
  }

  /**
   Pulse method.

   @param args arguments
   @throws IOException if execution fails
   */
  public static void main(String[] args) throws Exception {


  }

}

