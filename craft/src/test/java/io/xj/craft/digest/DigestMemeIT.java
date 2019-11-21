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
import io.xj.craft.digest.impl.DigestMemeImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class DigestMemeIT extends FixtureIT {
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
  public void digestMeme() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "User,Artist");

    DigestMeme result = digestFactory.meme(ingestFactory.ingest(access, ImmutableList.of(ChainBinding.create(chain1, library10000001))));

    // Fuzz
    DigestMemeImpl.DigestMemesItem result1 = result.getMemes().get("Fuzz");
    assertEquals(0, result1.getInstrumentIds().size());
    assertEquals(1, result1.getProgramIds().size());
    assertEquals(1, result1.getSequenceIds(program701.getId()).size());

    // Ants
    DigestMemeImpl.DigestMemesItem result2 = result.getMemes().get("Ants");
    assertEquals(1, result2.getInstrumentIds().size());
    assertEquals(2, result2.getProgramIds().size());

    // Peel
    DigestMemeImpl.DigestMemesItem result3 = result.getMemes().get("Peel");
    assertEquals(1, result3.getInstrumentIds().size());
    assertEquals(1, result3.getProgramIds().size());

    // Gravel
    DigestMemeImpl.DigestMemesItem result4 = result.getMemes().get("Gravel");
    assertEquals(0, result4.getInstrumentIds().size());
    assertEquals(1, result4.getProgramIds().size());
    assertEquals(1, result4.getSequenceIds(program701.getId()).size());

    // Mold
    DigestMemeImpl.DigestMemesItem result5 = result.getMemes().get("Mold");
    assertEquals(1, result5.getInstrumentIds().size());
  }

}
