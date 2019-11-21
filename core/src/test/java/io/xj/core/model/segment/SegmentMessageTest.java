//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.SegmentMessage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static io.xj.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;

public class SegmentMessageTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new SegmentMessage()
      .setType("Warning")
      .setSegmentId(UUID.randomUUID())
      .setBody("This is a warning")
      .validate();
  }

  @Test
  public void validate_failsWithoutSegmentID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Segment ID is required");

    new SegmentMessage()
      .setType("Warning")
      .setBody("This is a warning")
      .validate();
  }

  @Test
  public void validate_failsWithoutType() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Type is required");

    new SegmentMessage()
      .setSegmentId(UUID.randomUUID())
      .setBody("This is a warning")
      .validate();
  }

  @Test
  public void validate_failsWithInvalidType() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("not a valid type");

    new SegmentMessage()
      .setType("sneeze")
      .setSegmentId(UUID.randomUUID())
      .setBody("This is a warning")
      .validate();
  }

  @Test
  public void validate_failsWithoutBody() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Body is required");

    new SegmentMessage()
      .setType("Warning")
      .setSegmentId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("body", "type"), new SegmentMessage().getResourceAttributeNames());
  }

}
