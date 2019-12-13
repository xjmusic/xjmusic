// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.endpoint;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.core.CoreModule;
import io.xj.core.access.Access;
import io.xj.core.app.AppConfiguration;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Account;
import io.xj.core.model.Chain;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentMeme;
import io.xj.core.model.SegmentState;
import io.xj.core.model.User;
import io.xj.core.payload.Payload;
import io.xj.core.testing.AppTestConfiguration;
import io.xj.core.testing.InternalResources;
import io.xj.craft.CraftModule;
import io.xj.service.hub.SegmentEndpoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.Instant;

import static io.xj.core.access.Access.CONTEXT_KEY;
import static io.xj.core.testing.AssertPayload.assertPayload;
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
  private Access access;
  private SegmentEndpoint subject;
  private Chain chain25;

  @Before
  public void setUp() {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableList.of((Modules.override(new CoreModule(), new CraftModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(SegmentDAO.class).toInstance(segmentDAO);
        }
      }))));
    Account account1 = Account.create();
    User user101 = User.create();
    access = Access.create(user101, ImmutableList.of(account1), "User,Artist");
    chain25 = Chain.create();
    subject = new SegmentEndpoint(injector);
    injector.injectMembers(subject);
  }

  @Test
  public void readAll() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    // segments
    Segment segment5 = Segment.create(chain25, 4, SegmentState.Crafted, Instant.parse("2017-02-14T12:03:08.000001Z"), Instant.parse("2017-02-14T12:09:08.000001Z"), "C", 8, 0.6, 120, "chain-1-waveform.wav");
    Segment segment6 = Segment.create(chain25, 5, SegmentState.Planned, Instant.parse("2017-02-14T12:09:08.000001Z"), null, "C", 8, 0.6, 120, "chain-1-waveform.wav");
    when(segmentDAO.readMany(same(access), eq(ImmutableList.of(chain25.getId()))))
      .thenReturn(ImmutableList.of(segment5, segment6));
    // segments memes
    SegmentMeme segment5meme = SegmentMeme.create(segment5, "apple");
    SegmentMeme segment6meme = SegmentMeme.create(segment6, "banana");
    when(segmentDAO.readAllSubEntities(any(), eq(ImmutableSet.of(segment6.getId(), segment5.getId())), eq(false)))
      .thenReturn(ImmutableSet.of(segment5meme, segment6meme));

    Response result = subject.readAll(crc, chain25.getId().toString(), null, null);

    verify(segmentDAO).readAllSubEntities(any(), eq(ImmutableSet.of(segment6.getId(), segment5.getId())), eq(false));
    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    Payload payloadResult = InternalResources.deserializePayload(result.getEntity());
    assertPayload(payloadResult)
      .hasDataMany("segments", ImmutableSet.of(segment5.getId().toString(), segment6.getId().toString()));
    assertPayload(payloadResult)
      .hasIncluded("segment-memes", ImmutableList.of(segment5meme, segment6meme));
  }

  @Test
  public void readOne() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Segment segment1 = Segment.create()
      .setChainId(chain25.getId())
      .setOffset(1L)
      .setStateEnum(SegmentState.Crafted)
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z")
      .setKey("F Major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav");
    when(segmentDAO.readOne(same(access), eq(segment1.getId()))).thenReturn(segment1);

    Response result = subject.readOne(crc, segment1.getId().toString());

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    Payload resultPayload = InternalResources.deserializePayload(result.getEntity());
    assertPayload(resultPayload)
      .hasDataOne("segments", segment1.getId().toString());
  }

  /*

  FUTURE: implement these tests with ?include=entity,entity type parameter



  @Test
  public void readAll() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);

    Segment segment5 = Segment.create(chain25, 4, SegmentState.Crafted, Instant.parse("2017-02-14T12:03:08.000001Z"), Instant.parse("2017-02-14T12:09:08.000001Z"), "C", 8, 0.6, 120, "chain-1-waveform.wav");
    SegmentMeme segmentMeme1a = SegmentMeme.create(segment5, "tangerines");
    SegmentMeme segmentMeme1b = SegmentMeme.create(segment5, "oranges");
    Segment segment6 = Segment.create(chain25, 5, SegmentState.Planned, Instant.parse("2017-02-14T12:09:08.000001Z"), null, "C", 8, 0.6, 120, "chain-1-waveform.wav");
    SegmentMeme segmentMeme2a = SegmentMeme.create(segment6, "nectarines");
    SegmentMeme segmentMeme2b = SegmentMeme.create(segment6, "plums");

    Collection<Segment> segments = ImmutableList.of(
      segment5,
      segment6
    );
    when(segmentDAO.readMany(same(access), eq(ImmutableList.of(chain25.getId()))))
      .thenReturn(segments);
    subject.chainId = chain25.getId().toString();

    Response result = subject.readAll(crc);

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    Payload payloadResult = deserializePayload(result.getEntity());
    assertPayload(payloadResult)
      .hasDataMany("segments", ImmutableList.of(segment5.getId().toString(), segment6.getId().toString()));
    assertPayload(payloadResult)
      .hasIncluded("segment-memes", ImmutableList.of(segmentMeme1a, segmentMeme1b, segmentMeme2a, segmentMeme2b));
  }


   */
}
