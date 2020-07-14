package io.xj.service.nexus.entity;

import io.xj.lib.util.ValueException;
import org.junit.Test;

import static org.junit.Assert.*;

public class SegmentStateTest {
  @Test
  public void validate() throws ValueException {
    assertEquals(SegmentState.Planned, SegmentState.validate("Planned"));
    assertEquals(SegmentState.Crafting, SegmentState.validate("Crafting"));
    assertEquals(SegmentState.Crafted, SegmentState.validate("Crafted"));
    assertEquals(SegmentState.Dubbing, SegmentState.validate("Dubbing"));
    assertEquals(SegmentState.Dubbed, SegmentState.validate("Dubbed"));
    assertEquals(SegmentState.Failed, SegmentState.validate("Failed"));
  }
}
