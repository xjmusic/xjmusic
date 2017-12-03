package io.xj.core.work.impl;// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.

import org.jooq.types.ULong;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import io.xj.core.CoreModule;
import io.xj.core.config.Config;
import io.xj.core.dao.ChainDAO;
import io.xj.core.dao.PlatformMessageDAO;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.platform_message.PlatformMessage;
import io.xj.core.model.work.Work;
import io.xj.core.model.work.WorkState;
import io.xj.core.model.work.WorkType;
import io.xj.core.persistence.redis.RedisDatabaseProvider;
import io.xj.core.work.WorkManager;
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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WorkManagerImplTest {
  private WorkManager subject;
  private Injector injector;
  @Mock private ChainDAO chainDAO;
  @Mock private RedisDatabaseProvider redisDatabaseProvider;
  @Mock private Client queueClient;
  @Mock private JobFactory jobFactory;
  @Mock private Worker worker;
  @Mock private Jedis client;
  @Mock private PlatformMessageDAO platformMessageDAO;

  @Before
  public void setUp() throws Exception {
    createInjector();
    System.setProperty("work.queue.name", "xj_test");

    subject = injector.getInstance(WorkManager.class);
  }

  @After
  public void tearDown() throws Exception {
    System.clearProperty("work.queue.name");

    subject = null;
  }

  @Test
  public void startChainFabrication() throws Exception {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.startChainFabrication(ULong.valueOf(5977));

    verify(queueClient).recurringEnqueue(
      eq(Config.workQueueName()),
      eq(new Job(WorkType.ChainFabricate.toString(), ULong.valueOf(5977))),
      anyInt(), anyInt());
    verify(queueClient).end();
  }

  @Test
  public void stopChainFabrication() throws Exception {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.stopChainFabrication(ULong.valueOf(5977));

    verify(queueClient).removeRecurringEnqueue(Config.workQueueName(),
      new Job(WorkType.ChainFabricate.toString(), ULong.valueOf(5977)));
    verify(queueClient).end();
  }

  @Test
  public void scheduleLinkCraft() throws Exception {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.scheduleLinkCraft(ULong.valueOf(5977), 10);

    verify(queueClient).delayedEnqueue(
      eq(Config.workQueueName()),
      eq(new Job(WorkType.LinkCraft.toString(), ULong.valueOf(5977))),
      anyInt());
    verify(queueClient).end();
  }

  @Test
  public void scheduleLinkDub() throws Exception {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.scheduleLinkDub(ULong.valueOf(5977), 10);

    verify(queueClient).delayedEnqueue(
      eq(Config.workQueueName()),
      eq(new Job(WorkType.LinkDub.toString(), ULong.valueOf(5977))),
      anyInt());
    verify(queueClient).end();
  }

  @Test
  public void startChainDeletion() throws Exception {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.startChainDeletion(ULong.valueOf(5977));

    verify(queueClient).recurringEnqueue(
      eq(Config.workQueueName()),
      eq(new Job(WorkType.ChainErase.toString(), ULong.valueOf(5977))),
      anyInt(), anyInt());
    verify(queueClient).end();
  }

  @Test
  public void stopChainDeletion() throws Exception {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.stopChainDeletion(ULong.valueOf(5977));

    verify(queueClient).removeRecurringEnqueue(Config.workQueueName(),
      new Job(WorkType.ChainErase.toString(), ULong.valueOf(5977)));
    verify(queueClient).end();
  }

  @Test
  public void doAudioDeletion() throws Exception {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.doAudioDeletion(ULong.valueOf(5977));

    verify(queueClient).enqueue(
      eq(Config.workQueueName()),
      eq(new Job(WorkType.AudioErase.toString(), ULong.valueOf(5977))));
    verify(queueClient).end();
  }

  @Test
  public void getWorker() throws Exception {
    when(redisDatabaseProvider.getQueueWorker(jobFactory)).thenReturn(worker);

    Worker result = subject.getWorker(jobFactory);

    assertEquals(worker, result);
  }

  @Test
  public void readAllWork() throws Exception {
    // mock Chain records in Erase state
    Collection<Chain> testChainErase = Lists.newArrayList();
    testChainErase.add(new Chain(ULong.valueOf(157)).setState("ChainErase"));
    testChainErase.add(new Chain(ULong.valueOf(8907)).setState("ChainErase"));
    when(chainDAO.readAllInState(any(), eq(ChainState.Erase))).thenReturn(testChainErase);
    // mock Chain records in Fabricate state
    Collection<Chain> testChainFabricate = Lists.newArrayList();
    testChainFabricate.add(new Chain(ULong.valueOf(24)).setState("ChainFabricate"));
    testChainFabricate.add(new Chain(ULong.valueOf(3382)).setState("ChainFabricate"));
    when(chainDAO.readAllInState(any(), eq(ChainState.Fabricate))).thenReturn(testChainFabricate);
    // mock direct query of Redis Jesque jobs
    when(redisDatabaseProvider.getClient()).thenReturn(client);
    Set<String> testQueueData = Sets.newConcurrentHashSet();
    testQueueData.add("{\"class\":\"ChainFabricate\",\"args\":[24],\"vars\":null}");
    testQueueData.add("{\"class\":\"ChainFabricate\",\"args\":[3382],\"vars\":null}");
    testQueueData.add("{\"class\":\"ChainErase\",\"args\":[157],\"vars\":null}");
    when(client.zrange("xj:queue:xj_test", 0, -1)).thenReturn(testQueueData);

    Collection<Work> result = subject.readAllWork();

    verify(client).zrange("xj:queue:xj_test", 0, -1);
    // assert results
    Iterator<Work> resultIterator = result.iterator();
    // assert #0
    Work result0 = resultIterator.next();
    assertEquals(ULong.valueOf(324), result0.getId());
    assertEquals(ULong.valueOf(24), result0.getTargetId());
    assertEquals(WorkType.ChainFabricate, result0.getType());
    assertEquals(WorkState.Queued, result0.getState());
    // assert #1
    Work result1 = resultIterator.next();
    assertEquals(ULong.valueOf(33382), result1.getId());
    assertEquals(ULong.valueOf(3382), result1.getTargetId());
    assertEquals(WorkType.ChainFabricate, result1.getType());
    assertEquals(WorkState.Queued, result1.getState());
    // assert #2
    Work result2 = resultIterator.next();
    assertEquals(ULong.valueOf(28907), result2.getId());
    assertEquals(ULong.valueOf(8907), result2.getTargetId());
    assertEquals(WorkType.ChainErase, result2.getType());
    assertEquals(WorkState.Expected, result2.getState());
    // assert #3
    Work result3 = resultIterator.next();
    assertEquals(ULong.valueOf(2157), result3.getId());
    assertEquals(ULong.valueOf(157), result3.getTargetId());
    assertEquals(WorkType.ChainErase, result3.getType());
    assertEquals(WorkState.Queued, result3.getState());
  }

  @Test
  public void reinstateAllWork() throws Exception {
    // mock Chain records in Erase state
    Collection<Chain> testChainErase = Lists.newArrayList();
    testChainErase.add(new Chain(ULong.valueOf(157)).setState("ChainErase"));
    testChainErase.add(new Chain(ULong.valueOf(8907)).setState("ChainErase"));
    when(chainDAO.readAllInState(any(), eq(ChainState.Erase))).thenReturn(testChainErase);
    // mock Chain records in Fabricate state
    Collection<Chain> testChainFabricate = Lists.newArrayList();
    testChainFabricate.add(new Chain(ULong.valueOf(24)).setState("ChainFabricate"));
    testChainFabricate.add(new Chain(ULong.valueOf(3382)).setState("ChainFabricate"));
    when(chainDAO.readAllInState(any(), eq(ChainState.Fabricate))).thenReturn(testChainFabricate);
    // mock direct query of Redis Jesque jobs
    when(redisDatabaseProvider.getClient()).thenReturn(client);
    Set<String> testQueueData = Sets.newConcurrentHashSet();
    testQueueData.add("{\"class\":\"ChainFabricate\",\"args\":[24],\"vars\":null}");
    testQueueData.add("{\"class\":\"ChainFabricate\",\"args\":[3382],\"vars\":null}");
    testQueueData.add("{\"class\":\"ChainErase\",\"args\":[157],\"vars\":null}");
    when(client.zrange("xj:queue:xj_test", 0, -1)).thenReturn(testQueueData);
    // mock redis queue client
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    Collection<Work> result = subject.reinstateAllWork();

    verify(client).zrange("xj:queue:xj_test", 0, -1);
    // verify the platform message reporting that the job was reinstated
    ArgumentCaptor<PlatformMessage> resultMessage = ArgumentCaptor.forClass(PlatformMessage.class);
    verify(platformMessageDAO).create(any(), resultMessage.capture());
    assertEquals("Reinstated work {\"targetId\":8907,\"id\":28907,\"state\":\"Queued\",\"type\":\"ChainErase\"}", resultMessage.getValue().getBody());
    // verify the dropped chain deletion job got reinstated
    verify(queueClient).recurringEnqueue(
      eq(Config.workQueueName()),
      eq(new Job(WorkType.ChainErase.toString(), ULong.valueOf(8907))),
      anyInt(), anyInt());
    verify(queueClient).end();
    // assert results
    Iterator<Work> resultIterator = result.iterator();
    // assert #0
    Work result0 = resultIterator.next();
    assertEquals(ULong.valueOf(28907), result0.getId());
    assertEquals(ULong.valueOf(8907), result0.getTargetId());
    assertEquals(WorkType.ChainErase, result0.getType());
    assertEquals(WorkState.Queued, result0.getState());
  }

  private void createInjector() {
    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(ChainDAO.class).toInstance(chainDAO);
          bind(PlatformMessageDAO.class).toInstance(platformMessageDAO);
          bind(RedisDatabaseProvider.class).toInstance(redisDatabaseProvider);
        }
      }));
  }

}
