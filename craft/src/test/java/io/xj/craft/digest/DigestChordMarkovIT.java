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
import io.xj.craft.digest.chord_markov.DigestChordMarkov;
import io.xj.core.ingest.IngestFactory;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class DigestChordMarkovIT extends BaseIT {
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
  public void digest() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    DigestChordMarkov result = digestFactory.chordMarkov(ingestFactory.evaluate(access, ImmutableList.of(new Library(10000001))));

    assertNotNull(result);
  }

}
