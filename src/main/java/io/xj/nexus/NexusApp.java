// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus;

import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.app.App;
import io.xj.lib.app.AppException;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.Topology;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.util.TempFile;
import io.xj.lib.util.Text;
import io.xj.nexus.api.NexusAccessLogFilter;
import io.xj.nexus.dao.ChainDAO;
import io.xj.nexus.dao.exception.DAOExistenceException;
import io.xj.nexus.dao.exception.DAOFatalException;
import io.xj.nexus.dao.exception.DAOPrivilegeException;
import io.xj.nexus.dao.exception.DAOValidationException;
import io.xj.nexus.hub_client.client.HubAccessTokenFilter;
import io.xj.nexus.hub_client.client.HubClient;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.work.NexusWork;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 Base application for XJ services.
 <p>
 USAGE
 <p>
 + Create a Guice injector that will be used throughout the entire application, by means of:
 - Creating an application with new App(pathToConfigFile, resourcePackages, injector) <-- pass in Guice injector
 - Making that injector available to Jersey2-based resources for their injection
 - Ensuring all classes within the application are injected via their constructors (NOT creating another injector)
 - ensuring all classes rely on factory and provider modules (NOT creating another injector)
 <p>
 + Accept one runtime argument, pointing to the location of a TypeSafe config
 - ingest that configuration and make it available throughout the application
 <p>
 + Configure Jersey server resources
 <p>
 + Call application start()
 - Add shutdown hook that calls application stop()
 */
public class NexusApp extends App {
  private final org.slf4j.Logger LOG = LoggerFactory.getLogger(NexusApp.class);
  private final NexusWork work;
  private final String platformRelease;
  private final JsonProvider jsonProvider;

  /**
   Construct a new application by providing
   - a config,
   - a set of resource packages to add to the core set, and
   - an injector to create a child injector of in order to add the core set.@param resourcePackages to add to the core set of packages for the new application@param resourcePackages@param injector to add to the core set of modules for the new application
   */
  public NexusApp(
    Injector injector
  ) {
    super(injector, Collections.singleton("io.xj.nexus.api"));

    var config = injector.getInstance(Config.class);
    var env = injector.getInstance(Environment.class);

    // Configuration
    platformRelease = env.getEnvironment();

    // non-static logger for this class, because app must init first
    LOG.info("{} configuration:\n{}", getName(), Text.toReport(config));

    // core delegates
    work = injector.getInstance(NexusWork.class);
    jsonProvider = injector.getInstance(JsonProvider.class);

    // Setup Entity topology
    var entityFactory = injector.getInstance(EntityFactory.class);
    Topology.buildHubApiTopology(entityFactory);
    Topology.buildNexusApiTopology(entityFactory);

    // Register JAX-RS filter for access log only registers if file succeeds to open for writing
    String pathToWriteAccessLog = 0 < env.getAccessLogFilename().length() ?
      env.getAccessLogFilename() :
      TempFile.getTempFilePathPrefix() + File.separator + env.getAccessLogFilename();
    new NexusAccessLogFilter(pathToWriteAccessLog).registerTo(getResourceConfig());

    // Register JAX-RS filter for reading access control token
    HubClient hubClient = injector.getInstance(HubClient.class);
    getResourceConfig().register(new HubAccessTokenFilter(hubClient, env.getIngestTokenName()));

    // [#176285826] Nexus bootstraps Chains from JSON file on startup
    var chainDAO = injector.getInstance(ChainDAO.class);
    var access = HubClientAccess.internal();

    //[#176374643] Chains bootstrapped by Nexus are delayed by N seconds
    if (0 < env.getChainBootstrapJson().length()) {
      LOG.info("Will bootstrap chain from {}", env.getChainBootstrapJson());
      int bootstrapDelaySeconds = config.getInt("nexus.bootstrapDelaySeconds");
      ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
      executorService.schedule(() ->
      {
        try {
          bootstrapFromJson(env.getChainBootstrapJson(), chainDAO, access);

        } catch (IOException e) {
          LOG.error("Failed to bootstrap!", e);
        }
      }, bootstrapDelaySeconds, TimeUnit.SECONDS);
    } else {
      LOG.info("No Chain Bootstrap specified! {}", env.getChainBootstrapJson());
    }
  }


  private void bootstrapFromJson(String chainBootstrapJson, ChainDAO chainDAO, HubClientAccess access) throws IOException {
    var bootstrap = jsonProvider.getObjectMapper().readValue(chainBootstrapJson, NexusChainBootstrapPayload.class);
    try {
      chainDAO.bootstrap(access, bootstrap.getChain(), bootstrap.getChainBindings());
    } catch (DAOFatalException | DAOPrivilegeException | DAOValidationException | DAOExistenceException e) {
      LOG.error("Failed to add binding to bootstrap Chain!", e);
    }
  }

  /**
   Starts Grizzly HTTP server
   exposing JAX-RS resources defined in this app.
   */
  public void start() throws AppException {
    LOG.debug("{} will start work management before resource servers", getName());
    work.start();
    //
    super.start();
    LOG.info("{} ({}) is up at {}}", getName(), platformRelease, getBaseURI());
  }

  /**
   Get the current work manager

   @return work manager
   */
  public NexusWork getWork() {
    return work;
  }


  /**
   stop App Server
   */
  public void finish() {
    LOG.debug("{} will stop worker pool before resource servers", getName());
    work.finish();
    //
    super.finish();
    LOG.info("{} ({}}) did exit OK at {}", getName(), platformRelease, getBaseURI());
  }

  /**
   Get base URI

   @return base URI
   */
  public String getBaseURI() {
    //noinspection HttpUrlsUsage
    return "http://" + getRestHostname() + ":" + getRestPort() + "/";
  }

}
