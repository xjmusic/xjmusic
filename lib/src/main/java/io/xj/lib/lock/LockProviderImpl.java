// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.lock;

import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LockProviderImpl implements LockProvider {
  static final Logger LOG = LoggerFactory.getLogger(LockProviderImpl.class);
  final FileStoreProvider fileStoreProvider;
  final LockGenerator lockGenerator;
  static final String CONTENT_TYPE = "text/plain";
  static final String CACHE_KEY_FMT = "%s/%s";
  static final String LOCK_FILE_FMT = "%s.lock";
  final Map<String, String> locks = new ConcurrentHashMap<>();

  @Autowired
  public LockProviderImpl(
    FileStoreProvider fileStoreProvider,
    LockGenerator lockGenerator
  ) {
    this.fileStoreProvider = fileStoreProvider;
    this.lockGenerator = lockGenerator;
  }

  @Override
  public void acquire(String bucket, String key) throws LockException {
    String cacheKey = computeCacheKey(bucket, key);
    if (!locks.containsKey(cacheKey))
      try {
        String lock = lockGenerator.get();

        fileStoreProvider.putS3ObjectFromString(lock, bucket, String.format(LOCK_FILE_FMT, key), CONTENT_TYPE, null);
        locks.put(cacheKey, lock);
        LOG.info("Did acquire lock: {}={}", cacheKey, lock);
      } catch (FileStoreException e) {
        throw new LockException(String.format("Failed to acquire lock: %s/%s", bucket, key));
      }
  }

  @Override
  public void check(String bucket, String key) throws LockException {
    String cacheKey = computeCacheKey(bucket, key);
    String lock = locks.get(cacheKey);
    if (lock == null) {
      throw new LockException(String.format("Lock not cached: %s/%s", bucket, key));
    }

    String lockCheck = fileStoreProvider.getS3Object(bucket, String.format(LOCK_FILE_FMT, key));
    if (Objects.isNull(lockCheck)) {
      throw new LockException(String.format("Lock not found: %s/%s", bucket, key));
    }
    if (!lock.equals(lockCheck)) {
      throw new LockException(String.format("Lock key mismatched: %s/%s", bucket, key));
    }
  }

  /**
   * Compute the cache key for the given bucket and key.
   *
   * @param bucket from which to compute key
   * @param key    from which to compute key
   * @return computed cache key
   */
  String computeCacheKey(String bucket, String key) {
    return String.format(CACHE_KEY_FMT, bucket, key);
  }
}
