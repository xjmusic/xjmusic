package io.xj.gui.models;

import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.util.ValueUtils;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.ChainType;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.model.SegmentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.xj.nexus.HubIntegrationTestingFixtures.buildAccount;
import static io.xj.nexus.HubIntegrationTestingFixtures.buildTemplate;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SegmentOnTimelineTest {
  private Chain chain;
  private Segment segment;
  private SegmentOnTimeline subject;

  @BeforeEach
  void setUp() {
    Account account = buildAccount("Test");
    Template template = buildTemplate(account, "Test");
    chain = buildChain(account, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, template, null);
    segment = prepareSegment(SegmentState.CRAFTED);

    subject = new SegmentOnTimeline(segment);
  }

  private Segment prepareSegment(SegmentState state) {
    return buildSegment(
      chain,
      SegmentType.INITIAL,
      12,
      0,
      state,
      "D major",
      64,
      0.73,
      120.0,
      "chains-1-segments-9f7s89d8a7892",
      true);
  }

  @Test
  void getSegment() {
    assertSame(segment, subject.getSegment());
  }

  @Test
  void isActive_true() {
    subject = new SegmentOnTimeline(segment, (long) (12.5 * 64 * ValueUtils.MICROS_PER_SECOND));
    assertTrue(subject.isActive());
  }

  @Test
  void isActive_false() {
    subject = new SegmentOnTimeline(segment, (long) (14.5 * 64 * ValueUtils.MICROS_PER_SECOND));
    assertFalse(subject.isActive());
  }

  @Test
  void isSameButUpdated_falseIfIdentical() {
    var s1 = new SegmentOnTimeline(segment, (long) (12.5 * 64 * ValueUtils.MICROS_PER_SECOND));
    var s2 = new SegmentOnTimeline(segment, (long) (12.5 * 64 * ValueUtils.MICROS_PER_SECOND));
    assertFalse(s1.isSameButUpdated(s2));
  }

  @Test
  void isSameButUpdated_trueIfActivated() {
    var s1 = new SegmentOnTimeline(segment, (long) (10.5 * 64 * ValueUtils.MICROS_PER_SECOND));
    var s2 = new SegmentOnTimeline(segment, (long) (12.5 * 64 * ValueUtils.MICROS_PER_SECOND));
    assertTrue(s1.isSameButUpdated(s2));
  }

  @Test
  void isSameButUpdated_trueIfDeactivated() {
    var s1 = new SegmentOnTimeline(segment, (long) (12.5 * 64 * ValueUtils.MICROS_PER_SECOND));
    var s2 = new SegmentOnTimeline(segment, (long) (14.5 * 64 * ValueUtils.MICROS_PER_SECOND));
    assertTrue(s1.isSameButUpdated(s2));
  }

  @Test
  void isSameButUpdated_trueIfStateChanged() {
    var s1 = new SegmentOnTimeline(prepareSegment(SegmentState.CRAFTING));
    var s2 = new SegmentOnTimeline(prepareSegment(SegmentState.CRAFTED));
    assertTrue(s1.isSameButUpdated(s2));
  }

  @Test
  void getId() {
    assertEquals(12, subject.getId());
  }
}
