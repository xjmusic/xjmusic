// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.filestore;

import java.io.InputStream;

/**
 Requires these typesafe configurations to be set:
 - aws.defaultRegion
 - aws.accessKeyID
 - aws.secretKey
 */
public interface FileStoreProvider {
  String EXTENSION_JSON = "json";

  /**
   @return S3UploadPolicy for upload to AWS file storage (S3)
   */
  S3UploadPolicy generateAudioUploadPolicy() throws FileStoreException;

  /**
   Generate a new key of an object in AWS file storage (S3)
   [#

   @return key
   @param prefix of the key
   @param extension of the key
   */
  String generateKey(String prefix, String extension);

  /**
   Get the URL to upload an object to AWS file storage (S3)

   @return full URL
   */
  String getUploadURL() throws FileStoreException;

  /**
   Get the AWS Access Key ID

   @return key id
   */
  String getCredentialId() throws FileStoreException;

  /**
   Get the AWS Access Key Secret

   @return key id
   */
  String getCredentialSecret() throws FileStoreException;

  /**
   Get the AWS Bucket Name

   @return The name of the bucket in AWS file storage (S3)
   */
  String getAudioBucketName() throws FileStoreException;

  /**
   Get the AWS upload policy expire time in minutes

   @return The number of minutes before the upload policy expires and is unable to be used.
   */
  int getAudioUploadExpireInMinutes() throws FileStoreException;

  /**
   Get the AWS Access control list

   @return Access control list for upload to AWS file storage (S3)
   */
  String getAudioUploadACL() throws FileStoreException;

  /**
   Stream an object of S3

   @param bucketName to stream of
   @param key        of object to stream
   @return stream of object data
   */
  InputStream streamS3Object(String bucketName, String key) throws FileStoreException;

  /**
   Put an object to S3 (from a temp file)
   [#361] Segment & Audio S3 object key schema ought to have random String at the beginning of the key, in order to be optimized for S3 partitioning.

   @param filePath path to file for upload
   @param bucket   to put file to
   @param key      to put file at
   */
  void putS3ObjectFromTempFile(String filePath, String bucket, String key) throws FileStoreException;

  /**
   Put an object to S3 from a string
   [#162223929] Ship Segment data JSON with audio@param content path to file for upload

   @param bucket      to put file to
   @param key         to put file at
   @param contentType header for file
   */
  void putS3ObjectFromString(String content, String bucket, String key, String contentType) throws FileStoreException;

  /**
   Delete an object of S3
   If attempting to delete an object that does not exist,
   Amazon S3 returns a success message instead of an error message.

   @param bucket to delete file in
   @param key    to delete
   */
  void deleteS3Object(String bucket, String key) throws FileStoreException;

  /**
   Copy an object within S3
   If attempting to revived an object that does not exist,
   Amazon S3 returns a success message instead of an error message.

   @param sourceBucket to revived file in
   @param sourceKey    to revived
   @param targetBucket to revived file in
   @param targetKey    to revived
   */
  void copyS3Object(String sourceBucket, String sourceKey, String targetBucket, String targetKey) throws FileStoreException;

}
