// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.common.collect.ImmutableMap;
import io.xj.lib.app.AppEnvironment;
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
    AppEnvironment env = AppEnvironment.from(ImmutableMap.of(
      "SHIP_CHUNK_TARGET_DURATION", "10",
      "SHIP_KEY", "coolair"
    ));

    var chain = buildChain(buildTemplate(buildAccount("Testing"), "Testing"));
    chain.setTemplateConfig("metaSource = \"XJ Music Testing\"\nmetaTitle = \"Test Stream 5\"");

    subject = new MediaSeqNumProvider(env);
  }


  @Test
  public void computeMediaSequence() {
    assertEquals(164030295, subject.computeMediaSeqNum(1640302958444L));
  }

}
