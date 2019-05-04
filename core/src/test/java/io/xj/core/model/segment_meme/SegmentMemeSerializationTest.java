//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.segment_meme;

import com.google.gson.Gson;
import com.google.inject.Guice;
import io.xj.core.CoreModule;
import io.xj.core.transport.GsonProvider;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 [#166132897] Segment model handles all of its own entities
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 */
public class SegmentMemeSerializationTest {
  Gson gson = Guice.createInjector(new CoreModule()).getInstance(GsonProvider.class).gson();
  private SegmentMeme subject;
  private String subjectJson;

  private static void assertEquivalent(SegmentMeme m1, SegmentMeme m2) {
    assertEquals(m1.getSegmentId(), m2.getSegmentId());
    assertEquals(m1.getName(), m2.getName());
    assertEquals(m1.getUuid(), m2.getUuid());
  }

  @Before
  public void setUp() {
    subject = new SegmentMeme()
      .setSegmentId(BigInteger.valueOf(982))
      .setName("Green")
      .setUuid(UUID.fromString("12345678-9a75-48b0-9aa0-86409121465a"));
    subjectJson = "{\"uuid\":\"12345678-9a75-48b0-9aa0-86409121465a\",\"name\":\"Green\",\"segmentId\":982}";
  }

  @Test
  public void serialize() {
    String result = gson.toJson(subject);

    assertEquals(subjectJson, result);
  }

  @Test
  public void deserialize() {
    SegmentMeme result = gson.fromJson(subjectJson, SegmentMeme.class);

    assertEquivalent(subject, result);
  }

}
