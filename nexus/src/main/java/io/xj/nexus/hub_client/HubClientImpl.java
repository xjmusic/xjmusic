// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.hub_client;

import io.xj.hub.HubContent;
import io.xj.hub.HubContentPayload;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplatePlayback;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Implementation of a Hub Client for connecting to Hub and accessing contents
 */
@Service
public class HubClientImpl implements HubClient {
  static final String API_PATH_INGEST_FORMAT = "api/1/ingest/%s";
  static final String API_PATH_TEMPLATE_BY_ID_FORMAT = "api/1/templates/%s";
  static final String API_PATH_TEMPLATE_PLAYBACK_BY_ID_FORMAT = "api/1/template-playbacks/%s";
  static final String HEADER_COOKIE = "Cookie";
  final Logger LOG = LoggerFactory.getLogger(HubClientImpl.class);
  final String ingestUrl;
  final String ingestTokenName;
  final HttpClientProvider httpClientProvider;
  final JsonProviderImpl jsonProvider;
  final JsonapiPayloadFactory jsonapiPayloadFactory;
  final String ingestTokenValue;
  final String audioBaseUrl;

  @Autowired
  public HubClientImpl(
    HttpClientProvider httpClientProvider,
    JsonProviderImpl jsonProvider,
    JsonapiPayloadFactory jsonapiPayloadFactory,
    @Value("${ingest.url}") String ingestUrl,
    @Value("${ingest.token.name}") String ingestTokenName,
    @Value("${ingest.token.value}") String ingestTokenValue,
    @Value("${audio.base.url}") String audioBaseUrl
  ) {
    this.httpClientProvider = httpClientProvider;
    this.jsonProvider = jsonProvider;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;

    this.ingestUrl = ingestUrl;
    this.ingestTokenName = ingestTokenName;
    this.ingestTokenValue = ingestTokenValue;
    this.audioBaseUrl = audioBaseUrl;

    String obscuredSecret = Arrays.stream(ingestTokenValue.split("")).map(c -> "*").collect(Collectors.joining());
    LOG.info("Will connect to Hub at {} with token '{}' value '{}'", ingestUrl, ingestTokenName, obscuredSecret);
  }

  @Override
  public HubContent ingest(HubClientAccess access, UUID templateId) throws HubClientException {
    CloseableHttpClient client = httpClientProvider.getClient();
    var uri = buildURI(String.format(API_PATH_INGEST_FORMAT, templateId.toString()));
    try (
      CloseableHttpResponse response = client.execute(buildGetRequest(uri, ingestTokenValue))
    ) {
      // return content if successful.
      if (!Objects.equals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode()))
        throw buildException(uri.toString(), response);

      String json = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
      return HubContent.from(jsonProvider.getMapper().readValue(json, HubContentPayload.class));

    } catch (Exception e) {
      throw new HubClientException(e);
    }
  }

  @Override
  public Optional<Template> readPreviewTemplate(UUID templateId) throws HubClientException {
    return getOneFromHub(String.format(API_PATH_TEMPLATE_BY_ID_FORMAT, templateId));
  }

  @Override
  public Optional<TemplatePlayback> readPreviewTemplatePlayback(UUID templatePlaybackId) throws HubClientException {
    return getOneFromHub(String.format(API_PATH_TEMPLATE_PLAYBACK_BY_ID_FORMAT, templatePlaybackId));
  }

  @Override
  public HubContent load(String shipKey) throws HubClientException {
    var url = String.format("%s%s.json", audioBaseUrl, shipKey);
    LOG.info("Will load content from {}", url);
    CloseableHttpClient client = httpClientProvider.getClient();
    try (
      CloseableHttpResponse response = client.execute(new HttpGet(url))
    ) {
      // return content if successful.
      if (!Objects.equals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode()))
        throw buildException(url, response);

      String json = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
      return HubContent.from(jsonProvider.getMapper().readValue(json, HubContentPayload.class));

    } catch (Exception e) {
      throw new HubClientException(e);
    }
  }

  <N> Optional<N> getOneFromHub(String path) throws HubClientException {
    CloseableHttpClient client = httpClientProvider.getClient();
    var uri = buildURI(path);
    var request = buildGetRequest(uri, ingestTokenValue);
    try (
      CloseableHttpResponse response = client.execute(request)
    ) {
      if (!Objects.equals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode()))
        throw buildException(uri.toString(), response);

      var json = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());

      var payload = jsonapiPayloadFactory.deserialize(json);

      return payload.isEmpty() ? Optional.empty() : Optional.of(jsonapiPayloadFactory.toOne(payload));

    } catch (IOException | JsonapiException e) {
      throw new HubClientException(String.format("Failed executing Hub API request to %s", uri), e);
    }
  }

  /**
   Set the access token cookie header for a request to Hub

   @param uri              of request
   @param ingestTokenValue of request
   @return http request
   */
  HttpGet buildGetRequest(URI uri, String ingestTokenValue) {
    var request = new HttpGet(uri);
    request.setHeader(HEADER_COOKIE, String.format("%s=%s", ingestTokenName, ingestTokenValue));
    return request;
  }

  /**
   Build URI for specified API path

   @param path to build URI to
   @return URI for specified API path
   @throws HubClientException on failure to construct URI
   */
  URI buildURI(String path) throws HubClientException {
    try {
      URIBuilder b = new URIBuilder(String.format("%s%s", ingestUrl, path));
      return b.build();
    } catch (URISyntaxException e) {
      throw new HubClientException("Failed to construct URI", e);
    }
  }

  /**
   Log a failure message and returns a throwable exception based on a response@param uri

   @param response to log and throw
   */
  HubClientException buildException(String uri, CloseableHttpResponse response) throws HubClientException {
    // if we got here, it's a failure
    LOG.error("Request failed to {}\n response: {} {}", uri, response.getAllHeaders(), response);
    throw new HubClientException(String.format("Request failed to %s\nresponse: %d %s", uri, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
  }
}
