// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.lock;

public interface LockProvider {
  /**
   * Acquire the lock, e.g. on the target chain in order to begin fabrication
   *
   * @param bucket the bucket
   * @param key    the key
   */
  void acquire(String bucket, String key) throws LockException;

  /**
   * Whether the lock is still held, e.g. no other process has acquired it
   *
   * @param bucket the bucket
   * @param key    the key
   * @return true if the lock is still held
   */
  void check(String bucket, String key) throws LockException;
}
