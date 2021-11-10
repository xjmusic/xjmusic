// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.craft.background;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class BackgroundCraftImplTest {
  @Test
  public void withIdsRemoved() {
    var result = BackgroundCraftImpl.withIdsRemoved(ImmutableList.of(
      UUID.randomUUID(),
      UUID.randomUUID(),
      UUID.randomUUID(),
      UUID.randomUUID(),
      UUID.randomUUID()
    ), 2);

    assertEquals(3, result.size());
  }
}
