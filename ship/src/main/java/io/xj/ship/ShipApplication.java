package io.xj.ship;

import ch.qos.logback.classic.LoggerContext;
import io.xj.hub.HubTopology;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.entity.EntityFactory;
import io.xj.nexus.NexusTopology;
import io.xj.ship.work.ShipWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import javax.annotation.PreDestroy;
import java.util.Objects;

@SpringBootApplication
@ComponentScan(basePackages = {"io.xj.lib", "io.xj.nexus.model", "io.xj.nexus.persistence", "io.xj.ship"})
public class ShipApplication {
  final Logger LOG = LoggerFactory.getLogger(ShipApplication.class);
  private final EntityFactory entityFactory;
  private final AppEnvironment env;
  private final AppConfiguration config;
  private final ShipWork work;

  @Autowired
  public ShipApplication(EntityFactory entityFactory, AppEnvironment env, AppConfiguration config, ShipWork work) {
    this.entityFactory = entityFactory;
    this.env = env;
    this.config = config;
    this.work = work;
  }

  @EventListener(ContextRefreshedEvent.class)
  public void onApplicationReady() throws ShipException {
    // Add context to logs
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    lc.setPackagingDataEnabled(true);
    lc.putProperty("source", "java");
    lc.putProperty("service", "ship");
    lc.putProperty("host", env.getHostname());
    lc.putProperty("env", env.getPlatformEnvironment());

    // Setup Entity topology
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Start work. This blocks until a graceful exit on interrupt signal
    LOG.debug("{} will start", config.getName());
    if (Objects.isNull(work)) throw new ShipException("Failed to instantiate work");
    work.doWork();
  }

  @PreDestroy
  public void destroy() {
    if (Objects.nonNull(work)) work.finish();
    LOG.debug("{} did finish work and shutdown OK", config.getName());
  }

  public static void main(String[] args) {
    SpringApplication.run(ShipApplication.class, args);
  }

}
