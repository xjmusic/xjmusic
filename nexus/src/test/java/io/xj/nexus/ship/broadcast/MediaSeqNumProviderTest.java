// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.ship.broadcast;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildAccount;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildTemplate;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class MediaSeqNumProviderTest {
  // Under Test
  MediaSeqNumProvider subject;

  @BeforeEach
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
