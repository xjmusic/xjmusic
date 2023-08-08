// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.filestore;


import io.xj.hub.util.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.xj.hub.util.ValueUtils.NANOS_PER_SECOND;

@Service
class FileStoreProviderImpl implements FileStoreProvider {
  static final Logger log = LoggerFactory.getLogger(FileStoreProviderImpl.class);

  final String awsDefaultRegion;
  final String audioUploadUrl;
  final String awsAccessKeyId;
  final String awsSecretKey;
  final String audioFileBucket;
  final int awsFileUploadExpireMinutes;
  final String fileUploadACL;
  final boolean active;

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
    active = !StringUtils.isNullOrEmpty(awsAccessKeyId) && !StringUtils.isNullOrEmpty(awsSecretKey);
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
  S3Client s3Client() {
    AwsBasicCredentials awsCreds = AwsBasicCredentials.create(awsAccessKeyId, awsSecretKey);
    StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(awsCreds);
    return S3Client.builder()
      .region(Region.of(awsDefaultRegion))
      .credentialsProvider(credentialsProvider)
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

      Map<String, String> metadata = new HashMap<>();
      Path path = Path.of(filePath);
      metadata.put("Content-Length", String.valueOf(Files.size(path)));
      metadata.put("Content-Type", contentType);

      if (Objects.nonNull(expiresInSeconds)) {
        metadata.put("Expires", Date.from(Instant.now().plusSeconds(expiresInSeconds)).toString());
        metadata.put("Cache-Control", String.format("max-age=%d", expiresInSeconds));
      }

      RequestBody requestBody = RequestBody.fromFile(path);

      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .metadata(metadata)
        .build();

      try (var s3Client = s3Client()) {
        s3Client.putObject(putObjectRequest, requestBody);
      }
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

      Map<String, String> metadata = new HashMap<>();
      metadata.put("Content-Length", String.valueOf(content.length()));
      metadata.put("Content-Type", contentType);

      if (Objects.nonNull(expiresInSeconds)) {
        metadata.put("Expires", Date.from(Instant.now().plusSeconds(expiresInSeconds)).toString());
        metadata.put("Cache-Control", String.format("max-age=%d", expiresInSeconds));
      }

      RequestBody requestBody = RequestBody.fromString(content, StandardCharsets.UTF_8);

      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .metadata(metadata)
        .build();

      try (var s3Client = s3Client()) {
        s3Client.putObject(putObjectRequest, requestBody);
      }

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
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
      .bucket(bucket)
      .key(key)
      .build();

    try (var s3Client = s3Client()) {
      return s3Client.getObject(getObjectRequest,
        ResponseTransformer.toBytes()).asUtf8String();
    }
  }

}
