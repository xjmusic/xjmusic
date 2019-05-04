//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.choice;

import com.google.gson.Gson;
import com.google.inject.Guice;
import io.xj.core.CoreModule;
import io.xj.core.model.sequence.SequenceType;
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
public class ChoiceSerializationTest {
  Gson gson = Guice.createInjector(new CoreModule()).getInstance(GsonProvider.class).gson();
  private Choice subject;
  private String subjectJson;

  private static void assertEquivalent(Choice c1, Choice c2) {
    assertEquals(c1.getType(), c2.getType());
    assertEquals(c1.getSequenceId(), c2.getSequenceId());
    assertEquals(c1.getSegmentId(), c2.getSegmentId());
    assertEquals(c1.getTranspose(), c2.getTranspose());
    assertEquals(c1.getUuid(), c2.getUuid());
  }

  @Before
  public void setUp() {
    subject = new Choice()
      .setTypeEnum(SequenceType.Rhythm)
      .setSequenceId(BigInteger.valueOf(97))
      .setSegmentId(BigInteger.valueOf(982))
      .setTranspose(-5)
      .setUuid(UUID.fromString("12345678-9a75-48b0-9aa0-86409121465a"));
    subjectJson = "{\"uuid\":\"12345678-9a75-48b0-9aa0-86409121465a\",\"type\":\"Rhythm\",\"sequenceId\":97,\"segmentId\":982,\"transpose\":-5}";
  }

  @Test
  public void serialize() {
    String result = gson.toJson(subject);

    assertEquals(subjectJson, result);
  }

  @Test
  public void deserialize() {
    Choice result = gson.fromJson(subjectJson, Choice.class);

    assertEquivalent(subject, result);
  }

}
