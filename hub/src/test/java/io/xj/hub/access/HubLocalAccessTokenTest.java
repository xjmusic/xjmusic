// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;

import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

@SpringBootTest
public class HubLocalAccessTokenTest {
  private HubAccessTokenGenerator hubAccessTokenGenerator;

  @BeforeEach
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
