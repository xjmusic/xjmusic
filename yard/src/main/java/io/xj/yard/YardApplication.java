package io.xj.yard;

import ch.qos.logback.classic.LoggerContext;
import io.xj.hub.HubTopology;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.entity.EntityFactory;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.work.NexusWork;
import io.xj.ship.work.ShipWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;

import javax.annotation.PreDestroy;
import java.util.Objects;

@SpringBootApplication
@ComponentScan(
  basePackages = {
    "io.xj.lib",
    "io.xj.hub.client",
    "io.xj.hub.service",
    "io.xj.nexus.craft",
    "io.xj.nexus.dub",
    "io.xj.nexus.fabricator",
    "io.xj.nexus.model",
    "io.xj.nexus.persistence",
    "io.xj.nexus.work",
    "io.xj.ship.broadcast",
    "io.xj.ship.source",
    "io.xj.ship.work",
    "io.xj.yard",
  })
public class YardApplication {
  final Logger LOG = LoggerFactory.getLogger(YardApplication.class);
  private final EntityFactory entityFactory;
  private final AppConfiguration config;
  private final NexusWork nexusWork;
  private final ShipWork shipWork;
  private final String hostname;
  private final String platformEnvironment;
  private final ApplicationContext context;

  @Autowired
  public YardApplication(
    EntityFactory entityFactory,
    AppConfiguration config,
    ApplicationContext context,
    NexusWork nexusWork,
    ShipWork shipWork,
    @Value("${yard.local.mode.enabled}") boolean isYardLocalModeEnabled,
    @Value("${hostname}") String hostname,
    @Value("${platform.environment}") String platformEnvironment
  ) {
    this.entityFactory = entityFactory;
    this.config = config;
    this.nexusWork = nexusWork;
    this.shipWork = shipWork;
    this.hostname = hostname;
    this.platformEnvironment = platformEnvironment;
    this.context = context;

    if (isYardLocalModeEnabled) {
      throw new RuntimeException("App environment must have Yard Local Mode enabled");
    }
  }

  @EventListener(ApplicationStartedEvent.class)
  public void start() throws YardException {
    // Add context to logs
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    lc.setPackagingDataEnabled(true);
    lc.putProperty("source", "java");
    lc.putProperty("service", "yard");
    lc.putProperty("host", hostname);
    lc.putProperty("env", platformEnvironment);

    // Setup Entity topology
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Confirm workers are instantiated
    if (Objects.isNull(nexusWork)) throw new YardException("Failed to instantiate Nexus work");
    if (Objects.isNull(shipWork)) throw new YardException("Failed to instantiate Ship work");

    // Run work on separate threads.
    LOG.info("{} will start Nexus and Ship", config.getName());
    Thread nexus = new Thread(nexusWork::start);
    Thread ship = new Thread(shipWork::doWork);
    nexus.start();
    ship.start();

    // This blocks until a graceful exit on interrupt signal
    LOG.info("{} will wait for Nexus and Ship to finish", config.getName());
    try {
      ship.join(); // first, wait for ship to finish
      nexusWork.finish();
      shipWork.finish();
      nexus.join();
      shutdown();
    } catch (InterruptedException e) {
      LOG.info("{} was interrupted", config.getName());
    }
  }

  private void shutdown() {
    LOG.debug("{} will shutdown", config.getName());
    Thread shutdown = new Thread(() -> ((ConfigurableApplicationContext) context).close());
    shutdown.setDaemon(false);
    shutdown.start();
  }

  @PreDestroy
  public void destroy() {
    if (Objects.nonNull(nexusWork)) nexusWork.finish();
    if (Objects.nonNull(shipWork)) shipWork.finish();
    LOG.debug("{} did finish work and shutdown OK", config.getName());
  }

  public static void main(String[] args) {
    SpringApplication.run(YardApplication.class, args);
  }
}
