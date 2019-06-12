//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.resource.segment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.CoreTest;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.payload.Payload;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.sub.SegmentMeme;
import io.xj.craft.CraftModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Collection;

import static io.xj.core.access.impl.Access.CONTEXT_KEY;
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

  @Before
  public void setUp() {
    injector = Guice.createInjector(Modules.override(new CoreModule(), new CraftModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(SegmentDAO.class).toInstance(segmentDAO);
        }
      }));
    access = new Access(ImmutableMap.of(
      "roles", "Artist"
    ));
    subject = new SegmentIndexResource();
    subject.setInjector(injector);
  }

  @Test
  public void readAll() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);

    Segment segment1 = newSegment(5, 25, 4, Instant.parse("2017-02-14T12:03:08.000001Z"));
    SegmentMeme segmentMeme1a = segment1.add(newSegmentMeme("tangerines"));
    SegmentMeme segmentMeme1b = segment1.add(newSegmentMeme("oranges"));
    Segment segment2 = newSegment(6, 25, 5, Instant.parse("2017-02-14T12:09:08.000001Z"));
    SegmentMeme segmentMeme2a = segment1.add(newSegmentMeme("nectarines"));
    SegmentMeme segmentMeme2b = segment1.add(newSegmentMeme("plums"));

    Collection<Segment> segments = ImmutableList.of(
      segment1,
      segment2
    );
    when(segmentDAO.readMany(same(access), eq(ImmutableList.of(BigInteger.valueOf(25)))))
      .thenReturn(segments);
    subject.chainId = "25";

    Response result = subject.readAll(crc);

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    Payload payloadResult = deserializePayload(result.getEntity());
    assertPayload(payloadResult)
      .hasDataMany("segments", ImmutableList.of("5", "6"));
    assertPayload(payloadResult)
      .hasIncluded("segment-memes", ImmutableList.of(segmentMeme1a, segmentMeme1b, segmentMeme2a, segmentMeme2b));
  }

}
