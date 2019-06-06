// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.integration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.DAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.account_user.AccountUser;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio_chord.AudioChord;
import io.xj.core.model.audio_event.AudioEvent;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.chain_config.ChainConfig;
import io.xj.core.model.chain_config.ChainConfigType;
import io.xj.core.model.chain_library.ChainLibrary;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.instrument_meme.InstrumentMeme;
import io.xj.core.model.message.MessageType;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.pattern_chord.PatternChord;
import io.xj.core.model.pattern_event.PatternEvent;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentFactory;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.segment.impl.SegmentContent;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.sequence_meme.SequenceMeme;
import io.xj.core.model.sequence_pattern.SequencePattern;
import io.xj.core.model.sequence_pattern_meme.SequencePatternMeme;
import io.xj.core.model.user_access_token.UserAccessToken;
import io.xj.core.model.user_auth.UserAuth;
import io.xj.core.model.user_auth.UserAuthType;
import io.xj.core.model.user_role.UserRole;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.model.voice.Voice;
import io.xj.core.tables.records.AccountRecord;
import io.xj.core.tables.records.AccountUserRecord;
import io.xj.core.tables.records.AudioChordRecord;
import io.xj.core.tables.records.AudioEventRecord;
import io.xj.core.tables.records.AudioRecord;
import io.xj.core.tables.records.ChainConfigRecord;
import io.xj.core.tables.records.ChainInstrumentRecord;
import io.xj.core.tables.records.ChainLibraryRecord;
import io.xj.core.tables.records.ChainRecord;
import io.xj.core.tables.records.ChainSequenceRecord;
import io.xj.core.tables.records.InstrumentMemeRecord;
import io.xj.core.tables.records.InstrumentRecord;
import io.xj.core.tables.records.LibraryRecord;
import io.xj.core.tables.records.PatternChordRecord;
import io.xj.core.tables.records.PatternEventRecord;
import io.xj.core.tables.records.PatternRecord;
import io.xj.core.tables.records.PlatformMessageRecord;
import io.xj.core.tables.records.SegmentRecord;
import io.xj.core.tables.records.SequenceMemeRecord;
import io.xj.core.tables.records.SequencePatternMemeRecord;
import io.xj.core.tables.records.SequencePatternRecord;
import io.xj.core.tables.records.SequenceRecord;
import io.xj.core.tables.records.UserAccessTokenRecord;
import io.xj.core.tables.records.UserAuthRecord;
import io.xj.core.tables.records.UserRecord;
import io.xj.core.tables.records.UserRoleRecord;
import io.xj.core.tables.records.VoiceRecord;
import io.xj.core.transport.GsonProvider;
import io.xj.core.util.Text;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.ACCOUNT;
import static io.xj.core.Tables.ACCOUNT_USER;
import static io.xj.core.Tables.AUDIO;
import static io.xj.core.Tables.AUDIO_CHORD;
import static io.xj.core.Tables.AUDIO_EVENT;
import static io.xj.core.Tables.CHAIN;
import static io.xj.core.Tables.CHAIN_CONFIG;
import static io.xj.core.Tables.CHAIN_INSTRUMENT;
import static io.xj.core.Tables.CHAIN_LIBRARY;
import static io.xj.core.Tables.CHAIN_SEQUENCE;
import static io.xj.core.Tables.INSTRUMENT;
import static io.xj.core.Tables.INSTRUMENT_MEME;
import static io.xj.core.Tables.LIBRARY;
import static io.xj.core.Tables.PATTERN;
import static io.xj.core.Tables.PATTERN_CHORD;
import static io.xj.core.Tables.PATTERN_EVENT;
import static io.xj.core.Tables.PLATFORM_MESSAGE;
import static io.xj.core.Tables.SEGMENT;
import static io.xj.core.Tables.SEQUENCE;
import static io.xj.core.Tables.SEQUENCE_MEME;
import static io.xj.core.Tables.SEQUENCE_PATTERN;
import static io.xj.core.Tables.SEQUENCE_PATTERN_MEME;
import static io.xj.core.Tables.USER;
import static io.xj.core.Tables.USER_ACCESS_TOKEN;
import static io.xj.core.Tables.USER_AUTH;
import static io.xj.core.Tables.USER_ROLE;
import static io.xj.core.Tables.VOICE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public enum IntegrationTestEntity {
  ;
  private static final Logger log = LoggerFactory.getLogger(IntegrationTestEntity.class);
  private static final Map<Class, Long> parentedUniqueIdMap = Maps.newConcurrentMap();
  private static final Map<Class, Long> genericUniqueIdMap = Maps.newConcurrentMap();
  /**
   List of colors
   */
  private static final String[] COLORS = {
    "Amber",
    "Amethyst",
    "Apricot",
    "Aquamarine",
    "Azure",
    "Baby",
    "Beige",
    "Black",
    "Blue",
    "Blush",
    "Bronze",
    "Brown",
    "Burgundy",
    "Byzantium",
    "Carmine",
    "Cerise",
    "Cerulean",
    "Champagne",
    "Chartreuse",
    "Chocolate",
    "Cobalt",
    "Coffee",
    "Copper",
    "Coral",
    "Crimson",
    "Cyan",
    "Desert",
    "Electric",
    "Emerald",
    "Erin",
    "Gold",
    "Gray",
    "Green",
    "Harlequin",
    "Indigo",
    "Ivory",
    "Jade",
    "Jungle",
    "Lavender",
    "Lemon",
    "Lilac",
    "Lime",
    "Magenta",
    "Maroon",
    "Mauve",
    "Navy",
    "Ochre",
    "Olive",
    "Orange",
    "Orchid",
    "Peach",
    "Pear",
    "Periwinkle",
    "Persian",
    "Pink",
    "Plum",
    "Prussian",
    "Puce",
    "Purple",
    "Raspberry",
    "Red",
    "Rose",
    "Ruby",
    "Salmon",
    "Sangria",
    "Sapphire",
    "Scarlet",
    "Silver",
    "Slate",
    "Spring",
    "Tan",
    "Taupe",
    "Teal",
    "Turquoise",
    "Ultramarine",
    "Violet",
    "Viridian",
    "White",
    "Yellow"
  };
  /**
   List of variants
   */
  private static final String[] VARIANTS = {
    "Light",
    "Dark",
    "Saturated",
    "Dry",
    "Wet",
    "Pastel",
    "Transparent",
    "Glossy",
    "Matte",
    "Opaque",
    "Shiny",
    "Reflective",
    "Grainy",
    "Basic",
    "Simple",
    "Complex",
    "Evocative",
    "Dull",
    "Excellent",
    "Fantastic",
    "Tremendous"
  };
  /**
   List of variants
   */
  private static final String[] PERCUSSIVE_INFLECTIONS = {
    "AGOGOHIGH",
    "AGOGOLOW",
    "BELLRIDE",
    "BLOCKWOODHIGH",
    "BLOCKWOODLOW",
    "BONGOHIGH",
    "BONGOLOW",
    "CABASA",
    "CLAP",
    "CLAVES",
    "CONGAHIGHMUTE",
    "CONGAHIGHOPEN",
    "CONGALOW",
    "COWBELL",
    "CUICAMUTE",
    "CUICAOPEN",
    "CYMBALCRASH",
    "CYMBALRIDE",
    "CYMBALSPLASH",
    "GONG",
    "GUIROLONG",
    "GUIROSHORT",
    "HIHATCLOSED",
    "HIHATOPEN",
    "HIHATPEDAL",
    "KICK",
    "KICKLONG",
    "MARACAS",
    "SHAKER",
    "SLAP",
    "SNARE",
    "SNARERIM",
    "STICKSIDE",
    "TAMBOURINE",
    "TIMBALEHIGH",
    "TIMBALELOW",
    "TOMFLOORHIGH",
    "TOMFLOORLOW",
    "TOMHIGH",
    "TOMHIGHMID",
    "TOMLOW",
    "TOMLOWMID",
    "TRIANGLEMUTE",
    "TRIANGLEOPEN",
    "VIBRASLAP",
    "WHISTLELONG",
    "WHISTLESHORT"
  };
  /**
   List of musical keys
   */
  private static final String[] MUSICAL_CHORDS = {
    "A",
    "Am",
    "A#",
    "Bbm",
    "B",
    "Bm",
    "C",
    "Cm",
    "C#",
    "Dbm",
    "D",
    "Dm",
    "D#",
    "Ebm",
    "E",
    "Em",
    "F",
    "Fm",
    "F#",
    "Gbm",
    "G",
    "Gm",
    "G#",
    "Abm",
    "A7",
    "Am7",
    "A#7",
    "Bbm7",
    "B7",
    "Bm7",
    "C7",
    "Cm7",
    "C#7",
    "Dbm7",
    "D7",
    "Dm7",
    "D#7",
    "Ebm7",
    "E7",
    "Em7",
    "F7",
    "Fm7",
    "F#7",
    "Gbm7",
    "G7",
    "Gm7",
    "G#7",
    "Abm7",
    "A9",
    "Am9",
    "A#9",
    "Bbm9",
    "B9",
    "Bm9",
    "C9",
    "Cm9",
    "C#9",
    "Dbm9",
    "D9",
    "Dm9",
    "D#9",
    "Ebm9",
    "E9",
    "Em9",
    "F9",
    "Fm9",
    "F#9",
    "Gbm9",
    "G9",
    "Gm9",
    "G#9",
    "Abm9",
    "A dim",
    "Am dim",
    "A# dim",
    "Bbm dim",
    "B dim",
    "Bm dim",
    "C dim",
    "Cm dim",
    "C# dim",
    "Dbm dim",
    "D dim",
    "Dm dim",
    "D# dim",
    "Ebm dim",
    "E dim",
    "Em dim",
    "F dim",
    "Fm dim",
    "F# dim",
    "Gbm dim",
    "G dim",
    "Gm dim",
    "G# dim",
    "Abm dim",
    "A sus",
    "Am sus",
    "A# sus",
    "Bbm sus",
    "B sus",
    "Bm sus",
    "C sus",
    "Cm sus",
    "C# sus",
    "Dbm sus",
    "D sus",
    "Dm sus",
    "D# sus",
    "Ebm sus",
    "E sus",
    "Em sus",
    "F sus",
    "Fm sus",
    "F# sus",
    "Gbm sus",
    "G sus",
    "Gm sus",
    "G# sus",
    "Abm sus",
    "A/C",
    "Am/C",
    "A#/C#",
    "Bbm/Db",
    "B/D",
    "Bm/D",
    "C/D#",
    "Cm/Eb",
    "C#/E",
    "Dbm/E",
    "D/F",
    "Dm/F",
    "D#/F#",
    "Ebm/Gb",
    "E/G",
    "Em/G",
    "F/G#",
    "Fm/Ab",
    "F#/A",
    "Gbm/A",
    "G/A#",
    "Gm/Bb",
    "G#/B",
    "Abm/B"
  };
  /**
   List of musical keys
   */
  private static final String[] MUSICAL_KEYS = {
    "A Major",
    "A minor",
    "A# Major",
    "Bb minor",
    "B Major",
    "B minor",
    "C Major",
    "C minor",
    "C# Major",
    "Db minor",
    "D Major",
    "D minor",
    "D# Major",
    "Eb minor",
    "E Major",
    "E minor",
    "F Major",
    "F minor",
    "F# Major",
    "Gb minor",
    "G Major",
    "G minor",
    "G# Major",
    "Ab minor"
  };
  /**
   List of possible Pattern totals
   */
  private static final Integer[] PATTERN_TOTALS = {
    8,
    12,
    16,
    32,
    64
  };
  /**
   List of elements
   */
  private static final String[] ELEMENTS = {
    "Actinium",
    "Aluminum",
    "Americium",
    "Antimony",
    "Argon",
    "Arsenic",
    "Astatine",
    "Barium",
    "Berkelium",
    "Beryllium",
    "Bismuth",
    "Bohrium",
    "Boron",
    "Bromine",
    "Cadmium",
    "Calcium",
    "Californium",
    "Carbon",
    "Cerium",
    "Cesium",
    "Chlorine",
    "Chromium",
    "Cobalt",
    "Copper",
    "Curium",
    "Dubnium",
    "Dysprosium",
    "Einsteinium",
    "Erbium",
    "Europium",
    "Fermium",
    "Fluorine",
    "Francium",
    "Gadolinium",
    "Gallium",
    "Germanium",
    "Gold",
    "Hafnium",
    "Hassium",
    "Helium",
    "Holmium",
    "Hydrogen",
    "Indium",
    "Iodine",
    "Iridium",
    "Iron",
    "Krypton",
    "Lanthanum",
    "Lawrencium",
    "Lead",
    "Lithium",
    "Lutetium",
    "Magnesium",
    "Manganese",
    "Meitnerium",
    "Mendelevium",
    "Mercury",
    "Molybdenum",
    "Neodymium",
    "Neon",
    "Neptunium",
    "Nickel",
    "Niobium",
    "Nitrogen",
    "Nobelium",
    "Osmium",
    "Oxygen",
    "Palladium",
    "Phosphorus",
    "Platinum",
    "Plutonium",
    "Polonium",
    "Potassium",
    "Praseodymium",
    "Promethium",
    "Protactinium",
    "Radium",
    "Radon",
    "Rhenium",
    "Rhodium",
    "Rubidium",
    "Ruthenium",
    "Rutherfordium",
    "Samarium",
    "Scandium",
    "Seaborgium",
    "Selenium",
    "Silicon",
    "Silver",
    "Sodium",
    "Strontium",
    "Sulfur",
    "Tantalum",
    "Technetium",
    "Tellurium",
    "Terbium",
    "Thallium",
    "Thorium",
    "Thulium",
    "Tin",
    "Titanium",
    "Tungsten",
    "Uranium",
    "Vanadium",
    "Xenon",
    "Ytterbium",
    "Yttrium",
    "Zinc",
    "Zirconium"
  };

  /**
   Reset the database before an integration test.
   */
  public static void reset() throws CoreException {
    DSLContext db = IntegrationTestService.getDb();
    try {
      // Audio
      db.deleteFrom(AUDIO_CHORD).execute(); // before Audio
      db.deleteFrom(AUDIO_EVENT).execute(); // before Audio
      db.deleteFrom(AUDIO).execute(); // before Instrument

      // Voice
      db.deleteFrom(PATTERN_EVENT).execute(); // before Voice
      db.deleteFrom(VOICE).execute(); // before Pattern

      // Segment
      db.deleteFrom(SEGMENT).execute(); // before Chain

      // Chain
      db.deleteFrom(CHAIN_SEQUENCE).execute(); // before Chain & Sequence
      db.deleteFrom(CHAIN_INSTRUMENT).execute(); // before Chain & Instrument
      db.deleteFrom(CHAIN_LIBRARY).execute(); // before Chain & Library
      db.deleteFrom(CHAIN_CONFIG).execute(); // before Chain
      db.deleteFrom(CHAIN).execute(); // before Account

      // Instrument
      db.deleteFrom(INSTRUMENT_MEME).execute(); // before Instrument
      db.deleteFrom(INSTRUMENT).execute(); // before Library & Credit

      // Pattern
      db.deleteFrom(SEQUENCE_PATTERN_MEME).execute(); // before Pattern
      db.deleteFrom(PATTERN_CHORD).execute(); // before Pattern
      db.deleteFrom(SEQUENCE_PATTERN).execute(); // before Sequence
      db.deleteFrom(PATTERN).execute(); // before Sequence

      // Sequence
      db.deleteFrom(SEQUENCE_MEME).execute(); // before Sequence
      db.deleteFrom(SEQUENCE).execute(); // before Library & Credit

      // Library
      db.deleteFrom(LIBRARY).execute(); // before Account

      // Account
      db.deleteFrom(ACCOUNT_USER).execute(); // before Account
      db.deleteFrom(ACCOUNT).execute(); //before User

      // Platform Messages
      db.deleteFrom(PLATFORM_MESSAGE).execute(); // before Segment

      // User Access Token
      db.deleteFrom(USER_ACCESS_TOKEN).execute(); // before User & User Auth

      // User
      db.deleteFrom(USER_AUTH).execute(); // before User
      db.deleteFrom(USER_ROLE).execute(); // before User
      db.deleteFrom(USER).execute();

      // Finally, all queues
      IntegrationTestService.flushRedis();

    } catch (Exception e) {
      log.error(e.getClass().getName(), e);
      throw new CoreException(e.getClass().getName(), e);
    }
    parentedUniqueIdMap.clear();
    log.info("Did delete all records from integration database.");
  }

  public static void insertUserAuth(long userId, UserAuthType type, String externalAccessToken, String externalRefreshToken, String externalAccount) {
    UserAuthRecord record = IntegrationTestService.getDb().newRecord(USER_AUTH);
    record.setId(ULong.valueOf(nextUniqueId(UserAuth.class, userId)));
    record.setUserId(ULong.valueOf(userId));
    record.setType(type.toString());
    record.setExternalAccessToken(externalAccessToken);
    record.setExternalRefreshToken(externalRefreshToken);
    record.setExternalAccount(externalAccount);
    record.store();
  }

  public static void insertUser(long id, String name, String email, String avatarUrl) {
    UserRecord record = IntegrationTestService.getDb().newRecord(USER);
    record.setId(ULong.valueOf(id));
    record.setName(name);
    record.setEmail(email);
    record.setAvatarUrl(avatarUrl);
    record.store();
  }

  public static void insertUserRole(long userId, UserRoleType type) {
    UserRoleRecord record = IntegrationTestService.getDb().newRecord(USER_ROLE);
    record.setId(ULong.valueOf(nextUniqueId(UserRole.class, userId)));
    record.setUserId(ULong.valueOf(userId));
    record.setType(type.toString());
    record.store();
  }

  public static void insertUserRole(long userId, String legacyType) {
    UserRoleRecord record = IntegrationTestService.getDb().newRecord(USER_ROLE);
    record.setId(ULong.valueOf(nextUniqueId(UserRole.class, userId)));
    record.setUserId(ULong.valueOf(userId));
    record.setType(legacyType);
    record.store();
  }

  public static void insertAccountUser(long accountId, long userId) {
    AccountUserRecord record = IntegrationTestService.getDb().newRecord(ACCOUNT_USER);
    record.setId(ULong.valueOf(nextUniqueId(AccountUser.class, accountId, userId)));
    record.setAccountId(ULong.valueOf(accountId));
    record.setUserId(ULong.valueOf(userId));
    record.store();
  }

  public static void insertAccount(long id, String name) {
    AccountRecord record = IntegrationTestService.getDb().newRecord(ACCOUNT);
    record.setId(ULong.valueOf(id));
    record.setName(name);
    record.store();
  }

  public static void insertUserAccessToken(long userId, UserAuthType userAuthType, String accessToken) {
    Result<UserAuthRecord> parents = IntegrationTestService.getDb().selectFrom(USER_AUTH)
      .where(USER_AUTH.USER_ID.eq(ULong.valueOf(userId)))
      .and(USER_AUTH.TYPE.eq(userAuthType.toString()))
      .fetch();
    assertFalse(parents.isEmpty());
    UserAuthRecord parent = parents.get(0);
    UserAccessTokenRecord record = IntegrationTestService.getDb().newRecord(USER_ACCESS_TOKEN);
    record.setId(ULong.valueOf(nextUniqueId(UserAccessToken.class, userId)));
    record.setUserId(ULong.valueOf(userId));
    record.setUserAuthId(parent.getId());
    record.setAccessToken(accessToken);
    record.store();
  }

  public static void insertLibrary(long id, long accountId, String name) {
    insertLibrary(id, accountId, name, Instant.now());
  }

  public static void insertLibrary(long id, long accountId, String name, Instant createdUpdatedAt) {
    LibraryRecord record = IntegrationTestService.getDb().newRecord(LIBRARY);
    record.setId(ULong.valueOf(id));
    record.setAccountId(ULong.valueOf(accountId));
    record.setName(name);
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.store();
  }

  public static Sequence insertSequence(long id, long userId, long libraryId, SequenceType type, SequenceState state, String name, double density, String key, double tempo) {
    return insertSequence(id, userId, libraryId, type, state, name, density, key, tempo, Instant.now());
  }

  public static Sequence insertSequence(long id, long userId, long libraryId, SequenceType type, SequenceState state, String name, double density, String key, double tempo, Instant createdUpdatedAt) {
    SequenceRecord record = IntegrationTestService.getDb().newRecord(SEQUENCE);
    record.setId(ULong.valueOf(id));
    record.setUserId(ULong.valueOf(userId));
    record.setLibraryId(ULong.valueOf(libraryId));
    record.setType(type.toString());
    record.setName(name);
    record.setDensity(density);
    record.setKey(key);
    record.setTempo(tempo);
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.setState(state.toString());
    record.store();

    Sequence result = new Sequence();
    result.setId(BigInteger.valueOf(id));
    result.setUserId(BigInteger.valueOf(userId));
    result.setLibraryId(BigInteger.valueOf(libraryId));
    result.setType(type.toString());
    result.setName(name);
    result.setDensity(density);
    result.setKey(key);
    result.setTempo(tempo);
    return result;
  }

  public static void insertSequenceMeme(long sequenceId, String name) {
    insertSequenceMeme(sequenceId, name, Instant.now());
  }

  public static void insertSequenceMeme(long sequenceId, String name, Instant createdUpdatedAt) {
    SequenceMemeRecord record = IntegrationTestService.getDb().newRecord(SEQUENCE_MEME);
    record.setId(ULong.valueOf(nextUniqueId(SequenceMeme.class, sequenceId)));
    record.setSequenceId(ULong.valueOf(sequenceId));
    record.setName(name);
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.store();
  }

  public static void insertPattern(long id, long sequenceId, PatternType type, PatternState state, long total, String name, double density, String key, double tempo) {
    insertPattern(id, sequenceId, type, state, total, name, density, key, tempo, Instant.now(), 4, 4, 0);
  }

  public static void insertPattern(long id, long sequenceId, PatternType type, PatternState state, long total, String name, double density, String key, double tempo, Instant createdUpdatedAt, int meterSuper, int meterSub, int meterSwing) {
    PatternRecord record = IntegrationTestService.getDb().newRecord(PATTERN);
    record.setId(ULong.valueOf(id));
    record.setSequenceId(ULong.valueOf(sequenceId));
    record.setTotal(UInteger.valueOf(total));
    record.setName(name);
    record.setDensity(density);
    record.setKey(key);
    record.setTempo(tempo);
    record.setType(type.toString());
    record.setState(state.toString());
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.setMeterSuper(meterSuper);
    record.setMeterSub(meterSub);
    record.setMeterSwing(meterSwing);
    record.store();
  }

  public static void insertSequencePattern(long id, long sequenceId, long patternId, long offset) {
    insertSequencePattern(id, sequenceId, patternId, offset, Instant.now());
  }

  public static void insertSequencePattern(long id, long sequenceId, long patternId, long offset, Instant createdUpdatedAt) {
    SequencePatternRecord record = IntegrationTestService.getDb().newRecord(SEQUENCE_PATTERN);
    record.setId(ULong.valueOf(id));
    record.setSequenceId(ULong.valueOf(sequenceId));
    record.setPatternId(ULong.valueOf(patternId));
    record.setOffset(ULong.valueOf(offset));
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.store();
  }

  public static void insertSequencePatternMeme(long sequencePatternId, String name) {
    insertSequencePatternMeme(sequencePatternId, name, Instant.now());
  }

  public static void insertSequencePatternAndMeme(long sequenceId, long patternId, long offset, String name) {
    insertSequencePatternAndMeme(sequenceId, patternId, offset, name, Instant.now());
  }

  public static void insertSequencePatternMeme(long sequencePatternId, String name, Instant createdUpdatedAt) {
    SequencePatternMemeRecord record = IntegrationTestService.getDb().newRecord(SEQUENCE_PATTERN_MEME);
    record.setId(ULong.valueOf(nextUniqueId(SequencePatternMeme.class, sequencePatternId)));
    record.setSequencePatternId(ULong.valueOf(sequencePatternId));
    record.setName(name);
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.store();
  }

  public static void insertSequencePatternAndMeme(long sequenceId, long patternId, long offset, String name, Instant createdUpdatedAt) {
    long sequencePatternId = nextUniqueId(SequencePatternMeme.class, sequenceId, patternId);
    insertSequencePattern(sequencePatternId, sequenceId, patternId, offset, createdUpdatedAt);
    insertSequencePatternMeme(sequencePatternId, name, createdUpdatedAt);
  }

  public static void insertPatternChord(long patternId, long position, String name) {
    insertPatternChord(patternId, position, name, Instant.now());
  }

  public static void insertPatternChord(long patternId, double position, String name, Instant createdUpdatedAt) {
    PatternChordRecord record = IntegrationTestService.getDb().newRecord(PATTERN_CHORD);
    record.setId(ULong.valueOf(nextUniqueId(PatternChord.class, patternId)));
    record.setPatternId(ULong.valueOf(patternId));
    record.setPosition(position);
    record.setName(name);
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.store();
  }

  public static void insertVoice(long id, long sequenceId, InstrumentType type, String description) {
    insertVoice(id, sequenceId, type, description, Instant.now());
  }

  public static void insertVoice(long id, long sequenceId, InstrumentType type, String description, Instant createdUpdatedAt) {
    VoiceRecord record = IntegrationTestService.getDb().newRecord(VOICE);
    record.setId(ULong.valueOf(id));
    record.setSequenceId(ULong.valueOf(sequenceId));
    record.setType(type.toString());
    record.setDescription(description);
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.store();
  }

  public static void insertPatternEvent(long patternId, long voiceId, double position, double duration, String inflection, String note, double tonality, double velocity) {
    insertPatternEvent(patternId, voiceId, position, duration, inflection, note, tonality, velocity, Instant.now());
  }

  public static void insertPatternEvent(long patternId, long voiceId, double position, double duration, String inflection, String note, double tonality, double velocity, Instant createdUpdatedAt) {
    PatternEventRecord record = IntegrationTestService.getDb().newRecord(PATTERN_EVENT);
    record.setId(ULong.valueOf(nextUniqueId(PatternEvent.class, patternId, voiceId)));
    record.setPatternId(ULong.valueOf(patternId));
    record.setVoiceId(ULong.valueOf(voiceId));
    record.setPosition(position);
    record.setDuration(duration);
    record.setInflection(inflection);
    record.setNote(note);
    record.setTonality(tonality);
    record.setVelocity(velocity);
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.store();
  }

  public static void insertInstrument(long id, long libraryId, long userId, String description, InstrumentType type, double density) {
    insertInstrument(id, libraryId, userId, description, type, density, Instant.now());
  }

  public static void insertInstrument(long id, long libraryId, long userId, String description, InstrumentType type, double density, Instant createdUpdatedAt) {
    InstrumentRecord record = IntegrationTestService.getDb().newRecord(INSTRUMENT);
    record.setId(ULong.valueOf(id));
    record.setUserId(ULong.valueOf(userId));
    record.setLibraryId(ULong.valueOf(libraryId));
    record.setType(type.toString());
    record.setDescription(description);
    record.setDensity(density);
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.store();
  }

  public static void insertInstrumentMeme(long instrumentId, String name) {
    insertInstrumentMeme(instrumentId, name, Instant.now());
  }

  public static void insertInstrumentMeme(long instrumentId, String name, Instant createdUpdatedAt) {
    InstrumentMemeRecord record = IntegrationTestService.getDb().newRecord(INSTRUMENT_MEME);
    record.setId(ULong.valueOf(nextUniqueId(InstrumentMeme.class, instrumentId)));
    record.setInstrumentId(ULong.valueOf(instrumentId));
    record.setName(name);
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.store();
  }

  public static void insertAudio(long id, long instrumentId, String state, String name, String waveformKey, double start, double length, double tempo, double pitch) {
    insertAudio(id, instrumentId, state, name, waveformKey, start, length, tempo, pitch, Instant.now());
  }

  public static void insertAudio(long id, long instrumentId, String state, String name, String waveformKey, double start, double length, double tempo, double pitch, Instant createdUpdatedAt) {
    AudioRecord record = IntegrationTestService.getDb().newRecord(AUDIO);
    record.setId(ULong.valueOf(id));
    record.setInstrumentId(ULong.valueOf(instrumentId));
    record.setName(name);
    record.setWaveformKey(waveformKey);
    record.setStart(start);
    record.setLength(length);
    record.setTempo(tempo);
    record.setPitch(pitch);
    record.setState(state);
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.store();
  }

  public static void insertAudioEvent(long audioId, double position, double duration, String inflection, String note, double tonality, double velocity) {
    insertAudioEvent(audioId, position, duration, inflection, note, tonality, velocity, Instant.now());
  }

  public static void insertAudioEvent(long audioId, double position, double duration, String inflection, String note, double tonality, double velocity, Instant createdUpdatedAt) {
    AudioEventRecord record = IntegrationTestService.getDb().newRecord(AUDIO_EVENT);
    record.setId(ULong.valueOf(nextUniqueId(AudioEvent.class, audioId)));
    record.setAudioId(ULong.valueOf(audioId));
    record.setPosition(position);
    record.setDuration(duration);
    record.setInflection(inflection);
    record.setNote(note);
    record.setTonality(tonality);
    record.setVelocity(velocity);
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.store();
  }

  public static void insertAudioChord(long audioId, long position, String name) {
    insertAudioChord(audioId, position, name, Instant.now());
  }

  public static void insertAudioChord(long audioId, double position, String name, Instant createdUpdatedAt) {
    AudioChordRecord record = IntegrationTestService.getDb().newRecord(AUDIO_CHORD);
    record.setId(ULong.valueOf(nextUniqueId(AudioChord.class, audioId)));
    record.setAudioId(ULong.valueOf(audioId));
    record.setPosition(position);
    record.setName(name);
    record.setCreatedAt(Timestamp.from(createdUpdatedAt));
    record.setUpdatedAt(Timestamp.from(createdUpdatedAt));
    record.store();
  }

  public static Chain insertChain(long id, long accountId, String name, ChainType type, ChainState state, Instant startAt, @Nullable Instant stopAt, String embedKey) {
    ChainRecord record = IntegrationTestService.getDb().newRecord(CHAIN);
    record.setId(ULong.valueOf(id));
    record.setAccountId(ULong.valueOf(accountId));
    record.setType(type.toString());
    record.setName(name);
    record.setState(state.toString());
    record.setStartAt(Timestamp.from(startAt));
    if (Objects.nonNull(stopAt)) {
      record.setStopAt(Timestamp.from(stopAt));
    }
    if (Objects.nonNull(embedKey)) {
      record.setEmbedKey(embedKey);
    }
    record.store();

    Chain result = new Chain();
    result.setId(BigInteger.valueOf(id));
    result.setAccountId(BigInteger.valueOf(accountId));
    result.setTypeEnum(type);
    result.setName(name);
    result.setStateEnum(state);
    result.setStartAtInstant(startAt);
    if (Objects.nonNull(stopAt)) {
      result.setStopAtInstant(stopAt);
    }
    if (Objects.nonNull(embedKey)) {
      result.setEmbedKey(embedKey);
    }
    return result;
  }

  public static void insertChainConfig(long chainId, ChainConfigType chainConfigType, String value) {
    ChainConfigRecord record = IntegrationTestService.getDb().newRecord(CHAIN_CONFIG);
    record.setId(ULong.valueOf(nextUniqueId(ChainConfig.class, chainId)));
    record.setChainId(ULong.valueOf(chainId));
    record.setType(chainConfigType.toString());
    record.setValue(value);
    record.store();
  }

  public static void insertChainLibrary(long chainId, long libraryId) {
    ChainLibraryRecord record = IntegrationTestService.getDb().newRecord(CHAIN_LIBRARY);
    record.setId(ULong.valueOf(nextUniqueId(ChainLibrary.class, chainId, libraryId)));
    record.setChainId(ULong.valueOf(chainId));
    record.setLibraryId(ULong.valueOf(libraryId));
    record.store();
  }

  public static void insertChainSequence(long chainId, long sequenceId) {
    ChainSequenceRecord record = IntegrationTestService.getDb().newRecord(CHAIN_SEQUENCE);
    record.setId(ULong.valueOf(nextUniqueId(ChainLibrary.class, chainId, sequenceId)));
    record.setChainId(ULong.valueOf(chainId));
    record.setSequenceId(ULong.valueOf(sequenceId));
    record.store();
  }

  public static void insertChainInstrument(long chainId, long instrumentId) {
    ChainInstrumentRecord record = IntegrationTestService.getDb().newRecord(CHAIN_INSTRUMENT);
    record.setId(ULong.valueOf(nextUniqueId(ChainLibrary.class, chainId, instrumentId)));
    record.setChainId(ULong.valueOf(chainId));
    record.setInstrumentId(ULong.valueOf(instrumentId));
    record.store();
  }

  public static Segment insertSegment_NoContent(long id, long chainId, long offset, SegmentState state, Instant beginAt, Instant endAt, String key, int total, double density, double tempo, String waveformKey) throws CoreException {
    SegmentRecord record = IntegrationTestService.getDb().newRecord(SEGMENT);
    record.setId(ULong.valueOf(id));
    record.setChainId(ULong.valueOf(chainId));
    record.setOffset(ULong.valueOf(offset));
    record.setState(state.toString());
    record.setBeginAt(Timestamp.from(beginAt));
    record.setEndAt(Timestamp.from(endAt));
    record.setTotal(UInteger.valueOf(total));
    record.setKey(key);
    record.setDensity(density);
    record.setTempo(tempo);
    record.setWaveformKey(waveformKey);
    record.setContent(gsonProvider().gson().toJson(new SegmentContent()));
    record.store();

    Segment result = segmentFactory().newSegment(BigInteger.valueOf(4));
    result.setId(BigInteger.valueOf(id));
    result.setChainId(BigInteger.valueOf(chainId));
    result.setOffset(BigInteger.valueOf(offset));
    result.setState(state.toString());
    result.setBeginAtInstant(beginAt);
    result.setEndAtInstant(endAt);
    result.setTotal(total);
    result.setKey(key);
    result.setDensity(density);
    result.setTempo(tempo);
    result.setWaveformKey(waveformKey);
    return result;
  }

  private static GsonProvider gsonProvider() {
    Injector injector = Guice.createInjector(new CoreModule());
    return injector.getInstance(GsonProvider.class);
  }

  private static SegmentFactory segmentFactory() {
    Injector injector = Guice.createInjector(new CoreModule());
    return injector.getInstance(SegmentFactory.class);
  }

  public static Segment insertSegment_Planned(long id, long chainId, long offset, Instant beginAt) {
    SegmentRecord record = IntegrationTestService.getDb().newRecord(SEGMENT);
    record.setId(ULong.valueOf(id));
    record.setChainId(ULong.valueOf(chainId));
    record.setOffset(ULong.valueOf(offset));
    record.setState(SegmentState.Planned.toString());
    record.setBeginAt(Timestamp.from(beginAt));
    record.setContent(gsonProvider().gson().toJson(new SegmentContent()));
    record.store();

    Segment result = segmentFactory().newSegment(BigInteger.valueOf(4));
    result.setId(BigInteger.valueOf(id));
    result.setChainId(BigInteger.valueOf(chainId));
    result.setOffset(BigInteger.valueOf(offset));
    result.setStateEnum(SegmentState.Planned);
    result.setBeginAtInstant(beginAt);
    return result;
  }

  /**
   Insert a ready-to-go segment model with child entities

   @param segment to insert
   */
  public static void insert(Segment segment) {
    SegmentRecord record = IntegrationTestService.getDb().newRecord(SEGMENT);
    record.setId(ULong.valueOf(segment.getId()));
    record.setChainId(ULong.valueOf(segment.getChainId()));
    record.setOffset(ULong.valueOf(segment.getOffset()));
    record.setState(segment.getState().toString());
    record.setBeginAt(Timestamp.from(segment.getBeginAt()));
    record.setEndAt(Timestamp.from(segment.getEndAt()));
    record.setTotal(UInteger.valueOf(segment.getTotal()));
    record.setKey(segment.getKey());
    record.setDensity(segment.getDensity());
    record.setTempo(segment.getTempo());
    record.setWaveformKey(segment.getWaveformKey());
    record.setContent(gsonProvider().gson().toJson(segment.getContent()));
    record.store();
  }

  public static void insertPlatformMessage(long id, MessageType type, String body, Instant createdAt) {
    PlatformMessageRecord record = IntegrationTestService.getDb().newRecord(PLATFORM_MESSAGE);
    record.setId(ULong.valueOf(id));
    record.setType(type.toString());
    record.setBody(body);
    record.setCreatedAt(Timestamp.from(createdAt));
    record.store();
  }

  /**
   [#165813103] Integration test entities all have determined known id, for library hash tests
   <p>
   Determine a unique id for an entity, based on its class and parent id
   <p>
   id = ULong.valueOf(string concatenation of parentId followed by uniqueId zero-padded to three digits)
   e.g.
   the 4th instance of any class for parentId 999 would be ULong.valueOf("999004")

   @param forClass  of entity
   @param parentIds of entity
   @return unique id for new entity
   */
  public static long nextUniqueId(Class forClass, long... parentIds) {
    long nextId = parentedUniqueIdMap.containsKey(forClass) ? parentedUniqueIdMap.get(forClass) + 1 : 0;
    parentedUniqueIdMap.put(forClass, nextId);
    String[] concat = new String[parentIds.length + 1];
    int i = 0;
    long iL = parentIds.length;
    while (i < iL) {
      concat[i] = String.format(0 == i ? "%d" : "%03d", parentIds[i]);
      i++;
    }
    concat[i] = String.format("%03d", nextId);
    return Long.valueOf(String.join("", concat));
  }

  /**
   Determine a unique id for an entity, based on its class and parent id

   @param forClass of entity
   @return unique id for new entity
   */
  public static long nextUniqueId(Class forClass) {
    long nextId = genericUniqueIdMap.containsKey(forClass) ? genericUniqueIdMap.get(forClass) + 1 : 1;
    genericUniqueIdMap.put(forClass, nextId);
    return nextId;
  }

  /**
   Create the next N unique ids for an entity, based on its class and parent id

   @param N        number of entities
   @param forClass of entities
   @return unique ids for new entities
   */
  private static long[] nextUniqueIds(int N, Class forClass) {
    long[] result = new long[N];
    for (int i = 0; i < N; i++) {
      result[i] = nextUniqueId(forClass);
    }
    return result;
  }

  /**
   [#165951041] DAO methods throw exception when record is not found (instead of returning null)
   <p>
   Assert an entity does not exist, by making a DAO.readOne() request and asserting the exception

   @param testDAO to use for attempting to retrieve entity
   @param id      of entity
   @param <N>     DAO class
   */
  public static <N extends DAO> void assertNotExist(N testDAO, BigInteger id) {
    try {
      testDAO.readOne(Access.internal(), id);
      fail();
    } catch (CoreException e) {
      assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
    }
  }

  /**
   Insert a generated library

   @param N magnitude of library to generate
   */
  public static void insertLibraryGenerated(int N) {
    insertAccount(1L, "Generated");
    insertUser(1L, "generated", "generated@email.com", "http://pictures.com/generated.gif");
    insertUserRole(1L, UserRoleType.Admin);
    insertLibrary(1, 1, "generated");

    // Create a N-magnitude set of unique major memes
    String[] majorMemeNames = listOfUniqueRandom(N, COLORS);
    String[] minorMemeNames = listOfUniqueRandom((long) StrictMath.ceil(N / 2), VARIANTS);
    String[] percussiveInflections = listOfUniqueRandom(N, PERCUSSIVE_INFLECTIONS);

    // Generate a Percussive Instrument for each meme
    for (int i = 0; i < N; i++) {
      long instrumentId = nextUniqueId(Instrument.class);
      String majorMemeName = majorMemeNames[i];
      String minorMemeName = random(minorMemeNames);
      double density = random(0.3, 0.9);
      //
      insertInstrument(instrumentId, 1, 1, String.format("%s Drums", majorMemeName), InstrumentType.Percussive, density);
      insertInstrumentMeme(instrumentId, majorMemeName);
      insertInstrumentMeme(instrumentId, minorMemeName);
      // audios of instrument
      for (int k = 0; k < N; k++) {
        long audioId = nextUniqueId(Audio.class);
        insertAudio(audioId, instrumentId, "Published", Text.toProper(percussiveInflections[k]), String.format("%s.wav", Text.toLowerSlug(percussiveInflections[k])), random(0, 0.05), random(0.25, 2), random(80, 120), random(100, 4000));
        insertAudioEvent(audioId, 0, 1, percussiveInflections[k], "X", random(0.4, 1), random(0.8, 1));
      }
      //
      log.info("Generated Percussive-type instrumentId={}, minorMeme={}, majorMeme={}", instrumentId, minorMemeName, majorMemeName);
    }

    // Generate N*2 total Macro-type Sequences, each transitioning from one Meme to another
    for (int i = 0; i < N << 1; i++) {
      long sequenceId = nextUniqueId(Sequence.class);
      String[] twoMemeNames = listOfUniqueRandom(2, majorMemeNames);
      String majorMemeFromName = twoMemeNames[0];
      String majorMemeToName = twoMemeNames[1];
      String minorMemeName = random(minorMemeNames);
      String[] twoKeys = listOfUniqueRandom(2, MUSICAL_KEYS);
      String keyFrom = twoKeys[0];
      String keyTo = twoKeys[1];
      double densityFrom = random(0.3, 0.9);
      double tempoFrom = random(80, 120);
      long patternFromId = nextUniqueId(Pattern.class);
      long patternToId = nextUniqueId(Pattern.class);
      long sequencePatternFromId = nextUniqueId(SequencePattern.class);
      long sequencePatternToId = nextUniqueId(SequencePattern.class);
      //
      insertSequence(sequenceId, 1, 1, SequenceType.Macro, SequenceState.Published, String.format("%s, from %s to %s", minorMemeName, majorMemeFromName, majorMemeToName), densityFrom, keyFrom, tempoFrom);
      insertSequenceMeme(sequenceId, minorMemeName);
      // from offset 0
      insertPattern(patternFromId, sequenceId, PatternType.Macro, PatternState.Published, 0, String.format("Start %s", majorMemeFromName), densityFrom, keyFrom, tempoFrom);
      insertSequencePattern(sequencePatternFromId, sequenceId, patternFromId, 0);
      insertSequencePatternMeme(sequencePatternFromId, majorMemeFromName);
      // to offset 1
      double densityTo = random(0.3, 0.9);
      double tempoTo = random(803, 120);
      insertPattern(patternToId, sequenceId, PatternType.Macro, PatternState.Published, 0, String.format("Finish %s", majorMemeToName), densityTo, keyTo, tempoTo);
      insertSequencePattern(sequencePatternToId, sequenceId, sequencePatternToId, 1);
      insertSequencePatternMeme(sequencePatternToId, majorMemeToName);
      //
      log.info("Generated Macro-type sequenceId={}, minorMeme={}, majorMemeFrom={}, majorMemeTo={}", sequenceId, minorMemeName, majorMemeFromName, majorMemeToName);
    }

    // Generate N*4 total Main-type Sequences, each having N patterns comprised of ~N*2 chords, bound to N*4 sequence patterns
    for (int i = 0; i < N << 2; i++) {
      long sequenceId = nextUniqueId(Sequence.class);
      String majorMemeName = random(majorMemeNames);
      String[] patternNames = listOfUniqueRandom(N, ELEMENTS);
      String[] subKeys = listOfUniqueRandom(N, MUSICAL_KEYS);
      Double[] subDensities = listOfRandomValues(N, 0.3, 0.8);
      long[] patternIds = nextUniqueIds(N, Pattern.class);
      double tempo = random(80, 120);
      //
      insertSequence(sequenceId, 1, 1, SequenceType.Main, SequenceState.Published, String.format("%s: %s", majorMemeName, String.join(",", patternNames)), subDensities[0], subKeys[0], tempo);
      insertSequenceMeme(sequenceId, majorMemeName);
      // patterns of sequence
      for (int iP = 0; iP < N; iP++) {
        Integer total = random(PATTERN_TOTALS);
        insertPattern(patternIds[iP], sequenceId, PatternType.Main, PatternState.Published, total, String.format("%s in %s", majorMemeName, patternNames[iP]), subDensities[iP], subKeys[iP], tempo);
        for (int iPC = 0; iPC < N << 2; iPC++) {
          // always use first chord, then use more chords with more density
          if (0 == iPC || StrictMath.random() < subDensities[iP]) {
            insertPatternChord(patternIds[iP], (long) StrictMath.floor(total * iPC / N << 2), random(MUSICAL_CHORDS));
          }
        }
      }
      // sequence pattern binding
      for (int offset = 0; offset < N << 2; offset++) {
        int num = (int) StrictMath.floor(StrictMath.random() * N);
        long sequencePatternId = nextUniqueId(SequencePattern.class);
        insertSequencePattern(sequencePatternId, sequenceId, patternIds[num], offset);
        insertSequencePatternMeme(sequencePatternId, random(minorMemeNames));
      }
      log.info("Generated Main-type sequenceId={}, majorMeme={} with {} patterns bound {} times", sequenceId, majorMemeName, N, N << 2);
    }

    // Generate N total Rhythm-type Sequences, each having N voices, and N*2 patterns comprised of N*8 events
    for (int i = 0; i < N; i++) {
      long sequenceId = nextUniqueId(Sequence.class);
      String majorMemeName = majorMemeNames[i];
      double tempo = random(80, 120);
      String key = random(MUSICAL_KEYS);
      double density = random(0.4, 0.9);
      long[] voiceIds = nextUniqueIds(N, Voice.class);
      //
      insertSequence(sequenceId, 1, 1, SequenceType.Rhythm, SequenceState.Published, String.format("%s Beat", majorMemeName), density, key, tempo);
      insertSequenceMeme(sequenceId, majorMemeName);
      // voices of sequence
      for (int iV = 0; iV < N; iV++) {
        insertVoice(voiceIds[iV], sequenceId, InstrumentType.Percussive, String.format("%s %s", majorMemeName, percussiveInflections[iV]));
      }
      // patterns of sequence
      for (int iP = 0; iP < N << 1; iP++) {
        Integer total = random(PATTERN_TOTALS);
        long patternId = nextUniqueId(Pattern.class);

        // first pattern is always a Loop (because that's required) then the rest at random
        PatternType type = 0 == iP ? PatternType.Loop : randomRhythmPatternType();
        insertPattern(patternId, sequenceId, type, PatternState.Published, total, String.format("%s %s %s", majorMemeName, type.toString(), random(ELEMENTS)), density, key, tempo);
        for (int iPE = 0; iPE < N << 2; iPE++) {
          // always use first chord, then use more chords with more density
          if (0 == iPE || StrictMath.random() < density) {
            int num = (int) StrictMath.floor(StrictMath.random() * N);
            insertPatternEvent(patternId, voiceIds[num], (int) StrictMath.floor(total * iPE / N << 2), random(0.25, 1.0), percussiveInflections[num], "X", random(0.25, 0.5), random(0.4, 0.9));
          }
        }
      }
      log.info("Generated Rhythm-type sequenceId={}, majorMeme={} with {} patterns", sequenceId, majorMemeName, N);
    }
  }

  /**
   Random type of rhythm pattern

   @return randomly selected rhythm pattern type
   */
  private static PatternType randomRhythmPatternType() {
    return new PatternType[]{
      PatternType.Intro,
      PatternType.Loop,
      PatternType.Outro
    }[(int) StrictMath.floor(StrictMath.random() * 3)];
  }

  /**
   List of N random values

   @param N number of values
   @return list of values
   */
  private static Double[] listOfRandomValues(int N, double from, double to) {
    Double[] result = new Double[N];
    for (int i = 0; i < N; i++) {
      result[i] = random(from, to);
    }
    return result;
  }

  /**
   Random value between A and B

   @param A floor
   @param B ceiling
   @return A <= value <= B
   */
  private static Double random(double A, double B) {
    return A + StrictMath.random() * (B - A);
  }

  /**
   Create a N-magnitude list of unique Strings at random from a source list of Strings

   @param N           size of list
   @param sourceItems source Strings
   @return list of unique random Strings
   */
  private static String[] listOfUniqueRandom(long N, String[] sourceItems) {
    long count = 0;
    Collection<String> items = Lists.newArrayList();
    while (count < N) {
      String p = random(sourceItems);
      if (!items.contains(p)) {
        items.add(p);
        count++;
      }
    }
    return items.toArray(new String[0]);
  }

  /**
   Get random String from array

   @param array to get String from
   @return random String
   */
  private static String random(String[] array) {
    return array[(int) StrictMath.floor(StrictMath.random() * array.length)];
  }

  /**
   Get random long from array

   @param array to get long from
   @return random long
   */
  private static Integer random(Integer[] array) {
    return array[(int) StrictMath.floor(StrictMath.random() * array.length)];
  }

}
