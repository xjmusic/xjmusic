// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.persistence;

import io.xj.lib.app.AppEnvironment;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.Segment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FilePathProviderImpl implements FilePathProvider {
  private final String tempFilePathPrefix;

  @Autowired
  public FilePathProviderImpl(
    AppEnvironment env
  ) {
    this.tempFilePathPrefix = env.getTempFilePathPrefix();
  }

  @Override
  public String computeFullQualityAudioOutputFilePath(Segment segment) {
    return computeTempFilePath(Segments.getStorageFilename(segment));
  }

  @Override
  public String computeSegmentJsonOutputFilePath(Segment segment) {
    return computeTempFilePath(Segments.getStorageFilename(segment, FileStoreProvider.EXTENSION_JSON));
  }

  @Override
  public String computeChainFullJsonOutputFilePath(Chain chain) {
    return computeTempFilePath(Chains.getShipKey(Chains.getFullKey(Chains.computeBaseKey(chain)), FileStoreProvider.EXTENSION_JSON));
  }

  @Override
  public String computeChainJsonOutputFilePath(Chain chain) {
    return computeTempFilePath(Chains.getShipKey(Chains.computeBaseKey(chain), FileStoreProvider.EXTENSION_JSON));
  }

  @Override
  public String computeTempFilePath(String key) {
    return String.format("%s%s", tempFilePathPrefix, key);

  }

}
