// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.external;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.core.access.TokenGenerator;
import io.xj.core.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;

class AmazonProviderImpl implements AmazonProvider {
  private static final Logger log = LoggerFactory.getLogger(AmazonProviderImpl.class);
  private static final float microsInASecond = 1000000.0F;
  private static final float nanosInASecond = 1000.0F * microsInASecond;
  private final TokenGenerator tokenGenerator;
  private String awsDefaultRegion;
  private String nameSeparator;
  private String extensionSeparator;
  private String audioUploadUrl;
  private String awsAccessKeyId;
  private String awsSecretKey;
  private String audioFileBucket;
  private int audioFileUploadExpireMinutes;
  private String fileUploadACL;
  private int awsS3RetryLimit;

  @Inject
  public AmazonProviderImpl(
    TokenGenerator tokenGenerator,
    Config config
  ) {
    this.tokenGenerator = tokenGenerator;

    awsDefaultRegion = config.getString("aws.defaultRegion");
    nameSeparator = config.getString("audio.fileNameSeparator");
    extensionSeparator = config.getString("audio.fileExtensionSeparator");
    audioUploadUrl = config.getString("audio.uploadURL");
    awsAccessKeyId = config.getString("aws.accessKeyID");
    awsSecretKey = config.getString("aws.secretKey");
    audioFileBucket = config.getString("audio.fileBucket");
    audioFileUploadExpireMinutes = config.getInt("audio.uploadExpireMinutes");
    fileUploadACL = config.getString("aws.fileUploadACL");
    awsS3RetryLimit = config.getInt("aws.s3retryLimit");
  }

  /**
   Get an Amazon S3 client

   @return S3 client
   */
  private AmazonS3 s3Client() {
    BasicAWSCredentials credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey);
    return AmazonS3ClientBuilder.standard()
      .withRegion(awsDefaultRegion)
      .withCredentials(new AWSStaticCredentialsProvider(credentials))
      .build();
  }

  @Override
  public S3UploadPolicy generateAudioUploadPolicy() throws CoreException {
    return new S3UploadPolicy(getCredentialId(), getCredentialSecret(), getAudioUploadACL(), getAudioBucketName(), "", getAudioUploadExpireInMinutes());
  }

  @Override
  public String generateKey(String filename, String extension) {
    return String.format("%s%s%s%s%s", tokenGenerator.generateShort(), nameSeparator, filename, extensionSeparator, extension);
  }

  @Override
  public String getUploadURL() throws CoreException {
    return audioUploadUrl;
  }

  @Override
  public String getCredentialId() throws CoreException {
    return awsAccessKeyId;
  }

  @Override
  public String getCredentialSecret() throws CoreException {
    return awsSecretKey;
  }

  @Override
  public String getAudioBucketName() throws CoreException {
    return audioFileBucket;
  }

  @Override
  public int getAudioUploadExpireInMinutes() {
    return audioFileUploadExpireMinutes;
  }

  @Override
  public String getAudioUploadACL() {
    return fileUploadACL;
  }

  @Override
  public InputStream streamS3Object(String bucketName, String key) throws CoreException {
    AmazonS3 client = s3Client();
    GetObjectRequest request = new GetObjectRequest(bucketName, key);
    int count = 0;
    int maxTries = awsS3RetryLimit;
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
