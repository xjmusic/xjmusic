// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.HubTopology;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.dao.DAOException;
import io.xj.hub.dao.DAOModule;
import io.xj.hub.dao.InstrumentDAO;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Library;
import io.xj.lib.app.AppException;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiModule;
import io.xj.lib.jsonapi.JsonapiPayload;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static io.xj.hub.access.HubAccess.CONTEXT_KEY;
import static io.xj.lib.jsonapi.AssertPayload.assertPayload;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InstrumentEndpointTest {
  @Mock
  ContainerRequestContext crc;
  @Mock
  InstrumentDAO instrumentDAO;
  private HubAccess hubAccess;
  private InstrumentEndpoint subject;
  private Library library25;
  private Library library1;

  @Before
  public void setUp() throws AppException {
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
        bind(InstrumentDAO.class).toInstance(instrumentDAO);
      }
    }));

    HubTopology.buildHubApiTopology(injector.getInstance(EntityFactory.class));
    var account1 = buildAccount("Testing");
    hubAccess = HubAccess.create(ImmutableList.of(account1), "User,Artist");
    library25 = buildLibrary(account1, "Test 25");
    library1 = buildLibrary(account1, "Test 1");
    subject = injector.getInstance(InstrumentEndpoint.class);
    injector.injectMembers(subject);
  }

  @Test
  public void readMany() throws DAOException, IOException, JsonapiException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(hubAccess);
    Instrument instrument1 = new Instrument();
    instrument1.setId(UUID.randomUUID());
    instrument1.setLibraryId(library25.getId());
    instrument1.setType(InstrumentType.Drum);
    instrument1.setState(InstrumentState.Published);
    instrument1.setName("fonds");
    instrument1.setDensity(0.6f);
    Instrument instrument2 = new Instrument();
    instrument2.setId(UUID.randomUUID());
    instrument2.setLibraryId(library25.getId());
    instrument2.setType(InstrumentType.Drum);
    instrument2.setState(InstrumentState.Published);
    instrument2.setName("trunk");
    instrument2.setDensity(0.6f);
    Collection<Instrument> instruments = ImmutableList.of(instrument1, instrument2);
    when(instrumentDAO.readMany(same(hubAccess), eq(ImmutableList.of(library25.getId()))))
      .thenReturn(instruments);

    Response result = subject.readMany(crc, null, library25.getId().toString(), false);

    verify(instrumentDAO).readMany(same(hubAccess), eq(ImmutableList.of(library25.getId())));
    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(new ObjectMapper().readValue(String.valueOf(result.getEntity()), JsonapiPayload.class))
      .hasDataMany("instruments", ImmutableList.of(instrument1.getId().toString(), instrument2.getId().toString()));
  }

  @Test
  public void readOne() throws DAOException, IOException, JsonapiException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(hubAccess);
    Instrument instrument1 = new Instrument();
    instrument1.setId(UUID.randomUUID());
    instrument1.setLibraryId(library1.getId());
    instrument1.setType(InstrumentType.Drum);
    instrument1.setState(InstrumentState.Published);
    instrument1.setName("fonds");
    instrument1.setDensity(0.6f);
    when(instrumentDAO.readOne(same(hubAccess), eq(instrument1.getId()))).thenReturn(instrument1);

    Response result = subject.readOne(crc, instrument1.getId().toString(), "");

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    JsonapiPayload resultJsonapiPayload = new ObjectMapper().readValue(String.valueOf(result.getEntity()), JsonapiPayload.class);
    assertPayload(resultJsonapiPayload)
      .hasDataOne("instruments", instrument1.getId().toString());
  }
}
