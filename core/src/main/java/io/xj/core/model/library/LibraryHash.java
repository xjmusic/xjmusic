// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.library;

import io.xj.core.transport.CSV;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;

import org.json.JSONObject;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 [#154343470] Ops wants LibraryHash to compute the hash of an entire library, which can be used as a unique stamp of the state of the library's entire contents at any instant
 */
public class LibraryHash {
  private final Map<String, Timestamp> entities = Maps.newConcurrentMap();
  private final BigInteger libraryId;

  /**
   New LibraryHash instance

   @param libraryId to create checksum for
   */
  public LibraryHash(BigInteger libraryId) {
    this.libraryId = libraryId;
  }

  /**
   Get library id

   @return library id
   */
  public BigInteger getLibraryId() {
    return libraryId;
  }

  /**
   Get the map of all entities and their timestamps

   @return map of entity ids to timestamps
   */
  public Map<String, Timestamp> getEntities() {
    return Collections.unmodifiableMap(entities);
  }

  /**
   Output entire checksum as a JSONObject

   @return json object
   */
  public JSONObject toJSONObject() {
    JSONObject result = new JSONObject();
    entities.forEach((entityId, timestamp) -> result.put(entityId, timestamp.toString()));
    return result;
  }

  /**
   Output entire checksum as a String

   @return string
   */
  public String toString() {
    Collection<String> tuples = Lists.newArrayList();
    entities.forEach((entityId, timestamp) -> tuples.add(String.format("%s=%d", entityId, timestamp.getTime())));
    return CSV.join(tuples);
  }

  /**
   Output entire checksum as a String

   @return string
   */
  public String sha256() {
    return Hashing.sha256()
      .hashString(toString(), StandardCharsets.UTF_8)
      .toString();
  }

  /**
   Put a new entity into the checksum

   @param entityKey to put
   @param timestamp of entity
   */
  public void putEntity(String entityKey, Timestamp timestamp) {
    entities.put(entityKey, timestamp);
  }
}
