// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.external.amazon;

import io.outright.xj.core.app.exception.ConfigException;

/**
 *  Requires these System Properties to be set:
 *    aws.file.upload.url
 *    aws.file.upload.key
 *    aws.file.upload.secret
 */
public interface AmazonProvider {

  /**
   * @return String authorization code request URL
   * @param key of resource to generate an upload policy for
   */
  String generateUploadPolicy(String key) throws ConfigException;

  /**
   * Generate a new key of an object in AWS file storage (S3)
   * @param prefix of the file
   * @param extension of the file
   * @return key
   */
  String generateKey(String prefix, String extension);

  /**
   * Get the URL to upload an object to AWS file storage (S3)
   * @return full URL
   */
  String getUploadURL();

  /**
   * Get the AWS Access Key ID
   * @return key id
   */
  String getAccessKey();
}
