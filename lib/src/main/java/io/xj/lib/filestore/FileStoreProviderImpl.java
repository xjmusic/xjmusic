// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.filestore;


import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class FileStoreProviderImpl implements FileStoreProvider {
  @Autowired
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
