// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.ingest.IngestFactory;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.library.Library;
import io.xj.craft.BaseIT;
import io.xj.craft.CraftModule;
import io.xj.craft.digest.chord_progression.DigestChordProgression;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class DigestChordProgressionIT extends BaseIT {
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  private IngestFactory ingestFactory;
  private DigestFactory digestFactory;


  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();
    insertLibraryA();

    ingestFactory = injector.getInstance(IngestFactory.class);
    digestFactory = injector.getInstance(DigestFactory.class);
  }

  @Test
  public void digestChordProgression() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    DigestChordProgression result = digestFactory.chordProgression(ingestFactory.evaluate(access, ImmutableList.of(new Library(10000001))));

    assertNotNull(result);
  }

  @Test
  public void digestChordProgression_ofLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User,Artist",
      "accounts", "1"
    ));
    Collection<Entity> entities = ImmutableList.of(new Library(10000001));

    DigestChordProgression result = digestFactory.chordProgression(ingestFactory.evaluate(access, entities));

    assertEquals(16, result.getEvaluatedSequenceMap().size());
  }

}
