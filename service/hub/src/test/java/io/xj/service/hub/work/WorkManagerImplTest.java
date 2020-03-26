// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.work;// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

import com.google.api.client.util.Maps;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.AppException;
import io.xj.service.hub.HubModule;
import io.xj.service.hub.dao.ChainDAO;
import io.xj.service.hub.dao.PlatformMessageDAO;
import io.xj.service.hub.dao.ProgramDAO;
import io.xj.service.hub.model.Chain;
import io.xj.service.hub.model.ChainState;
import io.xj.service.hub.model.PlatformMessage;
import io.xj.service.hub.model.Program;
import io.xj.service.hub.model.ProgramSequencePattern;
import io.xj.service.hub.model.Work;
import io.xj.service.hub.model.WorkState;
import io.xj.service.hub.model.WorkType;
import io.xj.service.hub.persistence.RedisDatabaseProvider;
import io.xj.service.hub.testing.AppTestConfiguration;
import net.greghaines.jesque.Job;
import net.greghaines.jesque.client.Client;
import net.greghaines.jesque.worker.JobFactory;
import net.greghaines.jesque.worker.Worker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import redis.clients.jedis.Jedis;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WorkManagerImplTest {
  private Chain chain1;
  private WorkManager subject;

  @Mock
  private ChainDAO chainDAO;

  @Mock
  private RedisDatabaseProvider redisDatabaseProvider;

  @Mock
  private Client queueClient;

  @Mock
  private JobFactory jobFactory;

  @Mock
  private Worker worker;

  @Mock
  private Jedis redisConnection;

  @Mock
  private PlatformMessageDAO platformMessageDAO;

  @Mock
  private ProgramDAO programDAO;

  @Before
  public void setUp() throws AppException {
    Config config = AppTestConfiguration.getDefault();

    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(Modules.override(new HubModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(Config.class).toInstance(config);
          bind(ChainDAO.class).toInstance(chainDAO);
          bind(ProgramDAO.class).toInstance(programDAO);
          bind(PlatformMessageDAO.class).toInstance(platformMessageDAO);
          bind(RedisDatabaseProvider.class).toInstance(redisDatabaseProvider);
        }
      })));

    chain1 = Chain.create();

    subject = injector.getInstance(WorkManager.class);
  }

  @Test
  public void startChainFabrication() {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.startChainFabrication(chain1.getId());

    Map<String, String> vars = Maps.newHashMap();
    vars.put(Work.KEY_TARGET_ID, chain1.getId().toString());
    verify(queueClient).recurringEnqueue(
      eq("xj_test"),
      eq(new Job(WorkType.ChainFabricate.toString(), vars)),
      anyInt(), anyInt());
    verify(queueClient).end();
  }

  @Test
  public void stopChainFabrication() {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.stopChainFabrication(chain1.getId());

    Map<String, String> vars = Maps.newHashMap();
    vars.put(Work.KEY_TARGET_ID, chain1.getId().toString());
    verify(queueClient).removeRecurringEnqueue("xj_test",
      new Job(WorkType.ChainFabricate.toString(), vars));
    verify(queueClient).end();
  }

  @Test
  public void scheduleSegmentFabricate() {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.scheduleSegmentFabricate(10, chain1.getId());

    Map<String, String> vars = Maps.newHashMap();
    vars.put(Work.KEY_TARGET_ID, chain1.getId().toString());
    verify(queueClient).delayedEnqueue(
      eq("xj_test"),
      eq(new Job(WorkType.SegmentFabricate.toString(), vars)),
      anyInt());
    verify(queueClient).end();
  }

  @Test
  public void startChainErase() {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.startChainErase(chain1.getId());

    Map<String, String> vars = Maps.newHashMap();
    vars.put(Work.KEY_TARGET_ID, chain1.getId().toString());
    verify(queueClient).recurringEnqueue(
      eq("xj_test"),
      eq(new Job(WorkType.ChainErase.toString(), vars)),
      anyInt(), anyInt());
    verify(queueClient).end();
  }

  @Test
  public void stopChainErase() {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.stopChainErase(chain1.getId());

    Map<String, String> vars = Maps.newHashMap();
    vars.put(Work.KEY_TARGET_ID, chain1.getId().toString());
    verify(queueClient).removeRecurringEnqueue("xj_test",
      new Job(WorkType.ChainErase.toString(), vars));
    verify(queueClient).end();
  }

  @Test
  public void getWorker() {
    when(redisDatabaseProvider.getQueueWorker(jobFactory)).thenReturn(worker);

    Worker result = subject.getWorker(jobFactory);

    assertEquals(worker, result);
  }

  @Test
  public void readAllWork() throws Exception {
    // mock Chain records in Erase state
    Collection<Chain> testChainErase = Lists.newArrayList();
    Chain chain157 = Chain.create(ChainState.Erase);
    testChainErase.add(chain157);
    Chain chain8907 = Chain.create(ChainState.Erase);
    testChainErase.add(chain8907);
    when(chainDAO.readAllInState(any(), eq(ChainState.Erase))).thenReturn(testChainErase);
    // mock Chain records in Fabricate state
    Collection<Chain> testChainFabricate = Lists.newArrayList();
    Chain chain24 = Chain.create(ChainState.Fabricate);
    testChainFabricate.add(chain24);
    Chain chain3382 = Chain.create(ChainState.Fabricate);
    testChainFabricate.add(chain3382);
    when(chainDAO.readAllInState(any(), eq(ChainState.Fabricate))).thenReturn(testChainFabricate);
    // mock direct query of Redis Jesque jobs
    when(redisDatabaseProvider.getClient()).thenReturn(redisConnection);
    Set<String> testQueueData = Sets.newConcurrentHashSet();
    testQueueData.add("{\"class\":\"ChainFabricate\",\"vars\":{\"targetId\":\"" + chain24.getId() + "\"},\"args\":null}");
    testQueueData.add("{\"class\":\"ChainFabricate\",\"vars\":{\"targetId\":\"" + chain3382.getId() + "\"},\"args\":null}");
    testQueueData.add("{\"class\":\"ChainErase\",\"vars\":{\"targetId\":\"" + chain157.getId() + "\"},\"args\":null}");
    when(redisConnection.zrange("xj:queue:xj_test", 0L, -1L)).thenReturn(testQueueData);

    Collection<Work> allResults = subject.readAllWork();

    verify(redisConnection).zrange("xj:queue:xj_test", 0L, -1L);
    // assert results
    assertEquals(4L, allResults.size());
    Map<UUID, Work> resultWorkForTargetId = Maps.newHashMap();
    allResults.forEach(work -> resultWorkForTargetId.put(work.getTargetId(), work));
    // assert #1
    assertEquals(WorkType.ChainErase, resultWorkForTargetId.get(chain157.getId()).getType());
    assertEquals(WorkState.Queued, resultWorkForTargetId.get(chain157.getId()).getState());
    // assert #2
    assertEquals(WorkType.ChainFabricate, resultWorkForTargetId.get(chain24.getId()).getType());
    assertEquals(WorkState.Queued, resultWorkForTargetId.get(chain24.getId()).getState());
    // assert #3
    assertEquals(WorkType.ChainErase, resultWorkForTargetId.get(chain8907.getId()).getType());
    assertEquals(WorkState.Expected, resultWorkForTargetId.get(chain8907.getId()).getState());
    // assert #0
    assertEquals(WorkType.ChainFabricate, resultWorkForTargetId.get(chain3382.getId()).getType());
    assertEquals(WorkState.Queued, resultWorkForTargetId.get(chain3382.getId()).getState());

  }

  @Test
  public void reinstateAllWork() throws Exception {
    Program program2965 = Program.create();
    ProgramSequencePattern programPattern587 = ProgramSequencePattern.create();
    // mock Chain records in Erase state
    Collection<Chain> testChainErase = Lists.newArrayList();
    Chain chain157 = Chain.create(ChainState.Erase);
    testChainErase.add(chain157);
    Chain chain8907 = Chain.create(ChainState.Erase);
    testChainErase.add(chain8907);
    when(chainDAO.readAllInState(any(), eq(ChainState.Erase))).thenReturn(testChainErase);
    // mock Chain records in Fabricate state
    Collection<Chain> testChainFabricate = Lists.newArrayList();
    Chain chain24 = Chain.create(ChainState.Fabricate);
    testChainFabricate.add(chain24);
    Chain chain3382 = Chain.create(ChainState.Fabricate);
    testChainFabricate.add(chain3382);
    when(chainDAO.readAllInState(any(), eq(ChainState.Fabricate))).thenReturn(testChainFabricate);
    // mock direct query of Redis Jesque jobs
    when(redisDatabaseProvider.getClient()).thenReturn(redisConnection);
    Set<String> testQueueData = Sets.newConcurrentHashSet();
    testQueueData.add("{\"class\":\"ChainFabricate\",\"vars\":{\"targetId\":\"" + chain24.getId() + "\"},\"args\":null}");
    testQueueData.add("{\"class\":\"ChainFabricate\",\"vars\":{\"targetId\":\"" + chain3382.getId() + "\"},\"args\":null}");
    testQueueData.add("{\"class\":\"ChainErase\",\"vars\":{\"targetId\":\"" + chain157.getId() + "\"},\"args\":null}");
    when(redisConnection.zrange("xj:queue:xj_test", 0L, -1L)).thenReturn(testQueueData);
    // mock redis queue redisClient
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    Collection<Work> result = subject.reinstateAllWork();

    verify(redisConnection).zrange("xj:queue:xj_test", 0L, -1L);
    // verify the platform message reporting that the job was reinstated
    Optional<Work> chainEraseWork = result.stream().filter(work -> work.getType().equals(WorkType.ChainErase)).findFirst();
    assertTrue(chainEraseWork.isPresent());
    ArgumentCaptor<PlatformMessage> resultMessage = ArgumentCaptor.forClass(PlatformMessage.class);
    verify(platformMessageDAO).create(any(), resultMessage.capture());
    assertEquals(String.format("Reinstated Queued ChainErase #%s", chainEraseWork.get().getTargetId()), resultMessage.getValue().getBody());
    // verify the dropped chain erase job got reinstated
    Map<String, String> vars = Maps.newHashMap();
    vars.put(Work.KEY_TARGET_ID, chainEraseWork.get().getTargetId().toString());
    verify(queueClient).recurringEnqueue(
      eq("xj_test"),
      eq(new Job(WorkType.ChainErase.toString(), vars)),
      anyInt(), anyInt());
    verify(queueClient).end();
    // assert results
    Iterator<Work> resultIterator = result.iterator();
    // assert #0
    Work result0 = resultIterator.next();
    assertEquals(chain8907.getId(), result0.getTargetId());
    assertEquals(WorkType.ChainErase, result0.getType());
    assertEquals(WorkState.Queued, result0.getState());
  }

  @Test
  public void isExistingWork() throws Exception {
    Program program2965 = Program.create();
    ProgramSequencePattern programPattern587 = ProgramSequencePattern.create();
    // mock Chain records in Erase state
    Collection<Chain> testChainErase = Lists.newArrayList();
    Chain chain157 = Chain.create(ChainState.Erase);
    testChainErase.add(chain157);
    Chain chain8907 = Chain.create(ChainState.Erase);
    testChainErase.add(chain8907);
    when(chainDAO.readAllInState(any(), eq(ChainState.Erase))).thenReturn(testChainErase);
    // mock Chain records in Fabricate state
    Collection<Chain> testChainFabricate = Lists.newArrayList();
    Chain chain24 = Chain.create(ChainState.Fabricate);
    testChainFabricate.add(chain24);
    Chain chain3382 = Chain.create(ChainState.Fabricate);
    testChainFabricate.add(chain3382);
    when(chainDAO.readAllInState(any(), eq(ChainState.Fabricate))).thenReturn(testChainFabricate);
    // mock direct query of Redis Jesque jobs
    when(redisDatabaseProvider.getClient()).thenReturn(redisConnection);
    Set<String> testQueueData = Sets.newConcurrentHashSet();
    testQueueData.add("{\"class\":\"ChainFabricate\",\"vars\":{\"targetId\":\"" + chain24.getId() + "\"},\"args\":null}");
    testQueueData.add("{\"class\":\"ChainFabricate\",\"vars\":{\"targetId\":\"" + chain3382.getId() + "\"},\"args\":null}");
    testQueueData.add("{\"class\":\"ChainErase\",\"vars\":{\"targetId\":\"" + chain157.getId() + "\"},\"args\":null}");
    when(redisConnection.zrange("xj:queue:xj_test", 0L, -1L)).thenReturn(testQueueData);

    // proof is in the assertions
    assertTrue(subject.isExistingWork(WorkState.Queued, WorkType.ChainFabricate, chain3382.getId()));
    assertTrue(subject.isExistingWork(WorkState.Queued, WorkType.ChainErase, chain157.getId()));
    assertTrue(subject.isExistingWork(WorkState.Queued, WorkType.ChainFabricate, chain24.getId()));
    assertTrue(subject.isExistingWork(WorkState.Expected, WorkType.ChainErase, chain8907.getId()));
    assertFalse(subject.isExistingWork(WorkState.Expected, WorkType.ChainFabricate, chain3382.getId()));
    assertFalse(subject.isExistingWork(WorkState.Queued, WorkType.ChainFabricate, UUID.randomUUID()));
    assertFalse(subject.isExistingWork(WorkState.Queued, WorkType.ChainErase, chain8907.getId()));
  }

}
