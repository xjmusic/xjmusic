// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static io.xj.core.testing.Assert.assertSameItems;

public class PlatformMessageTest {

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("type",  "body"), new PlatformMessage().getResourceAttributeNames());
  }
}
