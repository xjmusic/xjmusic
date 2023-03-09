// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import io.xj.hub.HubTopology;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.manager.InstrumentManager;
import io.xj.hub.manager.InstrumentMemeManager;
import io.xj.hub.manager.ManagerException;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Library;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.app.AppException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static io.xj.hub.access.HubAccess.CONTEXT_KEY;
import static io.xj.lib.jsonapi.AssertPayload.assertPayload;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class InstrumentControllerTest {
  @Mock
  HttpServletRequest req;
  @Mock
  HttpServletResponse res;
  @Mock
  InstrumentManager instrumentManager;
  @Mock
  InstrumentMemeManager instrumentMemeManager;
  @Mock
  private HubSqlStoreProvider sqlStoreProvider;
  private HubAccess access;
  private InstrumentController subject;
  private Library library25;
  private Library library1;

  @BeforeEach
  public void setUp() throws AppException {
    var env = AppEnvironment.getDefault();
    JsonProvider jsonProvider = new JsonProviderImpl();
    EntityFactory entityFactory = new EntityFactoryImpl(jsonProvider);
    JsonapiPayloadFactory payloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    HubTopology.buildHubApiTopology(entityFactory);
    ApiUrlProvider apiUrlProvider = new ApiUrlProvider(env);
    JsonapiResponseProvider responseProvider = new JsonapiResponseProviderImpl(apiUrlProvider);

    var account1 = buildAccount("Testing");
    access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(account1), "User,Artist");
    library25 = buildLibrary(account1, "Test 25");
    library1 = buildLibrary(account1, "Test 1");
    subject = new InstrumentController(instrumentManager, instrumentMemeManager, sqlStoreProvider, responseProvider, payloadFactory, entityFactory);
  }

  @Test
  public void readMany() throws ManagerException, IOException, JsonapiException {
    when(req.getAttribute(CONTEXT_KEY)).thenReturn(access);
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
    when(instrumentManager.readMany(same(access), eq(ImmutableList.of(library25.getId()))))
      .thenReturn(instruments);

    var result = subject.readMany(req, res, null, library25.getId(), false);

    verify(instrumentManager).readMany(same(access), eq(ImmutableList.of(library25.getId())));
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    assertPayload(result.getBody())
      .hasDataMany("instruments", ImmutableList.of(instrument1.getId().toString(), instrument2.getId().toString()));
  }

  @Test
  public void readOne() throws ManagerException, IOException, JsonapiException {
    when(req.getAttribute(CONTEXT_KEY)).thenReturn(access);
    Instrument instrument1 = new Instrument();
    instrument1.setId(UUID.randomUUID());
    instrument1.setLibraryId(library1.getId());
    instrument1.setType(InstrumentType.Drum);
    instrument1.setState(InstrumentState.Published);
    instrument1.setName("fonds");
    instrument1.setDensity(0.6f);
    when(instrumentManager.readOne(same(access), eq(instrument1.getId()))).thenReturn(instrument1);

    var result = subject.readOne(req, res, instrument1.getId(), "");

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    JsonapiPayload resultJsonapiPayload = result.getBody();
    assertPayload(resultJsonapiPayload)
      .hasDataOne("instruments", instrument1.getId().toString());
  }

  /**
   * Lab UI should load memes when directly visiting an instrument https://www.pivotaltracker.com/story/show/181129203
   */
  @Test
  public void readOne_includingMemes() throws ManagerException, IOException, JsonapiException {
    when(req.getAttribute(CONTEXT_KEY)).thenReturn(access);
    var instrument1 = buildInstrument(library1, InstrumentType.Drum, InstrumentMode.Event, InstrumentState.Published, "test");
    var instrumentMeme1 = buildInstrumentMeme(instrument1, "RED");
    when(instrumentManager.readOne(same(access), eq(instrument1.getId()))).thenReturn(instrument1);
    when(instrumentManager.readChildEntities(same(access), eq(List.of(instrument1.getId())), eq(List.of("instrument-meme")))).thenReturn(List.of(instrumentMeme1));

    var result = subject.readOne(req, res, instrument1.getId(), "instrument-meme");

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    JsonapiPayload resultJsonapiPayload = result.getBody();
    assertPayload(resultJsonapiPayload).hasDataOne("instruments", instrument1.getId().toString());
    assertPayload(resultJsonapiPayload).hasIncluded("instrument-memes", List.of(instrumentMeme1));
  }
}
