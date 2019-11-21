// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.access.Access;
import io.xj.core.ingest.IngestFactory;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainBinding;
import io.xj.craft.CraftModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class DigestChordProgressionIT extends FixtureIT {
  private IngestFactory ingestFactory;
  private DigestFactory digestFactory;


  @Before
  public void setUp() throws Exception {
    reset();
    insertFixtureA();

    chain1 = Chain.create();

    injector = Guice.createInjector(new CoreModule(), new CraftModule());
    ingestFactory = injector.getInstance(IngestFactory.class);
    digestFactory = injector.getInstance(DigestFactory.class);
  }

  @Test
  public void digestChordProgression() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "User,Artist");

    DigestChordProgression result = digestFactory.chordProgression(ingestFactory.ingest(access, ImmutableList.of(ChainBinding.create(chain1, library10000001))));

    assertNotNull(result);
  }

  @Test
  public void digestChordProgression_ofLibrary() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "User,Artist");
    Collection<ChainBinding> entities = ImmutableList.of(ChainBinding.create(chain1, library10000001));

    DigestChordProgression result = digestFactory.chordProgression(ingestFactory.ingest(access, entities));

    assertNotNull(result);
  }

}
