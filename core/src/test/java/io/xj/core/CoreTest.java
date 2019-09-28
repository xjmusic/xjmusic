//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.account.Account;
import io.xj.core.model.account.AccountUser;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainConfigType;
import io.xj.core.model.chain.ChainFactory;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.chain.sub.ChainBinding;
import io.xj.core.model.chain.sub.ChainConfig;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentFactory;
import io.xj.core.model.instrument.InstrumentState;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.instrument.sub.Audio;
import io.xj.core.model.instrument.sub.AudioChord;
import io.xj.core.model.instrument.sub.AudioEvent;
import io.xj.core.model.instrument.sub.InstrumentMeme;
import io.xj.core.model.library.Library;
import io.xj.core.model.message.MessageType;
import io.xj.core.model.message.platform.PlatformMessage;
import io.xj.core.model.payload.Payload;
import io.xj.core.model.program.PatternType;
import io.xj.core.model.program.Program;
import io.xj.core.model.program.ProgramFactory;
import io.xj.core.model.program.ProgramState;
import io.xj.core.model.program.ProgramType;
import io.xj.core.model.program.sub.Event;
import io.xj.core.model.program.sub.Pattern;
import io.xj.core.model.program.sub.ProgramMeme;
import io.xj.core.model.program.sub.Sequence;
import io.xj.core.model.program.sub.SequenceBinding;
import io.xj.core.model.program.sub.SequenceBindingMeme;
import io.xj.core.model.program.sub.SequenceChord;
import io.xj.core.model.program.sub.Track;
import io.xj.core.model.program.sub.Voice;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentFactory;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.segment.sub.Arrangement;
import io.xj.core.model.segment.sub.Choice;
import io.xj.core.model.segment.sub.Pick;
import io.xj.core.model.segment.sub.SegmentChord;
import io.xj.core.model.segment.sub.SegmentMeme;
import io.xj.core.model.segment.sub.SegmentMessage;
import io.xj.core.model.user.User;
import io.xj.core.model.user.access_token.UserAccessToken;
import io.xj.core.model.user.auth.UserAuth;
import io.xj.core.model.user.auth.UserAuthType;
import io.xj.core.model.user.role.UserRole;
import io.xj.core.model.user.role.UserRoleType;
import io.xj.core.transport.GsonProvider;
import io.xj.core.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public class CoreTest {
  protected Logger log = LoggerFactory.getLogger(CoreTest.class);
  protected Injector injector = Guice.createInjector(new CoreModule());
  protected GsonProvider gsonProvider = injector.getInstance(GsonProvider.class);
  protected ChainFactory chainFactory = injector.getInstance(ChainFactory.class);
  protected ProgramFactory programFactory = injector.getInstance(ProgramFactory.class);
  protected SegmentFactory segmentFactory = injector.getInstance(SegmentFactory.class);
  protected InstrumentFactory instrumentFactory = injector.getInstance(InstrumentFactory.class);
  protected Access internal = Access.internal();

  /**
   Read a file as a string from java resources

   @param filePath to get and read as string
   @return contents of file
   @throws FileNotFoundException if resource does not exist
   */
  protected static String readResourceFile(String filePath) throws IOException {
    File file = resourceFile(filePath);
    String text;
    try (final FileReader reader = new FileReader(file)) {
      text = CharStreams.toString(reader);
    }
    return text;
  }

  /**
   get a file from java resources

   @param filePath to get
   @return File
   @throws FileNotFoundException if resource does not exist
   */
  protected static File resourceFile(String filePath) throws FileNotFoundException {
    ClassLoader classLoader = CoreTest.class.getClassLoader();
    URL resource = classLoader.getResource(filePath);
    if (Objects.isNull(resource))
      throw new FileNotFoundException(String.format("Failed to load resource: %s", filePath));
    return new File(resource.getFile());
  }

  /**
   Parse some test JSON, deserializing it into a Payload

   @param json to deserialize
   @return Payload
   @throws IOException on failure to parse
   */
  protected static Payload deserializePayload(Object json) throws IOException {
    return new ObjectMapper().readValue(String.valueOf(json), Payload.class);
  }

  /**
   List of N random values

   @param N number of values
   @return list of values
   */
  protected static Double[] listOfRandomValues(int N, double from, double to) {
    Double[] result = new Double[N];
    for (int i = 0; i < N; i++) {
      result[i] = random(from, to);
    }
    return result;
  }

  /**
   Create a N-magnitude list of unique Strings at random from a source list of Strings

   @param N           size of list
   @param sourceItems source Strings
   @return list of unique random Strings
   */
  protected static String[] listOfUniqueRandom(long N, String[] sourceItems) {
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
   Insert an AccountUser to the database

   @param accountId of AccountUser
   @param userId    of AccountUser
   @return new AccountUser
   */
  protected static AccountUser newAccountUser(long accountId, long userId) {
    AccountUser accountUser = new AccountUser();
    accountUser.setAccountId(BigInteger.valueOf(accountId));
    accountUser.setUserId(BigInteger.valueOf(userId));
    return accountUser;
  }

  /**
   Insert an Account to the database

   @param id   of Account
   @param name of Account
   @return new Account
   */
  protected static Account newAccount(long id, String name) {
    Account account = new Account();
    account.setId(BigInteger.valueOf(id));
    account.setName(name);
    return account;
  }

  /**
   Insert an Account to the database with a specified created/updated at

   @param id               of Account
   @param name             of Account
   @param createdUpdatedAt of account
   @return new Account
   */
  protected static Account newAccount(long id, String name, Instant createdUpdatedAt) {
    Account account = new Account();
    account.setId(BigInteger.valueOf(id));
    account.setName(name);
    account.setCreatedAtInstant(createdUpdatedAt);
    account.setUpdatedAtInstant(createdUpdatedAt);
    return account;
  }

  /**
   New Arrangement

   @return new Arrangement
   */
  protected static Arrangement newArrangement(Choice choice) {
    return new Arrangement()
      .setChoiceId(choice.getId())
      .setVoiceId(UUID.randomUUID())
      .setInstrumentId(BigInteger.valueOf(432L));
  }

  /**
   New Arrangement

   @return new Arrangement
   */
  protected static Arrangement newArrangement(Choice choice, Voice voice, Instrument instrument) {
    return new Arrangement()
      .setChoiceId(choice.getId())
      .setVoiceId(voice.getId())
      .setInstrumentId(instrument.getId());
  }

  /**
   Create a new Audio

   @param name        of audio
   @param waveformKey of audio
   @param start       of audio
   @param length      of audio
   @param tempo       of audio
   @param pitch       of audio
   @param density     of audio
   @return new Audio
   */
  protected static Audio newAudio(String name, String waveformKey, double start, double length, double tempo, double pitch, double density) {
    return new Audio().setName(name).setWaveformKey(waveformKey)
      .setStart(start).setLength(length).setTempo(tempo).setPitch(pitch).setDensity(density);
  }

  /**
   Create a new AudioChord

   @param audio    to create chord in
   @param position of chord
   @param name     of chord
   @return new audio chord
   */
  protected static AudioChord newAudioChord(Audio audio, double position, String name) {
    return new AudioChord()
      .setAudioId(audio.getId())
      .setPosition(position)
      .setName(name);
  }

  /**
   Create a new AudioEvent

   @param audio    to create event in
   @param position of AudioEvent
   @param duration of AudioEvent
   @param name     of AudioEvent
   @param note     of AudioEvent
   @param velocity of AudioEvent
   @return new AudioEvent
   */
  protected static AudioEvent newAudioEvent(Audio audio, double position, double duration, String name, String note, double velocity) {
    return new AudioEvent().setAudioId(audio.getId())
      .setPosition(position).setDuration(duration).setName(name).setNote(note).setVelocity(velocity);
  }

  /**
   New Choice

   @param id                of Choice
   @param type              of Choice
   @param programId         of Choice
   @param sequenceBindingId of Choice, or null if none should be specified
   @param transpose         of Choice
   @return new Choice
   */
  protected static Choice newChoice(UUID id, ProgramType type, long programId, @Nullable UUID sequenceBindingId, Integer transpose) {
    Choice choice = new Choice()
      .setId(id)
      .setTypeEnum(type)
      .setTranspose(transpose);
    choice.setProgramId(BigInteger.valueOf(programId));
    if (Objects.nonNull(sequenceBindingId))
      choice.setSequenceBindingId(sequenceBindingId);
    return choice;
  }

  /**
   New Choice

   @param type              of Choice
   @param programId         of Choice
   @param sequenceBindingId of Choice, or null if none should be specified
   @param transpose         of Choice
   @return new Choice
   */
  protected static Choice newChoice(ProgramType type, long programId, @Nullable UUID sequenceBindingId, Integer transpose) {
    Choice choice = new Choice()
      .setTypeEnum(type)
      .setTranspose(transpose);
    choice.setProgramId(BigInteger.valueOf(programId));
    if (Objects.nonNull(sequenceBindingId))
      choice.setSequenceBindingId(sequenceBindingId);
    return choice;
  }

  /**
   New Choice

   @param id of Choice
   @return new Choice
   */
  protected static Choice newChoice(UUID id) {
    return new Choice()
      .setId(id)
      .setTypeEnum(ProgramType.Main)
      .setProgramId(BigInteger.valueOf(5))
      .setSequenceBindingId(UUID.randomUUID())
      .setTranspose(-2);
  }

  /**
   Get a ChainBinding, to feed to an ingest

   @param targetClass to bind
   @param targetId    to bind
   @return new chain binding
   @throws CoreException on failure to create binding
   */
  protected static ChainBinding newChainBinding(String targetClass, int targetId) throws CoreException {
    return new ChainBinding()
      .setTargetClass(targetClass)
      .setTargetId(BigInteger.valueOf(targetId));
  }

  /**
   Create a new ChainConfig

   @param type  of ChainConfig
   @param value of ChainConfig
   @return new ChainConfig
   */
  protected static ChainConfig newChainConfig(ChainConfigType type, String value) {
    ChainConfig chainConfig = new ChainConfig();
    chainConfig.setTypeEnum(type);
    chainConfig.setValue(value);
    return chainConfig;
  }

  /**
   Get a ChainBinding, to feed to an ingest

   @param target to bind
   @return new chain binding
   @throws CoreException on failure to create binding
   */
  protected static <N extends Entity> ChainBinding newChainBinding(N target) throws CoreException {
    return new ChainBinding()
      .setTargetClass(Text.getSimpleName(target))
      .setTargetId(target.getId());
  }

  /**
   Create a new instrument meme

   @param name of meme
   @return meme
   */
  protected static InstrumentMeme newInstrumentMeme(String name) {
    return new InstrumentMeme().setName(name);
  }

  /**
   Create a new Library

   @param id        of Library
   @param accountId of Library
   @param name      of Library
   @param at        created/updated of Library
   @return new Library
   */
  protected static Library newLibrary(long id, long accountId, String name, Instant at) {
    Library library = new Library();
    library.setId(BigInteger.valueOf(id));
    library.setAccountId(BigInteger.valueOf(accountId));
    library.setName(name);
    library.setCreatedAtInstant(at);
    library.setUpdatedAtInstant(at);
    return library;
  }

  /**
   Create a new Pattern

   @param sequence of pattern
   @param voice    of pattern
   @param type     of pattern
   @param total    beats of pattern
   @param name     of pattern
   @return new pattern
   */
  protected static Pattern newPattern(Sequence sequence, Voice voice, PatternType type, int total, String name) {
    return new Pattern().setSequence(sequence).setVoice(voice).setTypeEnum(type).setTotal(total).setName(name);
  }

  /**
   Create a new Track

   @param voice of track
   @param name  of track
   @return new track
   */
  protected static Track newTrack(Voice voice, String name) {
    return new Track().setVoice(voice).setName(name);
  }

  /**
   Create a new Event

   @param pattern  to create event in
   @param track    of Event
   @param position of Event
   @param duration of Event
   @param note     of Event
   @param velocity of Event
   @return new Event
   */
  protected static Event newEvent(Pattern pattern, Track track, double position, double duration, String note, double velocity) {
    return new Event()
      .setPattern(pattern)
      .setTrack(track)
      .setPosition(position)
      .setDuration(duration)
      .setNote(note)
      .setVelocity(velocity);
  }

  /**
   New Pick

   @param arrangement of Pick
   @return new Pick
   */
  protected static Pick newPick(Arrangement arrangement) {
    return new Pick()
      .setArrangementId(arrangement.getId())
      .setEventId(UUID.randomUUID())
      .setAudioId(UUID.randomUUID())
      .setVoiceId(UUID.randomUUID())
      .setName("CLANG")
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(42.9);
  }

  /**
   Create a new Platform Message

   @param id   of PlatformMessage
   @param type of PlatformMessage
   @param body of PlatformMessage
   @param at   created/updated  of PlatformMessage
   @return PlatformMessage
   */
  protected static PlatformMessage newPlatformMessage(int id, MessageType type, String body, Instant at) {
    PlatformMessage platformMessage = new PlatformMessage();
    platformMessage.setId(BigInteger.valueOf(id));
    platformMessage.setTypeEnum(type);
    platformMessage.setBody(body);
    platformMessage.setCreatedAtInstant(at);
    platformMessage.setUpdatedAtInstant(at);
    return platformMessage;
  }

  /**
   Create a new program meme

   @param name of meme
   @return meme
   */
  protected static ProgramMeme newProgramMeme(String name) {
    return new ProgramMeme().setName(name);
  }

  /**
   New Segment ChordEntity

   @param position of ChordEntity
   @param name     of ChordEntity
   @return new Segment ChordEntity
   */
  protected static SegmentChord newSegmentChord(double position, String name) {
    return new SegmentChord()
      .setPosition(position)
      .setName(name);
  }

  /**
   New Segment MemeEntity

   @param name of MemeEntity
   @return new Segment MemeEntity
   */
  protected static SegmentMeme newSegmentMeme(String name) {
    return new SegmentMeme()
      .setName(name);
  }

  /**
   New Segment Message

   @param type of Message
   @param body of Message
   @return new Segment Message
   */
  protected static SegmentMessage newSegmentMessage(MessageType type, String body) {
    return new SegmentMessage()
      .setTypeEnum(type)
      .setBody(body);
  }

  /**
   Create a new sequence

   @param total   beats in sequence
   @param name    of sequence
   @param density of sequence
   @param key     of sequence
   @param tempo   of sequence
   @return new sequence
   */
  protected static Sequence newSequence(int total, String name, double density, String key, double tempo) {
    return new Sequence().setTotal(total).setName(name).setDensity(density).setKey(key).setTempo(tempo);
  }

  /**
   Create a new SequenceBinding

   @param sequence to create binding in
   @param offset   of binding
   @return new sequence binding
   */
  protected static SequenceBinding newSequenceBinding(Sequence sequence, long offset) {
    return new SequenceBinding()
      .setSequence(sequence)
      .setOffset(offset);
  }

  /**
   Create a new SequenceBindingMeme

   @param sequenceBinding to create meme in
   @param name            of meme
   @return new sequence binding meme
   */
  protected static SequenceBindingMeme newSequenceBindingMeme(SequenceBinding sequenceBinding, String name) {
    return new SequenceBindingMeme()
      .setSequenceBinding(sequenceBinding)
      .setName(name);
  }

  /**
   Create a new SequenceChord, with a random sequence id

   @param position of chord
   @param name     of chord
   @return new sequence chord
   */
  protected static SequenceChord newSequenceChord(double position, String name) {
    return new SequenceChord()
      .setSequenceId(UUID.randomUUID())
      .setPosition(position)
      .setName(name);
  }

  /**
   Create a new SequenceChord

   @param sequence to create chord in
   @param position of chord
   @param name     of chord
   @return new sequence chord
   */
  protected static SequenceChord newSequenceChord(Sequence sequence, double position, String name) {
    return new SequenceChord()
      .setSequenceId(sequence.getId())
      .setPosition(position)
      .setName(name);
  }

  /**
   Create a new UserAuth@param id

   @param userId               of UserAuth
   @param type                 of UserAuth
   @param externalAccessToken  of UserAuth
   @param externalRefreshToken of UserAuth
   @param externalAccount      of UserAuth
   */
  protected static UserAuth newUserAuth(long id, long userId, UserAuthType type, String externalAccessToken, String externalRefreshToken, String externalAccount) {
    UserAuth userAuth = new UserAuth();
    userAuth.setUserId(BigInteger.valueOf(userId));
    userAuth.setType(type.toString());
    userAuth.setExternalAccessToken(externalAccessToken);
    userAuth.setExternalRefreshToken(externalRefreshToken);
    userAuth.setExternalAccount(externalAccount);
    return userAuth;
  }

  /**
   Create a new User

   @param id        of User
   @param name      of User
   @param email     of User
   @param avatarUrl of User
   @return new User
   */
  protected static User newUser(long id, String name, String email, String avatarUrl) {
    User user = new User();
    user.setId(BigInteger.valueOf(id));
    user.setName(name);
    user.setEmail(email);
    user.setAvatarUrl(avatarUrl);
    return user;
  }

  /**
   Create a new UserRole

   @param userId of UserRole
   @param type   of UserRole
   @return UserRole
   */
  protected static UserRole newUserRole(long userId, UserRoleType type) {
    UserRole userRole = new UserRole();
    userRole.setUserId(BigInteger.valueOf(userId));
    userRole.setType(type.toString());
    return userRole;
  }

  /**
   Create a new UserRole

   @param userId     of UserRole
   @param legacyType of UserRole
   @return UserRole
   */
  protected static UserRole newUserRole(long userId, String legacyType) {
    UserRole userRole = new UserRole();
    userRole.setUserId(BigInteger.valueOf(userId));
    userRole.setType(legacyType);
    return userRole;
  }

  /**
   Create a new voice

   @param type of voice
   @param name of voice
   @return new Voice
   */
  protected static Voice newVoice(InstrumentType type, String name) {
    return new Voice().setTypeEnum(type).setName(name);
  }

  /**
   Create a new voice

   @param id   of voice
   @param type of voice
   @param name of voice
   @return new Voice
   */
  protected static Voice newVoice(UUID id, InstrumentType type, String name) {
    Voice voice = new Voice().setTypeEnum(type).setName(name);
    voice.setId(id);
    return voice;
  }

  /**
   Now

   @return now
   */
  protected static Instant now() {
    return Instant.now();
  }

  /**
   Random value between A and B

   @param A floor
   @param B ceiling
   @return A <= value <= B
   */
  protected static Double random(double A, double B) {
    return A + StrictMath.random() * (B - A);
  }

  /**
   Get random String from array

   @param array to get String from
   @return random String
   */
  protected static String random(String[] array) {
    return array[(int) StrictMath.floor(StrictMath.random() * array.length)];
  }

  /**
   Get random long from array

   @param array to get long from
   @return random long
   */
  protected static Integer random(Integer[] array) {
    return array[(int) StrictMath.floor(StrictMath.random() * array.length)];
  }

  /**
   Random type of rhythm pattern

   @return randomly selected rhythm pattern type
   */
  protected static PatternType randomRhythmPatternType() {
    return new PatternType[]{
      PatternType.Intro,
      PatternType.Loop,
      PatternType.Outro
    }[(int) StrictMath.floor(StrictMath.random() * 3)];
  }

  /**
   Insert UserAccessToken to database

   @param userAuth to get token of
   @return userAccessToken
   */
  protected static UserAccessToken newUserAccessToken(UserAuth userAuth, String accessToken) {
    UserAccessToken token = new UserAccessToken();
    token.setUserId(userAuth.getUserId());
    token.setUserAuthId(userAuth.getId());
    token.setAccessToken(accessToken);
    return token;
  }

  /**
   Get a new Chain

   @param id    of Chain
   @param state of Chain
   @return new Chain
   */
  protected Chain newChain(long id, ChainState state) {
    Chain chain = chainFactory.newChain(BigInteger.valueOf(id));
    chain.setAccountId(BigInteger.valueOf(1));
    chain.setTypeEnum(ChainType.Production);
    chain.setName("Test");
    chain.setStateEnum(state);
    chain.setStartAtInstant(now());
    return chain;
  }

  /**
   Get a new Chain

   @param id        of Chain
   @param accountId of Chain
   @param name      of Chain
   @param type      of Chain
   @param state     of Chain
   @param startAt   of Chain
   @param stopAt    of Chain
   @param embedKey  of Chain
   @param at        created/updated of Chain
   @return new Chain
   */
  protected Chain newChain(long id, long accountId, String name, ChainType type, ChainState state, Instant startAt, @Nullable Instant stopAt, String embedKey, Instant at) {
    Chain chain = chainFactory.newChain(BigInteger.valueOf(id));
    chain.setAccountId(BigInteger.valueOf(accountId));
    chain.setTypeEnum(type);
    chain.setName(name);
    chain.setStateEnum(state);
    chain.setStartAtInstant(startAt);
    chain.setCreatedAtInstant(at);
    chain.setUpdatedAtInstant(at);
    if (Objects.nonNull(stopAt)) {
      chain.setStopAtInstant(stopAt);
    }
    if (Objects.nonNull(embedKey)) {
      chain.setEmbedKey(embedKey);
    }
    return chain;
  }

  /**
   Get a new Chain

   @param id        of Chain
   @param accountId of Chain
   @param name      of Chain
   @param type      of Chain
   @param state     of Chain
   @param startAt   of Chain
   @param stopAt    of Chain
   @param embedKey  of Chain
   @param at        created/updated of Chain
   @return new Chain
   */
  protected Chain newChain(long id, long accountId, String name, ChainType type, ChainState state, Instant startAt, @Nullable Instant stopAt, String embedKey, Instant at, ChainBinding... bindings) throws CoreException {
    Chain chain = newChain(id, accountId, name, type, state, startAt, stopAt, embedKey, at);
    for (ChainBinding chainBinding : bindings) chain.add(chainBinding);
    return chain;
  }

  /**
   Create a new instrument

   @param id        of instrument
   @param userId    of instrument
   @param libraryId of instrument
   @param type      of instrument
   @param state     of instrument
   @param name      of instrument
   @param at        created/updated of instrument
   @return new instrument
   */
  protected Instrument newInstrument(long id, long userId, long libraryId, InstrumentType type, InstrumentState state, String name, Instant at) {
    Instrument instrument = instrumentFactory.newInstrument(BigInteger.valueOf(id))
      .setUserId(BigInteger.valueOf(userId))
      .setLibraryId(BigInteger.valueOf(libraryId))
      .setTypeEnum(type)
      .setStateEnum(state)
      .setName(name);
    instrument.setCreatedAtInstant(at).setUpdatedAtInstant(at);
    return instrument;
  }

  /**
   Create a new Program

   @param id        of program
   @param userId    of program
   @param libraryId of program
   @param type      of program
   @param state     of program
   @param name      of program
   @param key       of program
   @param tempo     of program
   @param at        created/updated of program
   @return new program
   */
  protected Program newProgram(long id, long userId, long libraryId, ProgramType type, ProgramState state, String name, String key, double tempo, Instant at) {
    Program program = programFactory.newProgram(BigInteger.valueOf(id))
      .setUserId(BigInteger.valueOf(userId))
      .setLibraryId(BigInteger.valueOf(libraryId))
      .setTypeEnum(type)
      .setStateEnum(state)
      .setName(name)
      .setKey(key)
      .setTempo(tempo);
    program.setCreatedAtInstant(at).setUpdatedAtInstant(at);
    return program;
  }

  /**
   Create a new Segment

   @param id          of Segment
   @param chainId     of Segment
   @param offset      of Segment
   @param state       of Segment
   @param beginAt     of Segment
   @param endAt       of Segment
   @param key         of Segment
   @param total       of Segment
   @param density     of Segment
   @param tempo       of Segment
   @param waveformKey of Segment
   @return new Segment
   */
  protected Segment newSegment(long id, long chainId, long offset, SegmentState state, Instant beginAt, Instant endAt, String key, int total, double density, double tempo, String waveformKey) {
    Segment segment = segmentFactory.newSegment(BigInteger.valueOf(4));
    segment.setId(BigInteger.valueOf(id));
    segment.setChainId(BigInteger.valueOf(chainId));
    segment.setOffset(offset);
    segment.setState(state.toString());
    segment.setBeginAtInstant(beginAt);
    segment.setEndAtInstant(endAt);
    segment.setTotal(total);
    segment.setKey(key);
    segment.setDensity(density);
    segment.setTempo(tempo);
    segment.setWaveformKey(waveformKey);
    return segment;
  }

  /**
   Create a new Segment in Planned state, with no end-at time

   @param id      of Segment
   @param chainId of Segment
   @param offset  of Segment
   @param beginAt of Segment
   @return new Segment
   */
  protected Segment newSegment(long id, long chainId, long offset, Instant beginAt) {
    Segment segment = segmentFactory.newSegment(BigInteger.valueOf(4));
    segment.setId(BigInteger.valueOf(id));
    segment.setChainId(BigInteger.valueOf(chainId));
    segment.setOffset(offset);
    segment.setStateEnum(SegmentState.Planned);
    segment.setBeginAtInstant(beginAt);
    segment.setWaveformKey(String.format("%s-%s-%s.ogg", chainId, offset, id));
    return segment;
  }

}
