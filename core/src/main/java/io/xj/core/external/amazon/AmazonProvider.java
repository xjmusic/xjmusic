// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.external.amazon;

import io.xj.core.app.exception.ConfigException;
import io.xj.core.app.exception.NetworkException;

import java.io.BufferedInputStream;

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
  S3UploadPolicy generateAudioUploadPolicy() throws ConfigException;

  /**
   Generate a new key of an object in AWS file storage (S3)

   @param prefix    of the file
   @param extension of the file
   @return key
   */
  String generateKey(String prefix, String extension);

  /**
   Get the URL to upload an object to AWS file storage (S3)

   @return full URL
   */
  String getUploadURL() throws ConfigException;

  /**
   Get the AWS Access Key ID

   @return key id
   */
  String getCredentialId() throws ConfigException;

  /**
   Get the AWS Access Key Secret

   @return key id
   */
  String getCredentialSecret() throws ConfigException;

  /**
   Get the AWS Bucket Name

   @return The name of the bucket in AWS file storage (S3)
   */
  String getAudioBucketName() throws ConfigException;

  /**
   Get the AWS upload policy expire time in minutes

   @return The number of minutes before the upload policy expires and is unable to be used.
   */
  int getAudioUploadExpireInMinutes() throws ConfigException;

  /**
   Get the AWS Access control list

   @return Access control list for upload to AWS file storage (S3)
   */
  String getAudioUploadACL() throws ConfigException;

  /**
   Stream an object from S3

   @param bucketName to stream from
   @param key        of object to stream
   @return stream of object data
   */
  BufferedInputStream streamS3Object(String bucketName, String key) throws NetworkException;

  /**
   Put an object to S3 (from a file)

   @param filePath path to file for upload
   @param bucket   to put file to
   @param key      to put file at
   */
  void putS3Object(String filePath, String bucket, String key) throws NetworkException;

  /**
   Delete an object from S3
   If attempting to delete an object that does not exist,
   Amazon S3 returns a success message instead of an error message.

   @param bucket to delete file in
   @param key    to delete
   */
  void deleteS3Object(String bucket, String key) throws NetworkException;
}
