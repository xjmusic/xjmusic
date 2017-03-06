// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.external.amazon;

import io.outright.xj.core.app.config.Config;
import io.outright.xj.core.app.config.Exposure;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.util.token.TokenGenerator;

import com.google.inject.Inject;

import com.amazonaws.SignableRequest;
import com.amazonaws.auth.AWS4Signer;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Request;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.SimpleTimeZone;

//import java.util.Collection;

public class AmazonProviderImpl implements AmazonProvider {
  private final TokenGenerator tokenGenerator;
  private static Logger log = LoggerFactory.getLogger(AmazonProviderImpl.class);

  // configs
  private String accessKey;
  private String accessSecret;
  private String httpMethod;
  private String regionName;
  private String serviceName;
  private String url;
  private URL endpointUrl;

  // constants
  private static final String SCHEME = "AWS4";
  private static final String ALGORITHM = "HMAC-SHA256";
  private static final String TERMINATOR = "aws4_request";

  // format strings for the date/time and date stamps required during signing
  private static final String ISO8601BasicFormat = "yyyyMMdd'T'HHmmss'Z'";
  private static final String DateStringFormat = "yyyyMMdd";

  // date/time formats
  private final SimpleDateFormat dateTimeFormat;
  private final SimpleDateFormat dateStampFormat;

  @Inject
  public AmazonProviderImpl(
    TokenGenerator tokenGenerator
  ) {
    this.tokenGenerator = tokenGenerator;
    try {
      this.url = Config.awsFileUploadUrl();
      this.accessKey = Config.awsFileUploadKey();
      this.accessSecret = Config.awsFileUploadSecret();
    } catch (ConfigException e) {
      log.error("Failed to initialize Amazon Provider: " + e.getMessage());
    }

    dateTimeFormat = new SimpleDateFormat(ISO8601BasicFormat);
    dateTimeFormat.setTimeZone(new SimpleTimeZone(0, "UTC"));
    dateStampFormat = new SimpleDateFormat(DateStringFormat);
    dateStampFormat.setTimeZone(new SimpleTimeZone(0, "UTC"));
  }

  @Override
  public String generateUploadPolicy(String key) throws ConfigException {
//    AWS4Signer aws4Signer = new AWS4Signer();
    return "TODO [#146] Implement real S3 upload policy generation";
  }

  @Override
  public String generateKey(String filename, String extension) {
    return filename + Exposure.FILE_SEPARATOR + tokenGenerator.generateShort() +
      Exposure.FILE_DOT + extension;
  }

  @Override
  public String getUploadURL() {
    return url;
  }

  @Override
  public String getAccessKey() {
    return this.accessKey;
  }

}
