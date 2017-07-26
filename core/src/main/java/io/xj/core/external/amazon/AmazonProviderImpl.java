// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.external.amazon;

import io.xj.core.app.config.Config;
import io.xj.core.app.config.Exposure;
import io.xj.core.app.exception.ConfigException;
import io.xj.core.app.exception.NetworkException;
import io.xj.core.util.token.TokenGenerator;

import com.google.inject.Inject;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.SimpleTimeZone;

public class AmazonProviderImpl implements AmazonProvider {
  private static Logger log = LoggerFactory.getLogger(AmazonProviderImpl.class);
  private final static float microsInASecond = 1000000;
  private final static float nanosInASecond = 1000 * microsInASecond;

  private final TokenGenerator tokenGenerator;
  // format strings for the date/time and date stamps required during signing
  private static final String ISO8601BasicFormat = "yyyyMMdd'T'HHmmss'Z'";
  private static final String DateStringFormat = "yyyyMMdd";

  @Inject
  public AmazonProviderImpl(
    TokenGenerator tokenGenerator
  ) {
    this.tokenGenerator = tokenGenerator;

    SimpleDateFormat dateTimeFormat = new SimpleDateFormat(ISO8601BasicFormat);
    dateTimeFormat.setTimeZone(new SimpleTimeZone(0, "UTC"));
    SimpleDateFormat dateStampFormat = new SimpleDateFormat(DateStringFormat);
    dateStampFormat.setTimeZone(new SimpleTimeZone(0, "UTC"));
  }

  @Override
  public S3UploadPolicy generateAudioUploadPolicy() throws ConfigException {
    return new S3UploadPolicy(getCredentialId(), getCredentialSecret(), getAudioUploadACL(), getAudioBucketName(), "", getAudioUploadExpireInMinutes());
  }

  @Override
  public String generateKey(String filename, String extension) {
    return tokenGenerator.generateShort() + Exposure.FILE_SEPARATOR + filename +
      Exposure.FILE_DOT + extension;
  }

  @Override
  public String getUploadURL() throws ConfigException {
    return Config.audioUploadUrl();
  }

  @Override
  public String getCredentialId() throws ConfigException {
    return Config.awsAccessKeyId();
  }

  @Override
  public String getCredentialSecret() throws ConfigException {
    return Config.awsSecretKey();
  }

  @Override
  public String getAudioBucketName() throws ConfigException {
    return Config.audioFileBucket();
  }

  @Override
  public int getAudioUploadExpireInMinutes() throws ConfigException {
    return Config.audioFileUploadExpireMinutes();
  }

  @Override
  public String getAudioUploadACL() throws ConfigException {
    return Config.audioFileUploadACL();
  }

  @Override
  public BufferedInputStream streamS3Object(String bucketName, String key) throws NetworkException {
    try {
      return new BufferedInputStream(s3Client()
        .getObject(new GetObjectRequest(bucketName, key))
        .getObjectContent());

    } catch (Exception e) {
      throw new NetworkException("Failed to stream S3 object", e);
    }
  }

  @Override
  public void putS3Object(String filePath, String bucket, String key) throws NetworkException {
    try {
      long startedAt = System.nanoTime();
      s3Client().putObject(new PutObjectRequest(
        bucket, key, new File(filePath)));
      log.info("Did ship {} to {}/{} OK in {}s", filePath, bucket,key, String.format("%.9f", (double) (System.nanoTime() - startedAt) / nanosInASecond));

    } catch (Exception e) {
      throw new NetworkException("Failed to put S3 object", e);
    }
  }

  @Override
  public void deleteS3Object(String bucket, String key) throws NetworkException {
    try {
      s3Client().deleteObject(bucket, key);

    } catch (Exception e) {
      throw new NetworkException("Failed to delete S3 object", e);
    }
  }

  /**
   * Get an Amazon S3 client
   *
   * @return S3 client
   */
  private AmazonS3 s3Client() {
    return AmazonS3ClientBuilder.standard()
      .withRegion(Config.awsDefaultRegion())
      .build();
  }

}
