// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.HubIntegrationTestModule;
import io.xj.hub.HubIntegrationTestProvider;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.tables.pojos.InstrumentMessage;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static org.junit.Assert.*;

// future test: permissions of different users to readMany vs. of vs. update or destroy instruments
@RunWith(MockitoJUnitRunner.class)
public class InstrumentMessageIT {
  private InstrumentMessageDAO testDAO;

  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule(), new HubIntegrationTestModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
      }
    }));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // Account "bananas"
    fake.account1 = test.insert(buildAccount("bananas"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    fake.user2 = test.insert(buildUser("john", "john@email.com", "https://pictures.com/john.gif", "Admin"));

    // Jenny has a "user" role and belongs to account "bananas"
    fake.user3 = test.insert(buildUser("jenny", "jenny@email.com", "https://pictures.com/jenny.gif", "User,Artist"));
    test.insert(buildAccountUser(fake.account1, fake.user3));

    // Library "palm tree" has an instrument "ANTS" and instrument "ANTS"
    fake.library1 = test.insert(buildLibrary(fake.account1, "palm tree"));
    fake.instrument8 = test.insert(buildInstrument(fake.library1, InstrumentType.PercLoop, InstrumentState.Published, "ANTS"));
    fake.instrumentMessage1 = test.insert(buildInstrumentMessage(fake.instrument8, fake.user2, "Commenting about stuff"));
    fake.instrument9 = test.insert(buildInstrument(fake.library1, InstrumentType.Stab, InstrumentState.Published, "ANTS"));

    // Library "boat" has an instrument "helm" and instrument "sail"
    fake.library2 = test.insert(buildLibrary(fake.account1, "boat"));
    fake.instrument201 = test.insert(buildInstrument(fake.library2, InstrumentType.PercLoop, InstrumentState.Published, "helm"));
    fake.instrumentMessage3 = test.insert(buildInstrumentMessage(fake.instrument201, fake.user2, "Commenting about stuff"));
    fake.instrument251 = test.insert(buildInstrument(fake.library2, InstrumentType.Sticky, InstrumentState.Published, "sail"));

    // Instantiate the test subject
    testDAO = injector.getInstance(InstrumentMessageDAO.class);
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void create() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user2, ImmutableList.of(fake.account1));
    var input = new InstrumentMessage();
    input.setId(UUID.randomUUID());
    input.setInstrumentId(fake.instrument201.getId());
    input.setUserId(fake.user2.getId());
    input.setBody("commenting about more stuff");

    var result = testDAO.create(
      hubAccess, input);

    assertNotNull(result);
    assertEquals(fake.instrument201.getId(), result.getInstrumentId());
    assertEquals(fake.user2.getId(), result.getUserId());
    assertEquals("commenting about more stuff", result.getBody());
  }

  @Test
  public void create_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(fake.user3, ImmutableList.of(fake.account1));
    var input = new InstrumentMessage();
    input.setId(UUID.randomUUID());
    input.setInstrumentId(fake.instrument201.getId());
    input.setUserId(fake.user3.getId());
    input.setBody("commenting about more stuff");

    var result = testDAO.create(
      hubAccess, input);

    assertNotNull(result);
    assertEquals(fake.instrument201.getId(), result.getInstrumentId());
    assertEquals(fake.user3.getId(), result.getUserId());
    assertEquals("commenting about more stuff", result.getBody());
  }

  @Test
  public void readOne() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");

    var result = testDAO.readOne(hubAccess, fake.instrumentMessage3.getId());

    assertNotNull(result);
    assertEquals(fake.instrumentMessage3.getId(), result.getId());
    assertEquals(fake.instrument201.getId(), result.getInstrumentId());
    assertEquals(fake.user2.getId(), result.getUserId());
    assertEquals("Commenting about stuff", result.getBody());
  }

  @Test
  public void readMany() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Admin");

    Collection<InstrumentMessage> result = testDAO.readMany(hubAccess, ImmutableList.of(fake.instrument201.getId()));

    assertEquals(1L, result.size());
  }

  @Test
  public void update_cannotBeChanged() {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "User, Artist");
    var input = new InstrumentMessage();
    input.setId(UUID.randomUUID());
    input.setBody("commenting about more stuff");
    input.setInstrumentId(UUID.randomUUID());

    var e = assertThrows(DAOException.class, () -> testDAO.update(hubAccess, fake.instrumentMessage3.getId(), input));
    assertEquals("Cannot update an instrument message!", e.getMessage());
  }

  @Test
  public void destroy_asArtist() throws Exception {
    HubAccess hubAccess = HubAccess.create(ImmutableList.of(fake.account1), "Artist");
    fake.instrumentMessage35 = test.insert(buildInstrumentMessage(fake.instrument9, fake.user2, "commenting about extra stuff"));

    testDAO.destroy(hubAccess, fake.instrumentMessage35.getId());

    assertEquals(Integer.valueOf(0), test.getDSL()
      .selectCount().from(io.xj.hub.tables.InstrumentMessage.INSTRUMENT_MESSAGE)
      .where(io.xj.hub.tables.InstrumentMessage.INSTRUMENT_MESSAGE.ID.eq(fake.instrumentMessage35.getId()))
      .fetchOne(0, int.class));
  }

}

