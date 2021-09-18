//  Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.xj.api.Chain;
import io.xj.api.ChainState;
import io.xj.api.ChainType;
import io.xj.api.Segment;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentChoiceArrangement;
import io.xj.api.SegmentChoiceArrangementPick;
import io.xj.api.SegmentChord;
import io.xj.api.SegmentChordVoicing;
import io.xj.api.SegmentMeme;
import io.xj.api.SegmentState;
import io.xj.api.SegmentType;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.LoremIpsum;
import io.xj.hub.Users;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramSequencePatternType;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.AccountUser;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.InstrumentMeme;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramMeme;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.tables.pojos.ProgramSequenceBindingMeme;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.hub.tables.pojos.ProgramSequenceChordVoicing;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.hub.tables.pojos.User;
import io.xj.hub.tables.pojos.UserAuth;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.util.Text;
import io.xj.lib.util.Value;
import io.xj.nexus.hub_client.client.HubClientAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildInstrumentMeme;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramMeme;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequenceBinding;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequenceBindingMeme;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplateBinding;

/**
 [#165954673] Integration tests use shared scenario fixtures as much as possible
 <p>
 Testing the hypothesis that, while unit tests are all independent,
 integration tests ought be as much about testing all features around a consensus model of the platform
 as they are about testing all resources.
 */
public class NexusIntegrationTestingFixtures {
  private static final Logger log = LoggerFactory.getLogger(NexusIntegrationTestingFixtures.class);
  private static final double RANDOM_VALUE_FROM = 0.3;
  private static final double RANDOM_VALUE_TO = 0.8;
  private static final float NANOS_PER_SECOND = 1000000000;

  // These are fully exposed (no getters/setters) for ease of use in testing
  public Account account1;
  public Account account2;
  public AccountUser accountUser1a;
  public Instrument instrument8;
  public Instrument instrument9;
  public InstrumentAudio instrument8_audio8bleep;
  public InstrumentAudio instrument8_audio8kick;
  public InstrumentAudio instrument8_audio8snare;
  public InstrumentAudio instrument8_audio8toot;
  public InstrumentAudio instrument9_audio8;
  public InstrumentMeme instrument8_meme0;
  public InstrumentMeme instrument9_meme0;
  public Library library1;
  public Library library2;
  public Program program10;
  public Program program15;
  public Program program1;
  public Program program2;
  public Program program35;
  public Program program3;
  public Program program4;
  public Program program5;
  public Program program6;
  public Program program7;
  public Program program9;
  public ProgramMeme program10_meme0;
  public ProgramMeme program15_meme0;
  public ProgramMeme program35_meme0;
  public ProgramMeme program3_meme0;
  public ProgramMeme program4_meme0;
  public ProgramMeme program5_meme0;
  public ProgramMeme program9_meme0;
  public ProgramSequence program10_sequence0;
  public ProgramSequence program15_sequence0;
  public ProgramSequence program15_sequence1;
  public ProgramSequence program35_sequence0;
  public ProgramSequence program3_sequence0;
  public ProgramSequence program3_sequence1;
  public ProgramSequence program4_sequence0;
  public ProgramSequence program4_sequence1;
  public ProgramSequence program4_sequence2;
  public ProgramSequence program5_sequence0;
  public ProgramSequence program5_sequence1;
  public ProgramSequence program9_sequence0;
  public ProgramSequenceBinding program15_sequence0_binding0;
  public ProgramSequenceBinding program15_sequence1_binding0;
  public ProgramSequenceBinding program3_sequence0_binding0;
  public ProgramSequenceBinding program3_sequence1_binding0;
  public ProgramSequenceBinding program4_sequence0_binding0;
  public ProgramSequenceBinding program4_sequence1_binding0;
  public ProgramSequenceBinding program4_sequence2_binding0;
  public ProgramSequenceBinding program5_sequence0_binding0;
  public ProgramSequenceBinding program5_sequence1_binding0;
  public ProgramSequenceBindingMeme program15_sequence0_binding0_meme0;
  public ProgramSequenceBindingMeme program15_sequence1_binding0_meme0;
  public ProgramSequenceBindingMeme program15_sequence1_binding0_meme1;
  public ProgramSequenceBindingMeme program3_sequence0_binding0_meme0;
  public ProgramSequenceBindingMeme program3_sequence1_binding0_meme0;
  public ProgramSequenceBindingMeme program4_sequence0_binding0_meme0;
  public ProgramSequenceBindingMeme program4_sequence1_binding0_meme0;
  public ProgramSequenceBindingMeme program4_sequence1_binding0_meme1;
  public ProgramSequenceBindingMeme program4_sequence2_binding0_meme0;
  public ProgramSequenceBindingMeme program5_sequence0_binding0_meme0;
  public ProgramSequenceBindingMeme program5_sequence1_binding0_meme0;
  public ProgramSequenceChord program15_sequence0_chord0;
  public ProgramSequenceChord program15_sequence0_chord1;
  public ProgramSequenceChord program15_sequence1_chord0;
  public ProgramSequenceChord program15_sequence1_chord1;
  public ProgramSequenceChord program5_sequence0_chord0;
  public ProgramSequenceChord program5_sequence0_chord1;
  public ProgramSequenceChord program5_sequence0_chord2;
  public ProgramSequenceChord program5_sequence1_chord0;
  public ProgramSequenceChord program5_sequence1_chord1;
  public ProgramSequenceChordVoicing program15_sequence0_chord0_voicing;
  public ProgramSequenceChordVoicing program15_sequence0_chord1_voicing;
  public ProgramSequenceChordVoicing program15_sequence1_chord0_voicing;
  public ProgramSequenceChordVoicing program15_sequence1_chord1_voicing;
  public ProgramSequenceChordVoicing program5_sequence0_chord0_voicing;
  public ProgramSequenceChordVoicing program5_sequence0_chord1_voicing;
  public ProgramSequenceChordVoicing program5_sequence0_chord2_voicing;
  public ProgramSequenceChordVoicing program5_sequence1_chord0_voicing;
  public ProgramSequenceChordVoicing program5_sequence1_chord1_voicing;
  public ProgramSequencePattern program10_sequence0_pattern0;
  public ProgramSequencePattern program10_sequence0_pattern1;
  public ProgramSequencePattern program10_sequence0_pattern2;
  public ProgramSequencePattern program10_sequence0_pattern3;
  public ProgramSequencePattern program35_sequence0_pattern0;
  public ProgramSequencePattern program35_sequence0_pattern1;
  public ProgramSequencePattern program9_sequence0_pattern0;
  public ProgramSequencePattern program9_sequence0_pattern1;
  public ProgramSequencePattern program9_sequence0_pattern2;
  public ProgramSequencePattern program9_sequence0_pattern3;
  public ProgramSequencePatternEvent program10_sequence0_pattern0_event0;
  public ProgramSequencePatternEvent program10_sequence0_pattern0_event1;
  public ProgramSequencePatternEvent program10_sequence0_pattern0_event2;
  public ProgramSequencePatternEvent program10_sequence0_pattern0_event3;
  public ProgramSequencePatternEvent program10_sequence0_pattern1_event0;
  public ProgramSequencePatternEvent program10_sequence0_pattern1_event1;
  public ProgramSequencePatternEvent program10_sequence0_pattern1_event2;
  public ProgramSequencePatternEvent program10_sequence0_pattern1_event3;
  public ProgramSequencePatternEvent program10_sequence0_pattern2_event0;
  public ProgramSequencePatternEvent program10_sequence0_pattern2_event1;
  public ProgramSequencePatternEvent program10_sequence0_pattern2_event2;
  public ProgramSequencePatternEvent program10_sequence0_pattern2_event3;
  public ProgramSequencePatternEvent program10_sequence0_pattern3_event0;
  public ProgramSequencePatternEvent program10_sequence0_pattern3_event1;
  public ProgramSequencePatternEvent program10_sequence0_pattern3_event2;
  public ProgramSequencePatternEvent program10_sequence0_pattern3_event3;
  public ProgramSequencePatternEvent program35_sequence0_pattern0_event0;
  public ProgramSequencePatternEvent program35_sequence0_pattern0_event1;
  public ProgramSequencePatternEvent program35_sequence0_pattern0_event2;
  public ProgramSequencePatternEvent program35_sequence0_pattern0_event3;
  public ProgramSequencePatternEvent program35_sequence0_pattern1_event0;
  public ProgramSequencePatternEvent program35_sequence0_pattern1_event1;
  public ProgramSequencePatternEvent program35_sequence0_pattern1_event2;
  public ProgramSequencePatternEvent program35_sequence0_pattern1_event3;
  public ProgramSequencePatternEvent program9_sequence0_pattern0_event0;
  public ProgramSequencePatternEvent program9_sequence0_pattern0_event1;
  public ProgramSequencePatternEvent program9_sequence0_pattern0_event2;
  public ProgramSequencePatternEvent program9_sequence0_pattern0_event3;
  public ProgramSequencePatternEvent program9_sequence0_pattern1_event0;
  public ProgramSequencePatternEvent program9_sequence0_pattern1_event1;
  public ProgramSequencePatternEvent program9_sequence0_pattern1_event2;
  public ProgramSequencePatternEvent program9_sequence0_pattern1_event3;
  public ProgramSequencePatternEvent program9_sequence0_pattern2_event0;
  public ProgramSequencePatternEvent program9_sequence0_pattern2_event1;
  public ProgramSequencePatternEvent program9_sequence0_pattern2_event2;
  public ProgramSequencePatternEvent program9_sequence0_pattern2_event3;
  public ProgramSequencePatternEvent program9_sequence0_pattern3_event0;
  public ProgramSequencePatternEvent program9_sequence0_pattern3_event1;
  public ProgramSequencePatternEvent program9_sequence0_pattern3_event2;
  public ProgramSequencePatternEvent program9_sequence0_pattern3_event3;
  public ProgramVoice program10_voice0;
  public ProgramVoice program35_voice0;
  public ProgramVoice program9_voice0;
  public ProgramVoiceTrack program10_voice0_track0;
  public ProgramVoiceTrack program35_voice0_track0;
  public ProgramVoiceTrack program35_voice0_track1;
  public ProgramVoiceTrack program35_voice0_track2;
  public ProgramVoiceTrack program35_voice0_track3;
  public ProgramVoiceTrack program9_voice0_track0;
  public ProgramVoiceTrack program9_voice0_track10;
  public ProgramVoiceTrack program9_voice0_track11;
  public ProgramVoiceTrack program9_voice0_track12;
  public ProgramVoiceTrack program9_voice0_track13;
  public ProgramVoiceTrack program9_voice0_track14;
  public ProgramVoiceTrack program9_voice0_track15;
  public ProgramVoiceTrack program9_voice0_track1;
  public ProgramVoiceTrack program9_voice0_track2;
  public ProgramVoiceTrack program9_voice0_track3;
  public ProgramVoiceTrack program9_voice0_track4;
  public ProgramVoiceTrack program9_voice0_track5;
  public ProgramVoiceTrack program9_voice0_track6;
  public ProgramVoiceTrack program9_voice0_track7;
  public ProgramVoiceTrack program9_voice0_track8;
  public ProgramVoiceTrack program9_voice0_track9;
  public Template template1;
  public TemplateBinding templateBinding1;
  public User user1;
  public User user2;
  public User user3;

  /**
   Random type of rhythm pattern

   @return randomly selected rhythm pattern type
   */
  protected static ProgramSequencePatternType randomRhythmPatternType() {
    return new ProgramSequencePatternType[]{
      ProgramSequencePatternType.Intro,
      ProgramSequencePatternType.Loop,
      ProgramSequencePatternType.Outro
    }[(int) StrictMath.floor(StrictMath.random() * 3)];
  }

  /**
   List of N random values

   @param N number of values
   @return list of values
   */
  protected static Float[] listOfRandomValues(int N) {
    Float[] result = new Float[N];
    for (int i = 0; i < N; i++) {
      result[i] = random(RANDOM_VALUE_FROM, RANDOM_VALUE_TO);
    }
    return result;
  }

  /**
   Create a N-magnitude list of unique Strings at random of a source list of Strings

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
   Random value between A and B

   @param A floor
   @param B ceiling
   @return A <= value <= B
   */
  protected static Float random(double A, double B) {
    return (float) (A + StrictMath.random() * (B - A));
  }

  /**
   Get random String of array

   @param array to get String of
   @return random String
   */
  protected static String random(String[] array) {
    return array[(int) StrictMath.floor(StrictMath.random() * array.length)];
  }

  /**
   Get random long of array

   @param array to get long of
   @return random long
   */
  protected static Integer random(Integer[] array) {
    return array[(int) StrictMath.floor(StrictMath.random() * array.length)];
  }

  public static Chain buildChain(Template template) {
    return buildChain(template, ChainState.FABRICATE);
  }

  public static Chain buildChain(Template template, ChainState state) {
    var chain = new Chain();
    chain.setId(UUID.randomUUID());
    chain.setAccountId(UUID.randomUUID());
    chain.setTemplateId(template.getId());
    chain.setName("Test Chain");
    chain.setType(ChainType.PRODUCTION);
    chain.setState(state);
    chain.startAt(Value.formatIso8601UTC(Instant.now()));
    return chain;
  }

  public static Chain buildChain(Account account, String name, ChainType type, ChainState state, Template template, Instant startAt) {
    return buildChain(account, name, type, state, template, startAt, null, Text.toShipKey(name));
  }

  public static Chain buildChain(Account account, Template template, String name, ChainType type, ChainState state, Instant startAt) {
    return buildChain(account, name, type, state, template, startAt, null, Text.toShipKey(name));
  }

  public static Chain buildChain(Account account, String name, ChainType type, ChainState state, Template template, Instant startAt, @Nullable Instant stopAt, @Nullable String shipKey) {
    var chain = new Chain();
    chain.setId(UUID.randomUUID());
    chain.setTemplateId(template.getId());
    chain.setAccountId(account.getId());
    chain.setName(name);
    chain.setType(type);
    chain.setState(state);
    chain.startAt(Value.formatIso8601UTC(startAt));
    if (Objects.nonNull(stopAt))
      chain.stopAt(Value.formatIso8601UTC(stopAt));
    if (Objects.nonNull(shipKey))
      chain.shipKey(shipKey);
    return chain;
  }

  public static Segment buildSegment() {
    var seg = new Segment();
    seg.setId(UUID.randomUUID());
    return seg;
  }

  public static Segment buildSegment(Chain chain, int offset, SegmentState state, Instant beginAt, @Nullable Instant endAt, String key, int total, double density, double tempo, String storageKey, String outputEncoder) {
    return buildSegment(chain,
      0 < offset ? SegmentType.CONTINUE : SegmentType.INITIAL,
      offset, 0, state, beginAt, endAt, key, total, density, tempo, storageKey, outputEncoder);
  }

  public static Segment buildSegment(Chain chain, SegmentType type, int offset, int delta, SegmentState state, Instant beginAt, @Nullable Instant endAt, String key, int total, double density, double tempo, String storageKey) {
    return buildSegment(chain, type, offset, delta, state, beginAt, endAt, key, total, density, tempo, storageKey, "OGG");

  }

  public static Segment buildSegment(Chain chain, SegmentType type, int offset, int delta, SegmentState state, Instant beginAt, @Nullable Instant endAt, String key, int total, double density, double tempo, String storageKey, String outputEncoder) {
    var segment = new Segment();
    segment.setId(UUID.randomUUID());
    segment.setChainId(chain.getId());
    segment.setType(type);
    segment.setOutputEncoder(outputEncoder);
    segment.setOffset((long) offset);
    segment.setDelta(delta);
    segment.setState(state);
    segment.setBeginAt(Value.formatIso8601UTC(beginAt));
    segment.setKey(key);
    segment.setTotal(total);
    segment.setDensity(density);
    segment.setTempo(tempo);
    segment.setStorageKey(storageKey);

    if (Objects.nonNull(endAt))
      segment.endAt(Value.formatIso8601UTC(endAt));

    return segment;
  }

  public static Segment buildSegment(Chain chain, String key, int total, float density, float tempo) {
    return buildSegment(
      chain,
      0,
      SegmentState.CRAFTING,
      Instant.parse(chain.getStartAt()),
      Instant.parse(chain.getStartAt()).plusNanos((long) (NANOS_PER_SECOND * total * (60 / tempo))), key, total, density, tempo, "segment123", "wav");
  }

  public static Segment buildSegment(Chain chain, int offset, Instant beginAt, String key, int total, float density, float tempo) {
    return buildSegment(
      chain,
      offset,
      SegmentState.CRAFTING,
      beginAt,
      beginAt.plusNanos((long) (NANOS_PER_SECOND * total * (60 / tempo))), key, total, density, tempo, "segment123", "wav");
  }

  public static SegmentChoice buildSegmentChoice(Segment segment, ProgramType programType, ProgramSequenceBinding programSequenceBinding) {
    var segmentChoice = new SegmentChoice();
    segmentChoice.setId(UUID.randomUUID());
    segmentChoice.setSegmentId(segment.getId());
    segmentChoice.setDeltaIn(Segments.DELTA_UNLIMITED);
    segmentChoice.setDeltaOut(Segments.DELTA_UNLIMITED);
    segmentChoice.setProgramId(programSequenceBinding.getProgramId());
    segmentChoice.setProgramSequenceBindingId(programSequenceBinding.getId());
    segmentChoice.setProgramType(programType.toString());
    return segmentChoice;
  }

  public static SegmentChoice buildSegmentChoice(Segment segment, int deltaIn, int deltaOut, Program program, InstrumentType instrumentType) {
    var segmentChoice = new SegmentChoice();
    segmentChoice.setId(UUID.randomUUID());
    segmentChoice.setSegmentId(segment.getId());
    segmentChoice.setDeltaIn(deltaIn);
    segmentChoice.setDeltaOut(deltaOut);
    segmentChoice.setProgramId(program.getId());
    segmentChoice.setProgramType(program.getType().toString());
    segmentChoice.setInstrumentType(instrumentType.toString());
    return segmentChoice;
  }

  public static SegmentChoice buildSegmentChoice(Segment segment, Program program) {
    var segmentChoice = new SegmentChoice();
    segmentChoice.setId(UUID.randomUUID());
    segmentChoice.setSegmentId(segment.getId());
    segmentChoice.setDeltaIn(Segments.DELTA_UNLIMITED);
    segmentChoice.setDeltaOut(Segments.DELTA_UNLIMITED);
    segmentChoice.setProgramId(program.getId());
    segmentChoice.setProgramType(program.getType().toString());
    return segmentChoice;
  }

  public static SegmentChoice buildSegmentChoice(Segment segment, Program program, ProgramSequence programSequence, ProgramVoice voice, Instrument instrument) {
    var segmentChoice = new SegmentChoice();
    segmentChoice.setId(UUID.randomUUID());
    segmentChoice.setProgramVoiceId(voice.getId());
    segmentChoice.setInstrumentId(instrument.getId());
    segmentChoice.setInstrumentType(instrument.getType().toString());
    segmentChoice.setDeltaIn(Segments.DELTA_UNLIMITED);
    segmentChoice.setDeltaOut(Segments.DELTA_UNLIMITED);
    segmentChoice.setSegmentId(segment.getId());
    segmentChoice.setProgramId(program.getId());
    segmentChoice.setProgramSequenceId(programSequence.getId());
    segmentChoice.setProgramType(program.getType().toString());
    return segmentChoice;
  }

  public static SegmentChoice buildSegmentChoice(Segment segment, int deltaIn, int deltaOut, Program program, ProgramSequenceBinding programSequenceBinding) {
    var choice = buildSegmentChoice(segment, deltaIn, deltaOut, program);
    choice.setProgramSequenceBindingId(programSequenceBinding.getId());
    return choice;
  }

  public static SegmentChoice buildSegmentChoice(Segment segment, int deltaIn, int deltaOut, Program program, ProgramVoice voice, Instrument instrument) {
    var choice = buildSegmentChoice(segment, deltaIn, deltaOut, program);
    choice.setProgramVoiceId(voice.getId());
    choice.setInstrumentId(instrument.getId());
    choice.setInstrumentType(instrument.getType().toString());
    return choice;
  }

  public static SegmentChoice buildSegmentChoice(Segment segment, int deltaIn, int deltaOut, Program program) {
    var choice = new SegmentChoice();
    choice.setId(UUID.randomUUID());
    choice.setSegmentId(segment.getId());
    choice.setDeltaIn(deltaIn);
    choice.setDeltaOut(deltaOut);
    choice.setProgramId(program.getId());
    choice.setProgramType(program.getType().toString());
    return choice;
  }

  public static SegmentMeme buildSegmentMeme(Segment segment, String name) {
    var segmentMeme = new SegmentMeme();
    segmentMeme.setId(UUID.randomUUID());
    segmentMeme.setSegmentId(segment.getId());
    segmentMeme.setName(name);
    return segmentMeme;
  }

  public static SegmentChord buildSegmentChord(Segment segment, Double position, String name) {
    var segmentChord = new SegmentChord();
    segmentChord.setId(UUID.randomUUID());
    segmentChord.setSegmentId(segment.getId());
    segmentChord.setPosition(position);
    segmentChord.setName(name);
    return segmentChord;
  }

  public static SegmentChordVoicing buildSegmentChordVoicing(SegmentChord chord, InstrumentType type, String notes) {
    var segmentChordVoicing = new SegmentChordVoicing();
    segmentChordVoicing.setId(UUID.randomUUID());
    segmentChordVoicing.setSegmentId(chord.getSegmentId());
    segmentChordVoicing.segmentChordId(chord.getId());
    segmentChordVoicing.setType(type.toString());
    segmentChordVoicing.setNotes(notes);
    return segmentChordVoicing;
  }

  public static SegmentChoiceArrangement buildSegmentChoiceArrangement(SegmentChoice segmentChoice) {
    var segmentChoiceArrangement = new SegmentChoiceArrangement();
    segmentChoiceArrangement.setId(UUID.randomUUID());
    segmentChoiceArrangement.setSegmentId(segmentChoice.getSegmentId());
    segmentChoiceArrangement.segmentChoiceId(segmentChoice.getId());
    return segmentChoiceArrangement;
  }

  public static SegmentChoiceArrangementPick buildSegmentChoiceArrangementPick(SegmentChoiceArrangement segmentChoiceArrangement, ProgramSequencePatternEvent event, InstrumentAudio instrumentAudio, String name) {
    var pick = new SegmentChoiceArrangementPick();
    pick.setId(UUID.randomUUID());
    pick.setSegmentId(segmentChoiceArrangement.getSegmentId());
    pick.setSegmentChoiceArrangementId(segmentChoiceArrangement.getId());
    pick.setProgramSequencePatternEventId(event.getId());
    pick.setInstrumentAudioId(instrumentAudio.getId());
    pick.setStart(Double.valueOf(event.getPosition()));
    pick.setLength(Double.valueOf(event.getDuration()));
    pick.setAmplitude(Double.valueOf(event.getVelocity()));
    pick.setNote(event.getNote());
    pick.setName(name);
    return pick;
  }

  /**
   Create a new HubAccess control object

   @param user     for access
   @param userAuth for access
   @param accounts for access
   @param rolesCSV for access
   @return access control object
   */
  public static HubClientAccess buildHubClientAccess(User user, UserAuth userAuth, ImmutableList<Account> accounts, String rolesCSV) {
    return new HubClientAccess()
      .setUserId(user.getId())
      .setUserAuthId(userAuth.getId())
      .setAccountIds(Entities.idsOf(accounts))
      .setRoleTypes(Users.userRoleTypesFromCsv(rolesCSV));
  }

  /**
   Create a new HubAccess control object

   @param user     for access
   @param rolesCSV for access
   @return access control object
   */
  public static HubClientAccess buildHubClientAccess(User user, String rolesCSV) {
    return new HubClientAccess()
      .setUserId(user.getId())
      .setRoleTypes(Users.userRoleTypesFromCsv(rolesCSV));
  }

  /**
   Create a new HubAccess control object

   @param user     for access
   @param accounts for access
   @param rolesCSV for access
   @return access control object
   */
  public static HubClientAccess buildHubClientAccess(User user, ImmutableList<Account> accounts, String rolesCSV) {
    return new HubClientAccess()
      .setUserId(user.getId())
      .setAccountIds(Entities.idsOf(accounts))
      .setRoleTypes(Users.userRoleTypesFromCsv(rolesCSV));
  }

  /**
   Create a new HubAccess control object

   @param user     for access
   @param userAuth for access
   @param accounts for access
   @return access control object
   */
  public static HubClientAccess buildHubClientAccess(User user, UserAuth userAuth, ImmutableList<Account> accounts) {
    return new HubClientAccess()
      .setUserId(user.getId())
      .setUserAuthId(userAuth.getId())
      .setAccountIds(Entities.idsOf(accounts));
  }

  /**
   Create a new HubAccess control object

   @param user     for access
   @param accounts for access
   @return access control object
   */
  public static HubClientAccess buildHubClientAccess(User user, ImmutableList<Account> accounts) {
    return new HubClientAccess()
      .setUserId(user.getId())
      .setAccountIds(Entities.idsOf(accounts));
  }

  /**
   Create a new HubAccess control object

   @param accounts for access
   @param rolesCSV for access
   @return access control object
   */
  public static HubClientAccess buildHubClientAccess(ImmutableList<Account> accounts, String rolesCSV) {
    return new HubClientAccess()
      .setAccountIds(Entities.idsOf(accounts))
      .setRoleTypes(Users.userRoleTypesFromCsv(rolesCSV));
  }

  /**
   Create a new HubAccess control object

   @param rolesCSV for access
   @return access control object
   */
  public static HubClientAccess buildHubClientAccess(String rolesCSV) {
    return new HubClientAccess().setRoleTypes(Users.userRoleTypesFromCsv(rolesCSV));
  }

  /**
   A whole library of mock content

   @return collection of entities
   */
  public Collection<Object> setupFixtureB1() throws EntityException {

    // Account "bananas"
    account1 = IntegrationTestingFixtures.buildAccount("bananas");

    // Library "house"
    library2 = IntegrationTestingFixtures.buildLibrary(account1, "house");

    // Template Binding to library 2
    template1 = buildTemplate(account1, "Test Template 1", "test1");
    templateBinding1 = buildTemplateBinding(template1, library2);

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    user2 = IntegrationTestingFixtures.buildUser("john", "john@email.com", "http://pictures.com/john.gif", "Admin");

    // Jenny has a "user" role and belongs to account "bananas"
    user3 = IntegrationTestingFixtures.buildUser("jenny", "jenny@email.com", "http://pictures.com/jenny.gif", "User");
    accountUser1a = IntegrationTestingFixtures.buildAccountUser(account1, user3);

    // "Tropical, Wild to Cozy" macro-program in house library
    program4 = IntegrationTestingFixtures.buildProgram(library2, ProgramType.Macro, ProgramState.Published, "Tropical, Wild to Cozy", "C", 120.0f, 0.6f);
    program4_meme0 = IntegrationTestingFixtures.buildMeme(program4, "Tropical");
    //
    program4_sequence0 = IntegrationTestingFixtures.buildSequence(program4, 0, "Start Wild", 0.6f, "C", 125.0f);
    program4_sequence0_binding0 = IntegrationTestingFixtures.buildBinding(program4_sequence0, 0);
    program4_sequence0_binding0_meme0 = IntegrationTestingFixtures.buildMeme(program4_sequence0_binding0, "Wild");
    //
    program4_sequence1 = IntegrationTestingFixtures.buildSequence(program4, 0, "Intermediate", 0.4f, "Bb minor", 115.0f);
    program4_sequence1_binding0 = IntegrationTestingFixtures.buildBinding(program4_sequence1, 1);
    program4_sequence1_binding0_meme0 = IntegrationTestingFixtures.buildMeme(program4_sequence1_binding0, "Cozy");
    program4_sequence1_binding0_meme1 = IntegrationTestingFixtures.buildMeme(program4_sequence1_binding0, "Wild");
    //
    program4_sequence2 = IntegrationTestingFixtures.buildSequence(program4, 0, "Finish Cozy", 0.4f, "Ab minor", 125.0f);
    program4_sequence2_binding0 = IntegrationTestingFixtures.buildBinding(program4_sequence2, 2);
    program4_sequence2_binding0_meme0 = IntegrationTestingFixtures.buildMeme(program4_sequence2_binding0, "Cozy");

    // Main program
    program5 = IntegrationTestingFixtures.buildProgram(library2, ProgramType.Main, ProgramState.Published, "Main Jam", "C minor", 140, 0.6f);
    program5_meme0 = IntegrationTestingFixtures.buildMeme(program5, "Outlook");
    //
    program5_sequence0 = IntegrationTestingFixtures.buildSequence(program5, 16, "Intro", 0.5f, "G major", 135.0f);
    program5_sequence0_chord0 = IntegrationTestingFixtures.buildChord(program5_sequence0, 0.0, "G major");

    program5_sequence0_chord0_voicing = IntegrationTestingFixtures.buildVoicing(InstrumentType.Bass, program5_sequence0_chord0, "G3, B3, D4");
    program5_sequence0_chord1 = IntegrationTestingFixtures.buildChord(program5_sequence0, 8.0, "Ab minor");

    program5_sequence0_chord1_voicing = IntegrationTestingFixtures.buildVoicing(InstrumentType.Bass, program5_sequence0_chord1, "Ab3, Db3, F4");
    program5_sequence0_chord2 = IntegrationTestingFixtures.buildChord(program5_sequence0, 75.0, "G-9"); // [#154090557] this ChordEntity should be ignored, because it's past the end of the main-pattern total

    program5_sequence0_chord2_voicing = IntegrationTestingFixtures.buildVoicing(InstrumentType.Bass, program5_sequence0_chord2, "G3, Bb3, D4, A4");
    program5_sequence0_binding0 = IntegrationTestingFixtures.buildBinding(program5_sequence0, 0);
    program5_sequence0_binding0_meme0 = IntegrationTestingFixtures.buildMeme(program5_sequence0_binding0, "Optimism");
    //
    program5_sequence1 = IntegrationTestingFixtures.buildSequence(program5, 32, "Drop", 0.5f, "G minor", 135.0f);
    program5_sequence1_chord0 = IntegrationTestingFixtures.buildChord(program5_sequence1, 0.0, "C major");

    program5_sequence1_chord0_voicing = IntegrationTestingFixtures.buildVoicing(InstrumentType.Bass, program5_sequence1_chord0, "Ab3, Db3, F4");
    program5_sequence1_chord1 = IntegrationTestingFixtures.buildChord(program5_sequence1, 8.0, "Bb minor");

    program5_sequence1_chord1_voicing = IntegrationTestingFixtures.buildVoicing(InstrumentType.Bass, program5_sequence1_chord1, "Ab3, Db3, F4");
    program5_sequence1_binding0 = IntegrationTestingFixtures.buildBinding(program5_sequence1, 1);
    program5_sequence1_binding0_meme0 = IntegrationTestingFixtures.buildMeme(program5_sequence1_binding0, "Pessimism");

    // A basic beat
    program35 = IntegrationTestingFixtures.buildProgram(library2, ProgramType.Rhythm, ProgramState.Published, "Basic Beat", "C", 121, 0.6f);
    program35_meme0 = IntegrationTestingFixtures.buildMeme(program35, "Basic");
    program35_voice0 = IntegrationTestingFixtures.buildVoice(program35, InstrumentType.Drum, "Drums");
    program35_voice0_track0 = IntegrationTestingFixtures.buildTrack(program35_voice0, "CLOCK");
    program35_voice0_track1 = IntegrationTestingFixtures.buildTrack(program35_voice0, "SNORT");
    program35_voice0_track2 = IntegrationTestingFixtures.buildTrack(program35_voice0, "KICK");
    program35_voice0_track3 = IntegrationTestingFixtures.buildTrack(program35_voice0, "SNARL");
    //
    program35_sequence0 = IntegrationTestingFixtures.buildSequence(program35, 16, "Base", 0.5f, "C", 110.3f);
    program35_sequence0_pattern0 = IntegrationTestingFixtures.buildPattern(program35_sequence0, program35_voice0, ProgramSequencePatternType.Loop, 4, "Drop");
    program35_sequence0_pattern0_event0 = IntegrationTestingFixtures.buildEvent(program35_sequence0_pattern0, program35_voice0_track0, 0.0f, 1.0f, "C2", 1.0f);
    program35_sequence0_pattern0_event1 = IntegrationTestingFixtures.buildEvent(program35_sequence0_pattern0, program35_voice0_track1, 1.0f, 1.0f, "G5", 0.8f);
    program35_sequence0_pattern0_event2 = IntegrationTestingFixtures.buildEvent(program35_sequence0_pattern0, program35_voice0_track2, 2.5f, 1.0f, "C2", 0.6f);
    program35_sequence0_pattern0_event3 = IntegrationTestingFixtures.buildEvent(program35_sequence0_pattern0, program35_voice0_track3, 3.0f, 1.0f, "G5", 0.9f);
    //
    program35_sequence0_pattern1 = IntegrationTestingFixtures.buildPattern(program35_sequence0, program35_voice0, ProgramSequencePatternType.Loop, 4, "Drop Alt");
    program35_sequence0_pattern1_event0 = IntegrationTestingFixtures.buildEvent(program35_sequence0_pattern1, program35_voice0_track0, 0.0f, 1.0f, "B5", 0.9f);
    program35_sequence0_pattern1_event1 = IntegrationTestingFixtures.buildEvent(program35_sequence0_pattern1, program35_voice0_track1, 1.0f, 1.0f, "D2", 1.0f);
    program35_sequence0_pattern1_event2 = IntegrationTestingFixtures.buildEvent(program35_sequence0_pattern1, program35_voice0_track2, 2.5f, 1.0f, "E4", 0.7f);
    program35_sequence0_pattern1_event3 = IntegrationTestingFixtures.buildEvent(program35_sequence0_pattern1, program35_voice0_track3, 3.0f, 1.0f, "c3", 0.5f);

    // List of all parent entities including the library
    // ORDER IS IMPORTANT because this list will be used for real database entities, so ordered from parent -> child
    return ImmutableList.of(
      account1,
      library2,
      user2,
      user3,
      accountUser1a,
      program35,
      program35_voice0,
      program35_voice0_track0,
      program35_voice0_track1,
      program35_voice0_track2,
      program35_voice0_track3,
      program35_meme0,
      program35_sequence0,
      program35_sequence0_pattern0,
      program35_sequence0_pattern0_event0,
      program35_sequence0_pattern0_event1,
      program35_sequence0_pattern0_event2,
      program35_sequence0_pattern0_event3,
      program35_sequence0_pattern1,
      program35_sequence0_pattern1_event0,
      program35_sequence0_pattern1_event1,
      program35_sequence0_pattern1_event2,
      program35_sequence0_pattern1_event3,
      program4,
      program4_meme0,
      program4_sequence0,
      program4_sequence0_binding0,
      program4_sequence0_binding0_meme0,
      program4_sequence1,
      program4_sequence1_binding0,
      program4_sequence1_binding0_meme0,
      program4_sequence1_binding0_meme1,
      program4_sequence2,
      program4_sequence2_binding0,
      program4_sequence2_binding0_meme0,
      program5,
      program5_meme0,
      program5_sequence0,
      program5_sequence0_binding0,
      program5_sequence0_binding0_meme0,
      program5_sequence0_chord0,
      program5_sequence0_chord0_voicing,
      program5_sequence0_chord1,
      program5_sequence0_chord1_voicing,
      program5_sequence0_chord2,
      program5_sequence0_chord2_voicing,
      program5_sequence1,
      program5_sequence1_binding0,
      program5_sequence1_binding0_meme0,
      program5_sequence1_chord0,
      program5_sequence1_chord0_voicing,
      program5_sequence1_chord1,
      program5_sequence1_chord1_voicing,
      template1,
      templateBinding1
    );
  }

  /**
   Library of Content B-2 (shared test fixture)
   <p>
   [#165954673] Integration tests use shared scenario fixtures as much as possible
   */
  public Collection<Object> setupFixtureB2() {
    // "Tangy, Chunky to Smooth" macro-program in house library
    program3 = IntegrationTestingFixtures.buildProgram(library2, ProgramType.Macro, ProgramState.Published, "Tangy, Chunky to Smooth", "G minor", 120.0f, 0.6f);
    program3_meme0 = IntegrationTestingFixtures.buildMeme(program3, "Tangy");
    //
    program3_sequence0 = IntegrationTestingFixtures.buildSequence(program3, 0, "Start Chunky", 0.4f, "G minor", 115.0f);
    program3_sequence0_binding0 = IntegrationTestingFixtures.buildBinding(program3_sequence0, 0);
    program3_sequence0_binding0_meme0 = IntegrationTestingFixtures.buildMeme(program3_sequence0_binding0, "Chunky");
    //
    program3_sequence1 = IntegrationTestingFixtures.buildSequence(program3, 0, "Finish Smooth", 0.6f, "C", 125.0f);
    program3_sequence1_binding0 = IntegrationTestingFixtures.buildBinding(program3_sequence1, 1);
    program3_sequence1_binding0_meme0 = IntegrationTestingFixtures.buildMeme(program3_sequence1_binding0, "Smooth");

    // Main program
    program15 = IntegrationTestingFixtures.buildProgram(library2, ProgramType.Main, ProgramState.Published, "Next Jam", "Db minor", 140, 0.6f);
    program15_meme0 = IntegrationTestingFixtures.buildMeme(program15, "Hindsight");
    //
    program15_sequence0 = IntegrationTestingFixtures.buildSequence(program15, 16, "Intro", 0.5f, "G minor", 135.0f);
    program15_sequence0_chord0 = IntegrationTestingFixtures.buildChord(program15_sequence0, 0.0, "G minor");
    program15_sequence0_chord0_voicing = IntegrationTestingFixtures.buildVoicing(InstrumentType.Bass, program15_sequence0_chord0, "G3, Bb3, D4");
    program15_sequence0_chord1 = IntegrationTestingFixtures.buildChord(program15_sequence0, 8.0, "Ab minor");
    program15_sequence0_chord1_voicing = IntegrationTestingFixtures.buildVoicing(InstrumentType.Bass, program15_sequence0_chord1, "Ab3, C3, Eb4");
    program15_sequence0_binding0 = IntegrationTestingFixtures.buildBinding(program15_sequence0, 0);
    program15_sequence0_binding0_meme0 = IntegrationTestingFixtures.buildMeme(program15_sequence0_binding0, "Regret");
    //
    program15_sequence1 = IntegrationTestingFixtures.buildSequence(program15, 32, "Outro", 0.5f, "A major", 135.0f);
    program15_sequence1_chord0 = IntegrationTestingFixtures.buildChord(program15_sequence1, 0.0, "C major");
    program15_sequence1_chord0_voicing = IntegrationTestingFixtures.buildVoicing(InstrumentType.Bass, program15_sequence0_chord0, "E3, G3, C4");
    program15_sequence1_chord1 = IntegrationTestingFixtures.buildChord(program15_sequence1, 8.0, "Bb major");
    program15_sequence1_chord1_voicing = IntegrationTestingFixtures.buildVoicing(InstrumentType.Bass, program15_sequence0_chord1, "F3, Bb3, D4");
    program15_sequence1_binding0 = IntegrationTestingFixtures.buildBinding(program15_sequence1, 1);
    program15_sequence1_binding0_meme0 = IntegrationTestingFixtures.buildMeme(program15_sequence1_binding0, "Pride");
    program15_sequence1_binding0_meme1 = IntegrationTestingFixtures.buildMeme(program15_sequence1_binding0, "Shame");

    // return them all
    return ImmutableList.of(
      program3,
      program3_meme0,
      program3_sequence0,
      program3_sequence0_binding0,
      program3_sequence0_binding0_meme0,
      program3_sequence1,
      program3_sequence1_binding0,
      program3_sequence1_binding0_meme0,
      program15,
      program15_meme0,
      program15_sequence0,
      program15_sequence0_chord0,
      program15_sequence0_chord0_voicing,
      program15_sequence0_chord1,
      program15_sequence0_chord1_voicing,
      program15_sequence0_binding0,
      program15_sequence0_binding0_meme0,
      program15_sequence1,
      program15_sequence1_chord0,
      program15_sequence1_chord0_voicing,
      program15_sequence1_chord1,
      program15_sequence1_chord1_voicing,
      program15_sequence1_binding0,
      program15_sequence1_binding0_meme0,
      program15_sequence1_binding0_meme1
    );
  }

  /**
   Library of Content B-3 (shared test fixture)
   <p>
   [#165954673] Integration tests use shared scenario fixtures as much as possible
   <p>
   [#163158036] memes bound to sequence-pattern because sequence-binding is not considered for rhythm sequences, rhythm sequence patterns do not have memes.
   <p>
   [#165954619] Choice is either by sequence-pattern (macro- or main-type sequences) or by sequence (rhythm- and detail-type sequences)
   <p>
   [#153976073] Artist wants Pattern to have type *Macro* or *Main* (for Macro- or Main-type sequences), or *Intro*, *Loop*, or *Outro* (for Rhythm or Detail-type Sequence) in order to of a composition that is dynamic when chosen to fill a Segment.
   + For this test, there's an Intro Pattern with all BLEEPS, multiple Loop Patterns with KICK and SNARE (2x each), and an Outro Pattern with all TOOTS.
   <p>
   [#150279647] Artist wants to of multiple Patterns with the same offset in the same Sequence, in order that XJ randomly select one of the patterns at that offset.
   */
  public Collection<Object> setupFixtureB3() {
    // A basic beat
    program9 = IntegrationTestingFixtures.buildProgram(library2, ProgramType.Rhythm, ProgramState.Published, "Basic Beat", "C", 121, 0.6f);
    program9_meme0 = IntegrationTestingFixtures.buildMeme(program9, "Basic");
    //
    program9_voice0 = IntegrationTestingFixtures.buildVoice(program9, InstrumentType.Drum, "Drums");
    program9_voice0_track0 = IntegrationTestingFixtures.buildTrack(program9_voice0, "BLEEP");
    program9_voice0_track1 = IntegrationTestingFixtures.buildTrack(program9_voice0, "BLEIP");
    program9_voice0_track2 = IntegrationTestingFixtures.buildTrack(program9_voice0, "BLEAP");
    program9_voice0_track3 = IntegrationTestingFixtures.buildTrack(program9_voice0, "BLEEEP");
    program9_voice0_track4 = IntegrationTestingFixtures.buildTrack(program9_voice0, "CLOCK");
    program9_voice0_track5 = IntegrationTestingFixtures.buildTrack(program9_voice0, "SNORT");
    program9_voice0_track6 = IntegrationTestingFixtures.buildTrack(program9_voice0, "KICK");
    program9_voice0_track7 = IntegrationTestingFixtures.buildTrack(program9_voice0, "SNARL");
    program9_voice0_track8 = IntegrationTestingFixtures.buildTrack(program9_voice0, "KIICK");
    program9_voice0_track9 = IntegrationTestingFixtures.buildTrack(program9_voice0, "SNARR");
    program9_voice0_track10 = IntegrationTestingFixtures.buildTrack(program9_voice0, "KEICK");
    program9_voice0_track11 = IntegrationTestingFixtures.buildTrack(program9_voice0, "SNAER");
    program9_voice0_track12 = IntegrationTestingFixtures.buildTrack(program9_voice0, "TOOT");
    program9_voice0_track13 = IntegrationTestingFixtures.buildTrack(program9_voice0, "TOOOT");
    program9_voice0_track14 = IntegrationTestingFixtures.buildTrack(program9_voice0, "TOOTE");
    program9_voice0_track15 = IntegrationTestingFixtures.buildTrack(program9_voice0, "TOUT");
    //
    program9_sequence0 = IntegrationTestingFixtures.buildSequence(program9, 16, "Base", 0.5f, "C", 110.3f);
    //
    program9_sequence0_pattern0 = IntegrationTestingFixtures.buildPattern(program9_sequence0, program9_voice0, ProgramSequencePatternType.Intro, 4, "Intro");
    program9_sequence0_pattern0_event0 = IntegrationTestingFixtures.buildEvent(program9_sequence0_pattern0, program9_voice0_track0, 0, 1, "C2", 1.0f);
    program9_sequence0_pattern0_event1 = IntegrationTestingFixtures.buildEvent(program9_sequence0_pattern0, program9_voice0_track1, 1, 1, "G5", 0.8f);
    program9_sequence0_pattern0_event2 = IntegrationTestingFixtures.buildEvent(program9_sequence0_pattern0, program9_voice0_track2, 2.5f, 1, "C2", 0.6f);
    program9_sequence0_pattern0_event3 = IntegrationTestingFixtures.buildEvent(program9_sequence0_pattern0, program9_voice0_track3, 3, 1, "G5", 0.9f);
    //
    program9_sequence0_pattern1 = IntegrationTestingFixtures.buildPattern(program9_sequence0, program9_voice0, ProgramSequencePatternType.Loop, 4, "Loop A");
    program9_sequence0_pattern1_event0 = IntegrationTestingFixtures.buildEvent(program9_sequence0_pattern1, program9_voice0_track4, 0, 1, "C2", 1.0f);
    program9_sequence0_pattern1_event1 = IntegrationTestingFixtures.buildEvent(program9_sequence0_pattern1, program9_voice0_track5, 1, 1, "G5", 0.8f);
    program9_sequence0_pattern1_event2 = IntegrationTestingFixtures.buildEvent(program9_sequence0_pattern1, program9_voice0_track6, 2.5f, 1, "C2", 0.6f);
    program9_sequence0_pattern1_event3 = IntegrationTestingFixtures.buildEvent(program9_sequence0_pattern1, program9_voice0_track7, 3, 1, "G5", 0.9f);
    //
    program9_sequence0_pattern2 = IntegrationTestingFixtures.buildPattern(program9_sequence0, program9_voice0, ProgramSequencePatternType.Loop, 4, "Loop B");
    program9_sequence0_pattern2_event0 = IntegrationTestingFixtures.buildEvent(program9_sequence0_pattern2, program9_voice0_track8, 0, 1, "B5", 0.9f);
    program9_sequence0_pattern2_event1 = IntegrationTestingFixtures.buildEvent(program9_sequence0_pattern2, program9_voice0_track9, 1, 1, "D2", 1.0f);
    program9_sequence0_pattern2_event2 = IntegrationTestingFixtures.buildEvent(program9_sequence0_pattern2, program9_voice0_track10, 2.5f, 1, "E4", 0.7f);
    program9_sequence0_pattern2_event3 = IntegrationTestingFixtures.buildEvent(program9_sequence0_pattern2, program9_voice0_track11, 3, 1, "C3", 0.5f);
    //
    program9_sequence0_pattern3 = IntegrationTestingFixtures.buildPattern(program9_sequence0, program9_voice0, ProgramSequencePatternType.Outro, 4, "Outro");
    program9_sequence0_pattern3_event0 = IntegrationTestingFixtures.buildEvent(program9_sequence0_pattern3, program9_voice0_track12, 0, 1, "C2", 1.0f);
    program9_sequence0_pattern3_event1 = IntegrationTestingFixtures.buildEvent(program9_sequence0_pattern3, program9_voice0_track13, 1, 1, "G5", 0.8f);
    program9_sequence0_pattern3_event2 = IntegrationTestingFixtures.buildEvent(program9_sequence0_pattern3, program9_voice0_track14, 2.5f, 1, "C2", 0.6f);
    program9_sequence0_pattern3_event3 = IntegrationTestingFixtures.buildEvent(program9_sequence0_pattern3, program9_voice0_track15, 3, 1, "G5", 0.9f);

    // Instrument "808"
    instrument8 = IntegrationTestingFixtures.buildInstrument(library2, InstrumentType.Drum, InstrumentState.Published, "808 Drums");
    instrument8_meme0 = IntegrationTestingFixtures.buildMeme(instrument8, "heavy");
    instrument8_audio8kick = IntegrationTestingFixtures.buildAudio(instrument8, "Kick", "19801735098q47895897895782138975898.wav", 0.01f, 2.123f, 120.0f, 0.62f, "KICK", "Eb", 1.0f);
    instrument8_audio8snare = IntegrationTestingFixtures.buildAudio(instrument8, "Snare", "975898198017350afghjkjhaskjdfjhk.wav", 0.01f, 1.5f, 120.0f, 0.62f, "SNARE", "Ab", 0.8f);
    instrument8_audio8bleep = IntegrationTestingFixtures.buildAudio(instrument8, "Bleep", "17350afghjkjhaskjdfjhk9758981980.wav", 0.01f, 1.5f, 120.0f, 0.62f, "BLEEP", "Ab", 0.8f);
    instrument8_audio8toot = IntegrationTestingFixtures.buildAudio(instrument8, "Toot", "askjdfjhk975898198017350afghjkjh.wav", 0.01f, 1.5f, 120.0f, 0.62f, "TOOT", "Ab", 0.8f);

    // return them all
    return ImmutableList.of(
      program9,
      program9_meme0,
      program9_voice0,
      program9_voice0_track0,
      program9_voice0_track1,
      program9_voice0_track2,
      program9_voice0_track3,
      program9_voice0_track4,
      program9_voice0_track5,
      program9_voice0_track6,
      program9_voice0_track7,
      program9_voice0_track8,
      program9_voice0_track9,
      program9_voice0_track10,
      program9_voice0_track11,
      program9_voice0_track12,
      program9_voice0_track13,
      program9_voice0_track14,
      program9_voice0_track15,
      program9_sequence0,
      program9_sequence0_pattern0,
      program9_sequence0_pattern0_event0,
      program9_sequence0_pattern0_event1,
      program9_sequence0_pattern0_event2,
      program9_sequence0_pattern0_event3,
      program9_sequence0_pattern1,
      program9_sequence0_pattern1_event0,
      program9_sequence0_pattern1_event1,
      program9_sequence0_pattern1_event2,
      program9_sequence0_pattern1_event3,
      program9_sequence0_pattern2,
      program9_sequence0_pattern2_event0,
      program9_sequence0_pattern2_event1,
      program9_sequence0_pattern2_event2,
      program9_sequence0_pattern2_event3,
      program9_sequence0_pattern3,
      program9_sequence0_pattern3_event0,
      program9_sequence0_pattern3_event1,
      program9_sequence0_pattern3_event2,
      program9_sequence0_pattern3_event3,
      instrument8,
      instrument8_meme0,
      instrument8_audio8kick,
      instrument8_audio8snare,
      instrument8_audio8bleep,
      instrument8_audio8toot
    );
  }

  /**
   Library of Content B-4 (shared test fixture)
   <p>
   [#154464276] Detail Craft v1
   */
  public Collection<Object> setupFixtureB4_DetailBass() {
    // A basic bass pattern
    program10 = IntegrationTestingFixtures.buildProgram(library2, ProgramType.Detail, ProgramState.Published, "Earth Bass Detail Pattern", "C", 121, 0.6f);
    program10_meme0 = IntegrationTestingFixtures.buildMeme(program10, "EARTH");
    //
    program10_voice0 = IntegrationTestingFixtures.buildVoice(program10, InstrumentType.Bass, "Dirty Bass");
    program10_voice0_track0 = IntegrationTestingFixtures.buildTrack(program10_voice0, "BUM");
    //
    program10_sequence0 = IntegrationTestingFixtures.buildSequence(program10, 16, "Simple Walk", 0.5f, "C", 110.3f);
    //
    program10_sequence0_pattern0 = IntegrationTestingFixtures.buildPattern(program10_sequence0, program10_voice0, ProgramSequencePatternType.Intro, 4, "Intro");
    program10_sequence0_pattern0_event0 = IntegrationTestingFixtures.buildEvent(program10_sequence0_pattern0, program10_voice0_track0, 0, 1, "C2", 1.0f);
    program10_sequence0_pattern0_event1 = IntegrationTestingFixtures.buildEvent(program10_sequence0_pattern0, program10_voice0_track0, 1, 1, "G5", 0.8f);
    program10_sequence0_pattern0_event2 = IntegrationTestingFixtures.buildEvent(program10_sequence0_pattern0, program10_voice0_track0, 2, 1, "C2", 0.6f);
    program10_sequence0_pattern0_event3 = IntegrationTestingFixtures.buildEvent(program10_sequence0_pattern0, program10_voice0_track0, 3, 1, "G5", 0.9f);
    //
    program10_sequence0_pattern1 = IntegrationTestingFixtures.buildPattern(program10_sequence0, program10_voice0, ProgramSequencePatternType.Loop, 4, "Loop A");
    program10_sequence0_pattern1_event0 = IntegrationTestingFixtures.buildEvent(program10_sequence0_pattern1, program10_voice0_track0, 0, 1, "C2", 1.0f);
    program10_sequence0_pattern1_event1 = IntegrationTestingFixtures.buildEvent(program10_sequence0_pattern1, program10_voice0_track0, 1, 1, "G5", 0.8f);
    program10_sequence0_pattern1_event2 = IntegrationTestingFixtures.buildEvent(program10_sequence0_pattern1, program10_voice0_track0, 2, 1, "C2", 0.6f);
    program10_sequence0_pattern1_event3 = IntegrationTestingFixtures.buildEvent(program10_sequence0_pattern1, program10_voice0_track0, 3, 1, "G5", 0.9f);
    //
    program10_sequence0_pattern2 = IntegrationTestingFixtures.buildPattern(program10_sequence0, program10_voice0, ProgramSequencePatternType.Loop, 4, "Loop B");
    program10_sequence0_pattern2_event0 = IntegrationTestingFixtures.buildEvent(program10_sequence0_pattern2, program10_voice0_track0, 0, 1, "B5", 0.9f);
    program10_sequence0_pattern2_event1 = IntegrationTestingFixtures.buildEvent(program10_sequence0_pattern2, program10_voice0_track0, 1, 1, "D2", 1.0f);
    program10_sequence0_pattern2_event2 = IntegrationTestingFixtures.buildEvent(program10_sequence0_pattern2, program10_voice0_track0, 2, 1, "E4", 0.7f);
    program10_sequence0_pattern2_event3 = IntegrationTestingFixtures.buildEvent(program10_sequence0_pattern2, program10_voice0_track0, 3, 1, "C3", 0.5f);
    //
    program10_sequence0_pattern3 = IntegrationTestingFixtures.buildPattern(program10_sequence0, program10_voice0, ProgramSequencePatternType.Outro, 4, "Outro");
    program10_sequence0_pattern3_event0 = IntegrationTestingFixtures.buildEvent(program10_sequence0_pattern3, program10_voice0_track0, 0, 1, "C2", 1.0f);
    program10_sequence0_pattern3_event1 = IntegrationTestingFixtures.buildEvent(program10_sequence0_pattern3, program10_voice0_track0, 1, 1, "G5", 0.8f);
    program10_sequence0_pattern3_event2 = IntegrationTestingFixtures.buildEvent(program10_sequence0_pattern3, program10_voice0_track0, 2, 1, "C2", 0.6f);
    program10_sequence0_pattern3_event3 = IntegrationTestingFixtures.buildEvent(program10_sequence0_pattern3, program10_voice0_track0, 3, 1, "G5", 0.9f);

    // Instrument "Bass"
    instrument9 = IntegrationTestingFixtures.buildInstrument(library2, InstrumentType.Bass, InstrumentState.Published, "Bass");
    instrument9_meme0 = IntegrationTestingFixtures.buildMeme(instrument9, "heavy");
    instrument9_audio8 = IntegrationTestingFixtures.buildAudio(instrument9, "bass", "19801735098q47895897895782138975898.wav", 0.01f, 2.123f, 120.0f, 0.62f, "BLOOP", "Eb", 1.0f);

    // return them all
    return ImmutableList.of(
      program10,
      program10_meme0,
      program10_voice0,
      program10_voice0_track0,
      program10_sequence0,
      program10_sequence0_pattern0,
      program10_sequence0_pattern0_event0,
      program10_sequence0_pattern0_event1,
      program10_sequence0_pattern0_event2,
      program10_sequence0_pattern0_event3,
      program10_sequence0_pattern1,
      program10_sequence0_pattern1_event0,
      program10_sequence0_pattern1_event1,
      program10_sequence0_pattern1_event2,
      program10_sequence0_pattern1_event3,
      program10_sequence0_pattern2,
      program10_sequence0_pattern2_event0,
      program10_sequence0_pattern2_event1,
      program10_sequence0_pattern2_event2,
      program10_sequence0_pattern2_event3,
      program10_sequence0_pattern3,
      program10_sequence0_pattern3_event0,
      program10_sequence0_pattern3_event1,
      program10_sequence0_pattern3_event2,
      program10_sequence0_pattern3_event3,
      instrument9,
      instrument9_meme0,
      instrument9_audio8
    );
  }

  /**
   Generate a Library comprising many related entities

   @param N magnitude of library to generate
   @return entities
   */
  public Collection<Object> generatedFixture(int N) {
    Collection<Object> entities = Lists.newArrayList();

    account1 = add(entities, IntegrationTestingFixtures.buildAccount("Generated"));
    user1 = add(entities, IntegrationTestingFixtures.buildUser("generated", "generated@email.com", "http://pictures.com/generated.gif", "Admin"));
    library1 = add(entities, IntegrationTestingFixtures.buildLibrary(account1, "generated"));

    template1 = buildTemplate(account1, "Complex Library Test", "complex");
    entities.add(template1);
    entities.add(buildTemplateBinding(template1, library1));

    // Create a N-magnitude set of unique major memes
    String[] majorMemeNames = listOfUniqueRandom(N, LoremIpsum.COLORS);
    String[] minorMemeNames = listOfUniqueRandom((long) StrictMath.ceil(N >> 1), LoremIpsum.VARIANTS);
    String[] percussiveNames = listOfUniqueRandom(N, LoremIpsum.PERCUSSIVE_NAMES);

    // Generate a Drum Instrument for each meme
    for (int i = 0; i < N; i++) {
      String majorMemeName = majorMemeNames[i];
      String minorMemeName = random(minorMemeNames);
      //
      Instrument instrument = add(entities, IntegrationTestingFixtures.buildInstrument(library1, InstrumentType.Drum, InstrumentState.Published, String.format("%s Drums", majorMemeName)));
      add(entities, buildInstrumentMeme(instrument, majorMemeName));
      add(entities, buildInstrumentMeme(instrument, minorMemeName));
      // audios of instrument
      for (int k = 0; k < N; k++)
        add(entities, IntegrationTestingFixtures.buildAudio(instrument, Text.toProper(percussiveNames[k]), String.format("%s.wav", Text.toLowerSlug(percussiveNames[k])), random(0, 0.05), random(0.25, 2), random(80, 120), 0.62f, percussiveNames[k], "X", random(0.8, 1)));
      //
      log.debug("Generated Drum-type Instrument id={}, minorMeme={}, majorMeme={}", instrument.getId(), minorMemeName, majorMemeName);
    }

    // Generate Perc Loop Instruments
    for (int i = 0; i < N; i++) {
      Instrument instrument = add(entities, IntegrationTestingFixtures.buildInstrument(library1, InstrumentType.PercLoop, InstrumentState.Published, "Perc Loop"));
      log.debug("Generated PercLoop-type Instrument id={}", instrument.getId());
    }

    // Generate N*2 total Macro-type programs, each transitioning of one MemeEntity to another
    for (int i = 0; i < N << 1; i++) {
      String[] twoMemeNames = listOfUniqueRandom(2, majorMemeNames);
      String majorMemeFromName = twoMemeNames[0];
      String majorMemeToName = twoMemeNames[1];
      String minorMemeName = random(minorMemeNames);
      String[] twoKeys = listOfUniqueRandom(2, LoremIpsum.MUSICAL_KEYS);
      String keyFrom = twoKeys[0];
      String keyTo = twoKeys[1];
      float densityFrom = random(0.3, 0.9);
      float tempoFrom = random(80, 120);
      //
      Program program = add(entities, IntegrationTestingFixtures.buildProgram(library1, ProgramType.Macro, ProgramState.Published, String.format("%s, create %s to %s", minorMemeName, majorMemeFromName, majorMemeToName), keyFrom, tempoFrom, 0.6f));
      add(entities, buildProgramMeme(program, minorMemeName));
      // of offset 0
      var sequence0 = add(entities, IntegrationTestingFixtures.buildSequence(program, 0, String.format("Start %s", majorMemeFromName), densityFrom, keyFrom, tempoFrom));
      var binding0 = add(entities, buildProgramSequenceBinding(sequence0, 0));
      add(entities, buildProgramSequenceBindingMeme(binding0, majorMemeFromName));
      // to offset 1
      float densityTo = random(0.3, 0.9);
      float tempoTo = random(803, 120);
      var sequence1 = add(entities, IntegrationTestingFixtures.buildSequence(program, 0, String.format("Finish %s", majorMemeToName), densityTo, keyTo, tempoTo));
      var binding1 = add(entities, buildProgramSequenceBinding(sequence1, 1));
      add(entities, buildProgramSequenceBindingMeme(binding1, majorMemeToName));
      //
      log.debug("Generated Macro-type Program id={}, minorMeme={}, majorMemeFrom={}, majorMemeTo={}", program.getId(), minorMemeName, majorMemeFromName, majorMemeToName);
    }

    // Generate N*4 total Main-type Programs, each having N patterns comprised of ~N*2 chords, bound to N*4 sequence patterns
    ProgramSequence[] sequences = new ProgramSequence[N];
    for (int i = 0; i < N << 2; i++) {
      String majorMemeName = random(majorMemeNames);
      String[] sequenceNames = listOfUniqueRandom(N, LoremIpsum.ELEMENTS);
      String[] subKeys = listOfUniqueRandom(N, LoremIpsum.MUSICAL_KEYS);
      Float[] subDensities = listOfRandomValues(N);
      float tempo = random(80, 120);
      //
      Program program = add(entities, IntegrationTestingFixtures.buildProgram(library1, ProgramType.Main, ProgramState.Published, String.format("%s: %s", majorMemeName, String.join(",", sequenceNames)), subKeys[0], tempo, 0.6f));
      add(entities, buildProgramMeme(program, majorMemeName));
      // sequences of program
      for (int iP = 0; iP < N; iP++) {
        Integer total = random(LoremIpsum.SEQUENCE_TOTALS);
        sequences[iP] = add(entities, IntegrationTestingFixtures.buildSequence(program, total, String.format("%s in %s", majorMemeName, sequenceNames[iP]), subDensities[iP], subKeys[iP], tempo));
        for (int iPC = 0; iPC < N << 2; iPC++) {
          // always use first chord, then use more chords with more density
          if (0 == iPC || StrictMath.random() < subDensities[iP]) {
            add(entities, IntegrationTestingFixtures.buildChord(sequences[iP], StrictMath.floor((float) iPC * total * 4 / N), random(LoremIpsum.MUSICAL_CHORDS)));
          }
        }
      }
      // sequence sequence binding
      for (int offset = 0; offset < N << 2; offset++) {
        int num = (int) StrictMath.floor(StrictMath.random() * N);
        var binding = add(entities, buildProgramSequenceBinding(sequences[num], offset));
        add(entities, IntegrationTestingFixtures.buildMeme(binding, random(minorMemeNames)));
      }
      log.debug("Generated Main-type Program id={}, majorMeme={} with {} sequences bound {} times", program.getId(), majorMemeName, N, N << 2);
    }

    // Generate N total Rhythm-type Sequences, each having N voices, and N*2 patterns comprised of N*8 events
    ProgramVoice[] voices = new ProgramVoice[N];
    Map<String, ProgramVoiceTrack> trackMap = Maps.newHashMap();
    for (int i = 0; i < N; i++) {
      String majorMemeName = majorMemeNames[i];
      float tempo = random(80, 120);
      String key = random(LoremIpsum.MUSICAL_KEYS);
      float density = random(0.4, 0.9);
      //
      Program program = add(entities, IntegrationTestingFixtures.buildProgram(library1, ProgramType.Rhythm, ProgramState.Published, String.format("%s Beat", majorMemeName), key, tempo, 0.6f));
      trackMap.clear();
      add(entities, buildProgramMeme(program, majorMemeName));
      // voices of program
      for (int iV = 0; iV < N; iV++) {
        voices[iV] = add(entities, IntegrationTestingFixtures.buildVoice(program, InstrumentType.Drum, String.format("%s %s", majorMemeName, percussiveNames[iV])));
      }
      var sequenceBase = add(entities, IntegrationTestingFixtures.buildSequence(program, random(LoremIpsum.SEQUENCE_TOTALS), "Base", density, key, tempo));
      // patterns of program
      for (int iP = 0; iP < N << 1; iP++) {
        Integer total = random(LoremIpsum.PATTERN_TOTALS);
        int num = (int) StrictMath.floor(StrictMath.random() * N);

        // first pattern is always a Loop (because that's required) then the rest at random
        ProgramSequencePatternType type = 0 == iP ? ProgramSequencePatternType.Loop : randomRhythmPatternType();
        var pattern = add(entities, IntegrationTestingFixtures.buildPattern(sequenceBase, voices[num], type, total, String.format("%s %s %s", majorMemeName, type.toString(), random(LoremIpsum.ELEMENTS))));
        for (int iPE = 0; iPE < N << 2; iPE++) {
          // always use first chord, then use more chords with more density
          if (0 == iPE || StrictMath.random() < density) {
            String name = percussiveNames[num];
            if (!trackMap.containsKey(name))
              trackMap.put(name, add(entities, IntegrationTestingFixtures.buildTrack(voices[num], name)));
            add(entities, IntegrationTestingFixtures.buildEvent(pattern, trackMap.get(name), (float) StrictMath.floor((double) iPE * total * 4 / N), random(0.25, 1.0), "X", random(0.4, 0.9)));
          }
        }
      }
      log.debug("Generated Rhythm-type Program id={}, majorMeme={} with {} patterns", program.getId(), majorMemeName, N);
    }

    return entities;
  }

  /**
   Add an entity to a collection, then return that entity

   @param to     collection
   @param entity to add
   @param <N>    type of entity
   @return entity that's been added
   */
  private <N> N add(Collection<Object> to, N entity) {
    to.add(entity);
    return entity;
  }

}
