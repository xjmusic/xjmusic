// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.external.amazon;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.inject.Inject;
import io.xj.core.access.token.TokenGenerator;
import io.xj.core.config.Config;
import io.xj.core.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;

public class AmazonProviderImpl implements AmazonProvider {
  private static final Logger log = LoggerFactory.getLogger(AmazonProviderImpl.class);
  private static final float microsInASecond = 1000000.0F;
  private static final float nanosInASecond = 1000.0F * microsInASecond;
  private final TokenGenerator tokenGenerator;

  @Inject
  public AmazonProviderImpl(
    TokenGenerator tokenGenerator
  ) {
    this.tokenGenerator = tokenGenerator;
  }

  /**
   Get an Amazon S3 client

   @return S3 client
   */
  private static AmazonS3 s3Client() {
    return AmazonS3ClientBuilder.standard()
      .withRegion(Config.getAwsDefaultRegion())
      .build();
  }

  @Override
  public S3UploadPolicy generateAudioUploadPolicy() throws CoreException {
    return new S3UploadPolicy(getCredentialId(), getCredentialSecret(), getAudioUploadACL(), getAudioBucketName(), "", getAudioUploadExpireInMinutes());
  }

  @Override
  public String generateKey(String filename, String extension) {
    return String.format("%s%s%s%s%s", tokenGenerator.generateShort(), Config.getNameSeparator(), filename, Config.getExtensionSeparator(), extension);
  }

  @Override
  public String getUploadURL() throws CoreException {
    return Config.getAudioUploadUrl();
  }

  @Override
  public String getCredentialId() throws CoreException {
    return Config.getAwsAccessKeyId();
  }

  @Override
  public String getCredentialSecret() throws CoreException {
    return Config.getAwsSecretKey();
  }

  @Override
  public String getAudioBucketName() throws CoreException {
    return Config.getAudioFileBucket();
  }

  @Override
  public int getAudioUploadExpireInMinutes() {
    return Config.getAudioFileUploadExpireMinutes();
  }

  @Override
  public String getAudioUploadACL() {
    return Config.getAudioFileUploadACL();
  }

  @Override
  public InputStream streamS3Object(String bucketName, String key) throws CoreException {
    AmazonS3 client = s3Client();
    GetObjectRequest request = new GetObjectRequest(bucketName, key);
    int count = 0;
    int maxTries = Config.getAwsS3RetryLimit();
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

}
