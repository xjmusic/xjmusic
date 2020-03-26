// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub;

import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.app.App;
import io.xj.lib.app.AppException;
import io.xj.lib.rest_api.ApiUrlProvider;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.util.TempFile;
import io.xj.lib.util.Text;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.access.AccessControlProvider;
import io.xj.service.hub.access.AccessLogFilter;
import io.xj.service.hub.access.AccessTokenAuthFilter;
import io.xj.service.hub.dao.PlatformMessageDAO;
import io.xj.service.hub.entity.MessageType;
import io.xj.service.hub.model.Account;
import io.xj.service.hub.model.AccountUser;
import io.xj.service.hub.model.Chain;
import io.xj.service.hub.model.ChainBinding;
import io.xj.service.hub.model.ChainConfig;
import io.xj.service.hub.model.Instrument;
import io.xj.service.hub.model.InstrumentAudio;
import io.xj.service.hub.model.InstrumentAudioChord;
import io.xj.service.hub.model.InstrumentAudioEvent;
import io.xj.service.hub.model.InstrumentMeme;
import io.xj.service.hub.model.Library;
import io.xj.service.hub.model.PlatformMessage;
import io.xj.service.hub.model.Program;
import io.xj.service.hub.model.ProgramMeme;
import io.xj.service.hub.model.ProgramSequence;
import io.xj.service.hub.model.ProgramSequenceBinding;
import io.xj.service.hub.model.ProgramSequenceBindingMeme;
import io.xj.service.hub.model.ProgramSequenceChord;
import io.xj.service.hub.model.ProgramSequencePattern;
import io.xj.service.hub.model.ProgramSequencePatternEvent;
import io.xj.service.hub.model.ProgramVoice;
import io.xj.service.hub.model.ProgramVoiceTrack;
import io.xj.service.hub.model.Segment;
import io.xj.service.hub.model.SegmentChoice;
import io.xj.service.hub.model.SegmentChoiceArrangement;
import io.xj.service.hub.model.SegmentChoiceArrangementPick;
import io.xj.service.hub.model.SegmentChord;
import io.xj.service.hub.model.SegmentMeme;
import io.xj.service.hub.model.SegmentMessage;
import io.xj.service.hub.model.User;
import io.xj.service.hub.model.UserAuth;
import io.xj.service.hub.model.UserAuthToken;
import io.xj.service.hub.model.UserRole;
import io.xj.service.hub.model.Work;
import io.xj.service.hub.persistence.Migration;
import io.xj.service.hub.persistence.SQLDatabaseProvider;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Set;

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
public class HubApp extends App {
  private final org.slf4j.Logger log = LoggerFactory.getLogger(HubApp.class);
  private final PlatformMessageDAO platformMessageDAO;
  private final String platformRelease;
  private final Injector injector;
  private final SQLDatabaseProvider sqlDatabaseProvider;

  /**
   Construct a new application by providing
   - a config,
   - a set of resource packages to add to the core set, and
   - an injector to create a child injector of in order to add the core set.@param resourcePackages to add to the core set of packages for the new application@param resourcePackages

   @param injector to add to the core set of modules for the new application
   */
  public HubApp(
    Set<String> resourcePackages,
    Injector injector
  ) {
    super(resourcePackages, injector, HubApp.class.getSimpleName());

    // Injection
    this.injector = injector;
    platformMessageDAO = injector.getInstance(PlatformMessageDAO.class);
    sqlDatabaseProvider = injector.getInstance(SQLDatabaseProvider.class);
    Config config = injector.getInstance(Config.class);

    // Configuration
    platformRelease = config.getString("platform.release");

    // Non-static logger for this class, because app must init first
    log.info("{} configuration:\n{}", getName(), Text.toReport(config));

    // Setup REST API payload topology
    buildApiTopology(injector.getInstance(PayloadFactory.class));

    // Configure REST API url provider
    configureApiUrls(config, injector.getInstance(ApiUrlProvider.class));

    // Register JAX-RS filter for access log only registers if file succeeds to open for writing
    String pathToWriteAccessLog = config.hasPath("app.accessLogFile") ?
      config.getString("app.accessLogFile") :
      String.format("%s%saccess.log", TempFile.getTempFilePathPrefix(), File.separator);
    new AccessLogFilter(pathToWriteAccessLog).registerTo(getResourceConfig());

    // Register JAX-RS filter for reading access control token
    AccessControlProvider accessControlProvider = injector.getInstance(AccessControlProvider.class);
    getResourceConfig().register(new AccessTokenAuthFilter(accessControlProvider, config.getString("access.tokenName")));
  }

  /**
   Given a payload factory, build the Hub REST API payload topology

   @param payloadFactory to build topology on
   */
  public static void buildApiTopology(PayloadFactory payloadFactory) {
    // Account
    payloadFactory.register(Account.class)
      .withAttribute("name")
      .hasMany(Library.class)
      .hasMany(AccountUser.class)
      .hasMany(Chain.class);

    // AccountUser
    payloadFactory.register(AccountUser.class)
      .belongsTo(Account.class)
      .belongsTo(User.class);

    // Chain
    payloadFactory.register(Chain.class)
      .withAttribute("name")
      .withAttribute("state")
      .withAttribute("type")
      .withAttribute("startAt")
      .withAttribute("stopAt")
      .withAttribute("embedKey")
      .belongsTo(Account.class)
      .hasMany(ChainBinding.class)
      .hasMany(ChainConfig.class);

    // ChainBinding
    payloadFactory.register(ChainBinding.class)
      .withAttribute("type")
      .withAttribute("targetId")
      .belongsTo(io.xj.service.hub.model.Chain.class);

    // ChainConfig
    payloadFactory.register(ChainConfig.class)
      .withAttribute("type")
      .withAttribute("value")
      .belongsTo(io.xj.service.hub.model.Chain.class);

    // Instrument
    payloadFactory.register(Instrument.class)
      .withAttribute("state")
      .withAttribute("type")
      .withAttribute("name")
      .withAttribute("density")
      .belongsTo(User.class)
      .belongsTo(Library.class)
      .hasMany(InstrumentAudio.class)
      .hasMany(InstrumentMeme.class);

    // InstrumentAudio
    payloadFactory.register(InstrumentAudio.class)
      .withAttribute("waveformKey")
      .withAttribute("name")
      .withAttribute("start")
      .withAttribute("length")
      .withAttribute("tempo")
      .withAttribute("pitch")
      .withAttribute("density")
      .belongsTo(io.xj.service.hub.model.Instrument.class)
      .hasMany(InstrumentAudioChord.class)
      .hasMany(InstrumentAudioEvent.class);

    // InstrumentAudioChord
    payloadFactory.register(InstrumentAudioChord.class)
      .withAttribute("name")
      .withAttribute("position")
      .belongsTo(io.xj.service.hub.model.Instrument.class)
      .belongsTo(io.xj.service.hub.model.InstrumentAudio.class);

    // InstrumentAudioEvent
    payloadFactory.register(InstrumentAudioEvent.class)
      .withAttribute("duration")
      .withAttribute("note")
      .withAttribute("position")
      .withAttribute("velocity")
      .withAttribute("name")
      .belongsTo(io.xj.service.hub.model.Instrument.class)
      .belongsTo(io.xj.service.hub.model.InstrumentAudio.class);

    // InstrumentMeme
    payloadFactory.register(InstrumentMeme.class)
      .withAttribute("name")
      .belongsTo(io.xj.service.hub.model.Instrument.class);

    // Library
    payloadFactory.register(Library.class)
      .withAttribute("name")
      .belongsTo(Account.class)
      .hasMany(io.xj.service.hub.model.Instrument.class)
      .hasMany(Program.class);

    // PlatformMessage
    payloadFactory.register(PlatformMessage.class)
      .withAttribute("body")
      .withAttribute("type");

    // Program
    payloadFactory.register(Program.class)
      .withAttribute("state")
      .withAttribute("key")
      .withAttribute("tempo")
      .withAttribute("type")
      .withAttribute("name")
      .withAttribute("density")
      .belongsTo(User.class)
      .belongsTo(io.xj.service.hub.model.Library.class)
      .hasMany(ProgramMeme.class)
      .hasMany(ProgramSequence.class)
      .hasMany(ProgramSequenceChord.class)
      .hasMany(ProgramSequencePattern.class)
      .hasMany(ProgramVoiceTrack.class)
      .hasMany(ProgramSequencePatternEvent.class)
      .hasMany(ProgramSequenceBinding.class)
      .hasMany(ProgramSequenceBindingMeme.class)
      .hasMany(ProgramVoice.class);

    // ProgramMeme
    payloadFactory.register(ProgramMeme.class)
      .withAttribute("name")
      .belongsTo(io.xj.service.hub.model.Program.class);

    // ProgramSequence
    payloadFactory.register(ProgramSequence.class)
      .withAttribute("name")
      .withAttribute("key")
      .withAttribute("density")
      .withAttribute("total")
      .withAttribute("tempo")
      .belongsTo(io.xj.service.hub.model.Program.class)
      .hasMany(ProgramSequencePattern.class)
      .hasMany(ProgramSequenceBinding.class)
      .hasMany(ProgramSequenceChord.class);

    // ProgramSequenceBinding
    payloadFactory.register(ProgramSequenceBinding.class)
      .withAttribute("offset")
      .belongsTo(io.xj.service.hub.model.Program.class)
      .belongsTo(ProgramSequence.class)
      .hasMany(ProgramSequenceBindingMeme.class);

    // ProgramSequenceBindingMeme
    payloadFactory.register(ProgramSequenceBindingMeme.class)
      .withAttribute("name")
      .belongsTo(io.xj.service.hub.model.Program.class)
      .belongsTo(ProgramSequenceBinding.class);

    // ProgramSequenceChord
    payloadFactory.register(ProgramSequenceChord.class)
      .withAttribute("name")
      .withAttribute("position")
      .belongsTo(io.xj.service.hub.model.Program.class)
      .belongsTo(io.xj.service.hub.model.ProgramSequence.class);

    // ProgramSequencePattern
    payloadFactory.register(ProgramSequencePattern.class)
      .withAttribute("type")
      .withAttribute("total")
      .withAttribute("name")
      .belongsTo(io.xj.service.hub.model.Program.class)
      .belongsTo(io.xj.service.hub.model.ProgramSequence.class)
      .belongsTo(ProgramVoice.class)
      .hasMany(ProgramSequencePatternEvent.class);

    // ProgramSequencePatternEvent
    payloadFactory.register(ProgramSequencePatternEvent.class)
      .withAttribute("duration")
      .withAttribute("note")
      .withAttribute("position")
      .withAttribute("velocity")
      .belongsTo(io.xj.service.hub.model.Program.class)
      .belongsTo(io.xj.service.hub.model.ProgramSequencePattern.class)
      .belongsTo(ProgramVoiceTrack.class);

    // ProgramVoice
    payloadFactory.register(ProgramVoice.class)
      .withAttribute("type")
      .withAttribute("name")
      .belongsTo(io.xj.service.hub.model.Program.class)
      .hasMany(io.xj.service.hub.model.ProgramSequencePattern.class);

    // ProgramVoiceTrack
    payloadFactory.register(ProgramVoiceTrack.class)
      .withAttribute("name")
      .belongsTo(io.xj.service.hub.model.Program.class)
      .belongsTo(io.xj.service.hub.model.ProgramVoice.class)
      .hasMany(io.xj.service.hub.model.ProgramSequencePatternEvent.class);

    // Segment
    payloadFactory.register(Segment.class)
      .withAttribute("state")
      .withAttribute("beginAt")
      .withAttribute("endAt")
      .withAttribute("key")
      .withAttribute("total")
      .withAttribute("offset")
      .withAttribute("density")
      .withAttribute("tempo")
      .withAttribute("waveformKey")
      .withAttribute("waveformPreroll")
      .withAttribute("type")
      .belongsTo(io.xj.service.hub.model.Chain.class)
      .hasMany(SegmentChoiceArrangement.class)
      .hasMany(SegmentChoice.class)
      .hasMany(SegmentChoiceArrangementPick.class)
      .hasMany(SegmentChord.class)
      .hasMany(SegmentMeme.class)
      .hasMany(SegmentMessage.class);

    // SegmentChoice
    payloadFactory.register(SegmentChoice.class)
      .withAttribute("type")
      .withAttribute("transpose")
      .belongsTo(io.xj.service.hub.model.Segment.class)
      .belongsTo(io.xj.service.hub.model.Program.class)
      .belongsTo(io.xj.service.hub.model.ProgramSequenceBinding.class)
      .hasMany(SegmentChoiceArrangement.class);

    // SegmentChoiceArrangement
    payloadFactory.register(SegmentChoiceArrangement.class)
      .belongsTo(io.xj.service.hub.model.Segment.class)
      .belongsTo(io.xj.service.hub.model.SegmentChoice.class)
      .belongsTo(io.xj.service.hub.model.ProgramVoice.class)
      .belongsTo(io.xj.service.hub.model.Instrument.class);

    // SegmentChoiceArrangementPick
    payloadFactory.register(SegmentChoiceArrangementPick.class)
      .withAttribute("start")
      .withAttribute("length")
      .withAttribute("amplitude")
      .withAttribute("pitch")
      .withAttribute("name")
      .belongsTo(io.xj.service.hub.model.Segment.class)
      .belongsTo(io.xj.service.hub.model.SegmentChoiceArrangement.class)
      .belongsTo(io.xj.service.hub.model.InstrumentAudio.class)
      .belongsTo(io.xj.service.hub.model.ProgramSequencePatternEvent.class);

    // SegmentChord
    payloadFactory.register(SegmentChord.class)
      .withAttribute("name")
      .withAttribute("position")
      .belongsTo(io.xj.service.hub.model.Segment.class);

    // SegmentMeme
    payloadFactory.register(SegmentMeme.class)
      .withAttribute("name")
      .belongsTo(io.xj.service.hub.model.Segment.class);

    // SegmentMessage
    payloadFactory.register(SegmentMessage.class)
      .withAttribute("body")
      .withAttribute("type")
      .belongsTo(io.xj.service.hub.model.Segment.class);

    // User
    payloadFactory.register(User.class)
      .withAttribute("name")
      .withAttribute("roles")
      .withAttribute("email")
      .withAttribute("avatarUrl")
      .hasMany(UserAuth.class)
      .hasMany(UserAuthToken.class);

    // UserAuth
    payloadFactory.register(UserAuth.class)
      .withAttribute("type")
      .withAttribute("externalAccessToken")
      .withAttribute("externalRefreshToken")
      .withAttribute("externalAccount")
      .belongsTo(io.xj.service.hub.model.User.class);

    // UserAuthToken
    payloadFactory.register(UserAuthToken.class)
      .withAttribute("accessToken")
      .belongsTo(io.xj.service.hub.model.User.class)
      .belongsTo(io.xj.service.hub.model.UserAuth.class);

    // UserRole
    payloadFactory.register(UserRole.class)
      .withAttribute("type")
      .belongsTo(io.xj.service.hub.model.User.class);

    // Work
    payloadFactory.register(Work.class);
  }

  /**
   Given an instance of the REST API library's ApiUrlProvider, configure it for this Hub Application@param apiUrlProvider of app to configure
   */
  public static void configureApiUrls(Config config, ApiUrlProvider apiUrlProvider) {
    apiUrlProvider.setApiPath(config.getString("app.apiURL"));
    apiUrlProvider.setAppBaseUrl(config.getString("app.baseURL"));
    apiUrlProvider.setAppHost(config.getString("app.host"));
    apiUrlProvider.setAppHostname(config.getString("app.hostname"));
    apiUrlProvider.setAppName(config.getString("app.name"));
    apiUrlProvider.setAudioBaseUrl(config.getString("audio.baseURL"));
    apiUrlProvider.setSegmentBaseUrl(config.getString("segment.baseURL"));
    apiUrlProvider.setAppPathUnauthorized(config.getString("api.unauthorizedRedirectPath"));
    apiUrlProvider.setAppPathWelcome(config.getString("api.welcomeRedirectPath"));
  }

  /**
   Starts Grizzly HTTP server
   exposing JAX-RS resources defined in this app.
   */
  public void start() throws AppException {
    super.start();

    sendPlatformMessage(String.format(
      "%s (%s) is up at %s",
      getName(), platformRelease, getBaseURI()
    ));
  }

  /**
   stop App Server
   */
  public void stop() {
    super.stop();

    // send messages about successful shutdown
    sendPlatformMessage(String.format(
      "%s (%s) did exit OK at %s",
      getName(), platformRelease, getBaseURI()
    ));

    // shutdown SQL database connection pool
    sqlDatabaseProvider.shutdown();
    log.info("{} SQL database connection pool did shutdown OK", getName());
  }

  /**
   Get base URI

   @return base URI
   */
  public String getBaseURI() {
    return "http://" + getRestHostname() + ":" + getRestPort() + "/";
  }

  /**
   [#153539503] Developer wants any app to send PlatformMessage on startup, including code version, region, ip@param body of message
   */
  private void sendPlatformMessage(String body) {
    try {
      platformMessageDAO.create(Access.internal(), new PlatformMessage().setType(String.valueOf(MessageType.Debug)).setBody(body));
    } catch (Exception e) {
      log.error("failed to send startup platform message", e);
    }
  }

  /**
   Run database migrations
   */
  public void migrate() {
    // Database migrations
    try {
      injector.getInstance(Migration.class).migrate();
    } catch (HubException e) {
      System.out.println(String.format("Migrations failed! HubApp will not start. %s: %s\n%s", e.getClass().getSimpleName(), e.getMessage(), Text.formatStackTrace(e)));
      System.exit(1);
    }
  }

}
