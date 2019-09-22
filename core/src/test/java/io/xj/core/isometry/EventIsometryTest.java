//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.isometry;

import com.google.common.collect.ImmutableList;
import io.xj.core.model.program.sub.Event;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;

public class EventIsometryTest {

  @Test
  public void of_List() {
    EventIsometry result = EventIsometry.ofEvents(ImmutableList.of(
      new Event().setPatternId(UUID.randomUUID()).setName("Kick"),
      new Event().setPatternId(UUID.randomUUID()).setName("Snare")
    ));

    assertArrayEquals(new String[]{"KK", "SNR"}, result.getSources().toArray());
  }

  @Test
  public void add() {
    EventIsometry result = EventIsometry.ofEvents(ImmutableList.of(
      new Event().setPatternId(UUID.randomUUID()).setName("Kick")
    ));
    result.add(new Event().setPatternId(UUID.randomUUID()).setName("Snare"));

    assertArrayEquals(new String[]{"KK", "SNR"}, result.getSources().toArray());
  }

  @Test
  public void getSourceStems() {
    List<String> result = EventIsometry.ofEvents(ImmutableList.of(
      new Event().setPatternId(UUID.randomUUID()).setName("TomHigh"),
      new Event().setPatternId(UUID.randomUUID()).setName("TomLow"),
      new Event().setPatternId(UUID.randomUUID()).setName("Tom")
    )).getSources();

    assertArrayEquals(new String[]{"TMH", "TML", "TM"}, result.toArray());
  }

}
