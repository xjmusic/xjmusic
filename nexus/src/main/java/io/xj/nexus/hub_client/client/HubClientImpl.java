// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.hub_client.client;

import com.google.api.client.util.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.hub.ingest.HubContentPayload;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.app.Environment;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 Implementation of a Hub Client for connecting to Hub and accessing contents
 */
@Singleton
public class HubClientImpl implements HubClient {
  private static final String API_PATH_INGEST_PREFIX = "api/1/ingest/";
  private static final String API_PATH_TEMPLATES_PREFIX = "api/1/templates/";
  private static final String API_PATH_TEMPLATES_PLAYING = "api/1/templates/playing";
  private static final String API_PATH_AUTH = "auth";
  private static final String HEADER_COOKIE = "Cookie";
  private final Logger LOG = LoggerFactory.getLogger(HubClientImpl.class);
  private final String ingestUrl;
  private final String ingestTokenName;
  private final HttpClientProvider httpClientProvider;
  private final JsonProviderImpl jsonProvider;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final String ingestTokenValue;

  @Inject
  public HubClientImpl(
    Environment env,
    HttpClientProvider httpClientProvider,
    JsonProviderImpl jsonProvider,
    JsonapiPayloadFactory jsonapiPayloadFactory
  ) {
    this.httpClientProvider = httpClientProvider;
    this.jsonProvider = jsonProvider;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;

    ingestUrl = env.getIngestURL();
    ingestTokenName = env.getIngestTokenName();
    ingestTokenValue = env.getIngestTokenValue();

    String obscuredSecret = Arrays.stream(ingestTokenValue.split("")).map(c -> "*").collect(Collectors.joining());
    LOG.info("Will connect to Hub at {} with token '{}' value '{}'", ingestUrl, ingestTokenName, obscuredSecret);
  }

  @Override
  public HubContent ingest(HubClientAccess access, UUID templateId) throws HubClientException {
    CloseableHttpClient client = httpClientProvider.getClient();
    try (
      CloseableHttpResponse response = client.execute(buildGetRequest(buildURI(String.format("%s%s", API_PATH_INGEST_PREFIX, templateId.toString())), ingestTokenValue))
    ) {
      // return content if successful.
      if (!Objects.equals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode()))
        throw buildException(response);

      var json = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
      HubContentPayload content = jsonProvider.getMapper().readValue(json, HubContentPayload.class);
      List<Object> entities = Lists.newArrayList();
      entities.addAll(content.getTemplates());
      entities.addAll(content.getTemplateBindings());
      entities.addAll(content.getInstruments());
      entities.addAll(content.getInstrumentAudios());
      entities.addAll(content.getInstrumentMemes());
      entities.addAll(content.getPrograms());
      entities.addAll(content.getProgramMemes());
      entities.addAll(content.getProgramSequences());
      entities.addAll(content.getProgramSequenceBindings());
      entities.addAll(content.getProgramSequenceBindingMemes());
      entities.addAll(content.getProgramSequenceChords());
      entities.addAll(content.getProgramSequenceChordVoicings());
      entities.addAll(content.getProgramSequencePatterns());
      entities.addAll(content.getProgramSequencePatternEvents());
      entities.addAll(content.getProgramVoices());
      entities.addAll(content.getProgramVoiceTracks());
      return new HubContent(entities);

    } catch (IOException e) {
      throw new HubClientException(e);
    }
  }

  @Override
  public HubClientAccess auth(String accessToken) throws HubClientException {
    CloseableHttpClient client = httpClientProvider.getClient();
    try (
      CloseableHttpResponse response = client.execute(buildGetRequest(buildURI(API_PATH_AUTH), accessToken))
    ) {
      return jsonProvider.getMapper().readValue(response.getEntity().getContent(), HubClientAccess.class);
    } catch (IOException e) {
      throw new HubClientException("Failed to authenticate with Hub API", e);
    }
  }

  @Override
  public Template readTemplate(String identifier) throws HubClientException {
    CloseableHttpClient client = httpClientProvider.getClient();
    try (
      CloseableHttpResponse response = client.execute(buildGetRequest(buildURI(String.format("%s%s", API_PATH_TEMPLATES_PREFIX, identifier)), ingestTokenValue))
    ) {

      // return template if found
      if (!Objects.equals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode()))
        throw buildException(response);

      var json = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
      var payload = jsonapiPayloadFactory.deserialize(json);
      return jsonapiPayloadFactory.toOne(payload);

    } catch (IOException | JsonapiException e) {
      throw new HubClientException(e);
    }
  }

  @Override
  public Collection<Template> readAllTemplatesPlaying() throws HubClientException {
    CloseableHttpClient client = httpClientProvider.getClient();
    try (
      CloseableHttpResponse response = client.execute(buildGetRequest(buildURI(API_PATH_TEMPLATES_PLAYING), ingestTokenValue))
    ) {
      if (!Objects.equals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode()))
        throw buildException(response);

      var json = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
      var payload = jsonapiPayloadFactory.deserialize(json);
      return jsonapiPayloadFactory.toMany(payload);

    } catch (IOException | JsonapiException e) {
      throw new HubClientException(e);
    }
  }

  /**
   Set the access token cookie header for a request to Hub

   @param uri              of request
   @param ingestTokenValue of request
   @return http request
   */
  private HttpGet buildGetRequest(URI uri, String ingestTokenValue) {
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
  private URI buildURI(String path) throws HubClientException {
    try {
      URIBuilder b = new URIBuilder(String.format("%s%s", ingestUrl, path));
      return b.build();
    } catch (URISyntaxException e) {
      throw new HubClientException("Failed to construct URI", e);
    }
  }

  /**
   Log a failure message and returns a throwable exception based on a response

   @param response to log and throw
   */
  private HubClientException buildException(CloseableHttpResponse response) throws HubClientException {
    // if we got here, it's a failure
    LOG.error("Request failed! response: {} {}", response.getAllHeaders(), response);
    throw new HubClientException(String.format("Request failed with response Code %d %s", response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
  }
}
