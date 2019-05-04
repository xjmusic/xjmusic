// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.external.amazon;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.inject.Inject;
import io.xj.core.access.token.TokenGenerator;
import io.xj.core.config.Config;
import io.xj.core.config.Exposure;
import io.xj.core.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.SimpleTimeZone;

public class AmazonProviderImpl implements AmazonProvider {
  private static final Logger log = LoggerFactory.getLogger(AmazonProviderImpl.class);
  private static final float microsInASecond = 1000000.0F;
  private static final float nanosInASecond = 1000.0F * microsInASecond;

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
  public S3UploadPolicy generateAudioUploadPolicy() throws CoreException {
    return new S3UploadPolicy(getCredentialId(), getCredentialSecret(), getAudioUploadACL(), getAudioBucketName(), "", getAudioUploadExpireInMinutes());
  }

  @Override
  public String generateKey(String filename, String extension) {
    return tokenGenerator.generateShort() + Exposure.FILE_SEPARATOR + filename +
      Exposure.FILE_DOT + extension;
  }

  @Override
  public String getUploadURL() throws CoreException {
    return Config.audioUploadUrl();
  }

  @Override
  public String getCredentialId() throws CoreException {
    return Config.awsAccessKeyId();
  }

  @Override
  public String getCredentialSecret() throws CoreException {
    return Config.awsSecretKey();
  }

  @Override
  public String getAudioBucketName() throws CoreException {
    return Config.audioFileBucket();
  }

  @Override
  public int getAudioUploadExpireInMinutes() throws CoreException {
    return Config.audioFileUploadExpireMinutes();
  }

  @Override
  public String getAudioUploadACL() throws CoreException {
    return Config.audioFileUploadACL();
  }

  @Override
  public InputStream streamS3Object(String bucketName, String key) throws CoreException {
    AmazonS3 client = s3Client();
    GetObjectRequest request = new GetObjectRequest(bucketName, key);
    int count = 0;
    int maxTries = Config.awsS3RetryLimit();
    while (true) {
      try {
        return client.getObject(request).getObjectContent();

      } catch (Exception e) {
        ++count;
        if (count == maxTries)
          throw new CoreException("Failed to stream S3 object", e);
      }
    }
  }

  @Override
  public void putS3Object(String filePath, String bucket, String key) throws CoreException {
    try {
      long startedAt = System.nanoTime();
      log.info("Will ship {} to {}/{}", filePath, bucket, key);
      s3Client().putObject(new PutObjectRequest(
        bucket, key, new File(filePath)));
      log.info("Did ship {} to {}/{} OK in {}s", filePath, bucket, key, String.format("%.9f", (double) (System.nanoTime() - startedAt) / nanosInASecond));

    } catch (Exception e) {
      throw new CoreException("Failed to put S3 object", e);
    }
  }

  @Override
  public void deleteS3Object(String bucket, String key) throws CoreException {
    try {
      s3Client().deleteObject(bucket, key);

    } catch (Exception e) {
      throw new CoreException("Failed to delete S3 object", e);
    }
  }

  @Override
  public void copyS3Object(String sourceBucket, String sourceKey, String targetBucket, String targetKey) throws CoreException {
    try {
      s3Client().copyObject(sourceBucket, sourceKey, targetBucket, targetKey);

    } catch (Exception e) {
      throw new CoreException("Failed to revived S3 object", e);
    }
  }

  /**
   Get an Amazon S3 client

   @return S3 client
   */
  private AmazonS3 s3Client() {
    return AmazonS3ClientBuilder.standard()
      .withRegion(Config.awsDefaultRegion())
      .build();
  }

}
