package io.xj.service.nexus.entity;

import io.xj.lib.util.ValueException;
import org.junit.Test;

import static org.junit.Assert.*;

public class SegmentTypeTest {
  @Test
  public void validate() throws ValueException {
    assertEquals(SegmentType.Pending, SegmentType.validate("Pending"));
    assertEquals(SegmentType.Initial, SegmentType.validate("Initial"));
    assertEquals(SegmentType.Continue, SegmentType.validate("Continue"));
    assertEquals(SegmentType.NextMain, SegmentType.validate("NextMain"));
    assertEquals(SegmentType.NextMacro, SegmentType.validate("NextMacro"));
  }
}
