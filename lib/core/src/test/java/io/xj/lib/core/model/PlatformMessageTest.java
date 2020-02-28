// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.core.model;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static io.xj.lib.core.testing.Assert.assertSameItems;

public class PlatformMessageTest {

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("type",  "body"), new PlatformMessage().getResourceAttributeNames());
  }
}
