// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.filestore;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.inject.Inject;
import io.xj.lib.app.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static io.xj.lib.util.Values.NANOS_PER_SECOND;

class FileStoreProviderImpl implements FileStoreProvider {
  private static final Logger log = LoggerFactory.getLogger(FileStoreProviderImpl.class);
  private static final String NAME_SEPARATOR = "-";
  private final String awsDefaultRegion;
  private final String audioUploadUrl;
  private final String awsAccessKeyId;
  private final String awsSecretKey;
  private final String audioFileBucket;
  private final int awsFileUploadExpireMinutes;
  private final String fileUploadACL;
  private final int awsS3RetryLimit;

  @Inject
  public FileStoreProviderImpl(
    Environment env
  ) {
    audioFileBucket = env.getAudioFileBucket();
    audioUploadUrl = env.getAudioUploadURL();
    awsAccessKeyId = env.getAwsAccessKeyID();
    awsDefaultRegion = env.getAwsDefaultRegion();
    awsFileUploadExpireMinutes = env.getAwsUploadExpireMinutes();
    awsS3RetryLimit = env.getAwsS3retryLimit();
    awsSecretKey = env.getAwsSecretKey();
    fileUploadACL = env.getAwsFileUploadACL();
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
  public S3UploadPolicy generateAudioUploadPolicy() {
    return new S3UploadPolicy(getCredentialId(), getCredentialSecret(), getAudioUploadACL(), getAudioBucketName(), "", getAudioUploadExpireInMinutes());
  }

  @Override
  public String generateKey(String filename) {
    return String.format("%s%s%s", filename, NAME_SEPARATOR, UUID.randomUUID());
  }

  @Override
  public String getUploadURL() {
    return audioUploadUrl;
  }

  @Override
  public String getCredentialId() {
    return awsAccessKeyId;
  }

  @Override
  public String getCredentialSecret() {
    return awsSecretKey;
  }

  @Override
  public String getAudioBucketName() {
    return audioFileBucket;
  }

  @Override
  public int getAudioUploadExpireInMinutes() {
    return awsFileUploadExpireMinutes;
  }

  @Override
  public String getAudioUploadACL() {
    return fileUploadACL;
  }

  @Override
  public InputStream streamS3Object(String bucketName, String key) throws FileStoreException {
    AmazonS3 client = s3Client();
    GetObjectRequest request = new GetObjectRequest(bucketName, key);
    int count = 0;
    while (true) {
      try {
        return client.getObject(request).getObjectContent();

      } catch (Exception e) {
        ++count;
        if (count == awsS3RetryLimit)
          throw new FileStoreException("Failed to stream S3 object", e);
      }
    }
  }

  @Override
  public void putS3ObjectFromTempFile(String filePath, String bucket, String key) throws FileStoreException {
    try {
      long startedAt = System.nanoTime();
      log.debug("Will ship {} to {}/{}", filePath, bucket, key);
      s3Client().putObject(new PutObjectRequest(
        bucket, key, new File(filePath)));
      log.debug("Did ship {} to {}/{} OK in {}s", filePath, bucket, key, String.format("%.9f", (double) (System.nanoTime() - startedAt) / NANOS_PER_SECOND));

    } catch (Exception e) {
      throw new FileStoreException("Failed to put S3 object", e);
    }
  }

  @Override
  public void putS3ObjectFromString(String content, String bucket, String key, String contentType) throws FileStoreException {
    try {
      long startedAt = System.nanoTime();
      log.debug("Will ship {} bytes of content to {}/{}", content.length(), bucket, key);


      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength(content.length());
      metadata.setContentType(contentType);
      s3Client().putObject(new PutObjectRequest(bucket, key, new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), metadata));
      log.debug("Did ship {} bytes to {}/{} OK in {}s", content.length(), bucket, key, String.format("%.9f", (double) (System.nanoTime() - startedAt) / NANOS_PER_SECOND));

    } catch (Exception e) {
      throw new FileStoreException("Failed to put S3 object", e);
    }
  }

  @Override
  public void deleteS3Object(String bucket, String key) throws FileStoreException {
    try {
      s3Client().deleteObject(bucket, key);

    } catch (Exception e) {
      throw new FileStoreException("Failed to delete S3 object", e);
    }
  }

  @Override
  public void copyS3Object(String sourceBucket, String sourceKey, String targetBucket, String targetKey) throws FileStoreException {
    try {
      s3Client().copyObject(sourceBucket, sourceKey, targetBucket, targetKey);

    } catch (Exception e) {
      throw new FileStoreException("Failed to revived S3 object", e);
    }
  }

}
