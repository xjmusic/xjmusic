// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus;

import com.google.api.client.util.Lists;
import com.google.api.client.util.Strings;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.api.Chain;
import io.xj.api.ChainBinding;
import io.xj.api.Segment;
import io.xj.api.SegmentState;
import io.xj.lib.app.App;
import io.xj.lib.app.AppException;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.Topology;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.TempFile;
import io.xj.lib.util.Text;
import io.xj.nexus.api.NexusAccessLogFilter;
import io.xj.nexus.dao.ChainDAO;
import io.xj.nexus.dao.Chains;
import io.xj.nexus.dao.exception.DAOExistenceException;
import io.xj.nexus.dao.exception.DAOFatalException;
import io.xj.nexus.dao.exception.DAOPrivilegeException;
import io.xj.nexus.dao.exception.DAOValidationException;
import io.xj.nexus.hub_client.client.HubAccessTokenFilter;
import io.xj.nexus.hub_client.client.HubClient;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.work.NexusWork;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.lib.filestore.FileStoreProvider.EXTENSION_JSON;

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
  private final Environment env;
  private final FileStoreProvider fileStoreProvider;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final NexusEntityStore entityStore;
  private final int rehydrateFabricatedAheadThreshold;

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

    Config config = injector.getInstance(Config.class);
    env = injector.getInstance(Environment.class);

    // Configuration
    platformRelease = env.getPlatformEnvironment();
    rehydrateFabricatedAheadThreshold = config.getInt("work.rehydrateFabricatedAheadThreshold");

    // non-static logger for this class, because app must init first
    LOG.info("{} configuration:\n{}", getName(), Text.toReport(config));

    // core delegates
    work = injector.getInstance(NexusWork.class);
    jsonProvider = injector.getInstance(JsonProvider.class);
    fileStoreProvider = injector.getInstance(FileStoreProvider.class);
    entityStore = injector.getInstance(NexusEntityStore.class);
    jsonapiPayloadFactory = injector.getInstance(JsonapiPayloadFactory.class);

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
    if (0 < env.getChainBootstrapJson().length())
      try {
        LOG.info("Will bootstrap chain from {}", env.getChainBootstrapJson());
        bootstrapFromJson(env.getChainBootstrapJson(), chainDAO, access);
      } catch (IOException e) {
        LOG.error("Failed to bootstrap!", e);
      }
    else
      LOG.info("No Chain Bootstrap specified! {}", env.getChainBootstrapJson());
  }


  /**
   Bootstrap a chain from JSON chain bootstrap data,
   first rehydrating store from last shipped JSON matching this embed key.
   <p>
   Nexus with bootstrap chain rehydrates store on startup from shipped JSON files
   https://www.pivotaltracker.com/story/show/178718006

   @param chainBootstrapJson from which to bootstrap
   @param chainDAO           to access
   @param access             control
   @throws IOException on failure
   */
  private void bootstrapFromJson(String chainBootstrapJson, ChainDAO chainDAO, HubClientAccess access) throws IOException {
    var bootstrap = jsonProvider.getObjectMapper().readValue(chainBootstrapJson, NexusChainBootstrapPayload.class);

    if (Strings.isNullOrEmpty(bootstrap.getChain().getEmbedKey())) {
      LOG.error("Can't bootstrap chain with no embed key!");
      return;
    }

    var successfulRehydration = attemptToRehydrateFrom(bootstrap);

    if (!successfulRehydration)
      try {
        LOG.info("Will bootstrap Chain[{}] bound to {}", Chains.getIdentifier(bootstrap.getChain()),
          Chains.describe(bootstrap.getChainBindings()));
        chainDAO.bootstrap(access, bootstrap.getChain(), bootstrap.getChainBindings());
      } catch (DAOFatalException | DAOPrivilegeException | DAOValidationException | DAOExistenceException e) {
        LOG.error("Failed to add binding to bootstrap Chain!", e);
      }
  }

  /**
   Attempt to rehydrate the store from a bootstrap, and return true if successful so we can skip other stuff

   @param bootstrap to rehydrate from
   @return true if successful
   */
  private Boolean attemptToRehydrateFrom(NexusChainBootstrapPayload bootstrap) {
    var success = new AtomicBoolean(true);
    Collection<Object> entities = Lists.newArrayList();
    String chainStorageKey;
    InputStream chainStream;
    JsonapiPayload chainPayload;
    Chain chain;
    try {
      LOG.info("Will check for last shipped data");
      chainStorageKey = fileStoreProvider.getChainStorageKey(Chains.getFullKey(bootstrap.getChain().getEmbedKey()), EXTENSION_JSON);
      chainStream = fileStoreProvider.streamS3Object(env.getSegmentFileBucket(), chainStorageKey);
      chainPayload = jsonProvider.getObjectMapper().readValue(chainStream, JsonapiPayload.class);
      chain = jsonapiPayloadFactory.toOne(chainPayload);
      entities.add(chain);
    } catch (FileStoreException | JsonapiException | ClassCastException | IOException e) {
      LOG.error("Failed to retrieve previously fabricated chain because {}", e.getMessage());
      return false;
    }

    try {
      LOG.info("Will load Chain[{}] for embed key \"{}\"", chain.getId(), bootstrap.getChain().getEmbedKey());
      chainPayload.getIncluded().stream()
        .filter(po -> po.isType(ChainBinding.class))
        .forEach(chainBinding -> {
          try {
            entities.add(jsonapiPayloadFactory.toOne(chainBinding));
          } catch (JsonapiException e) {
            success.set(false);
            LOG.error("Could not deserialize ChainBinding from shipped Chain JSON because {}", e.getMessage());
          }
        });

      chainPayload.getIncluded().stream()
        .filter(po -> po.isType(Segment.class))
        .flatMap(po -> {
          try {
            return Stream.of((Segment) jsonapiPayloadFactory.toOne(po));
          } catch (JsonapiException | ClassCastException e) {
            LOG.error("Could not deserialize Segment from shipped Chain JSON because {}", e.getMessage());
            success.set(false);
            return Stream.empty();
          }
        })
        .filter(seg -> SegmentState.DUBBED.equals(seg.getState()))
        .forEach(segment -> {
          try {
            var segmentStorageKey = fileStoreProvider.getSegmentStorageKey(segment.getStorageKey(), EXTENSION_JSON);
            var segmentStream = fileStoreProvider.streamS3Object(env.getSegmentFileBucket(), segmentStorageKey);
            var segmentPayload = jsonProvider.getObjectMapper().readValue(segmentStream, JsonapiPayload.class);
            AtomicInteger childCount = new AtomicInteger();
            entities.add(segment);
            segmentPayload.getIncluded().stream()
              .flatMap(po -> {
                try {
                  return Stream.of(jsonapiPayloadFactory.toOne(po));
                } catch (JsonapiException e) {
                  LOG.error("Could not deserialize Segment from shipped Chain JSON", e);
                  success.set(false);
                  return Stream.empty();
                }
              })
              .forEach(entity -> {
                entities.add(entity);
                childCount.getAndIncrement();
              });
            LOG.info("Read Segment[{}] and {} child entities", segment.getStorageKey(), childCount);

          } catch (FileStoreException | IOException | ClassCastException e) {
            LOG.error("Could not load Segment[{}]", segment.getStorageKey(), e);
            success.set(false);
          }
        });

      // Quit if anything failed up to here
      if (!success.get()) return false;

      // Nexus with bootstrap won't rehydrate stale Chain
      // https://www.pivotaltracker.com/story/show/178727631
      var fabricatedAheadSeconds = work.computeFabricatedAheadSeconds(chain,
        entities.stream()
          .filter(e -> Entities.isType(e, Segment.class))
          .map(e -> (Segment) e)
          .collect(Collectors.toList()));

      if (fabricatedAheadSeconds < rehydrateFabricatedAheadThreshold) {
        LOG.info("Will not rehydrate Chain[{}] fabricated ahead {}s (not > {}s)",
          Chains.getIdentifier(chain), fabricatedAheadSeconds, rehydrateFabricatedAheadThreshold);
        return false;
      }

      // Okay to rehydrate
      entityStore.putAll(entities);
      LOG.info("Rehydrated {} entities OK. Chain[{}] is fabricated ahead {}s",
        entities.size(), Chains.getIdentifier(chain), fabricatedAheadSeconds);
      return true;

    } catch (NexusException e) {
      LOG.error("Failed to rehydrate store because {}", e.getMessage());
      return false;
    }
  }

  /**
   Starts Grizzly HTTP server
   exposing JAX-RS resources defined in this app.
   */
  public void start() throws AppException {
    super.start();
    LOG.info("{} ({}) is up at {}}", getName(), platformRelease, getBaseURI());
  }

  /**
   Stop the work
   */
  public void stop() throws AppException {
    work.stop();
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
