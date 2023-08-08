// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.ship.broadcast;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static io.xj.test_fixtures.HubIntegrationTestingFixtures.buildAccount;
import static io.xj.test_fixtures.HubIntegrationTestingFixtures.buildTemplate;
import static io.xj.test_fixtures.NexusIntegrationTestingFixtures.buildChain;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class MediaSeqNumProviderTest {
  // Under Test
  MediaSeqNumProvider subject;

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
