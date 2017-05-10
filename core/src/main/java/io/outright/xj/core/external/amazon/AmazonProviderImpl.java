// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.external.amazon;

import com.amazonaws.services.ec2.util.S3UploadPolicy;
import io.outright.xj.core.app.config.Config;
import io.outright.xj.core.app.config.Exposure;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.util.token.TokenGenerator;

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.SimpleTimeZone;

//import java.util.Collection;

public class AmazonProviderImpl implements AmazonProvider {
  // constants
  private static final String SCHEME = "AWS4";
  private static final String ALGORITHM = "HMAC-SHA256";
  private static final String TERMINATOR = "aws4_request";
  // format strings for the date/time and date stamps required during signing
  private static final String ISO8601BasicFormat = "yyyyMMdd'T'HHmmss'Z'";
  private static final String DateStringFormat = "yyyyMMdd";
  private static Logger log = LoggerFactory.getLogger(AmazonProviderImpl.class);
  private final TokenGenerator tokenGenerator;
  // date/time formats
  private final SimpleDateFormat dateTimeFormat;
  private final SimpleDateFormat dateStampFormat;

  @Inject
  public AmazonProviderImpl(
    TokenGenerator tokenGenerator
  ) {
    this.tokenGenerator = tokenGenerator;

    dateTimeFormat = new SimpleDateFormat(ISO8601BasicFormat);
    dateTimeFormat.setTimeZone(new SimpleTimeZone(0, "UTC"));
    dateStampFormat = new SimpleDateFormat(DateStringFormat);
    dateStampFormat.setTimeZone(new SimpleTimeZone(0, "UTC"));
  }

  @Override
  public S3UploadPolicy generateUploadPolicy() throws ConfigException {
    S3UploadPolicy s3UploadPolicy = new S3UploadPolicy(getAccessKey(), getAccessSecret(), getBucketName(), "", getExpireInMinutes());
    return s3UploadPolicy;
  }

  @Override
  public String generateKey(String filename, String extension) {
    return filename + Exposure.FILE_SEPARATOR + tokenGenerator.generateShort() +
      Exposure.FILE_DOT + extension;
  }

  @Override
  public String getUploadURL() throws ConfigException {
    return Config.awsFileUploadUrl();
  }

  @Override
  public String getAccessKey() throws ConfigException {
    return Config.awsFileUploadKey();
  }

  @Override
  public String getAccessSecret() throws ConfigException {
    return Config.awsFileUploadSecret();
  }

  @Override
  public String getBucketName() throws ConfigException {
    return Config.awsFileUploadBucket();
  }

  @Override
  public int getExpireInMinutes() throws ConfigException {
    return Config.awsFileUploadExpireMinutes();
  }

  @Override
  public String getUploadACL() throws ConfigException {
    return Config.awsFileUploadACL();
  }

}
