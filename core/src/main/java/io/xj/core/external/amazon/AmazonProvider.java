// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.external.amazon;

import io.xj.core.exception.CoreException;
import io.xj.core.exception.CoreException;

import java.io.InputStream;

/**
 Requires these System Properties to be set:
 audio.url.upload
 aws.accessKeyId
 aws.secretKey
 */
public interface AmazonProvider {

  /**
   @return S3UploadPolicy for upload to AWS file storage (S3)
   */
  S3UploadPolicy generateAudioUploadPolicy() throws CoreException;

  /**
   Generate a new key of an object in AWS file storage (S3)
   [#

   @param prefix    of the file
   @param extension of the file
   @return key
   */
  String generateKey(String prefix, String extension);

  /**
   Get the URL to upload an object to AWS file storage (S3)

   @return full URL
   */
  String getUploadURL() throws CoreException;

  /**
   Get the AWS Access Key ID

   @return key id
   */
  String getCredentialId() throws CoreException;

  /**
   Get the AWS Access Key Secret

   @return key id
   */
  String getCredentialSecret() throws CoreException;

  /**
   Get the AWS Bucket Name

   @return The name of the bucket in AWS file storage (S3)
   */
  String getAudioBucketName() throws CoreException;

  /**
   Get the AWS upload policy expire time in minutes

   @return The number of minutes before the upload policy expires and is unable to be used.
   */
  int getAudioUploadExpireInMinutes() throws CoreException;

  /**
   Get the AWS Access control list

   @return Access control list for upload to AWS file storage (S3)
   */
  String getAudioUploadACL() throws CoreException;

  /**
   Stream an object from S3

   @param bucketName to stream from
   @param key        of object to stream
   @return stream of object data
   */
  InputStream streamS3Object(String bucketName, String key) throws CoreException;

  /**
   Put an object to S3 (from a file)
   [#361] Segment & Audio S3 object key schema ought to have random UUID at the beginning of the key, in order to be optimized for S3 partitioning.

   @param filePath path to file for upload
   @param bucket   to put file to
   @param key      to put file at
   */
  void putS3Object(String filePath, String bucket, String key) throws CoreException;

  /**
   Delete an object from S3
   If attempting to delete an object that does not exist,
   Amazon S3 returns a success message instead of an error message.

   @param bucket to delete file in
   @param key    to delete
   */
  void deleteS3Object(String bucket, String key) throws CoreException;

  /**
   Copy an object within S3
   If attempting to revived an object that does not exist,
   Amazon S3 returns a success message instead of an error message.

   @param sourceBucket to revived file in
   @param sourceKey    to revived
   @param targetBucket to revived file in
   @param targetKey    to revived
   */
  void copyS3Object(String sourceBucket, String sourceKey, String targetBucket, String targetKey) throws CoreException;
}
