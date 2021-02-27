// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.status.OnConsoleStatusListener;
import ch.qos.logback.core.status.StatusManager;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.Account;
import io.xj.Chain;
import io.xj.ChainBinding;
import io.xj.Instrument;
import io.xj.InstrumentAudio;
import io.xj.Program;
import io.xj.ProgramSequenceBinding;
import io.xj.ProgramSequencePatternEvent;
import io.xj.ProgramVoice;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChoiceArrangement;
import io.xj.SegmentChoiceArrangementPick;
import io.xj.SegmentChord;
import io.xj.SegmentChordVoicing;
import io.xj.SegmentMeme;
import io.xj.SegmentMessage;
import io.xj.lib.app.App;
import io.xj.lib.app.AppException;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.util.TempFile;
import io.xj.lib.util.Text;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.access.HubAccessLogFilter;
import io.xj.service.hub.client.HubAccessTokenFilter;
import io.xj.service.hub.client.HubClient;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.dao.ChainDAO;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;
import io.xj.service.nexus.work.NexusWork;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
  private static final String CONFIG_CHAIN_BOOTSTRAP_JSON_PATH = "chain.bootstrapJsonPath";
  private static final String CONFIG_ACCESS_TOKEN_NAME = "access.tokenName";
  private static final String ACCESS_LOG_FILE_NAME = "access.log";
  private static final String CONFIG_ACCESS_LOG_FILE = "app.accessLogFile";
  private static final String CONFIG_PLATFORM_RELEASE = "platform.release";
  private final org.slf4j.Logger log = LoggerFactory.getLogger(NexusApp.class);
  private final NexusWork work;
  private final String platformRelease;

  /**
   Construct a new application by providing
   - a config,
   - a set of resource packages to add to the core set, and
   - an injector to create a child injector of in order to add the core set.@param resourcePackages to add to the core set of packages for the new application@param resourcePackages@param injector to add to the core set of modules for the new application
   */
  public NexusApp(
    Injector injector
  ) {
    super(injector, Collections.singleton("io.xj.service.nexus.api"));

    Config config = injector.getInstance(Config.class);

    // Configuration
    platformRelease = config.getString(CONFIG_PLATFORM_RELEASE);

    // non-static logger for this class, because app must init first
    log.info("{} configuration:\n{}", getName(), Text.toReport(config));

    // core delegates
    work = injector.getInstance(NexusWork.class);

    // Setup Entity topology
    var entityFactory = injector.getInstance(EntityFactory.class);
    HubApp.buildApiTopology(entityFactory);
    buildApiTopology(entityFactory);

    // Register JAX-RS filter for access log only registers if file succeeds to open for writing
    String pathToWriteAccessLog = config.hasPath(CONFIG_ACCESS_LOG_FILE) ?
      config.getString(CONFIG_ACCESS_LOG_FILE) :
      TempFile.getTempFilePathPrefix() + File.separator + ACCESS_LOG_FILE_NAME;
    new HubAccessLogFilter(pathToWriteAccessLog).registerTo(getResourceConfig());

    // Register JAX-RS filter for reading access control token
    HubClient hubClient = injector.getInstance(HubClient.class);
    getResourceConfig().register(new HubAccessTokenFilter(hubClient, config.getString(CONFIG_ACCESS_TOKEN_NAME)));

    // [#176285826] Nexus bootstraps Chains from JSON file on startup
    var payloadFactory = injector.getInstance(PayloadFactory.class);
    var chainDAO = injector.getInstance(ChainDAO.class);
    var access = HubClientAccess.internal();

    //[#176374643] Chains bootstrapped by Nexus are delayed by N seconds
    if (config.hasPath(CONFIG_CHAIN_BOOTSTRAP_JSON_PATH)) {
      int bootstrapDelaySeconds = config.getInt("chain.bootstrapDelaySeconds");
      ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
      executorService.schedule(() ->
        bootstrapFromPath(config.getString(CONFIG_CHAIN_BOOTSTRAP_JSON_PATH), payloadFactory, entityFactory, chainDAO, access), bootstrapDelaySeconds, TimeUnit.SECONDS);
    }
  }

  private void bootstrapFromPath(String bootstrapJson, PayloadFactory payloadFactory, EntityFactory entityFactory, ChainDAO chainDAO, HubClientAccess access) {
    try {
      bootstrapFromPayload(payloadFactory.deserialize(new BufferedReader(new FileReader(bootstrapJson))), payloadFactory, entityFactory, chainDAO, access);

    } catch (FileNotFoundException e) {
      log.info("No Chain bootstrap JSON file found.");

    } catch (JsonApiException e) {
      log.warn("Failed to read Chain bootstrap JSON file!", e);
    }
  }

  private void bootstrapFromPayload(JsonapiPayload bootstrapJsonapiPayload, PayloadFactory payloadFactory, EntityFactory entityFactory, ChainDAO chainDAO, HubClientAccess access) {
    bootstrapJsonapiPayload.getDataMany().stream()
      .filter(entity -> entity.isType(Chain.class))
      .flatMap(entity -> {
        try {
          return Stream.of(payloadFactory.consume(entityFactory.getInstance(Chain.class), entity));
        } catch (JsonApiException | EntityException e) {
          log.error("Failed to bootstrap Chain!", e);
          return Stream.empty();
        }
      })
      .forEach(chain -> {
        try {
          chainDAO.bootstrap(access, chain,
            bootstrapJsonapiPayload.getIncluded().stream()
              .filter(entity -> entity.isType(ChainBinding.class))
              .flatMap(entity -> {
                try {
                  return Stream.of(payloadFactory.consume(entityFactory.getInstance(ChainBinding.class), entity));
                } catch (JsonApiException | EntityException e) {
                  log.error("Failed to bootstrap Chain!", e);
                  return Stream.empty();
                }
              })
              .collect(Collectors.toList()));
        } catch (DAOFatalException | DAOPrivilegeException | DAOValidationException | DAOExistenceException e) {
          log.error("Failed to add binding to bootstrap Chain!", e);
        }
      });
  }

  /**
   Given a entity factory, build the Hub REST API entity topology

   @param entityFactory to build topology on
   */
  public static void buildApiTopology(EntityFactory entityFactory) {
    // Chain
    entityFactory.register(Chain.class)
      .createdBy(Chain::getDefaultInstance)
      .withAttribute("name")
      .withAttribute("config")
      .withAttribute("state")
      .withAttribute("type")
      .withAttribute("startAt")
      .withAttribute("stopAt")
      .withAttribute("embedKey")
      .withAttribute("fabricationLatencySeconds")
      .belongsTo(Account.class)
      .hasMany(ChainBinding.class);

    // ChainBinding
    entityFactory.register(ChainBinding.class)
      .createdBy(ChainBinding::getDefaultInstance)
      .withAttribute("type")
      .withAttribute("targetId")
      .belongsTo(Chain.class);

    // Segment
    entityFactory.register(Segment.class)
      .createdBy(Segment::getDefaultInstance)
      .withAttribute("state")
      .withAttribute("beginAt")
      .withAttribute("endAt")
      .withAttribute("key")
      .withAttribute("total")
      .withAttribute("offset")
      .withAttribute("density")
      .withAttribute("tempo")
      .withAttribute("storageKey")
      .withAttribute("outputEncoder")
      .withAttribute("waveformPreroll")
      .withAttribute("type")
      .belongsTo(Chain.class)
      .hasMany(SegmentChoiceArrangement.class)
      .hasMany(SegmentChoice.class)
      .hasMany(SegmentChoiceArrangementPick.class)
      .hasMany(SegmentChord.class)
      .hasMany(SegmentChordVoicing.class)
      .hasMany(SegmentMeme.class)
      .hasMany(SegmentMessage.class);

    // SegmentChoice
    entityFactory.register(SegmentChoice.class)
      .createdBy(SegmentChoice::getDefaultInstance)
      .withAttribute("programType")
      .withAttribute("instrumentType")
      .withAttribute("transpose")
      .belongsTo(Segment.class)
      .belongsTo(Program.class)
      .belongsTo(ProgramSequenceBinding.class)
      .hasMany(SegmentChoiceArrangement.class);

    // SegmentChoiceArrangement
    entityFactory.register(SegmentChoiceArrangement.class)
      .createdBy(SegmentChoiceArrangement::getDefaultInstance)
      .belongsTo(Segment.class)
      .belongsTo(SegmentChoice.class)
      .belongsTo(ProgramVoice.class)
      .belongsTo(Instrument.class);

    // SegmentChoiceArrangementPick
    entityFactory.register(SegmentChoiceArrangementPick.class)
      .createdBy(SegmentChoiceArrangementPick::getDefaultInstance)
      .withAttribute("start")
      .withAttribute("length")
      .withAttribute("amplitude")
      .withAttribute("pitch")
      .withAttribute("name")
      .belongsTo(Segment.class)
      .belongsTo(SegmentChoiceArrangement.class)
      .belongsTo(InstrumentAudio.class)
      .belongsTo(ProgramSequencePatternEvent.class);

    // SegmentChord
    entityFactory.register(SegmentChord.class)
      .createdBy(SegmentChord::getDefaultInstance)
      .withAttribute("name")
      .withAttribute("position")
      .hasMany(SegmentChordVoicing.class)
      .belongsTo(Segment.class);

    // SegmentChordVoicing
    entityFactory.register(SegmentChordVoicing.class)
      .createdBy(SegmentChord::getDefaultInstance)
      .withAttribute("notes")
      .withAttribute("type")
      .belongsTo(Segment.class)
      .belongsTo(SegmentChord.class);

    // SegmentMeme
    entityFactory.register(SegmentMeme.class)
      .createdBy(SegmentMeme::getDefaultInstance)
      .withAttribute("name")
      .belongsTo(Segment.class);

    // SegmentMessage
    entityFactory.register(SegmentMessage.class)
      .createdBy(SegmentMessage::getDefaultInstance)
      .withAttribute("body")
      .withAttribute("type")
      .belongsTo(Segment.class);
  }

  /**
   Starts Grizzly HTTP server
   exposing JAX-RS resources defined in this app.
   */
  public void start() throws AppException {
    log.debug("{} will start work management before resource servers", getName());
    work.start();
    //
    super.start();
    log.info("{} ({}) is up at {}}", getName(), platformRelease, getBaseURI());
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
    log.debug("{} will stop worker pool before resource servers", getName());
    work.finish();
    //
    super.finish();
    log.info("{} ({}}) did exit OK at {}", getName(), platformRelease, getBaseURI());
  }

  /**
   Get base URI

   @return base URI
   */
  public String getBaseURI() {
    return "http://" + getRestHostname() + ":" + getRestPort() + "/";
  }
}
