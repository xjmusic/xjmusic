// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.filestore;

import org.jetbrains.annotations.Nullable;

/**
 * Requires these typesafe configurations to be set:
 * - aws.defaultRegion
 * - aws.accessKeyID
 * - aws.secretKey
 */
public interface FileStoreProvider {
  String EXTENSION_JSON = "json";

  /**
   * @return S3UploadPolicy for upload to AWS file storage (S3)
   */
  S3UploadPolicy generateAudioUploadPolicy() throws FileStoreException;

  /**
   * Get the URL to upload an object to AWS file storage (S3)
   *
   * @return full URL
   */
  String getUploadURL() throws FileStoreException;

  /**
   * Get the AWS Access Key ID
   *
   * @return key id
   */
  String getCredentialId() throws FileStoreException;

  /**
   * Get the AWS Access Key Secret
   *
   * @return key id
   */
  String getCredentialSecret() throws FileStoreException;

  /**
   * Get the AWS Bucket Name
   *
   * @return The name of the bucket in AWS file storage (S3)
   */
  String getAudioBucketName() throws FileStoreException;

  /**
   * Get the AWS upload policy expire time in minutes
   *
   * @return The number of minutes before the upload policy expires and is unable to be used.
   */
  int getAudioUploadExpireInMinutes() throws FileStoreException;

  /**
   * Get the AWS Access control list
   *
   * @return Access control list for upload to AWS file storage (S3)
   */
  String getAudioUploadACL() throws FileStoreException;

  /**
   * Put an object to S3 (from a temp file)
   * [#361] Segment & Audio S3 object key schema ought to have random String at the beginning of the key, in order to be optimized for S3 partitioning.@param filePath path to file for upload
   *
   * @param bucket           to put file to
   * @param key              to put file at
   * @param contentType      content-type
   * @param expiresInSeconds seconds until cache should expire
   */
  void putS3ObjectFromTempFile(String filePath, String bucket, String key, String contentType, @Nullable Integer expiresInSeconds) throws FileStoreException;

  /**
   * Put an object to S3 from a string
   * Ship Segment data JSON with audio https://www.pivotaltracker.com/story/show/162223929
   *
   * @param content          path to file for upload
   * @param bucket           to put file to
   * @param key              to put file at
   * @param contentType      header for file
   * @param expiresInSeconds seconds until cache should expire
   */
  void putS3ObjectFromString(String content, String bucket, String key, String contentType, @Nullable Integer expiresInSeconds) throws FileStoreException;

  /**
   * Get an object from S3
   *
   * @param bucket the bucket
   * @param key    the key
   * @return the object
   */
  String getS3Object(String bucket, String key);
}
