package io.xj.gui.models;

import io.xj.hub.tables.pojos.Account;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.ChainType;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.model.SegmentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.xj.nexus.HubIntegrationTestingFixtures.buildAccount;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static org.junit.jupiter.api.Assertions.*;

class SegmentOnTimelineTest {

  private Account account;
  private Chain chain;
  private Segment segment;
  private SegmentOnTimeline subject;

  @BeforeEach
  void setUp() {
    account = buildAccount("Test");
    chain = buildChain(account, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, template, null);
    segment = buildSegment(
      chain,
      SegmentType.INITIAL,
      0,
      0,
      SegmentState.CRAFTED,
      "D major",
      64,
      0.73,
      120.0,
      "chains-1-segments-9f7s89d8a7892",
      true);

    subject = new SegmentOnTimeline(segment);
  }

  @Test
  void getSegment() {
  }

  @Test
  void isActive() {
  }

  @Test
  void setActive() {
  }

  @Test
  void isSameButUpdated() {
  }

  @Test
  void getId() {
  }
}
