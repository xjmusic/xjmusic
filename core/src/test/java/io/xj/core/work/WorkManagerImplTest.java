//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.work;// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import com.google.api.client.util.Maps;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.CoreTest;
import io.xj.core.config.Config;
import io.xj.core.dao.ChainDAO;
import io.xj.core.dao.PlatformMessageDAO;
import io.xj.core.dao.ProgramDAO;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.message.platform.PlatformMessage;
import io.xj.core.model.work.Work;
import io.xj.core.model.work.WorkState;
import io.xj.core.model.work.WorkType;
import io.xj.core.persistence.redis.RedisDatabaseProvider;
import net.greghaines.jesque.Job;
import net.greghaines.jesque.client.Client;
import net.greghaines.jesque.worker.JobFactory;
import net.greghaines.jesque.worker.Worker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import redis.clients.jedis.Jedis;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WorkManagerImplTest extends CoreTest {
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
  public void setUp() throws Exception {
    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(ChainDAO.class).toInstance(chainDAO);
          bind(ProgramDAO.class).toInstance(programDAO);
          bind(PlatformMessageDAO.class).toInstance(platformMessageDAO);
          bind(RedisDatabaseProvider.class).toInstance(redisDatabaseProvider);
        }
      }));

    System.setProperty("work.queue.name", "xj_test");

    subject = injector.getInstance(WorkManager.class);
  }

  @After
  public void tearDown() {
    System.clearProperty("work.queue.name");
  }

  @Test
  public void startChainFabrication() {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.startChainFabrication(BigInteger.valueOf(5977L));

    Map<String, String> vars = Maps.newHashMap();
    vars.put(Work.KEY_TARGET_ID, "5977");
    verify(queueClient).recurringEnqueue(
      eq(Config.getWorkQueueName()),
      eq(new Job(WorkType.ChainFabricate.toString(), vars)),
      anyInt(), anyInt());
    verify(queueClient).end();
  }

  @Test
  public void stopChainFabrication() {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.stopChainFabrication(BigInteger.valueOf(5977L));

    Map<String, String> vars = Maps.newHashMap();
    vars.put(Work.KEY_TARGET_ID, "5977");
    verify(queueClient).removeRecurringEnqueue(Config.getWorkQueueName(),
      new Job(WorkType.ChainFabricate.toString(), vars));
    verify(queueClient).end();
  }

  @Test
  public void scheduleSegmentFabricate() {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.scheduleSegmentFabricate(10, BigInteger.valueOf(5977L));

    Map<String, String> vars = Maps.newHashMap();
    vars.put(Work.KEY_TARGET_ID, "5977");
    verify(queueClient).delayedEnqueue(
      eq(Config.getWorkQueueName()),
      eq(new Job(WorkType.SegmentFabricate.toString(), vars)),
      anyInt());
    verify(queueClient).end();
  }

  @Test
  public void startChainErase() {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.startChainErase(BigInteger.valueOf(5977L));

    Map<String, String> vars = Maps.newHashMap();
    vars.put(Work.KEY_TARGET_ID, "5977");
    verify(queueClient).recurringEnqueue(
      eq(Config.getWorkQueueName()),
      eq(new Job(WorkType.ChainErase.toString(), vars)),
      anyInt(), anyInt());
    verify(queueClient).end();
  }

  @Test
  public void stopChainErase() {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.stopChainErase(BigInteger.valueOf(5977L));

    Map<String, String> vars = Maps.newHashMap();
    vars.put(Work.KEY_TARGET_ID, "5977");
    verify(queueClient).removeRecurringEnqueue(Config.getWorkQueueName(),
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
    testChainErase.add(newChain(157, ChainState.Erase));
    testChainErase.add(newChain(8907, ChainState.Erase));
    when(chainDAO.readAllInState(any(), eq(ChainState.Erase))).thenReturn(testChainErase);
    // mock Chain records in Fabricate state
    Collection<Chain> testChainFabricate = Lists.newArrayList();
    testChainFabricate.add(newChain(24, ChainState.Fabricate));
    testChainFabricate.add(newChain(3382, ChainState.Fabricate));
    when(chainDAO.readAllInState(any(), eq(ChainState.Fabricate))).thenReturn(testChainFabricate);
    // mock direct query of Redis Jesque jobs
    when(redisDatabaseProvider.getClient()).thenReturn(redisConnection);
    Set<String> testQueueData = Sets.newConcurrentHashSet();
    testQueueData.add("{\"class\":\"ChainFabricate\",\"vars\":{\"targetId\":\"24\"},\"args\":null}");
    testQueueData.add("{\"class\":\"ChainFabricate\",\"vars\":{\"targetId\":\"3382\"},\"args\":null}");
    testQueueData.add("{\"class\":\"ChainErase\",\"vars\":{\"targetId\":\"157\"},\"args\":null}");
    when(redisConnection.zrange("xj:queue:xj_test", 0L, -1L)).thenReturn(testQueueData);

    Collection<Work> allResults = subject.readAllWork();

    verify(redisConnection).zrange("xj:queue:xj_test", 0L, -1L);
    // assert results
    Iterator<Work> resultIterator = allResults.iterator();
    assertEquals(4L, allResults.size());
    // assert #1
    Work next0 = resultIterator.next();
    assertEquals(BigInteger.valueOf(10000157L), next0.getId());
    assertEquals(BigInteger.valueOf(157L), next0.getTargetId());
    assertEquals(WorkType.ChainErase, next0.getType());
    assertEquals(WorkState.Queued, next0.getState());
    // assert #2
    Work next1 = resultIterator.next();
    assertEquals(BigInteger.valueOf(2000024L), next1.getId());
    assertEquals(BigInteger.valueOf(24L), next1.getTargetId());
    assertEquals(WorkType.ChainFabricate, next1.getType());
    assertEquals(WorkState.Queued, next1.getState());
    // assert #3
    Work next2 = resultIterator.next();
    assertEquals(BigInteger.valueOf(100008907L), next2.getId());
    assertEquals(BigInteger.valueOf(8907L), next2.getTargetId());
    assertEquals(WorkType.ChainErase, next2.getType());
    assertEquals(WorkState.Expected, next2.getState());
    // assert #0
    Work next3 = resultIterator.next();
    assertEquals(BigInteger.valueOf(200003382L), next3.getId());
    assertEquals(BigInteger.valueOf(3382L), next3.getTargetId());
    assertEquals(WorkType.ChainFabricate, next3.getType());
    assertEquals(WorkState.Queued, next3.getState());
  }

  @Test
  public void reinstateAllWork() throws Exception {
    // mock Chain records in Erase state
    Collection<Chain> testChainErase = Lists.newArrayList();
    testChainErase.add(newChain(157, ChainState.Erase));
    testChainErase.add(newChain(8907, ChainState.Erase));
    when(chainDAO.readAllInState(any(), eq(ChainState.Erase))).thenReturn(testChainErase);
    // mock Chain records in Fabricate state
    Collection<Chain> testChainFabricate = Lists.newArrayList();
    testChainFabricate.add(newChain(24, ChainState.Fabricate));
    testChainFabricate.add(newChain(3382, ChainState.Fabricate));
    when(chainDAO.readAllInState(any(), eq(ChainState.Fabricate))).thenReturn(testChainFabricate);
    // mock direct query of Redis Jesque jobs
    when(redisDatabaseProvider.getClient()).thenReturn(redisConnection);
    Set<String> testQueueData = Sets.newConcurrentHashSet();
    testQueueData.add("{\"class\":\"ChainFabricate\",\"vars\":{\"targetId\":\"24\"},\"args\":null}");
    testQueueData.add("{\"class\":\"ChainFabricate\",\"vars\":{\"targetId\":\"3382\"},\"args\":null}");
    testQueueData.add("{\"class\":\"ChainErase\",\"vars\":{\"targetId\":\"157\"},\"args\":null}");
    testQueueData.add("{\"class\":\"AudioErase\",\"vars\":{\"targetId\":\"157\"},\"args\":null}");
    testQueueData.add("{\"class\":\"ProgramErase\",\"vars\":{\"targetId\":\"2965\"},\"args\":null}");
    testQueueData.add("{\"class\":\"PatternErase\",\"vars\":{\"targetId\":\"587\"},\"args\":null}");
    when(redisConnection.zrange("xj:queue:xj_test", 0L, -1L)).thenReturn(testQueueData);
    // mock redis queue redisClient
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    Collection<Work> result = subject.reinstateAllWork();

    verify(redisConnection).zrange("xj:queue:xj_test", 0L, -1L);
    // verify the platform message reporting that the job was reinstated
    ArgumentCaptor<PlatformMessage> resultMessage = ArgumentCaptor.forClass(PlatformMessage.class);
    verify(platformMessageDAO).create(any(), resultMessage.capture());
    assertEquals("Reinstated Queued ChainErase #8907", resultMessage.getValue().getBody());
    // verify the dropped chain erase job got reinstated
    Map<String, String> vars = Maps.newHashMap();
    vars.put(Work.KEY_TARGET_ID, "8907");
    verify(queueClient).recurringEnqueue(
      eq(Config.getWorkQueueName()),
      eq(new Job(WorkType.ChainErase.toString(), vars)),
      anyInt(), anyInt());
    verify(queueClient).end();
    // assert results
    Iterator<Work> resultIterator = result.iterator();
    // assert #0
    Work result0 = resultIterator.next();
    assertEquals(BigInteger.valueOf(100008907L), result0.getId());
    assertEquals(BigInteger.valueOf(8907L), result0.getTargetId());
    assertEquals(WorkType.ChainErase, result0.getType());
    assertEquals(WorkState.Queued, result0.getState());
  }

  @Test
  public void isExistingWork() throws Exception {
    // mock Chain records in Erase state
    Collection<Chain> testChainErase = Lists.newArrayList();
    testChainErase.add(newChain(157, ChainState.Erase));
    testChainErase.add(newChain(8907, ChainState.Erase));
    when(chainDAO.readAllInState(any(), eq(ChainState.Erase))).thenReturn(testChainErase);
    // mock Chain records in Fabricate state
    Collection<Chain> testChainFabricate = Lists.newArrayList();
    testChainFabricate.add(newChain(24, ChainState.Fabricate));
    testChainFabricate.add(newChain(3382, ChainState.Fabricate));
    when(chainDAO.readAllInState(any(), eq(ChainState.Fabricate))).thenReturn(testChainFabricate);
    // mock direct query of Redis Jesque jobs
    when(redisDatabaseProvider.getClient()).thenReturn(redisConnection);
    Set<String> testQueueData = Sets.newConcurrentHashSet();
    testQueueData.add("{\"class\":\"ChainFabricate\",\"vars\":{\"targetId\":\"24\"},\"args\":null}");
    testQueueData.add("{\"class\":\"ChainFabricate\",\"vars\":{\"targetId\":\"3382\"},\"args\":null}");
    testQueueData.add("{\"class\":\"ChainErase\",\"vars\":{\"targetId\":\"157\"},\"args\":null}");
    testQueueData.add("{\"class\":\"AudioErase\",\"vars\":{\"targetId\":\"157\"},\"args\":null}");
    testQueueData.add("{\"class\":\"ProgramErase\",\"vars\":{\"targetId\":\"2965\"},\"args\":null}");
    testQueueData.add("{\"class\":\"PatternErase\",\"vars\":{\"targetId\":\"587\"},\"args\":null}");
    when(redisConnection.zrange("xj:queue:xj_test", 0L, -1L)).thenReturn(testQueueData);

    // proof is in the assertions
    assertTrue(subject.isExistingWork(WorkState.Queued, WorkType.ChainFabricate, BigInteger.valueOf(3382L)));
    assertTrue(subject.isExistingWork(WorkState.Queued, WorkType.ChainErase, BigInteger.valueOf(157L)));
    assertTrue(subject.isExistingWork(WorkState.Queued, WorkType.ChainFabricate, BigInteger.valueOf(24L)));
    assertTrue(subject.isExistingWork(WorkState.Expected, WorkType.ChainErase, BigInteger.valueOf(8907L)));
    assertFalse(subject.isExistingWork(WorkState.Expected, WorkType.ChainFabricate, BigInteger.valueOf(3382L)));
    assertFalse(subject.isExistingWork(WorkState.Queued, WorkType.ChainFabricate, BigInteger.valueOf(27L)));
    assertFalse(subject.isExistingWork(WorkState.Queued, WorkType.ChainErase, BigInteger.valueOf(8907L)));
  }

}
