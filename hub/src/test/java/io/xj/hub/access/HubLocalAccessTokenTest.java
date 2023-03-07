// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

@RunWith(MockitoJUnitRunner.class)
public class HubLocalAccessTokenTest {
  private HubAccessTokenGenerator hubAccessTokenGenerator;

  @Before
  public void setUp() throws Exception {
    hubAccessTokenGenerator = new HubAccessTokenGeneratorImpl();
  }

  @Test
  public void generate_UniqueTokens() {
    String t1 = hubAccessTokenGenerator.generate();
    String t2 = hubAccessTokenGenerator.generate();
    assertNotNull(t1);
    assertNotNull(t2);
    assertNotSame(t1, t2);
  }
}
