// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.evaluation.digest.hash.impl;

import io.xj.core.evaluation.digest.DigestType;
import io.xj.core.evaluation.Evaluation;
import io.xj.core.evaluation.digest.hash.DigestHash;
import io.xj.core.evaluation.impl.DigestImpl;
import io.xj.core.model.entity.Entity;
import io.xj.core.transport.CSV;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 In-memory cache of evaluation of all hash in a library
 <p>
 [#154234716] Architect wants evaluation of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
public class DigestHashImpl extends DigestImpl implements DigestHash {
  private final Logger log = LoggerFactory.getLogger(DigestHashImpl.class);
  private final Map<String, String> hash = Maps.newConcurrentMap();

  /**
   Instantiate a new digest with a collection of target entities

   @param evaluation to digest
   */
  @Inject
  public DigestHashImpl(
    @Assisted("evaluation") Evaluation evaluation
  ) {
    super(evaluation, DigestType.DigestHash);
    try {
      digest();
    } catch (Exception e) {
      log.error("Failed to digest hash of evaluation {}", evaluation, e);
    }
  }

  /**
   Get the hash key of one entity

   @param entity to get hash key of
   @return hash key
   */
  private static String hashKey(Entity entity) {
    return String.format("%s-%s",
      entity.getClass().getSimpleName(),
      entity.getId());
  }

  /**
   Get the hash value of one entity

   @param entity to get hash value of
   @return hash value
   */
  private static String hashValue(Entity entity) {
    return String.format("%d",
      entity.getUpdatedAt().toInstant().getEpochSecond());
  }

  /**
   Digest the evaluation entities into a hash map
   */
  private void digest() {
    evaluation.all().forEach(entity -> hash.put(hashKey(entity), hashValue(entity)));
  }

  @Override
  public JSONObject toJSONObject() {
    return new JSONObject(hash);
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

