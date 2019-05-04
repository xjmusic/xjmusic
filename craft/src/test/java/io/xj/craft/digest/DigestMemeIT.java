// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.library.Library;
import io.xj.craft.BaseIT;
import io.xj.craft.CraftModule;
import io.xj.craft.digest.meme.DigestMeme;
import io.xj.craft.digest.meme.impl.DigestMemesItem;
import io.xj.core.ingest.IngestFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class DigestMemeIT extends BaseIT {
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  private IngestFactory ingestFactory;
  private DigestFactory digestFactory;

  /**
   assert if two collections are equivalent, irrelevant of what type of collection

   @param o1 collection to compare
   @param o2 collection to compare
   */
  private static void assertEquivalent(Collection<BigInteger> o1, Collection<BigInteger> o2) {
    ImmutableList.Builder<String> builder1 = ImmutableList.builder();
    o1.forEach(bigInteger -> builder1.add(bigInteger.toString()));
    ImmutableList.Builder<String> builder2 = ImmutableList.builder();
    o2.forEach(bigInteger -> builder2.add(bigInteger.toString()));
    assertEquals(builder1.build(), builder2.build());
  }

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();
    insertLibraryA();

    ingestFactory = injector.getInstance(IngestFactory.class);
    digestFactory = injector.getInstance(DigestFactory.class);
  }

  @Test
  public void digestMeme() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    DigestMeme result = digestFactory.meme(ingestFactory.evaluate(access, ImmutableList.of(new Library(10000001))));

    // Fuzz
    DigestMemesItem result1 = result.getMemes().get("Fuzz");
    assertEquivalent(ImmutableList.of(), result1.getInstrumentIds());
    assertEquivalent(ImmutableList.of(BigInteger.valueOf(701)), result1.getSequenceIds());
    assertEquivalent(ImmutableList.of(BigInteger.valueOf(901), BigInteger.valueOf(902)), result1.getPatternIds(BigInteger.valueOf(701)));
    assertEquivalent(ImmutableList.of(), result1.getPatternIds(BigInteger.valueOf(751))); // not ingested

    // Ants
    DigestMemesItem result2 = result.getMemes().get("Ants");
    assertEquivalent(ImmutableList.of(BigInteger.valueOf(201)), result2.getInstrumentIds());
    assertEquivalent(ImmutableList.of(BigInteger.valueOf(701), BigInteger.valueOf(702)), result2.getSequenceIds());
    assertEquivalent(ImmutableList.of(), result2.getPatternIds(BigInteger.valueOf(701)));

    // Peel
    DigestMemesItem result3 = result.getMemes().get("Peel");
    assertEquivalent(ImmutableList.of(BigInteger.valueOf(202)), result3.getInstrumentIds());
    assertEquivalent(ImmutableList.of(BigInteger.valueOf(703)), result3.getSequenceIds());
    assertEquivalent(ImmutableList.of(), result3.getPatternIds(BigInteger.valueOf(701)));

    // Gravel
    DigestMemesItem result4 = result.getMemes().get("Gravel");
    assertEquivalent(ImmutableList.of(), result4.getInstrumentIds());
    assertEquivalent(ImmutableList.of(BigInteger.valueOf(701)), result4.getSequenceIds());
    assertEquivalent(ImmutableList.of(BigInteger.valueOf(901)), result4.getPatternIds(BigInteger.valueOf(701)));

    // Mold
    DigestMemesItem result5 = result.getMemes().get("Mold");
    assertEquivalent(ImmutableList.of(BigInteger.valueOf(201)), result5.getInstrumentIds());
    assertEquivalent(ImmutableList.of(), result5.getSequenceIds());
    assertEquivalent(ImmutableList.of(), result5.getPatternIds(BigInteger.valueOf(701)));
  }

}
