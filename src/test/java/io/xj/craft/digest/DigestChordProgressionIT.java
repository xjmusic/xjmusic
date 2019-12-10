// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.core.CoreModule;
import io.xj.core.IntegrationTestingFixtures;
import io.xj.core.access.Access;
import io.xj.core.app.AppConfiguration;
import io.xj.core.ingest.IngestFactory;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainBinding;
import io.xj.core.testing.AppTestConfiguration;
import io.xj.core.testing.IntegrationTestProvider;
import io.xj.craft.CraftModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class DigestChordProgressionIT {
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
  public void digestChordProgression() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "User,Artist");

    DigestChordProgression result = digestFactory.chordProgression(ingestFactory.ingest(access, ImmutableList.of(ChainBinding.create(fake.chain1, fake.library10000001))));

    assertNotNull(result);
  }

  @Test
  public void digestChordProgression_ofLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "User,Artist");
    Collection<ChainBinding> entities = ImmutableList.of(ChainBinding.create(fake.chain1, fake.library10000001));

    DigestChordProgression result = digestFactory.chordProgression(ingestFactory.ingest(access, entities));

    assertNotNull(result);
  }

}
