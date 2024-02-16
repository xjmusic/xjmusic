// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.hub_client;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.xj.hub.HubContent;
import io.xj.hub.HubContentPayload;
import io.xj.hub.HubUploadAuthorization;
import io.xj.hub.json.JsonProvider;
import io.xj.hub.jsonapi.JsonapiPayloadFactory;
import io.xj.hub.util.StringUtils;
import io.xj.nexus.NexusException;
import io.xj.nexus.http.HttpClientProvider;
import io.xj.nexus.project.ProjectAudioUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.mime.FormBodyPart;
import org.apache.hc.client5.http.entity.mime.FormBodyPartBuilder;
import org.apache.hc.client5.http.entity.mime.InputStreamBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.BasicHttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

/**
 * Implementation of a Hub Client for connecting to Hub and accessing contents
 */
public class HubClientFactoryImpl implements HubClientFactory {
  static final String API_PATH_GET_PROJECT = "api/2/projects/%s";
  static final String API_PATH_AUTHORIZE_INSTRUMENT_AUDIO_UPLOAD = "api/2/instrument-audios/%s/upload?extension=%s";
  static final String API_PATH_SYNC_PROJECT = "api/2/projects/%s/sync";
  static final String HEADER_COOKIE = "Cookie";
  private final int audioDownloadRetries;
  final Logger LOG = LoggerFactory.getLogger(HubClientFactoryImpl.class);
  final HttpClientProvider httpClientProvider;
  final JsonProvider jsonProvider;
  final JsonapiPayloadFactory jsonapiPayloadFactory;
  final String hubAccessTokenName = "access_token";

  public HubClientFactoryImpl(
      HttpClientProvider httpClientProvider,
      JsonProvider jsonProvider,
      JsonapiPayloadFactory jsonapiPayloadFactory,
      int audioDownloadRetries
  ) {
    this.httpClientProvider = httpClientProvider;
    this.jsonProvider = jsonProvider;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    this.audioDownloadRetries = audioDownloadRetries;
  }

  @Override
  public void postProjectSyncApiV2(CloseableHttpClient httpClient, String baseUrl, HubClientAccess access, HubContent content) throws HubClientException {
    var uri = buildURI(baseUrl, String.format(API_PATH_SYNC_PROJECT, content.getProject().getId().toString()));
    LOG.info("Will post content to {}", uri);
    var request = buildPostRequest(uri, access.getToken(), content);
    //noinspection deprecation
    try (
        CloseableHttpResponse response = httpClient.execute(request)
    ) {
      if (!Objects.equals(HttpStatus.SC_OK, response.getCode())) {
        var result = jsonProvider.getMapper().readValue(IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset()), HubContent.class);
        throw new HubClientException(StringUtils.toProperCsvAnd(result.getErrors().stream().map(error -> error.getCause().getMessage()).toList()));
      }

      LOG.debug("Did post content; will read bytes of JSON");

    } catch (Exception e) {
      throw new HubClientException(e);
    }
  }

  @Override
  public HubContent getProjectApiV2(CloseableHttpClient httpClient, String baseUrl, HubClientAccess access, UUID projectId) throws HubClientException {
    var uri = buildURI(baseUrl, String.format(API_PATH_GET_PROJECT, projectId.toString()));
    LOG.info("Will ingest content from {}", uri);
    //noinspection deprecation
    try (
        CloseableHttpResponse response = httpClient.execute(buildGetRequest(uri, access.getToken()))
    ) {
      // return content if successful.
      if (!Objects.equals(HttpStatus.SC_OK, response.getCode()))
        throw buildException(uri.toString(), response);

      String json = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
      LOG.debug("Did ingest content; will read bytes of JSON");
      return jsonProvider.getMapper().readValue(json, HubContent.class);

    } catch (Exception e) {
      throw new HubClientException(e);
    }
  }

  @Override
  public HubUploadAuthorization authorizeInstrumentAudioUploadApiV2(CloseableHttpClient httpClient, String baseUrl, HubClientAccess access, UUID instrumentAudioId, String extension) throws HubClientException {
    var uri = buildURI(baseUrl, String.format(API_PATH_AUTHORIZE_INSTRUMENT_AUDIO_UPLOAD, instrumentAudioId.toString(), extension));
    LOG.info("Will request upload authorization from {}", uri);
    //noinspection deprecation
    try (
        CloseableHttpResponse response = httpClient.execute(buildGetRequest(uri, access.getToken()))
    ) {
      // return content if successful.
      if (!Objects.equals(HttpStatus.SC_ACCEPTED, response.getCode()))
        throw buildException(uri.toString(), response);

      String json = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
      LOG.debug("upload authorized; will read bytes of JSON upload authorization");
      return jsonProvider.getMapper().readValue(json, HubUploadAuthorization.class);

    } catch (Exception e) {
      throw new HubClientException(e);
    }
  }

  @Override
  public HubContent loadApiV1(CloseableHttpClient httpClient, String shipKey, String audioBaseUrl) throws HubClientException {
    var url = String.format("%s%s.json", audioBaseUrl, shipKey);
    LOG.info("Will load content from {}", url);
    //noinspection deprecation
    try (
        CloseableHttpResponse response = httpClient.execute(new HttpGet(url))
    ) {
      // return content if successful.
      if (!Objects.equals(HttpStatus.SC_OK, response.getCode()))
        throw buildException(url, response);

      String json = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
      LOG.debug("Did load content; will read bytes of JSON");
      return HubContent.from(jsonProvider.getMapper().readValue(json, HubContentPayload.class));

    } catch (Exception e) {
      throw new HubClientException(e);
    }
  }

  @Override
  public boolean downloadRemoteFileWithRetry(CloseableHttpClient httpClient, String url, String outputPath, long expectedSize) {
    for (int attempt = 1; attempt <= audioDownloadRetries; attempt++) {
      try {
        Path path = Paths.get(outputPath);
        Files.deleteIfExists(path);
        downloadRemoteFile(httpClient, url, outputPath);
        long downloadedSize = Files.size(path);
        if (downloadedSize == expectedSize) {
          return true;
        }
        LOG.info("File size does not match! Attempt " + attempt + " of " + audioDownloadRetries + " to download " + url + " to " + outputPath + " failed. Expected " + expectedSize + " bytes, but got " + downloadedSize + " bytes.");

      } catch (Exception e) {
        LOG.info("Attempt " + attempt + " of " + audioDownloadRetries + " to download " + url + " to " + outputPath + " failed because " + e.getMessage());
      }
    }
    return false;
  }

  /**
   * Download a file from the given URL to the given output path
   *
   * @param httpClient http client (don't close the client; only close the responses from it)
   * @param url        url
   * @param outputPath output path
   */
  private void downloadRemoteFile(CloseableHttpClient httpClient, String url, String outputPath) throws IOException, NexusException {
    //noinspection deprecation
    try (
        CloseableHttpResponse response = httpClient.execute(new HttpGet(url))
    ) {
      if (Objects.isNull(response.getEntity().getContent()))
        throw new NexusException(String.format("Unable to write bytes to disk: %s", outputPath));

      try (OutputStream toFile = FileUtils.openOutputStream(new File(outputPath))) {
        var size = IOUtils.copy(response.getEntity().getContent(), toFile); // stores number of bytes copied
        LOG.debug("Did write media item to disk: {} ({} bytes)", outputPath, size);
      }
    }
  }

  @Override
  public long getRemoteFileSize(CloseableHttpClient httpClient, String url) throws Exception {
    //noinspection deprecation
    try (
        CloseableHttpResponse response = httpClient.execute(new HttpHead(url))
    ) {
      if (response.getCode() == HttpStatus.SC_NOT_FOUND) {
        return FILE_SIZE_NOT_FOUND;
      }
      var contentLengthHeader = response.getFirstHeader("Content-Length");
      if (Objects.isNull(contentLengthHeader)) {
        throw new NexusException(String.format("No Content-Length header found: %s", url));
      }
      return Long.parseLong(contentLengthHeader.getValue());
    } catch (Exception e) {
      throw new NexusException(String.format("Unable to get %s", url), e);
    }
  }

  @Override
  public void uploadInstrumentAudioFile(HubClientAccess hubAccess, String hubBaseUrl, CloseableHttpClient httpClient, ProjectAudioUpload upload) {
    try {
      upload.setAuth(authorizeInstrumentAudioUploadApiV2(httpClient, hubBaseUrl, hubAccess, upload.getInstrumentAudioId(), upload.getExtension()));
    } catch (HubClientException e) {
      upload.addError("Failed to authorize instrument audio upload because " + e.getMessage());
      return;
    }

    File file = new File(upload.getPathOnDisk());
    try (FileInputStream fileInputStream = new FileInputStream(file)) {
      HttpPost httpPost = new HttpPost(upload.getAuth().getUploadUrl());
      httpPost.setHeader("Accept", "*/*");
      httpPost.setConfig(RequestConfig.custom()
          .setConnectionKeepAlive(TimeValue.MAX_VALUE)
          .setResponseTimeout(Timeout.DISABLED)
          .setConnectionRequestTimeout(Timeout.DISABLED)
          .build());

      // Create the MultipartEntityBuilder
      MultipartEntityBuilder builder = MultipartEntityBuilder.create();

      // Add the form fields
      builder.addTextBody("policy", upload.getAuth().getUploadPolicy());
      builder.addTextBody("signature", upload.getAuth().getUploadPolicySignature());
      builder.addTextBody("acl", upload.getAuth().getAcl());
      builder.addTextBody("bucket", upload.getAuth().getBucketName());
      builder.addTextBody("key", upload.getAuth().getWaveformKey());
      builder.addTextBody("awsAccessKeyId", upload.getAuth().getAwsAccessKeyId());

      FormBodyPart bodyPart = FormBodyPartBuilder.create().setName("file")
          .setBody(new KnownSizeInputStreamBody(fileInputStream, file.length(), ContentType.APPLICATION_OCTET_STREAM)).build();
      builder.addPart(bodyPart);
      HttpEntity multipart = builder.build();
      httpPost.setEntity(multipart);

      //noinspection deprecation
      try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
        // Convert response body to string
        if (response.getCode() == HttpStatus.SC_NO_CONTENT) {
          upload.setSuccess(true);
        } else {
          upload.addError("Failed to upload instrument audio because " + EntityUtils.toString(response.getEntity()));
        }
      }

    } catch (FileNotFoundException e) {
      upload.addError("Failed to upload instrument audio because file " + upload.getPathOnDisk() + " not found!");
    } catch (IOException e) {
      upload.addError("Failed to upload instrument audio because of I/O error " + e.getMessage());
    } catch (Exception e) {
      upload.addError("Failed to upload instrument audio because " + e.getMessage());
    }
  }

  static class KnownSizeInputStreamBody extends InputStreamBody {
    private final long contentLength;

    public KnownSizeInputStreamBody(InputStream in, long contentLength, ContentType contentType) {
      super(in, contentType);
      this.contentLength = contentLength;
    }

    @Override
    public long getContentLength() {
      return contentLength;
    }
  }

  /**
   * Set the access token cookie header for a request to Hub
   *
   * @param uri   of request
   * @param token of request
   * @return http request
   */
  HttpGet buildGetRequest(URI uri, String token) {
    var request = new HttpGet(uri);
    request.setHeader(HEADER_COOKIE, String.format("%s=%s", hubAccessTokenName, token));
    return request;
  }

  /**
   * Set the access token cookie header for a request to Hub
   *
   * @param uri     of request
   * @param token   of request
   * @param content of request
   * @return http request
   */
  HttpPost buildPostRequest(URI uri, String token, HubContent content) throws HubClientException {
    try {
      var body = jsonProvider.getMapper().writeValueAsString(content);
      var entity = new BasicHttpEntity(IOUtils.toInputStream(body, Charset.defaultCharset()), ContentType.APPLICATION_JSON);
      var request = new HttpPost(uri);
      request.setHeader(HEADER_COOKIE, String.format("%s=%s", hubAccessTokenName, token));
      request.setEntity(entity);
      return request;
    } catch (JsonProcessingException e) {
      throw new HubClientException(e.getMessage(), e);
    }
  }

  /**
   * Build URI for specified API path
   *
   * @param baseUrl to build URI
   * @param path    to build URI
   * @return URI for specified API path
   * @throws HubClientException on failure to construct URI
   */
  URI buildURI(String baseUrl, String path) throws HubClientException {
    try {
      URIBuilder b = new URIBuilder(String.format("%s%s", baseUrl, path));
      return b.build();
    } catch (URISyntaxException e) {
      throw new HubClientException("Failed to construct URI", e);
    }
  }

  /**
   * Log a failure message and returns a throwable exception based on a response@param uri
   *
   * @param response to log and throw
   */
  HubClientException buildException(String uri, CloseableHttpResponse response) throws HubClientException {
    throw new HubClientException(String.format("Request failed to %s\nresponse: %d %s", uri, response.getCode(), response.getReasonPhrase()));
  }
}
