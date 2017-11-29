package io.xj.core.work;

import io.xj.core.work.impl.RobustWorkerPool;

import net.greghaines.jesque.Config;
import net.greghaines.jesque.ConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Testing for {@link RobustWorkerPool}.
 * <p>
 * NOTES: redis-server must upped on testing environment.
 */
class RobustWorkerPoolTest {
  private static final Logger log = LoggerFactory.getLogger(RobustWorkerPoolTest.class);

  private static final Config CONFIG = new ConfigBuilder().build();

/*
 This is a dirty, dirty test.
 If we're going to test the exception cases of the robust worker pool,
 Everything needs to be mocked, and the test must be tight.
 As it is now, with a sleep, begging for race condition failure.....
 It stays commented out.


  @Before
  public void setup() {
    Jedis jedis = new Jedis("localhost");
    jedis.flushAll();
  }

  @Test
  public void shouldWorkerPoolingSuccessfully() throws InterruptedException {
    // Add a job to the queue
    final Job job = new Job("TestAction", Collections.emptyList());
    final Client client = new ClientImpl(CONFIG);
    client.enqueue("foo", job);
    client.end();

    // Start a worker to run jobs from the queue
    final Map<String, Class<? extends Runnable>> actionMap = new HashMap<>();
    actionMap.put("TestAction", TestAction.class);

    final RobustWorkerPool workerPool = new RobustWorkerPool(() ->
      new WorkerImpl(CONFIG, Collections.singletonList("foo"), new MapBasedJobFactory(actionMap)),
      10, Executors.defaultThreadFactory());

    workerPool.run();

    // XXX: dirty...
    // To wait until all workers are spawned
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
    }

    assertThat(workerPool.getNumWorkers()).isEqualTo(10);
    assertThat(workerPool.getWorkerSet().size()).isEqualTo(10);
    assertThat(workerPool.getWorkerThreadMap().size()).isEqualTo(10);

    workerPool.endAndJoin(false, 0);
  }

  @Test
  public void shouldMissingWorkerReincarnationSuccessfully() throws InterruptedException {
    // Add a job to the queue
    final Job job = new Job("TestAction", Collections.emptyList());
    final Client client = new ClientImpl(CONFIG);
    client.enqueue("foo", job);
    client.end();

    // Start a worker to run jobs from the queue
    final Map<String, Class<? extends Runnable>> actionMap = new HashMap<>();
    actionMap.put("TestAction", TestAction.class);

    final RobustWorkerPool workerPool = new RobustWorkerPool(() ->
      new FailingWorker(CONFIG, Collections.singletonList("foo"), new MapBasedJobFactory(actionMap)),
      10, Executors.defaultThreadFactory(), 500);

    workerPool.run();

    // XXX: dirty...
    // To wait until all workers are spawned
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
    }

    assertThat(workerPool.getNumWorkers()).isEqualTo(10);
    assertThat(workerPool.getWorkerSet().size()).isEqualTo(10);
    assertThat(workerPool.getWorkerThreadMap().size()).isEqualTo(10);
  }

  @Test
  public void shouldExcessWorkerTerminationSuccessfully() throws InterruptedException {
    // Add a job to the queue
    final Job job = new Job("TestAction", Collections.emptyList());
    final Client client = new ClientImpl(CONFIG);
    client.enqueue("foo", job);
    client.end();

    // Start a worker to run jobs from the queue
    final Map<String, Class<? extends Runnable>> actionMap = new HashMap<>();
    actionMap.put("TestAction", TestAction.class);

    final RobustWorkerPool workerPool = new RobustWorkerPool(() ->
      new FailingWorker(CONFIG, Collections.singletonList("foo"), new MapBasedJobFactory(actionMap)),
      10, Executors.defaultThreadFactory(), 500);

    workerPool.setNumWorkers(8); // change number of upper limit of workers

    workerPool.run();

    // XXX: dirty...
    // To wait until all workers are spawned
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
    }

    assertThat(workerPool.getNumWorkers()).isEqualTo(8);
    assertThat(workerPool.getWorkerSet().size()).isEqualTo(8);
    assertThat(workerPool.getWorkerThreadMap().size()).isEqualTo(8);
  }

  public static class TestAction implements Runnable {
    public void run() {
      log.debug("Run");
    }
  }

  public static class FailingWorker extends WorkerImpl {
    public FailingWorker(Config config, Collection<String> queues, JobFactory jobFactory) {
      super(config, queues, jobFactory);
    }

    @Override
    public void poll() {
      log.debug("Fail");
      throw new RuntimeException();
    }
  }*/
}
