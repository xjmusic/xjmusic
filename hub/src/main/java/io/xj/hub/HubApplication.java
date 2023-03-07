package io.xj.hub;

import ch.qos.logback.classic.LoggerContext;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.persistence.HubMigration;
import io.xj.hub.persistence.HubPersistenceException;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import javax.annotation.PreDestroy;

@SpringBootApplication
@ComponentScan(basePackages = {"io.xj.lib", "io.xj.hub"})
public class HubApplication {
  final Logger LOG = LoggerFactory.getLogger(HubApplication.class);
  private final HubSqlStoreProvider hubSqlStoreProvider;
  private final EntityFactory entityFactory;
  private final HubMigration hubMigration;
  private final AppEnvironment env;
  private final AppConfiguration config;

  @Autowired
  public HubApplication(HubSqlStoreProvider hubSqlStoreProvider, EntityFactory entityFactory, HubMigration hubMigration, AppEnvironment env, AppConfiguration config) {
    this.hubSqlStoreProvider = hubSqlStoreProvider;
    this.entityFactory = entityFactory;
    this.hubMigration = hubMigration;
    this.env = env;
    this.config = config;
  }

  @EventListener(ContextRefreshedEvent.class)
  public void onApplicationReady() {
    // Add context to logs
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    lc.setPackagingDataEnabled(true);
    lc.putProperty("source", "java");
    lc.putProperty("service", "hub");
    lc.putProperty("host", env.getHostname());
    lc.putProperty("env", env.getPlatformEnvironment());

    // Setup Entity topology
    HubTopology.buildHubApiTopology(entityFactory);

    // run database migrations
    try {
      hubMigration.migrate();
    } catch (HubPersistenceException e) {
      String platformRelease = env.getPlatformEnvironment();
      System.out.printf("Migrations failed! HubApp %s will not start. %s: %s\n%s%n", platformRelease, e.getClass().getSimpleName(), e.getMessage(), Text.formatStackTrace(e));
      System.exit(1);
    }

    LOG.debug("{} will start", config.getName());
  }

  @PreDestroy
  public void destroy() {
    hubSqlStoreProvider.shutdown();
    LOG.debug("{} did teardown SQL connection pool and shutdown OK", config.getName());
  }

  public static void main(String[] args) {
    SpringApplication.run(HubApplication.class, args);
  }

}
