// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.ingest;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.Instrument;
import io.xj.Library;
import io.xj.Program;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.dao.InstrumentDAO;
import io.xj.service.hub.dao.ProgramDAO;
import io.xj.service.hub.testing.HubTestConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class HubIngestImplTest {
  @Mock
  InstrumentDAO instrumentDAO;
  @Mock
  ProgramDAO programDAO;

  @Test
  public void instantiateWithUUIDs() throws Exception {
    Config config = HubTestConfiguration.getDefault();
    var injector = Guice.createInjector(new HubIngestModule(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(InstrumentDAO.class).toInstance(instrumentDAO);
        bind(ProgramDAO.class).toInstance(programDAO);
        bind(Config.class).toInstance(config);
      }
    });
    Library library1 = Library.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    Library library2 = Library.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    Program program1 = Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    Program program2 = Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    Instrument instrument1 = Instrument.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    Instrument instrument2 = Instrument.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();

    HubIngest subject = injector.getInstance(HubIngestFactory.class).ingest(HubAccess.internal(),
      ImmutableSet.of(library1.getId(), library2.getId()),
      ImmutableSet.of(program1.getId(), program2.getId()),
      ImmutableSet.of(instrument1.getId(), instrument2.getId()));

    assertNotNull(subject);
  }

}
