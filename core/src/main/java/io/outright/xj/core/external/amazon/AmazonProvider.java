// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.external.amazon;

import io.outright.xj.core.app.exception.ConfigException;

/**
 Requires these System Properties to be set:
 aws.file.upload.url
 aws.file.upload.key
 aws.file.upload.secret
 */
public interface AmazonProvider {

  /**
   @return S3UploadPolicy for upload to AWS file storage (S3)
   */
  S3UploadPolicy generateUploadPolicy() throws ConfigException;

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
  String getAccessKey() throws ConfigException;

  /**
   Get the AWS Access Key Secret

   @return key id
   */
  String getAccessSecret() throws ConfigException;

  /**
   Get the AWS Bucket Name

   @return The name of the bucket in AWS file storage (S3)
   */
  String getBucketName() throws ConfigException;

  /**
   Get the AWS upload policy expire time in minutes

   @return The number of minutes before the upload policy expires and is unable to be used.
   */
  int getExpireInMinutes() throws ConfigException;

  /**
   Get the AWS Access control list

   @return Access control list for upload to AWS file storage (S3)
   */
  String getUploadACL() throws ConfigException;
}
