//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.arrangement;

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
public class ArrangementSerializationTest {
  Gson gson = Guice.createInjector(new CoreModule()).getInstance(GsonProvider.class).gson();
  private Arrangement subject;
  private String subjectJson;

  private static void assertEquivalent(Arrangement a1, Arrangement a2) {
    assertEquals(a1.getUuid(), a2.getUuid());
    assertEquals(a1.getChoiceUuid(), a2.getChoiceUuid());
    assertEquals(a1.getVoiceId(), a2.getVoiceId());
    assertEquals(a1.getInstrumentId(), a2.getInstrumentId());
  }

  @Before
  public void setUp() {
    subject = new Arrangement()
      .setInstrumentId(BigInteger.valueOf(98))
      .setVoiceId(BigInteger.valueOf(7879))
      .setUuid(UUID.fromString("12345678-9a75-48b0-9aa0-86409121465a"))
      .setChoiceUuid(UUID.fromString("12345678-9a75-48b0-9aa0-86409121465a"));
    subjectJson = "{\"uuid\":\"12345678-9a75-48b0-9aa0-86409121465a\",\"choiceUuid\":\"12345678-9a75-48b0-9aa0-86409121465a\",\"voiceId\":7879,\"instrumentId\":98}";
  }

  @Test
  public void serialize() {
    String result = gson.toJson(subject);

    assertEquals(subjectJson, result);
  }

  @Test
  public void deserialize() {
    Arrangement result = gson.fromJson(subjectJson, Arrangement.class);

    assertEquivalent(subject, result);
  }

}
