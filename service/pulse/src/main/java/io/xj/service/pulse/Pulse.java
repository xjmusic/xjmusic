// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.pulse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
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
  HttpClient httpClient;
  LambdaExecutionConfiguration config;
  String heartbeatKey;
  String requestURL;
  Integer timeoutMillis;

  /**
   Instantiate a new Pulse app

   @throws Exception if environment parameter is not set
   */
  public Pulse() throws Exception {
    config = new LambdaExecutionConfiguration();
    httpClient = HttpClients.createMinimal();
  }

  /**
   Instantiate a new Pulse app with specified internal components
   */
  public Pulse(LambdaExecutionConfiguration config, HttpClient httpClient) {
    this.config = config;
    this.httpClient = httpClient;
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
   Handles a Lambda Function request

   @param input   The Lambda Function input
   @param context The Lambda execution environment context object.
   @return The Lambda Function output
   */
  @Override
  public JSONObject handleRequest(JSONObject input, Context context) {
    context.getLogger().log("Input: " + input);
    return send();
  }

  /**
   Send the request

   @return JSON results
   */
  public JSONObject send() {
    try {
      heartbeatKey = config.getHeartbeatKey();
      requestURL = config.getHeartbeatURL();
      timeoutMillis = config.getTimeoutMillis();
    } catch (Exception e) {
      return resultJSON(String.format("LambdaExecutionConfiguration failure: %s", e));
    }

    HttpPost httpRequest = new HttpPost(requestURL);
    httpRequest.setConfig(RequestConfig.custom()
      .setConnectionRequestTimeout(timeoutMillis)
      .setConnectTimeout(timeoutMillis)
      .setSocketTimeout(timeoutMillis)
      .build());
    httpRequest.setHeader("Connection", "close");

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

    } finally {
      httpRequest.releaseConnection();
    }

    int code = response.getStatusLine().getStatusCode();
    if (CODE_SUCCESS == code) {
      return resultJSON(String.format("Pulse success, code %d", CODE_SUCCESS));
    }
    return resultJSON(String.format("Pulse failure, code %d", code));
  }

}

