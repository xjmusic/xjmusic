//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.payload;

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
