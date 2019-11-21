//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.platform_message;

import com.google.common.collect.ImmutableList;
import io.xj.core.model.PlatformMessage;
import org.junit.Test;

import static io.xj.core.testing.Assert.assertSameItems;

public class PlatformMessageTest {

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("type",  "body"), new PlatformMessage().getResourceAttributeNames());
  }
}
