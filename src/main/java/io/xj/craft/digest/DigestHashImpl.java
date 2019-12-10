// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.ingest.Ingest;
import io.xj.core.entity.Entity;
import io.xj.core.util.CSV;
import io.xj.core.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 In-memory cache of ingest of all hash in a library
 <p>
 [#154234716] Architect wants ingest of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
public class DigestHashImpl extends DigestImpl implements DigestHash {
  private final Map<String, String> hash = Maps.newHashMap();

  /**
   Instantiate a new digest with a collection of target entities

   @param ingest to digest
   */
  @Inject
  public DigestHashImpl(
    @Assisted("ingest") Ingest ingest
  ) {
    super(ingest, DigestType.DigestHash);
    try {
      digest();
    } catch (Exception e) {
      Logger log = LoggerFactory.getLogger(DigestHashImpl.class);
      log.error("Failed to digest hash create ingest {}", ingest, e);
    }
  }

  /**
   Get the hash key of one entity

   @param entity to get hash key of
   @return hash key
   */
  private static String hashKey(Entity entity) {
    return String.format("%s-%s",
      Text.getSimpleName(entity),
      entity.getId());
  }

  /**
   Get the hash value of one entity

   @param entity to get hash value of
   @return hash value
   */
  private static String hashValue(Entity entity) {
    return String.format("%d",
      Objects.requireNonNull(entity.getUpdatedAt()).getEpochSecond());
  }

  /**
   Digest the ingest entities into a hash map
   */
  private void digest() {
    ingest.getAllEntities().forEach(entity -> hash.put(hashKey(entity), hashValue(entity)));
  }

  @Override
  public String toString() {
    List<String> tuples = Lists.newArrayList();
    hash.forEach((key, value) -> tuples.add(String.format("%s=%s", key, value)));
    tuples.sort(Comparator.naturalOrder());
    return CSV.join(tuples);
  }

  @Override
  public String sha256() {
    return Hashing.sha256()
      .hashString(toString(), StandardCharsets.UTF_8)
      .toString();
  }

}

