// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.api;

import io.xj.hub.HubIntegrationTest;
import io.xj.hub.HubIntegrationTestFactory;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.manager.*;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.PayloadDataType;
import org.junit.jupiter.api.AfterEach;
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
import java.util.Objects;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static io.xj.hub.access.HubAccess.CONTEXT_KEY;
import static io.xj.lib.jsonapi.AssertPayload.assertPayload;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
public class InstrumentControllerDbTest {
  @Mock
  HttpServletRequest req;
  @Mock
  HttpServletResponse res;
  private InstrumentController subject;
  private HubIntegrationTest test;
  private IntegrationTestingFixtures fake;
  private JsonapiPayloadFactory jsonapiPayloadFactory;

  @BeforeEach
  public void setUp() throws Exception {
    var env = AppEnvironment.getDefault();
    test = HubIntegrationTestFactory.build(env);
    fake = new IntegrationTestingFixtures(test);
    //
    test.reset();
    //
    // Account "palm tree" has instrument "leaves" and instrument InstrumentType.Negative
    fake.account1 = test.insert(buildAccount("palm tree"));
    fake.library1 = test.insert(buildLibrary(fake.account1, "sandwich"));
    fake.instrument201 = test.insert(buildInstrument(fake.library1, InstrumentType.Pad, InstrumentMode.Event, InstrumentState.Published, "buns"));
    fake.instrument202 = test.insert(buildInstrument(fake.library1, InstrumentType.Drum, InstrumentMode.Event, InstrumentState.Published, "jams"));
    //
    // User in account
    fake.user1 = test.insert(buildUser("jim", "jim@jim.com", "https://www.jim.com/jim.png", "User"));
    test.insert(buildAccountUser(fake.account1, fake.user1));
    //
    // Instantiate the test subject
    jsonapiPayloadFactory = test.getJsonapiPayloadFactory();
    InstrumentManager instrumentManager = new InstrumentManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider());
    InstrumentMemeManager instrumentMemeManager = new InstrumentMemeManagerImpl(test.getEntityFactory(), test.getSqlStoreProvider());
    subject = new InstrumentController(instrumentManager, instrumentMemeManager, test.getSqlStoreProvider(), test.getHttpResponseProvider(), test.getJsonapiPayloadFactory(), test.getEntityFactory());
  }

  //
  @AfterEach
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void readMany_forLibrary() {
    when(req.getAttribute(CONTEXT_KEY)).thenReturn(HubAccess.internal());

    var result = subject.readMany(req, res, null, fake.library1.getId(), null);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    var resultPayload = Objects.requireNonNull(result.getBody());
    assertEquals(PayloadDataType.Many, resultPayload.getDataType());
    assertEquals(2, resultPayload.getDataMany().size());
  }

  @Test
  public void readMany_forAccount() {
    when(req.getAttribute(CONTEXT_KEY)).thenReturn(HubAccess.internal());

    var result = subject.readMany(req, res, fake.account1.getId(), null, null);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    var resultPayload = Objects.requireNonNull(result.getBody());
    assertEquals(PayloadDataType.Many, resultPayload.getDataType());
    assertEquals(2, resultPayload.getDataMany().size());
  }

  @Test
  public void create() throws ManagerException, IOException, JsonapiException {
    var toCreate = buildInstrument(fake.library1, InstrumentType.Drum, InstrumentMode.Event, InstrumentState.Published, "test");
    var input = jsonapiPayloadFactory.from(toCreate);
    when(req.getAttribute(CONTEXT_KEY)).thenReturn(HubAccess.internal());

    var result = subject.create(req, res, input, null);

    assertEquals(HttpStatus.CREATED, result.getStatusCode());
    var resultPayload = Objects.requireNonNull(result.getBody());
    assertEquals(PayloadDataType.One, resultPayload.getDataType());
  }

  /**
   * Lab entity attribute invalidations should throw clean errors in api payload
   * https://www.pivotaltracker.com/story/show/181516000
   */
  @Test
  public void create_invalidThrowsCleanErrorPayload() throws JsonapiException {
    var toCreate = buildInstrument(fake.library1, InstrumentType.Drum, InstrumentMode.Event, InstrumentState.Published, ""); // empty name not allowed
    var input = jsonapiPayloadFactory.from(toCreate);
    when(req.getAttribute(CONTEXT_KEY)).thenReturn(HubAccess.internal());

    var result = subject.create(req, res, input, null);

    assertEquals(HttpStatus.NOT_ACCEPTABLE, result.getStatusCode());
    assertPayload(result.getBody()).hasErrorCount(1);
    var error = Objects.requireNonNull(result.getBody()).getErrors().iterator().next();
    assertEquals("Name is required.", error.getTitle());
  }

}
