//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.isometry;

import com.google.common.collect.ImmutableList;
import io.xj.core.model.pattern_event.PatternEvent;
import org.junit.Test;

import java.math.BigInteger;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

public class EventIsometryTest {

  @Test
  public void of_List() {
    EventIsometry result = EventIsometry.ofEvents(ImmutableList.of(
      new PatternEvent().setPatternId(BigInteger.valueOf(12)).setInflection("Kick"),
      new PatternEvent().setPatternId(BigInteger.valueOf(14)).setInflection("Snare")
    ));

    assertArrayEquals(new String[]{"KK", "SNR"}, result.getSources().toArray());
  }

  @Test
  public void add() {
    EventIsometry result = EventIsometry.ofEvents(ImmutableList.of(
      new PatternEvent().setPatternId(BigInteger.valueOf(12)).setInflection("Kick")
    ));
    result.add(new PatternEvent().setPatternId(BigInteger.valueOf(14)).setInflection("Snare"));

    assertArrayEquals(new String[]{"KK", "SNR"}, result.getSources().toArray());
  }

  @Test
  public void getSourceStems() {
    List<String> result = EventIsometry.ofEvents(ImmutableList.of(
      new PatternEvent().setPatternId(BigInteger.valueOf(6)).setInflection("TomHigh"),
      new PatternEvent().setPatternId(BigInteger.valueOf(7)).setInflection("TomLow"),
      new PatternEvent().setPatternId(BigInteger.valueOf(8)).setInflection("Tom")
    )).getSources();

    assertArrayEquals(new String[]{"TMH", "TML", "TM"}, result.toArray());
  }

}
