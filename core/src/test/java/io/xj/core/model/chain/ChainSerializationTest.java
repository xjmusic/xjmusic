//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.chain;

import com.google.gson.Gson;
import com.google.inject.Guice;
import io.xj.core.CoreModule;
import io.xj.core.transport.GsonProvider;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

/**
 [#166132897] Chain model handles all of its own entities
 [#166273140] Chain Child Entities are identified and related by UUID (not id)
 */
public class ChainSerializationTest {
  Gson gson = Guice.createInjector(new CoreModule()).getInstance(GsonProvider.class).gson();
  private Chain subject;
  private String subjectJson;

  private static void assertEquivalent(Chain c1, Chain c2) {
    assertEquals(c1.getId(), c2.getId());
    assertEquals(c1.getState(), c2.getState());
    assertEquals(c1.getAccountId(), c2.getAccountId());
    assertEquals(c1.getType(), c2.getType());
    assertEquals(c1.getName(), c2.getName());
    assertEquals(c1.getStartAt(), c2.getStartAt());
    assertEquals(c1.getStopAt(), c2.getStopAt());
    assertEquals(c1.getEmbedKey(), c2.getEmbedKey());
    assertEquals(c1.getCreatedAt(), c2.getCreatedAt());
    assertEquals(c1.getUpdatedAt(), c2.getUpdatedAt());
  }

  @Before
  public void setUp() {
    subject = new Chain(BigInteger.valueOf(7))
      .setStateEnum(ChainState.Fabricate)
      .setAccountId(BigInteger.valueOf(5))
      .setTypeEnum(ChainType.Production)
      .setName("Bingo Ambience")
      .setStartAt("2014-07-12T12:00:00.00Z")
      .setStopAt("2014-08-12T12:00:00.00Z")
      .setEmbedKey("bingo_ambience");
    subject.setCreatedAt("2014-09-11T12:14:00.00Z");
    subject.setUpdatedAt("2014-09-11T12:15:00.00Z");
    subjectJson = "{\"id\":7,\"state\":\"Fabricate\",\"accountId\":5,\"type\":\"Production\",\"name\":\"Bingo Ambience\",\"startAt\":\"2014-07-12T12:00:00Z\",\"stopAt\":\"2014-08-12T12:00:00Z\",\"embedKey\":\"bingo_ambience\",\"createdAt\":\"2014-09-11T12:14:00Z\",\"updatedAt\":\"2014-09-11T12:15:00Z\"}";
  }

  @Test
  public void serialize() {
    String result = gson.toJson(subject);

    assertEquals(subjectJson, result);
  }

  @Test
  public void deserialize() {
    Chain result = gson.fromJson(subjectJson, Chain.class);

    assertEquivalent(subject, result);
  }

}
