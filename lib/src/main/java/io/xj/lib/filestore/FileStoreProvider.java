// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.filestore;

import org.jetbrains.annotations.Nullable;

/**
 No-op stub for FileStoreProvider
 */
public interface FileStoreProvider {

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
}
