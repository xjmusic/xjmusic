// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.entity;

import io.xj.lib.util.ValueException;
import io.xj.service.hub.entity.Account;
import io.xj.service.nexus.testing.NexusTestConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.sound.sampled.AudioFormat;
import java.time.Instant;

import static org.junit.Assert.assertEquals;

public class ChainConfigTest {
  private ChainConfig subject;
  private Chain chain1;

  @Before
  public void setUp() throws ValueException {
    Account account25 = Account.create();
    chain1 = Chain.create(account25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null);
    subject = new ChainConfig(chain1, NexusTestConfiguration.getDefault());
  }

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void getOutputChannels() {
    assertEquals(2, subject.getOutputChannels());
  }

  @Test
  public void getOutputContainer() {
    assertEquals("AAC", subject.getOutputContainer());
  }

  @Test
  public void getOutputContainer_fromChain() throws ValueException {
    chain1.setConfig("outputContainer=\"BARGE\"");
    subject = new ChainConfig(chain1, NexusTestConfiguration.getDefault());

    assertEquals("BARGE", subject.getOutputContainer());
  }

  @Test
  public void getOutputEncoding() {
    assertEquals(AudioFormat.Encoding.PCM_SIGNED, subject.getOutputEncoding());
  }

  @Test
  public void getOutputEncodingQuality() {
    assertEquals(0.618, subject.getOutputEncodingQuality(), 0.001);
  }

  @Test
  public void getOutputFrameRate() {
    assertEquals(48000, subject.getOutputFrameRate());
  }

  @Test
  public void getOutputSampleBits() {
    assertEquals(16, subject.getOutputSampleBits());
  }

}
