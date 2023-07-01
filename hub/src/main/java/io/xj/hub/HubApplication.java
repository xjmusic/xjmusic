package io.xj.hub;

import ch.qos.logback.classic.LoggerContext;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.persistence.HubMigration;
import io.xj.hub.persistence.HubPersistenceException;
import io.xj.hub.persistence.kv.HubKvStoreProvider;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
  private final HubKvStoreProvider hubKvStoreProvider;
  private final HubSqlStoreProvider hubSqlStoreProvider;
  private final EntityFactory entityFactory;
  private final HubMigration hubMigration;
  private final AppConfiguration config;
  private final String hostname;
  private final String platformEnvironment;

  @Autowired
  public HubApplication(HubKvStoreProvider hubKvStoreProvider, HubSqlStoreProvider hubSqlStoreProvider, EntityFactory entityFactory, HubMigration hubMigration, AppConfiguration config, @Value("${hostname}") String hostname, @Value("${platform.environment}") String platformEnvironment) {
    this.hubKvStoreProvider = hubKvStoreProvider;
    this.hubSqlStoreProvider = hubSqlStoreProvider;
    this.entityFactory = entityFactory;
    this.hubMigration = hubMigration;
    this.config = config;
    this.hostname = hostname;
    this.platformEnvironment = platformEnvironment;
  }

  @EventListener(ContextRefreshedEvent.class)
  public void onApplicationReady() {
    // Add context to logs
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    lc.setPackagingDataEnabled(true);
    lc.putProperty("source", "java");
    lc.putProperty("service", "hub");
    lc.putProperty("host", hostname);
    lc.putProperty("env", platformEnvironment);

    // Setup Entity topology
    HubTopology.buildHubApiTopology(entityFactory);

    // run database migrations
    try {
      hubMigration.migrate();
    } catch (HubPersistenceException e) {
      String platformRelease = platformEnvironment;
      System.out.printf("Migrations failed! HubApp %s will not start. %s: %s\n%s%n", platformRelease, e.getClass().getSimpleName(), e.getMessage(), Text.formatStackTrace(e));
      System.exit(1);
    }

    LOG.debug("{} will start", config.getName());
  }

  @PreDestroy
  public void destroy() {
    hubSqlStoreProvider.shutdown();
    hubKvStoreProvider.shutdown();
    LOG.debug("{} did teardown SQL connection pool and shutdown OK", config.getName());
  }

  public static void main(String[] args) {
    SpringApplication.run(HubApplication.class, args);
  }

}
