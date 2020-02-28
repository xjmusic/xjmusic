// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.craft.digest;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.core.CoreModule;
import io.xj.lib.core.IntegrationTestingFixtures;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.app.AppConfiguration;
import io.xj.lib.core.ingest.IngestFactory;
import io.xj.lib.core.model.Chain;
import io.xj.lib.core.model.ChainBinding;
import io.xj.lib.core.testing.AppTestConfiguration;
import io.xj.lib.core.testing.IntegrationTestProvider;
import io.xj.lib.craft.CraftModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class DigestChordMarkovIT {
  private IngestFactory ingestFactory;
  private DigestFactory digestFactory;
  private IntegrationTestingFixtures fake;
  private IntegrationTestProvider test;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableList.of(new CoreModule(), new CraftModule()));
    test = injector.getInstance(IntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();
    fake.insertFixtureA();

    fake.chain1 = Chain.create();

    ingestFactory = injector.getInstance(IngestFactory.class);
    digestFactory = injector.getInstance(DigestFactory.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void digest() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "User,Artist");

    DigestChordMarkov result = digestFactory.chordMarkov(ingestFactory.ingest(access, ImmutableList.of(ChainBinding.create(fake.chain1, fake.library10000001))));

    assertNotNull(result);
  }

}
