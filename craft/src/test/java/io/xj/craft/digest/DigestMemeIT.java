// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.access.impl.Access;
import io.xj.core.ingest.IngestFactory;
import io.xj.craft.CraftModule;
import io.xj.craft.digest.meme.DigestMeme;
import io.xj.craft.digest.meme.impl.DigestMemesItem;
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

    injector = Guice.createInjector(new CoreModule(), new CraftModule());
    ingestFactory = injector.getInstance(IngestFactory.class);
    digestFactory = injector.getInstance(DigestFactory.class);
  }

  @Test
  public void digestMeme() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    DigestMeme result = digestFactory.meme(ingestFactory.ingest(access, ImmutableList.of(newChainBinding("Library", 10000001))));

    // Fuzz
    DigestMemesItem result1 = result.getMemes().get("Fuzz");
    assertEquals(0, result1.getInstrumentIds().size());
    assertEquals(1, result1.getProgramIds().size());
    assertEquals(1, result1.getSequenceIds(BigInteger.valueOf(701)).size());

    // Ants
    DigestMemesItem result2 = result.getMemes().get("Ants");
    assertEquals(1, result2.getInstrumentIds().size());
    assertEquals(2, result2.getProgramIds().size());

    // Peel
    DigestMemesItem result3 = result.getMemes().get("Peel");
    assertEquals(1, result3.getInstrumentIds().size());
    assertEquals(1, result3.getProgramIds().size());

    // Gravel
    DigestMemesItem result4 = result.getMemes().get("Gravel");
    assertEquals(0, result4.getInstrumentIds().size());
    assertEquals(1, result4.getProgramIds().size());
    assertEquals(1, result4.getSequenceIds(BigInteger.valueOf(701)).size());

    // Mold
    DigestMemesItem result5 = result.getMemes().get("Mold");
    assertEquals(1, result5.getInstrumentIds().size());
  }

}
