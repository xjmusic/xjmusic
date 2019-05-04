//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.pick;

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
public class PickSerializationTest {
  Gson gson = Guice.createInjector(new CoreModule()).getInstance(GsonProvider.class).gson();
  private Pick subject;
  private String subjectJson;

  private static void assertEquivalent(Pick p1, Pick p2) {
    assertEquals(p1.getArrangementUuid(), p2.getArrangementUuid());
    assertEquals(p1.getPatternEventId(), p2.getPatternEventId());
    assertEquals(p1.getAmplitude(), p2.getAmplitude());
    assertEquals(p1.getAudioId(), p2.getAudioId());
    assertEquals(p1.getInflection(), p2.getInflection());
    assertEquals(p1.getLength(), p2.getLength());
    assertEquals(p1.getPitch(), p2.getPitch());
    assertEquals(p1.getStart(), p2.getStart());
    assertEquals(p1.getVoiceId(), p2.getVoiceId());
    assertEquals(p1.getUuid(), p2.getUuid());
  }

  @Before
  public void setUp() {
    subject = new Pick()
      .setArrangementUuid(UUID.fromString("12345678-9a75-48b0-9aa0-86409121465a"))
      .setPatternEventId(BigInteger.valueOf(186732))
      .setAmplitude(1.0)
      .setAudioId(BigInteger.valueOf(128609))
      .setInflection("SMACK")
      .setLength(2.56)
      .setPitch(443.38)
      .setStart(0.5)
      .setVoiceId(BigInteger.valueOf(98))
      .setUuid(UUID.fromString("12345678-9a75-48b0-9aa0-86409121465a"));
    subjectJson = "{\"uuid\":\"12345678-9a75-48b0-9aa0-86409121465a\",\"arrangementUuid\":\"12345678-9a75-48b0-9aa0-86409121465a\",\"audioId\":128609,\"patternEventId\":186732,\"start\":0.5,\"length\":2.56,\"amplitude\":1.0,\"pitch\":443.38,\"inflection\":\"SMACK\",\"voiceId\":98}";
  }

  @Test
  public void serialize() {
    String result = gson.toJson(subject);

    assertEquals(subjectJson, result);
  }

  @Test
  public void deserialize() {
    Pick result = gson.fromJson(subjectJson, Pick.class);

    assertEquivalent(subject, result);
  }

}
