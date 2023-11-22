// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.filestore;


import jakarta.annotation.Nullable;

public class FileStoreProviderImpl implements FileStoreProvider {
  public FileStoreProviderImpl(
  ) {
    // no op
  }

  @Override
  public void putS3ObjectFromTempFile(String filePath, String bucket, String key, String contentType, @Nullable Integer expiresInSeconds) throws FileStoreException {
    // no op
  }

  @Override
  public void putS3ObjectFromString(String content, String bucket, String key, String contentType, @Nullable Integer expiresInSeconds) throws FileStoreException {
    // no op
  }
}
