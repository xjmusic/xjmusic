// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.work.impl;// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import com.google.api.client.util.Maps;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.config.Config;
import io.xj.core.dao.AudioDAO;
import io.xj.core.dao.ChainDAO;
import io.xj.core.dao.PatternDAO;
import io.xj.core.dao.PlatformMessageDAO;
import io.xj.core.dao.SequenceDAO;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio.AudioState;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.platform_message.PlatformMessage;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence.SequenceState;
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
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WorkManagerImplTest {
  private WorkManager subject;
  @Mock
  private ChainDAO chainDAO;
  @Mock
  private AudioDAO audioDAO;
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
  private SequenceDAO sequenceDAO;
  @Mock
  private PatternDAO patternDAO;

  @Before
  public void setUp() throws Exception {
    System.setProperty("work.queue.name", "xj_test");

    subject = createInjector().getInstance(WorkManager.class);
  }

  @After
  public void tearDown() throws Exception {
    System.clearProperty("work.queue.name");

    subject = null;
  }

  @Test
  public void startChainFabrication() {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.startChainFabrication(BigInteger.valueOf(5977L));

    Map<String, String> vars = Maps.newHashMap();
    vars.put(Work.KEY_TARGET_ID, "5977");
    verify(queueClient).recurringEnqueue(
      eq(Config.workQueueName()),
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
    verify(queueClient).removeRecurringEnqueue(Config.workQueueName(),
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
      eq(Config.workQueueName()),
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
      eq(Config.workQueueName()),
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
    verify(queueClient).removeRecurringEnqueue(Config.workQueueName(),
      new Job(WorkType.ChainErase.toString(), vars));
    verify(queueClient).end();
  }

  @Test
  public void doSequenceErase() {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.doSequenceErase(BigInteger.valueOf(5977L));

    Map<String, String> vars = Maps.newHashMap();
    vars.put(Work.KEY_TARGET_ID, "5977");
    verify(queueClient).delayedEnqueue(
      eq(Config.workQueueName()),
      eq(new Job(WorkType.SequenceErase.toString(), vars)),
      anyInt());
    verify(queueClient).end();
  }

  @Test
  public void doPatternErase() {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.doPatternErase(BigInteger.valueOf(5977L));

    Map<String, String> vars = Maps.newHashMap();
    vars.put(Work.KEY_TARGET_ID, "5977");
    verify(queueClient).delayedEnqueue(
      eq(Config.workQueueName()),
      eq(new Job(WorkType.PatternErase.toString(), vars)),
      anyInt());
    verify(queueClient).end();
  }

  @Test
  public void doAudioErase() {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.doAudioErase(BigInteger.valueOf(5977L));

    Map<String, String> vars = Maps.newHashMap();
    vars.put(Work.KEY_TARGET_ID, "5977");
    verify(queueClient).delayedEnqueue(
      eq(Config.workQueueName()),
      eq(new Job(WorkType.AudioErase.toString(), vars)),
      anyInt());
    verify(queueClient).end();
  }

  @Test
  public void doInstrumentClone() {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.doInstrumentClone(BigInteger.valueOf(421L), BigInteger.valueOf(78L));

    Map<String, String> vars = Maps.newHashMap();
    vars.put(Work.KEY_SOURCE_ID, "421");
    vars.put(Work.KEY_TARGET_ID, "78");
    verify(queueClient).delayedEnqueue(
      eq(Config.workQueueName()),
      eq(new Job(WorkType.InstrumentClone.toString(), vars)),
      anyInt());
    verify(queueClient).end();
  }

  @Test
  public void doAudioClone() {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.doAudioClone(BigInteger.valueOf(890L), BigInteger.valueOf(23L));

    Map<String, String> vars = Maps.newHashMap();
    vars.put(Work.KEY_SOURCE_ID, "890");
    vars.put(Work.KEY_TARGET_ID, "23");
    verify(queueClient).delayedEnqueue(
      eq(Config.workQueueName()),
      eq(new Job(WorkType.AudioClone.toString(), vars)),
      anyInt());
    verify(queueClient).end();
  }

  @Test
  public void doSequenceClone() {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.doSequenceClone(BigInteger.valueOf(421L), BigInteger.valueOf(78L));

    Map<String, String> vars = Maps.newHashMap();
    vars.put(Work.KEY_SOURCE_ID, "421");
    vars.put(Work.KEY_TARGET_ID, "78");
    verify(queueClient).delayedEnqueue(
      eq(Config.workQueueName()),
      eq(new Job(WorkType.SequenceClone.toString(), vars)),
      anyInt());
    verify(queueClient).end();
  }

  @Test
  public void doPatternClone() {
    when(redisDatabaseProvider.getQueueClient()).thenReturn(queueClient);

    subject.doPatternClone(BigInteger.valueOf(890L), BigInteger.valueOf(23L));

    Map<String, String> vars = Maps.newHashMap();
    vars.put(Work.KEY_SOURCE_ID, "890");
    vars.put(Work.KEY_TARGET_ID, "23");
    verify(queueClient).delayedEnqueue(
      eq(Config.workQueueName()),
      eq(new Job(WorkType.PatternClone.toString(), vars)),
      anyInt());
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
    // mock Audio records in Erase state
    Collection<Audio> testAudioErase = Lists.newArrayList();
    testAudioErase.add(new Audio(BigInteger.valueOf(157L)).setState("Erase"));
    when(audioDAO.readAllInState(any(), eq(AudioState.Erase))).thenReturn(testAudioErase);
    // mock Sequence records in Erase state
    Collection<Sequence> testSequenceErase = Lists.newArrayList();
    testSequenceErase.add(new Sequence(BigInteger.valueOf(2965L)).setState("Erase"));
    when(sequenceDAO.readAllInState(any(), eq(SequenceState.Erase))).thenReturn(testSequenceErase);
    // mock Pattern records in Erase state
    Collection<Pattern> testPatternErase = Lists.newArrayList();
    testPatternErase.add(new Pattern(BigInteger.valueOf(587L)).setState("Erase"));
    when(patternDAO.readAllInState(any(), eq(PatternState.Erase))).thenReturn(testPatternErase);
    // mock Chain records in Erase state
    Collection<Chain> testChainErase = Lists.newArrayList();
    testChainErase.add(new Chain(BigInteger.valueOf(157L)).setState("ChainErase"));
    testChainErase.add(new Chain(BigInteger.valueOf(8907L)).setState("ChainErase"));
    when(chainDAO.readAllInState(any(), eq(ChainState.Erase))).thenReturn(testChainErase);
    // mock Chain records in Fabricate state
    Collection<Chain> testChainFabricate = Lists.newArrayList();
    testChainFabricate.add(new Chain(BigInteger.valueOf(24L)).setState("ChainFabricate"));
    testChainFabricate.add(new Chain(BigInteger.valueOf(3382L)).setState("ChainFabricate"));
    when(chainDAO.readAllInState(any(), eq(ChainState.Fabricate))).thenReturn(testChainFabricate);
    // mock direct query of Redis Jesque jobs
    when(redisDatabaseProvider.getClient()).thenReturn(redisConnection);
    Set<String> testQueueData = Sets.newConcurrentHashSet();
    testQueueData.add("{\"class\":\"ChainFabricate\",\"vars\":{\"targetId\":\"24\"},\"args\":null}");
    testQueueData.add("{\"class\":\"ChainFabricate\",\"vars\":{\"targetId\":\"3382\"},\"args\":null}");
    testQueueData.add("{\"class\":\"ChainErase\",\"vars\":{\"targetId\":\"157\"},\"args\":null}");
    testQueueData.add("{\"class\":\"AudioErase\",\"vars\":{\"targetId\":\"157\"},\"args\":null}");
    testQueueData.add("{\"class\":\"SequenceErase\",\"vars\":{\"targetId\":\"2965\"},\"args\":null}");
    testQueueData.add("{\"class\":\"PatternErase\",\"vars\":{\"targetId\":\"587\"},\"args\":null}");
    when(redisConnection.zrange("xj:queue:xj_test", 0L, -1L)).thenReturn(testQueueData);

    Collection<Work> allResults = subject.readAllWork();

    verify(redisConnection).zrange("xj:queue:xj_test", 0L, -1L);
    // assert results
    Iterator<Work> resultIterator = allResults.iterator();
    assertEquals(7L, allResults.size());
    Work result;
    // assert #0
    result = resultIterator.next();
    assertEquals(BigInteger.valueOf(400003382L), result.getId());
    assertEquals(BigInteger.valueOf(3382L), result.getTargetId());
    assertEquals(WorkType.ChainFabricate, result.getType());
    assertEquals(WorkState.Queued, result.getState());
    // assert #1
    result = resultIterator.next();
    assertEquals(BigInteger.valueOf(30000157L), result.getId());
    assertEquals(BigInteger.valueOf(157L), result.getTargetId());
    assertEquals(WorkType.ChainErase, result.getType());
    assertEquals(WorkState.Queued, result.getState());
    // assert #2
    result = resultIterator.next();
    assertEquals(BigInteger.valueOf(4000024L), result.getId());
    assertEquals(BigInteger.valueOf(24L), result.getTargetId());
    assertEquals(WorkType.ChainFabricate, result.getType());
    assertEquals(WorkState.Queued, result.getState());
    // assert #5
    result = resultIterator.next();
    assertEquals(BigInteger.valueOf(800002965L), result.getId());
    assertEquals(BigInteger.valueOf(2965L), result.getTargetId());
    assertEquals(WorkType.SequenceErase, result.getType());
    assertEquals(WorkState.Queued, result.getState());
    // assert #6
    result = resultIterator.next();
    assertEquals(BigInteger.valueOf(300008907L), result.getId());
    assertEquals(BigInteger.valueOf(8907L), result.getTargetId());
    assertEquals(WorkType.ChainErase, result.getType());
    assertEquals(WorkState.Expected, result.getState());
    // assert #7
    result = resultIterator.next();
    assertEquals(BigInteger.valueOf(20000157L), result.getId());
    assertEquals(BigInteger.valueOf(157L), result.getTargetId());
    assertEquals(WorkType.AudioErase, result.getType());
    assertEquals(WorkState.Queued, result.getState());
    // assert #8
    result = resultIterator.next();
    assertEquals(BigInteger.valueOf(100000587L), result.getId());
    assertEquals(BigInteger.valueOf(587L), result.getTargetId());
    assertEquals(WorkType.PatternErase, result.getType());
    assertEquals(WorkState.Queued, result.getState());
  }

  @Test
  public void reinstateAllWork() throws Exception {
    // mock Audio records in Erase state
    Collection<Audio> testAudioErase = Lists.newArrayList();
    testAudioErase.add(new Audio(BigInteger.valueOf(157L)).setState("Erase"));
    when(audioDAO.readAllInState(any(), eq(AudioState.Erase))).thenReturn(testAudioErase);
    // mock Chain records in Erase state
    Collection<Chain> testChainErase = Lists.newArrayList();
    testChainErase.add(new Chain(BigInteger.valueOf(157L)).setState("ChainErase"));
    testChainErase.add(new Chain(BigInteger.valueOf(8907L)).setState("ChainErase"));
    when(chainDAO.readAllInState(any(), eq(ChainState.Erase))).thenReturn(testChainErase);
    // mock Chain records in Fabricate state
    Collection<Chain> testChainFabricate = Lists.newArrayList();
    testChainFabricate.add(new Chain(BigInteger.valueOf(24L)).setState("ChainFabricate"));
    testChainFabricate.add(new Chain(BigInteger.valueOf(3382L)).setState("ChainFabricate"));
    when(chainDAO.readAllInState(any(), eq(ChainState.Fabricate))).thenReturn(testChainFabricate);
    // mock Sequence records in Erase state
    Collection<Sequence> testSequenceErase = Lists.newArrayList();
    testSequenceErase.add(new Sequence(BigInteger.valueOf(2965L)).setState("Erase"));
    when(sequenceDAO.readAllInState(any(), eq(SequenceState.Erase))).thenReturn(testSequenceErase);
    // mock Pattern records in Erase state
    Collection<Pattern> testPatternErase = Lists.newArrayList();
    testPatternErase.add(new Pattern(BigInteger.valueOf(587L)).setState("Erase"));
    when(patternDAO.readAllInState(any(), eq(PatternState.Erase))).thenReturn(testPatternErase);
    // mock direct query of Redis Jesque jobs
    when(redisDatabaseProvider.getClient()).thenReturn(redisConnection);
    Set<String> testQueueData = Sets.newConcurrentHashSet();
    testQueueData.add("{\"class\":\"ChainFabricate\",\"vars\":{\"targetId\":\"24\"},\"args\":null}");
    testQueueData.add("{\"class\":\"ChainFabricate\",\"vars\":{\"targetId\":\"3382\"},\"args\":null}");
    testQueueData.add("{\"class\":\"ChainErase\",\"vars\":{\"targetId\":\"157\"},\"args\":null}");
    testQueueData.add("{\"class\":\"AudioErase\",\"vars\":{\"targetId\":\"157\"},\"args\":null}");
    testQueueData.add("{\"class\":\"SequenceErase\",\"vars\":{\"targetId\":\"2965\"},\"args\":null}");
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
      eq(Config.workQueueName()),
      eq(new Job(WorkType.ChainErase.toString(), vars)),
      anyInt(), anyInt());
    verify(queueClient).end();
    // assert results
    Iterator<Work> resultIterator = result.iterator();
    // assert #0
    Work result0 = resultIterator.next();
    assertEquals(BigInteger.valueOf(300008907L), result0.getId());
    assertEquals(BigInteger.valueOf(8907L), result0.getTargetId());
    assertEquals(WorkType.ChainErase, result0.getType());
    assertEquals(WorkState.Queued, result0.getState());
  }

  @Test
  public void isExistingWork() throws Exception {
    // mock Audio records in Erase state
    Collection<Audio> testAudioErase = Lists.newArrayList();
    testAudioErase.add(new Audio(BigInteger.valueOf(157L)).setState("Erase"));
    when(audioDAO.readAllInState(any(), eq(AudioState.Erase))).thenReturn(testAudioErase);
    // mock Sequence records in Erase state
    Collection<Sequence> testSequenceErase = Lists.newArrayList();
    testSequenceErase.add(new Sequence(BigInteger.valueOf(2965L)).setState("Erase"));
    when(sequenceDAO.readAllInState(any(), eq(SequenceState.Erase))).thenReturn(testSequenceErase);
    // mock Pattern records in Erase state
    Collection<Pattern> testPatternErase = Lists.newArrayList();
    testPatternErase.add(new Pattern(BigInteger.valueOf(587L)).setState("Erase"));
    when(patternDAO.readAllInState(any(), eq(PatternState.Erase))).thenReturn(testPatternErase);
    // mock Chain records in Erase state
    Collection<Chain> testChainErase = Lists.newArrayList();
    testChainErase.add(new Chain(BigInteger.valueOf(157L)).setState("ChainErase"));
    testChainErase.add(new Chain(BigInteger.valueOf(8907L)).setState("ChainErase"));
    when(chainDAO.readAllInState(any(), eq(ChainState.Erase))).thenReturn(testChainErase);
    // mock Chain records in Fabricate state
    Collection<Chain> testChainFabricate = Lists.newArrayList();
    testChainFabricate.add(new Chain(BigInteger.valueOf(24L)).setState("ChainFabricate"));
    testChainFabricate.add(new Chain(BigInteger.valueOf(3382L)).setState("ChainFabricate"));
    when(chainDAO.readAllInState(any(), eq(ChainState.Fabricate))).thenReturn(testChainFabricate);
    // mock direct query of Redis Jesque jobs
    when(redisDatabaseProvider.getClient()).thenReturn(redisConnection);
    Set<String> testQueueData = Sets.newConcurrentHashSet();
    testQueueData.add("{\"class\":\"ChainFabricate\",\"vars\":{\"targetId\":\"24\"},\"args\":null}");
    testQueueData.add("{\"class\":\"ChainFabricate\",\"vars\":{\"targetId\":\"3382\"},\"args\":null}");
    testQueueData.add("{\"class\":\"ChainErase\",\"vars\":{\"targetId\":\"157\"},\"args\":null}");
    testQueueData.add("{\"class\":\"AudioErase\",\"vars\":{\"targetId\":\"157\"},\"args\":null}");
    testQueueData.add("{\"class\":\"SequenceErase\",\"vars\":{\"targetId\":\"2965\"},\"args\":null}");
    testQueueData.add("{\"class\":\"PatternErase\",\"vars\":{\"targetId\":\"587\"},\"args\":null}");
    when(redisConnection.zrange("xj:queue:xj_test", 0L, -1L)).thenReturn(testQueueData);

    // proof is in the assertions
    assertTrue(subject.isExistingWork(WorkState.Queued, WorkType.ChainFabricate, BigInteger.valueOf(3382L)));
    assertTrue(subject.isExistingWork(WorkState.Queued, WorkType.ChainErase, BigInteger.valueOf(157L)));
    assertTrue(subject.isExistingWork(WorkState.Queued, WorkType.ChainFabricate, BigInteger.valueOf(24L)));
    assertTrue(subject.isExistingWork(WorkState.Queued, WorkType.SequenceErase, BigInteger.valueOf(2965L)));
    assertTrue(subject.isExistingWork(WorkState.Expected, WorkType.ChainErase, BigInteger.valueOf(8907L)));
    assertTrue(subject.isExistingWork(WorkState.Queued, WorkType.AudioErase, BigInteger.valueOf(157L)));
    assertTrue(subject.isExistingWork(WorkState.Queued, WorkType.PatternErase, BigInteger.valueOf(587L)));
    assertFalse(subject.isExistingWork(WorkState.Expected, WorkType.ChainFabricate, BigInteger.valueOf(3382L)));
    assertFalse(subject.isExistingWork(WorkState.Queued, WorkType.SequenceErase, BigInteger.valueOf(157L)));
    assertFalse(subject.isExistingWork(WorkState.Queued, WorkType.ChainFabricate, BigInteger.valueOf(27L)));
    assertFalse(subject.isExistingWork(WorkState.Expected, WorkType.SequenceErase, BigInteger.valueOf(2965L)));
    assertFalse(subject.isExistingWork(WorkState.Queued, WorkType.ChainErase, BigInteger.valueOf(8907L)));
    assertFalse(subject.isExistingWork(WorkState.Queued, WorkType.AudioErase, BigInteger.valueOf(1507L)));
    assertFalse(subject.isExistingWork(WorkState.Queued, WorkType.PatternErase, BigInteger.valueOf(5087L)));
  }


  private Injector createInjector() {
    return Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(ChainDAO.class).toInstance(chainDAO);
          bind(AudioDAO.class).toInstance(audioDAO);
          bind(SequenceDAO.class).toInstance(sequenceDAO);
          bind(PatternDAO.class).toInstance(patternDAO);
          bind(PlatformMessageDAO.class).toInstance(platformMessageDAO);
          bind(RedisDatabaseProvider.class).toInstance(redisDatabaseProvider);
        }
      }));
  }

}
