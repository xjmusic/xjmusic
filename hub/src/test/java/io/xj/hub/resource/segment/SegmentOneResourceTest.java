//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.resource.segment;

import com.google.common.collect.ImmutableList;
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

import static io.xj.core.access.Access.CONTEXT_KEY;
import static io.xj.core.testing.AssertPayload.assertPayload;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SegmentOneResourceTest extends CoreTest {
  @Mock
  ContainerRequestContext crc;
  @Mock
  SegmentDAO segmentDAO;
  private Access access;
  private SegmentOneResource subject;
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
    subject = new SegmentOneResource();
    subject.setInjector(injector);
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
    subject.id = segment1.getId().toString();

    Response result = subject.readOne(crc);

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    Payload resultPayload = deserializePayload(result.getEntity());
    assertPayload(resultPayload)
      .hasDataOne("segments", segment1.getId().toString());
  }

}
