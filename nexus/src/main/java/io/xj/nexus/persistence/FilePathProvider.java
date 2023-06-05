// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.persistence;

import io.xj.nexus.NexusException;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.Segment;

public interface FilePathProvider {

  /**
   * @return Output file path for a High-quality Audio file
   */
  String computeFullQualityAudioOutputFilePath(Segment segment) throws NexusException;

  /**
   * @return Output file path for a Segment JSON file
   */
  String computeSegmentJsonOutputFilePath(Segment segment) throws NexusException;

  /**
   * @return Output file path for a Chain Full JSON file
   */
  String computeChainFullJsonOutputFilePath(Chain chain) throws NexusException;

  /**
   * @return Output file path for a Chain JSON file
   */
  String computeChainJsonOutputFilePath(Chain chain) throws NexusException;

  /**
   * @return path to temp file for a given key
   */
  String computeTempFilePath(String key);
}
