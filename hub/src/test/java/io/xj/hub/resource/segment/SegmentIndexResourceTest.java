//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.resource.segment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.CoreTest;
import io.xj.core.access.Access;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Account;
import io.xj.core.model.Chain;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentState;
import io.xj.core.model.User;
import io.xj.core.payload.Payload;
import io.xj.craft.CraftModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;

import static io.xj.core.access.Access.CONTEXT_KEY;
import static io.xj.core.testing.AssertPayload.assertPayload;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SegmentIndexResourceTest extends CoreTest {
  @Mock
  ContainerRequestContext crc;
  @Mock
  SegmentDAO segmentDAO;
  private Access access;
  private SegmentIndexResource subject;
  private Chain chain25;

  @Before
  public void setUp() {
    injector = Guice.createInjector(Modules.override(new CoreModule(), new CraftModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(SegmentDAO.class).toInstance(segmentDAO);
        }
      }));
    Account account1 = Account.create();
    User user101 = User.create();
    access = Access.create(user101, ImmutableList.of(account1), "User,Artist");
    chain25 = Chain.create();
    subject = new SegmentIndexResource();
    subject.setInjector(injector);
  }

  @Test
  public void readAll() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Segment segment5 = Segment.create(chain25, 4, SegmentState.Crafted, Instant.parse("2017-02-14T12:03:08.000001Z"), Instant.parse("2017-02-14T12:09:08.000001Z"), "C", 8, 0.6, 120, "chain-1-waveform.wav");
    Segment segment6 = Segment.create(chain25, 5, SegmentState.Planned, Instant.parse("2017-02-14T12:09:08.000001Z"), null, "C", 8, 0.6, 120, "chain-1-waveform.wav");
    Collection<Segment> segments = ImmutableList.of(segment5, segment6);
    when(segmentDAO.readMany(same(access), eq(ImmutableList.of(chain25.getId())))).thenReturn(segments);
    subject.chainId = chain25.getId().toString();

    Response result = subject.readAll(crc);

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    Payload payloadResult = deserializePayload(result.getEntity());
    assertPayload(payloadResult)
      .hasDataMany("segments", ImmutableList.of(segment5.getId().toString(), segment6.getId().toString()));
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
