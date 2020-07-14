// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.ingest;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.dao.InstrumentDAO;
import io.xj.service.hub.dao.ProgramDAO;
import io.xj.service.hub.entity.Instrument;
import io.xj.service.hub.entity.Library;
import io.xj.service.hub.entity.Program;
import io.xj.service.hub.testing.HubTestConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
    Injector injector = Guice.createInjector(new HubIngestModule(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(InstrumentDAO.class).toInstance(instrumentDAO);
        bind(ProgramDAO.class).toInstance(programDAO);
        bind(Config.class).toInstance(config);
      }
    });
    Library library1 = Library.create();
    Library library2 = Library.create();
    Program program1 = Program.create();
    Program program2 = Program.create();
    Instrument instrument1 = Instrument.create();
    Instrument instrument2 = Instrument.create();

    HubIngest subject = injector.getInstance(HubIngestFactory.class).ingest(HubAccess.internal(),
      ImmutableSet.of(library1.getId(), library2.getId()),
      ImmutableSet.of(program1.getId(), program2.getId()),
      ImmutableSet.of(instrument1.getId(), instrument2.getId()));

    assertNotNull(subject);
  }

}
