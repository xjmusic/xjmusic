// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.hub_client.client;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.Entities;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 Implementation of a Hub Client for connecting to Hub and accessing contents
 */
public class HubClientImpl implements HubClient {
  private final Logger LOG = LoggerFactory.getLogger(HubClientImpl.class);
  private static final String API_PATH_INGEST = "api/1/ingest";
  private static final String API_PATH_AUTH = "auth";
  private static final String HEADER_COOKIE = "Set-Cookie";
  private final CloseableHttpClient httpClient;
  private final String ingestUrl;
  private final String ingestTokenName;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final JsonProviderImpl jsonProvider;
  private final String ingestTokenValue;

  @Inject
  public HubClientImpl(
    Environment env,
    JsonapiPayloadFactory jsonapiPayloadFactory,
    JsonProviderImpl jsonProvider
  ) {
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    this.jsonProvider = jsonProvider;
    httpClient = HttpClients.createDefault();

    ingestUrl = env.getIngestURL();
    ingestTokenName = env.getIngestTokenName();
    ingestTokenValue = env.getIngestTokenValue();

    LOG.info("Will connect to Hub at {}", ingestUrl);
  }

  @Override
  public HubContent ingest(HubClientAccess access, Set<String> libraryIds, Set<String> programIds, Set<String> instrumentIds) throws HubClientException {
    try {
      HttpGet request = new HttpGet(buildURI(HubClientImpl.API_PATH_INGEST, ImmutableMap.of(
        "libraryIds", Entities.csvOf(libraryIds),
        "programIds", Entities.csvOf(programIds),
        "instrumentIds", Entities.csvOf(instrumentIds)
      )));
      request.setHeader(HEADER_COOKIE, String.format("%s=%s", ingestTokenName, ingestTokenValue));
      CloseableHttpResponse response = httpClient.execute(request);

      // return content if successful.
      if (Objects.equals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode())) {
        String entity = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
        JsonapiPayload jsonapiPayload = jsonapiPayloadFactory.deserialize(entity);
        return new HubContent(jsonapiPayloadFactory.toMany(jsonapiPayload));
      }

      // if we got here, it's a failure
      LOG.error("Failed to request {} because {}", request.getURI(), response);
      throw new HubClientException(String.format("Failed to request %s because %s",
        request.getURI(), response.getStatusLine().getReasonPhrase()));

    } catch (IOException | JsonApiException e) {
      throw new HubClientException(e);
    }
  }

  @Override
  public HubClientAccess auth(String accessToken) throws HubClientException {
    HttpGet request = new HttpGet(buildURI(HubClientImpl.API_PATH_AUTH, ImmutableMap.of()));
    request.setHeader(HEADER_COOKIE, String.format("%s=%s", ingestTokenName, accessToken));
    request.setHeader(HEADER_COOKIE, String.format("%s=%s", ingestTokenName, ingestTokenValue));
    HubClientAccess access;
    CloseableHttpResponse response;
    try {
      response = httpClient.execute(request);
      access = jsonProvider.getObjectMapper().readValue(response.getEntity().getContent(), HubClientAccess.class);
    } catch (IOException e) {
      throw new HubClientException("Failed to authenticate with Hub API", e);
    }
    return access;
  }

  /**
   Build URI for specified API path

   @param path        to build URI to
   @param queryParams to include with path
   @return URI for specified API path
   @throws HubClientException on failure to construct URI
   */
  private URI buildURI(String path, Map<String, String> queryParams) throws HubClientException {
    try {
      URIBuilder b = new URIBuilder(String.format("%s%s", ingestUrl, path));
      queryParams.forEach(b::addParameter);
      return b.build();
    } catch (URISyntaxException e) {
      throw new HubClientException("Failed to construct URI", e);
    }
  }

}
