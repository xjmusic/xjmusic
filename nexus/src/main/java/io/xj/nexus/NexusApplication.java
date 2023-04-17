package io.xj.nexus;

import ch.qos.logback.classic.LoggerContext;
import io.xj.hub.HubTopology;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.entity.EntityFactory;
import io.xj.nexus.work.NexusWork;
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
@ComponentScan(basePackages = {"io.xj.lib", "io.xj.hub", "io.xj.nexus"})
public class NexusApplication {
  final Logger LOG = LoggerFactory.getLogger(NexusApplication.class);
  private final EntityFactory entityFactory;
  private final AppEnvironment env;
  private final AppConfiguration config;
  private final NexusWork work;

  @Autowired
  public NexusApplication(EntityFactory entityFactory, AppEnvironment env, AppConfiguration config, NexusWork work) {
    this.entityFactory = entityFactory;
    this.env = env;
    this.config = config;
    this.work = work;
  }

  @EventListener(ContextRefreshedEvent.class)
  public void onApplicationReady() throws NexusException {
    // Add context to logs
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    lc.setPackagingDataEnabled(true);
    lc.putProperty("source", "java");
    lc.putProperty("service", "nexus");
    lc.putProperty("host", env.getHostname());
    lc.putProperty("env", env.getPlatformEnvironment());

    // Setup Entity topology
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Start work. This blocks until a graceful exit on interrupt signal
    if (Objects.isNull(work)) throw new NexusException("Failed to instantiate work");
    work.start();

    LOG.debug("{} will start", config.getName());
  }

  @PreDestroy
  public void destroy() {
    if (Objects.nonNull(work)) work.finish();
    LOG.debug("{} did finish work and shutdown OK", config.getName());
  }

  public static void main(String[] args) {
    SpringApplication.run(NexusApplication.class, args);
  }

}