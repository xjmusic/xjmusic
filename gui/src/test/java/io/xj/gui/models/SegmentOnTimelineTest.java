package io.xj.gui.models;

import io.xj.hub.enums.TemplateType;
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

import java.util.Objects;
import java.util.UUID;

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
    segment = buildSegment(SegmentState.CRAFTED);

    subject = new SegmentOnTimeline(segment);
  }

  @Test
  void getSegment() {
    assertSame(segment, subject.getSegment());
  }

  @Test
  void isActive_true() {
    subject = new SegmentOnTimeline(segment, segment.getBeginAtChainMicros() + 100);
    assertTrue(subject.isActive());
  }

  @Test
  void isActive_false() {
    subject = new SegmentOnTimeline(segment, segment.getBeginAtChainMicros() - 100);
    assertFalse(subject.isActive());
  }

  @Test
  void isSameButUpdated_falseIfIdentical() {
    var s1 = new SegmentOnTimeline(segment, segment.getBeginAtChainMicros() + 100);
    var s2 = new SegmentOnTimeline(segment, segment.getBeginAtChainMicros() + 100);
    assertFalse(s1.isSameButUpdated(s2));
  }

  @Test
  void isSameButUpdated_trueIfActivated() {
    var s1 = new SegmentOnTimeline(segment, segment.getBeginAtChainMicros() - 100);
    var s2 = new SegmentOnTimeline(segment, segment.getBeginAtChainMicros() + 100);
    assertTrue(s1.isSameButUpdated(s2));
  }

  @Test
  void isSameButUpdated_trueIfDeactivated() {
    var s1 = new SegmentOnTimeline(segment, segment.getBeginAtChainMicros() + 100);
    var s2 = new SegmentOnTimeline(segment, segment.getBeginAtChainMicros() - 100);
    assertTrue(s1.isSameButUpdated(s2));
  }

  @Test
  void isSameButUpdated_trueIfStateChanged() {
    var s1 = new SegmentOnTimeline(buildSegment(SegmentState.CRAFTING));
    var s2 = new SegmentOnTimeline(buildSegment(SegmentState.CRAFTED));
    assertTrue(s1.isSameButUpdated(s2));
  }

  @Test
  void getId() {
    assertEquals(12, subject.getId());
  }

  private Segment buildSegment(SegmentState state) {
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

  public static Account buildAccount(String name) {
    var account = new Account();
    account.setId(UUID.randomUUID());
    account.setName(name);
    return account;
  }

  public static Segment buildSegment(Chain chain, SegmentType type, int id, int delta, SegmentState state, String key, int total, double density, double tempo, String storageKey, boolean hasEndSet) {
    var segment = new Segment();
    segment.setChainId(chain.getId());
    segment.setType(type);
    segment.setId(id);
    segment.setDelta(delta);
    segment.setState(state);
    segment.setBeginAtChainMicros((long) (id * ValueUtils.MICROS_PER_SECOND * total * ValueUtils.SECONDS_PER_MINUTE / tempo));
    segment.setKey(key);
    segment.setTotal(total);
    segment.setDensity(density);
    segment.setTempo(tempo);
    segment.setStorageKey(storageKey);
    segment.setWaveformPreroll(0.0);
    segment.setWaveformPostroll(0.0);

    var durationMicros = (long) (ValueUtils.MICROS_PER_SECOND * total * ValueUtils.SECONDS_PER_MINUTE / tempo);
    if (hasEndSet)
      segment.setDurationMicros(durationMicros);

    return segment;
  }

  public static Template buildTemplate(Account account1, String name) {
    var template = new Template();
    template.setId(UUID.randomUUID());
    template.setShipKey("name");
    template.setType(TemplateType.Production);
    template.setAccountId(account1.getId());
    template.setName(name);
    return template;
  }

  public static Chain buildChain(Account account, String name, ChainType type, ChainState state, Template template, /*@Nullable*/ String shipKey) {
    var chain = new Chain();
    chain.setId(UUID.randomUUID());
    chain.setTemplateId(template.getId());
    chain.setAccountId(account.getId());
    chain.setName(name);
    chain.setType(type);
    chain.setState(state);
    chain.setTemplateConfig(template.getConfig());
    if (Objects.nonNull(shipKey))
      chain.shipKey(shipKey);
    return chain;
  }
}
