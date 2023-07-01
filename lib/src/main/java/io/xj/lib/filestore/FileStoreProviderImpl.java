// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.filestore;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

import static io.xj.lib.util.Values.NANOS_PER_SECOND;

@Service
class FileStoreProviderImpl implements FileStoreProvider {
  private static final Logger log = LoggerFactory.getLogger(FileStoreProviderImpl.class);

  private final String awsDefaultRegion;
  private final String audioUploadUrl;
  private final String awsAccessKeyId;
  private final String awsSecretKey;
  private final String audioFileBucket;
  private final int awsFileUploadExpireMinutes;
  private final String fileUploadACL;
  private final boolean active;

  @Autowired
  public FileStoreProviderImpl(
    @Value("${aws.default.region}")
    String awsDefaultRegion,
    @Value("${audio.upload.url}")
    String audioUploadUrl,
    @Value("${aws.access.key.id}")
    String awsAccessKeyId,
    @Value("${aws.secret.key}")
    String awsSecretKey,
    @Value("${audio.file.bucket}")
    String audioFileBucket,
    @Value("${aws.upload.expire.minutes}")
    int awsFileUploadExpireMinutes,
    @Value("${aws.file.upload.acl}")
    String fileUploadACL
  ) {
    active = !Strings.isNullOrEmpty(awsAccessKeyId) && !Strings.isNullOrEmpty(awsSecretKey);
    this.awsDefaultRegion = awsDefaultRegion;
    this.audioUploadUrl = audioUploadUrl;
    this.awsAccessKeyId = awsAccessKeyId;
    this.awsSecretKey = awsSecretKey;
    this.audioFileBucket = audioFileBucket;
    this.awsFileUploadExpireMinutes = awsFileUploadExpireMinutes;
    this.fileUploadACL = fileUploadACL;
  }

  /**
   * Get an Amazon S3 client
   *
   * @return S3 client
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
  public void putS3ObjectFromTempFile(String filePath, String bucket, String key, String contentType, @Nullable Integer expiresInSeconds) throws FileStoreException {
    try {
      if (!active) {
        log.debug("Will NOT ship {} to {}/{}", filePath, bucket, key);
        return;
      }
      long startedAt = System.nanoTime();
      log.debug("Will ship {} to {}/{}", filePath, bucket, key);

      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength(Files.size(Path.of(filePath)));
      metadata.setContentType(contentType);
      if (Objects.nonNull(expiresInSeconds)) {
        metadata.setExpirationTime(Date.from(Instant.now().plusSeconds(expiresInSeconds)));
        metadata.setCacheControl(String.format("max-age=%d", expiresInSeconds));
      }
      s3Client().putObject(new PutObjectRequest(bucket, key, new File(filePath)));
      log.debug("Did ship {} to {}/{} OK in {}s", filePath, bucket, key, String.format("%.9f", (double) (System.nanoTime() - startedAt) / NANOS_PER_SECOND));
    } catch (Exception e) {
      throw new FileStoreException("Failed to put S3 object", e);
    }
  }

  @Override
  public void putS3ObjectFromString(String content, String bucket, String key, String contentType, @Nullable Integer expiresInSeconds) throws FileStoreException {
    try {
      if (!active) {
        log.debug("Will NOT ship {} bytes of content to {}/{}", content.length(), bucket, key);
        return;
      }
      long startedAt = System.nanoTime();
      log.debug("Will ship {} bytes of content to {}/{}", content.length(), bucket, key);

      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength(content.length());
      metadata.setContentType(contentType);
      if (Objects.nonNull(expiresInSeconds)) {
        metadata.setExpirationTime(Date.from(Instant.now().plusSeconds(expiresInSeconds)));
        metadata.setCacheControl(String.format("max-age=%d", expiresInSeconds));
      }
      s3Client().putObject(new PutObjectRequest(bucket, key, new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), metadata));
      log.debug("Did ship {} bytes to {}/{} OK in {}s", content.length(), bucket, key, String.format("%.9f", (double) (System.nanoTime() - startedAt) / NANOS_PER_SECOND));

    } catch (Exception e) {
      throw new FileStoreException("Failed to put S3 object", e);
    }
  }

  @Override
  public String getS3Object(String bucket, String key) {
    if (!active) {
      throw new RuntimeException("FileStoreProvider is not active");
    }
    return s3Client().getObjectAsString(bucket, key);
  }

}
