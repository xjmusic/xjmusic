// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.nexus.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.Account;
import io.xj.Chain;
import io.xj.Segment;
import io.xj.SegmentMeme;
import io.xj.User;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.AppException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.jsonapi.Payload;
import io.xj.lib.mixer.MixerModule;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.NexusApp;
import io.xj.service.nexus.dao.SegmentDAO;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.testing.NexusTestConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.UUID;

import static io.xj.lib.jsonapi.AssertPayload.assertPayload;
import static io.xj.service.hub.client.HubClientAccess.CONTEXT_KEY;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildHubClientAccess;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SegmentEndpointTest {
  @Mock
  ContainerRequestContext crc;
  @Mock
  SegmentDAO segmentDAO;
  private HubClientAccess access;
  private SegmentEndpoint subject;
  private Chain chain25;

  @Before
  public void setUp() throws AppException {
    Config config = NexusTestConfiguration.getDefault();
    var injector = AppConfiguration.inject(config, ImmutableSet.of((Modules.override(new MixerModule(), new JsonApiModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(SegmentDAO.class).toInstance(segmentDAO);
        }
      }))));
    HubApp.buildApiTopology(injector.getInstance(EntityFactory.class));
    NexusApp.buildApiTopology(injector.getInstance(EntityFactory.class));
    var account1 = Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    User user101 = User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    access = buildHubClientAccess(user101, ImmutableList.of(account1), "User,Artist");
    chain25 = Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    subject = injector.getInstance(SegmentEndpoint.class);
    injector.injectMembers(subject);
  }

  @Test
  public void readMany() throws IOException, JsonApiException, DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    // segments
    Segment segment5 = Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain25.getId())
      .setOffset(4)
      .setState(Segment.State.Crafted)
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setEndAt("2017-02-14T12:09:08.000001Z")
      .setKey("C")
      .setTotal(8)
      .setDensity(0.6)
      .setTempo(120)
      .setStorageKey("chain-1-waveform.wav")
      .build();
    Segment segment6 = Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain25.getId())
      .setOffset(5)
      .setState(Segment.State.Planned)
      .setBeginAt("2017-02-14T12:09:08.000001Z")
      .setKey("C")
      .setTotal(8)
      .setDensity(0.6)
      .setTempo(120)
      .setStorageKey("chain-1-waveform.wav")
      .build();
    when(segmentDAO.readMany(same(access), eq(ImmutableList.of(chain25.getId()))))
      .thenReturn(ImmutableList.of(segment5, segment6));
    // segments memes
    SegmentMeme segment5meme = SegmentMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment5.getId())
      .setName("apple")
      .build();
    SegmentMeme segment6meme = SegmentMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment6.getId())
      .setName("banana")
      .build();
    when(segmentDAO.readManySubEntities(any(), eq(ImmutableSet.of(segment6.getId(), segment5.getId())), eq(false)))
      .thenReturn(ImmutableSet.of(segment5meme, segment6meme));

    Response result = subject.readMany(crc, chain25.getId(), null, null, true);

    verify(segmentDAO).readManySubEntities(any(), eq(ImmutableSet.of(segment6.getId(), segment5.getId())), eq(false));
    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    Payload payloadResult = new ObjectMapper().readValue(String.valueOf(result.getEntity()), Payload.class);
    assertPayload(payloadResult)
      .hasDataMany("segments", ImmutableSet.of(segment5.getId(), segment6.getId()));
    assertPayload(payloadResult)
      .hasIncluded("segment-memes", ImmutableList.of(segment5meme, segment6meme));
  }

  @Test
  public void readOne() throws IOException, JsonApiException, DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Segment segment1 = Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain25.getId())
      .setOffset(1L)
      .setState(Segment.State.Crafted)
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z")
      .setKey("F Major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .build();
    when(segmentDAO.readOne(same(access), eq(segment1.getId()))).thenReturn(segment1);

    Response result = subject.readOne(crc, segment1.getId());

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    Payload resultPayload = new ObjectMapper().readValue(String.valueOf(result.getEntity()), Payload.class);
    assertPayload(resultPayload)
      .hasDataOne("segments", segment1.getId());
  }

}
