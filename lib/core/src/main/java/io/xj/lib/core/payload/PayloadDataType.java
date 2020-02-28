// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.core.payload;

/**
 [#167276586] JSON API facilitates complex transactions
 <p>
Much of the complexity of serializing and deserializing stems of the fact that
 the JSON:API standard uses a data object for One record, and a data array for Many records.
 */
public enum PayloadDataType {
  Ambiguous,
  HasOne,
  HasMany
}
