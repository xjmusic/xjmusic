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
import io.xj.craft.digest.pattern_style.DigestSequenceStyle;
import io.xj.core.ingest.IngestFactory;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class DigestSequenceStyleIT extends BaseIT {
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  private IngestFactory ingestFactory;
  private DigestFactory digestFactory;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();
    insertLibraryB1();
    insertLibraryB2();
    insertLibraryB3();

    ingestFactory = injector.getInstance(IngestFactory.class);
    digestFactory = injector.getInstance(DigestFactory.class);
  }

  @Test
  public void digest() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    DigestSequenceStyle result = digestFactory.sequenceStyle(ingestFactory.evaluate(access, ImmutableList.of(new Library(2))));

    assertNotNull(result);
    assertEquals(2.0, result.getMainPatternsPerSequenceStats().min(), 0.1);
    assertEquals(4.0, result.getMainPatternsPerSequenceStats().max(), 0.1);
    assertEquals(3.0, result.getMainPatternsPerSequenceStats().mean(), 0.1);
    assertEquals(2.0, result.getMainPatternsPerSequenceStats().count(), 0.1);
    assertEquals(1, result.getMainPatternsPerSequenceHistogram().count(2));
    assertEquals(1, result.getMainPatternsPerSequenceHistogram().count(4));
    assertEquals(0, result.getMainPatternsPerSequenceHistogram().count(1));
    assertEquals(16.0, result.getMainPatternTotalStats().min(), 0.1);
    assertEquals(32.0, result.getMainPatternTotalStats().max(), 0.1);
    assertEquals(26.6, result.getMainPatternTotalStats().mean(), 0.1);
    assertEquals(6.0, result.getMainPatternTotalStats().count(), 0.1);
    assertEquals(2, result.getMainPatternTotalHistogram().count(16));
    assertEquals(4, result.getMainPatternTotalHistogram().count(32));
  }

}
