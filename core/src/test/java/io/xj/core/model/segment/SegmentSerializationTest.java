//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.segment;

import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.transport.GsonProvider;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

/**
 [#166132897] Segment model handles all of its own entities
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 */
public class SegmentSerializationTest {
  Gson gson;
  Injector injector;
  SegmentFactory segmentFactory;
  Segment segment;
  private String subjectJson;

  private static void assertEquivalent(Segment c1, Segment c2) {
    assertEquals(c1.getType(), c2.getType());
    assertEquals(c1.getId(), c2.getId());
    assertEquals(c1.getChainId(), c2.getChainId());
    assertEquals(c1.getOffset(), c2.getOffset());
    assertEquals(c1.getState(), c2.getState());
    assertEquals(c1.getBeginAt(), c2.getBeginAt());
    assertEquals(c1.getEndAt(), c2.getEndAt());
    assertEquals(c1.getTotal(), c2.getTotal());
    assertEquals(c1.getDensity(), c2.getDensity());
    assertEquals(c1.getKey(), c2.getKey());
    assertEquals(c1.getTempo(), c2.getTempo());
    assertEquals(c1.getCreatedAt(), c2.getCreatedAt());
    assertEquals(c1.getUpdatedAt(), c2.getUpdatedAt());
    assertEquals(c1.getWaveformKey(), c2.getWaveformKey());
  }

  @Before
  public void setUp() {
    injector = Guice.createInjector(new CoreModule());
    gson = injector.getInstance(GsonProvider.class).gson();
    segmentFactory = injector.getInstance(SegmentFactory.class);
    segment = segmentFactory.newSegment(BigInteger.valueOf(7))
      .setChainId(BigInteger.valueOf(5))
    .setOffset(BigInteger.valueOf(25))
      .setStateEnum(SegmentState.Dubbed)
    .setBeginAt("2014-08-12 12:17:02.52714")
      .setEndAt("2014-09-11 12:17:01.0475")
    .setTotal(16)
      .setDensity(0.85)
    .setKey("G Major")
      .setTempo(120.0)
      .setWaveformKey("a1b2c3d4e5g6.ogg");
    segment.setCreatedAt("2014-09-11 12:14:00.00");
    segment.setUpdatedAt("2014-09-11 12:15:00.00");
    subjectJson = "{\"id\":7,\"chainId\":5,\"offset\":25,\"state\":\"Dubbed\",\"beginAt\":\"2014-08-12 12:17:02.52714Z\",\"endAt\":\"2014-09-11 12:17:01.0475Z\",\"total\":16,\"density\":0.85,\"key\":\"G Major\",\"tempo\":120.0,\"createdAt\":\"2014-09-11 12:14:00.0Z\",\"updatedAt\":\"2014-09-11 12:15:00.0Z\",\"waveformKey\":\"a1b2c3d4e5g6.ogg\"}";
  }

  @Test
  public void serialize() {
    String result = gson.toJson(segment);

    assertEquals(subjectJson, result);
  }

  @Test
  public void deserialize() {
    Segment result = gson.fromJson(subjectJson, Segment.class);

    assertEquivalent(segment, result);
  }

}
