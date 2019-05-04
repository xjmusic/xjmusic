//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.segment_message;

import com.google.gson.Gson;
import com.google.inject.Guice;
import io.xj.core.CoreModule;
import io.xj.core.model.message.MessageType;
import io.xj.core.model.segment_message.SegmentMessage;
import io.xj.core.transport.GsonProvider;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 [#166132897] Segment model handles all of its own entities
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 */
public class SegmentMessageSerializationTest {
  Gson gson = Guice.createInjector(new CoreModule()).getInstance(GsonProvider.class).gson();
  private SegmentMessage subject;
  private String subjectJson;

  private static void assertEquivalent(SegmentMessage m1, SegmentMessage m2) {
    assertEquals(m1.getSegmentId(), m2.getSegmentId());
    assertEquals(m1.getBody(), m2.getBody());
    assertSame(m1.getType(), m2.getType());
  }

  @Before
  public void setUp() {
    subject = new SegmentMessage()
      .setSegmentId(BigInteger.valueOf(982))
      .setTypeEnum(MessageType.Warning)
      .setBody("Be Awarned!")
      .setUuid(UUID.fromString("12345678-9a75-48b0-9aa0-86409121465a"));
    subjectJson = "{\"uuid\":\"12345678-9a75-48b0-9aa0-86409121465a\",\"type\":\"Warning\",\"body\":\"Be Awarned!\",\"segmentId\":982}";
  }

  @Test
  public void serialize() {
    String result = gson.toJson(subject);

    assertEquals(subjectJson, result);
  }

  @Test
  public void deserialize() {
    SegmentMessage result = gson.fromJson(subjectJson, SegmentMessage.class);

    assertEquivalent(subject, result);
  }

}
