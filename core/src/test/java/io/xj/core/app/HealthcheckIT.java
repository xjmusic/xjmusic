// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.app;

import io.xj.core.CoreIT;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class HealthcheckIT extends CoreIT {
  private static final int STRESS_TEST_ITERATIONS = 10;
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private Health health;

  @Before
  public void setUp() throws Exception {
    reset();
    health = injector.getInstance(Health.class);
  }

  @Test
  public void check() throws Exception {
    for (int i = 0; STRESS_TEST_ITERATIONS > i; i++) {
      health.check();
    }
  }

}
