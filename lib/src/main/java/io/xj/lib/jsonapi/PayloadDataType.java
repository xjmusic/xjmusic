// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.jsonapi;

/**
 * Data Type for a Payload sent/received to/from a XJ Music REST JSON:API service
 * <p>
 * Created by Charney Kaye on 2020/03/05
 * <p>
 * Payloads are serialized & deserialized with custom Jackson implementations.
 * Much of the complexity of serializing and deserializing stems of the fact that
 * the JSON:API standard uses a data object for One record, and a data array for Many records.
 */
public enum PayloadDataType {
  Ambiguous,
  One,
  Many
}
