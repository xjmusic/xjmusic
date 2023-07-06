// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.ship.broadcast;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class MediaSeqNumProviderTest {
  // Under Test
  private MediaSeqNumProvider subject;

  @Before
  public void setUp() {
    var chain = buildChain(buildTemplate(buildAccount("Testing"), "Testing"));
    chain.setTemplateConfig("metaSource = \"XJ Music Testing\"\nmetaTitle = \"Test Stream 5\"");

    subject = new MediaSeqNumProvider(10);
  }


  @Test
  public void computeMediaSequence() {
    assertEquals(90, subject.computeMediaSeqNum(900000000L));
  }

}
