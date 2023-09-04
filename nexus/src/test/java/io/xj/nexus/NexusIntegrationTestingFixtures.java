// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus;

import io.xj.hub.enums.*;
import io.xj.hub.tables.pojos.*;
import io.xj.hub.util.StringUtils;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityUtils;
import io.xj.nexus.hub_client.HubClientAccess;
import io.xj.nexus.hub_client.access.Users;
import io.xj.nexus.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static io.xj.hub.util.ValueUtils.MICROS_PER_SECOND;
import static io.xj.hub.util.ValueUtils.SECONDS_PER_MINUTE;
import static io.xj.nexus.HubIntegrationTestingFixtures.*;

/**
 Integration tests use shared scenario fixtures as much as possible https://www.pivotaltracker.com/story/show/165954673
 <p>
 Testing the hypothesis that, while unit tests are all independent,
 integration tests ought to be as much about testing all features around a consensus model of the platform
 as they are about testing all resources.
 */
public class NexusIntegrationTestingFixtures {
  static final Logger LOG = LoggerFactory.getLogger(NexusIntegrationTestingFixtures.class);
  static final double RANDOM_VALUE_FROM = 0.3;
  static final double RANDOM_VALUE_TO = 0.8;

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
  public ProgramSequenceBinding program5_sequence1_binding1;
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
  public ProgramSequenceBindingMeme program5_sequence1_binding1_meme0;
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
  public ProgramVoice program15_voiceBass;
  public ProgramVoice program35_voice0;
  public ProgramVoice program5_voiceBass;
  public ProgramVoice program5_voicePad;
  public ProgramVoice program5_voiceSticky;
  public ProgramVoice program5_voiceStripe;
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
   List of N random values

   @param N number of values
   @return array of values
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
   @return array of unique random Strings
   */
  protected static String[] listOfUniqueRandom(long N, String[] sourceItems) {
    long count = 0;
    Collection<String> items = new ArrayList<>();
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
    chain.setTemplateConfig(template.getConfig());
    chain.setState(state);
    return chain;
  }

  public static Chain buildChain(Account account, String name, ChainType type, ChainState state, Template template) {
    return buildChain(account, name, type, state, template, StringUtils.toShipKey(name));
  }

  public static Chain buildChain(Account account, Template template, String name, ChainType type, ChainState state) {
    return buildChain(account, name, type, state, template, StringUtils.toShipKey(name));
  }

  public static Chain buildChain(Account account, String name, ChainType type, ChainState state, Template template, /*@Nullable*/ String shipKey) {
    var chain = new Chain();
    chain.setId(UUID.randomUUID());
    chain.setTemplateId(template.getId());
    chain.setAccountId(account.getId());
    chain.setName(name);
    chain.setType(type);
    chain.setState(state);
    chain.setTemplateConfig(TEST_TEMPLATE_CONFIG);
    if (Objects.nonNull(shipKey))
      chain.shipKey(shipKey);
    return chain;
  }

  public static Segment buildSegment() {
    var seg = new Segment();
    seg.setId(UUID.randomUUID());
    return seg;
  }

  public static Segment buildSegment(Chain chain, int offset, SegmentState state, String key, int total, double density, double tempo, String storageKey) {
    return buildSegment(chain,
      0 < offset ? SegmentType.CONTINUE : SegmentType.INITIAL,
      offset, 0, state, key, total, density, tempo, storageKey, state == SegmentState.CRAFTED);
  }


  public static Segment buildSegment(Chain chain, SegmentType type, int offset, int delta, SegmentState state, String key, int total, double density, double tempo, String storageKey, boolean hasEndSet) {
    var segment = new Segment();
    segment.setId(UUID.randomUUID());
    segment.setChainId(chain.getId());
    segment.setType(type);
    segment.setOffset((long) offset);
    segment.setDelta(delta);
    segment.setState(state);
    segment.setBeginAtChainMicros((long) (offset * MICROS_PER_SECOND * total * SECONDS_PER_MINUTE / tempo));
    segment.setKey(key);
    segment.setTotal(total);
    segment.setDensity(density);
    segment.setTempo(tempo);
    segment.setStorageKey(storageKey);
    segment.setWaveformPreroll(0.0);
    segment.setWaveformPostroll(0.0);

    var durationMicros = (long) (MICROS_PER_SECOND * total * SECONDS_PER_MINUTE / tempo);
    if (hasEndSet)
      segment.setDurationMicros(durationMicros);

    return segment;
  }

  public static Segment buildSegment(Chain chain, String key, int total, float density, float tempo) {
    return buildSegment(
      chain,
      0,
      SegmentState.CRAFTING,
      key, total, density, tempo, "segment123");
  }

  public static Segment buildSegment(Chain chain, int offset, String key, int total, float density, float tempo) {
    return buildSegment(
      chain,
      offset,
      SegmentState.CRAFTING,
      key, total, density, tempo, "segment123");
  }

  public static SegmentChoice buildSegmentChoice(Segment segment, ProgramType programType, ProgramSequenceBinding programSequenceBinding) {
    var segmentChoice = new SegmentChoice();
    segmentChoice.setId(UUID.randomUUID());
    segmentChoice.setSegmentId(segment.getId());
    segmentChoice.setDeltaIn(Segment.DELTA_UNLIMITED);
    segmentChoice.setDeltaOut(Segment.DELTA_UNLIMITED);
    segmentChoice.setProgramId(programSequenceBinding.getProgramId());
    segmentChoice.setProgramSequenceBindingId(programSequenceBinding.getId());
    segmentChoice.setProgramType(programType);
    return segmentChoice;
  }

  public static SegmentChoice buildSegmentChoice(Segment segment, ProgramType programType, ProgramSequence programSequence) {
    var segmentChoice = new SegmentChoice();
    segmentChoice.setId(UUID.randomUUID());
    segmentChoice.setSegmentId(segment.getId());
    segmentChoice.setDeltaIn(Segment.DELTA_UNLIMITED);
    segmentChoice.setDeltaOut(Segment.DELTA_UNLIMITED);
    segmentChoice.setProgramId(programSequence.getProgramId());
    segmentChoice.setProgramSequenceId(programSequence.getId());
    segmentChoice.setProgramType(programType);
    return segmentChoice;
  }

  public static SegmentChoice buildSegmentChoice(Segment segment, int deltaIn, int deltaOut, Program program, InstrumentType instrumentType, InstrumentMode instrumentMode) {
    var segmentChoice = new SegmentChoice();
    segmentChoice.setId(UUID.randomUUID());
    segmentChoice.setSegmentId(segment.getId());
    segmentChoice.setDeltaIn(deltaIn);
    segmentChoice.setDeltaOut(deltaOut);
    segmentChoice.setProgramId(program.getId());
    segmentChoice.setProgramType(program.getType());
    segmentChoice.setMute(false);
    segmentChoice.setInstrumentType(instrumentType);
    segmentChoice.setInstrumentMode(instrumentMode);
    return segmentChoice;
  }

  public static SegmentChoice buildSegmentChoice(Segment segment, Program program) {
    var segmentChoice = new SegmentChoice();
    segmentChoice.setId(UUID.randomUUID());
    segmentChoice.setSegmentId(segment.getId());
    segmentChoice.setDeltaIn(Segment.DELTA_UNLIMITED);
    segmentChoice.setDeltaOut(Segment.DELTA_UNLIMITED);
    segmentChoice.setProgramId(program.getId());
    segmentChoice.setProgramType(program.getType());
    return segmentChoice;
  }

  public static SegmentMeta buildSegmentMeta(Segment segment, String key, String value) {
    var segmentMeta = new SegmentMeta();
    segmentMeta.setId(UUID.randomUUID());
    segmentMeta.setSegmentId(segment.getId());
    segmentMeta.setKey(key);
    segmentMeta.setValue(value);
    return segmentMeta;
  }

  public static SegmentChoice buildSegmentChoice(Segment segment, Program program, ProgramSequence programSequence, ProgramVoice voice, Instrument instrument) {
    var segmentChoice = new SegmentChoice();
    segmentChoice.setId(UUID.randomUUID());
    segmentChoice.setProgramVoiceId(voice.getId());
    segmentChoice.setInstrumentId(instrument.getId());
    segmentChoice.setInstrumentType(instrument.getType());
    segmentChoice.setMute(false);
    segmentChoice.setInstrumentMode(instrument.getMode());
    segmentChoice.setDeltaIn(Segment.DELTA_UNLIMITED);
    segmentChoice.setDeltaOut(Segment.DELTA_UNLIMITED);
    segmentChoice.setSegmentId(segment.getId());
    segmentChoice.setProgramId(program.getId());
    segmentChoice.setProgramSequenceId(programSequence.getId());
    segmentChoice.setProgramType(program.getType());
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
    choice.setInstrumentType(instrument.getType());
    choice.setMute(false);
    choice.setInstrumentMode(instrument.getMode());
    return choice;
  }

  public static SegmentChoice buildSegmentChoice(Segment segment, int deltaIn, int deltaOut, Program program) {
    var choice = new SegmentChoice();
    choice.setId(UUID.randomUUID());
    choice.setSegmentId(segment.getId());
    choice.setDeltaIn(deltaIn);
    choice.setDeltaOut(deltaOut);
    choice.setProgramId(program.getId());
    choice.setProgramType(program.getType());
    return choice;
  }

  public static SegmentMeme buildSegmentMeme(Segment segment, String name) {
    var segmentMeme = new SegmentMeme();
    segmentMeme.setId(UUID.randomUUID());
    segmentMeme.setSegmentId(segment.getId());
    segmentMeme.setName(name);
    return segmentMeme;
  }

  public static SegmentChord buildSegmentChord(Segment segment, Double atPosition, String name) {
    var segmentChord = new SegmentChord();
    segmentChord.setId(UUID.randomUUID());
    segmentChord.setSegmentId(segment.getId());
    segmentChord.setPosition(atPosition);
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

  public static SegmentChoiceArrangementPick buildSegmentChoiceArrangementPick(Segment segment, SegmentChoiceArrangement segmentChoiceArrangement, ProgramSequencePatternEvent event, InstrumentAudio instrumentAudio, String pickEvent) {
    var pick = new SegmentChoiceArrangementPick();
    pick.setId(UUID.randomUUID());
    pick.setSegmentId(segmentChoiceArrangement.getSegmentId());
    pick.setSegmentChoiceArrangementId(segmentChoiceArrangement.getId());
    pick.setProgramSequencePatternEventId(event.getId());
    pick.setInstrumentAudioId(instrumentAudio.getId());
    pick.setStartAtSegmentMicros((long) (event.getPosition() * MICROS_PER_SECOND / segment.getTempo()));
    pick.setLengthMicros((long) (event.getDuration() * MICROS_PER_SECOND / segment.getTempo()));
    pick.setAmplitude(Double.valueOf(event.getVelocity()));
    pick.setTones(event.getTones());
    pick.setEvent(pickEvent);
    return pick;
  }

  public static SegmentChoiceArrangementPick buildSegmentChoiceArrangementPick(Segment segment, SegmentChoiceArrangement segmentChoiceArrangement, ProgramSequencePatternEvent event, InstrumentAudio instrumentAudio, String tones, String pickEvent) {
    var pick = buildSegmentChoiceArrangementPick(segment, segmentChoiceArrangement, event, instrumentAudio, pickEvent);
    pick.setTones(tones);
    return pick;
  }

  public static SegmentChoiceArrangementPick buildSegmentChoiceArrangementPick(Segment segment, SegmentChoiceArrangement segmentChoiceArrangement, ProgramSequencePatternEvent event, InstrumentAudio instrumentAudio, String tones, Double start) {
    var pick = buildSegmentChoiceArrangementPick(segment, segmentChoiceArrangement, event, instrumentAudio, tones);
    pick.setTones(tones);
    pick.setStartAtSegmentMicros((long) (start * MICROS_PER_SECOND / segment.getTempo()));
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
  public static HubClientAccess buildHubClientAccess(User user, UserAuth userAuth, List<Account> accounts, String rolesCSV) {
    return new HubClientAccess()
      .setUserId(user.getId())
      .setUserAuthId(userAuth.getId())
      .setAccountIds(EntityUtils.idsOf(accounts))
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
  public static HubClientAccess buildHubClientAccess(User user, List<Account> accounts, String rolesCSV) {
    return new HubClientAccess()
      .setUserId(user.getId())
      .setAccountIds(EntityUtils.idsOf(accounts))
      .setRoleTypes(Users.userRoleTypesFromCsv(rolesCSV));
  }

  /**
   Create a new HubAccess control object

   @param user     for access
   @param userAuth for access
   @param accounts for access
   @return access control object
   */
  public static HubClientAccess buildHubClientAccess(User user, UserAuth userAuth, List<Account> accounts) {
    return new HubClientAccess()
      .setUserId(user.getId())
      .setUserAuthId(userAuth.getId())
      .setAccountIds(EntityUtils.idsOf(accounts));
  }

  /**
   Create a new HubAccess control object

   @param user     for access
   @param accounts for access
   @return access control object
   */
  public static HubClientAccess buildHubClientAccess(User user, List<Account> accounts) {
    return new HubClientAccess()
      .setUserId(user.getId())
      .setAccountIds(EntityUtils.idsOf(accounts));
  }

  /**
   Create a new HubAccess control object

   @param accounts for access
   @param rolesCSV for access
   @return access control object
   */
  public static HubClientAccess buildHubClientAccess(List<Account> accounts, String rolesCSV) {
    return new HubClientAccess()
      .setAccountIds(EntityUtils.idsOf(accounts))
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
    account1 = buildAccount("bananas");

    // Library "house"
    library2 = buildLibrary(account1, "house");

    // Template Binding to library 2
    template1 = buildTemplate(account1, "Test Template 1", "test1");
    templateBinding1 = buildTemplateBinding(template1, library2);

    // John has "user" and "admin" roles, belongs to account "bananas"
    user2 = buildUser("john", "john@email.com", "https://pictures.com/john.gif", "Admin");

    // Jenny has a "user" role and belongs to account "bananas"
    user3 = buildUser("jenny", "jenny@email.com", "https://pictures.com/jenny.gif", "User");
    accountUser1a = buildAccountUser(account1, user3);

    // "Tropical, Wild to Cozy" macro-program in house library
    program4 = buildProgram(library2, ProgramType.Macro, ProgramState.Published, "Tropical, Wild to Cozy", "C", 120.0f, 0.6f);
    program4_meme0 = buildMeme(program4, "Tropical");
    //
    program4_sequence0 = buildSequence(program4, 0, "Start Wild", 0.6f, "C");
    program4_sequence0_binding0 = buildBinding(program4_sequence0, 0);
    program4_sequence0_binding0_meme0 = buildMeme(program4_sequence0_binding0, "Wild");
    //
    program4_sequence1 = buildSequence(program4, 0, "Intermediate", 0.4f, "Bb minor");
    program4_sequence1_binding0 = buildBinding(program4_sequence1, 1);
    program4_sequence1_binding0_meme0 = buildMeme(program4_sequence1_binding0, "Cozy");
    program4_sequence1_binding0_meme1 = buildMeme(program4_sequence1_binding0, "Wild");
    //
    program4_sequence2 = buildSequence(program4, 0, "Finish Cozy", 0.4f, "Ab minor");
    program4_sequence2_binding0 = buildBinding(program4_sequence2, 2);
    program4_sequence2_binding0_meme0 = buildMeme(program4_sequence2_binding0, "Cozy");

    // Main program
    program5 = buildProgram(library2, ProgramType.Main, ProgramState.Published, "Main Jam", "C minor", 140, 0.6f);
    program5_voiceBass = buildVoice(program5, InstrumentType.Bass, "Bass");
    program5_voiceSticky = buildVoice(program5, InstrumentType.Sticky, "Sticky");
    program5_voiceStripe = buildVoice(program5, InstrumentType.Stripe, "Stripe");
    program5_voicePad = buildVoice(program5, InstrumentType.Pad, "Pad");
    program5_meme0 = buildMeme(program5, "Outlook");
    //
    program5_sequence0 = buildSequence(program5, 16, "Intro", 0.5f, "G major");
    program5_sequence0_chord0 = buildChord(program5_sequence0, 0.0, "G major");
    program5_sequence0_chord0_voicing = buildVoicing(program5_sequence0_chord0, program5_voiceBass, "G3, B3, D4");
    program5_sequence0_chord1 = buildChord(program5_sequence0, 8.0, "Ab minor");
    program5_sequence0_chord1_voicing = buildVoicing(program5_sequence0_chord1, program5_voiceBass, "Ab3, Db3, F4");
    program5_sequence0_chord2 = buildChord(program5_sequence0, 75.0, "G-9"); // this ChordEntity should be ignored, because it's past the end of the main-pattern total https://www.pivotaltracker.com/story/show/154090557
    program5_sequence0_chord2_voicing = buildVoicing(program5_sequence0_chord2, program5_voiceBass, "G3, Bb3, D4, A4");
    program5_sequence0_binding0 = buildBinding(program5_sequence0, 0);
    program5_sequence0_binding0_meme0 = buildMeme(program5_sequence0_binding0, "Optimism");
    //
    program5_sequence1 = buildSequence(program5, 32, "Drop", 0.5f, "G minor");
    program5_sequence1_chord0 = buildChord(program5_sequence1, 0.0, "C major");
    //
    program5_sequence1_chord0_voicing = buildVoicing(program5_sequence1_chord0, program5_voiceBass, "Ab3, Db3, F4");
    program5_sequence1_chord1 = buildChord(program5_sequence1, 8.0, "Bb minor");
    //
    program5_sequence1_chord1_voicing = buildVoicing(program5_sequence1_chord1, program5_voiceBass, "Ab3, Db3, F4");
    program5_sequence1_binding0 = buildBinding(program5_sequence1, 1);
    program5_sequence1_binding0_meme0 = buildMeme(program5_sequence1_binding0, "Pessimism");
    program5_sequence1_binding1 = buildBinding(program5_sequence1, 1);
    program5_sequence1_binding1_meme0 = buildMeme(program5_sequence1_binding0, "Pessimism");

    // A basic beat
    program35 = buildProgram(library2, ProgramType.Beat, ProgramState.Published, "Basic Beat", "C", 121, 0.6f);
    program35_meme0 = buildMeme(program35, "Basic");
    program35_voice0 = buildVoice(program35, InstrumentType.Drum, "Drums");
    program35_voice0_track0 = buildTrack(program35_voice0, "CLOCK");
    program35_voice0_track1 = buildTrack(program35_voice0, "SNORT");
    program35_voice0_track2 = buildTrack(program35_voice0, "KICK");
    program35_voice0_track3 = buildTrack(program35_voice0, "SNARL");
    //
    program35_sequence0 = buildSequence(program35, 16, "Base", 0.5f, "C");
    program35_sequence0_pattern0 = buildPattern(program35_sequence0, program35_voice0, 4, "Drop");
    program35_sequence0_pattern0_event0 = buildEvent(program35_sequence0_pattern0, program35_voice0_track0, 0.0f, 1.0f, "C2", 1.0f);
    program35_sequence0_pattern0_event1 = buildEvent(program35_sequence0_pattern0, program35_voice0_track1, 1.0f, 1.0f, "G5", 0.8f);
    program35_sequence0_pattern0_event2 = buildEvent(program35_sequence0_pattern0, program35_voice0_track2, 2.5f, 1.0f, "C2", 0.6f);
    program35_sequence0_pattern0_event3 = buildEvent(program35_sequence0_pattern0, program35_voice0_track3, 3.0f, 1.0f, "G5", 0.9f);
    //
    program35_sequence0_pattern1 = buildPattern(program35_sequence0, program35_voice0, 4, "Drop Alt");
    program35_sequence0_pattern1_event0 = buildEvent(program35_sequence0_pattern1, program35_voice0_track0, 0.0f, 1.0f, "B5", 0.9f);
    program35_sequence0_pattern1_event1 = buildEvent(program35_sequence0_pattern1, program35_voice0_track1, 1.0f, 1.0f, "D2", 1.0f);
    program35_sequence0_pattern1_event2 = buildEvent(program35_sequence0_pattern1, program35_voice0_track2, 2.5f, 1.0f, "E4", 0.7f);
    program35_sequence0_pattern1_event3 = buildEvent(program35_sequence0_pattern1, program35_voice0_track3, 3.0f, 1.0f, "c3", 0.5f);

    // List of all parent entities including the library
    // ORDER IS IMPORTANT because this list will be used for real database entities, so ordered from parent -> child
    return List.of(
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
      program5_voiceBass,
      program5_voiceSticky,
      program5_voiceStripe,
      program5_voicePad,
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
   Integration tests use shared scenario fixtures as much as possible https://www.pivotaltracker.com/story/show/165954673
   */
  public Collection<Object> setupFixtureB2() {
    // "Tangy, Chunky to Smooth" macro-program in house library
    program3 = buildProgram(library2, ProgramType.Macro, ProgramState.Published, "Tangy, Chunky to Smooth", "G minor", 120.0f, 0.6f);
    program3_meme0 = buildMeme(program3, "Tangy");
    //
    program3_sequence0 = buildSequence(program3, 0, "Start Chunky", 0.4f, "G minor");
    program3_sequence0_binding0 = buildBinding(program3_sequence0, 0);
    program3_sequence0_binding0_meme0 = buildMeme(program3_sequence0_binding0, "Chunky");
    //
    program3_sequence1 = buildSequence(program3, 0, "Finish Smooth", 0.6f, "C");
    program3_sequence1_binding0 = buildBinding(program3_sequence1, 1);
    program3_sequence1_binding0_meme0 = buildMeme(program3_sequence1_binding0, "Smooth");

    // Main program
    program15 = buildProgram(library2, ProgramType.Main, ProgramState.Published, "Next Jam", "Db minor", 140, 0.6f);
    program15_voiceBass = buildVoice(program5, InstrumentType.Bass, "Bass");
    program15_meme0 = buildMeme(program15, "Hindsight");
    //
    program15_sequence0 = buildSequence(program15, 16, "Intro", 0.5f, "G minor");
    program15_sequence0_chord0 = buildChord(program15_sequence0, 0.0, "G minor");
    program15_sequence0_chord0_voicing = buildVoicing(program15_sequence0_chord0, program15_voiceBass, "G3, Bb3, D4");
    program15_sequence0_chord1 = buildChord(program15_sequence0, 8.0, "Ab minor");
    program15_sequence0_chord1_voicing = buildVoicing(program15_sequence0_chord1, program15_voiceBass, "Ab3, C3, Eb4");
    program15_sequence0_binding0 = buildBinding(program15_sequence0, 0);
    program15_sequence0_binding0_meme0 = buildMeme(program15_sequence0_binding0, "Regret");
    //
    program15_sequence1 = buildSequence(program15, 32, "Outro", 0.5f, "A major");
    program15_sequence1_chord0 = buildChord(program15_sequence1, 0.0, "C major");
    program15_sequence1_chord0_voicing = buildVoicing(program15_sequence0_chord0, program15_voiceBass, "E3, G3, C4");
    program15_sequence1_chord1 = buildChord(program15_sequence1, 8.0, "Bb major");
    program15_sequence1_chord1_voicing = buildVoicing(program15_sequence0_chord1, program15_voiceBass, "F3, Bb3, D4");
    program15_sequence1_binding0 = buildBinding(program15_sequence1, 1);
    program15_sequence1_binding0_meme0 = buildMeme(program15_sequence1_binding0, "Pride");
    program15_sequence1_binding0_meme1 = buildMeme(program15_sequence1_binding0, "Shame");

    // return them all
    return List.of(
      program3,
      program3_meme0,
      program3_sequence0,
      program3_sequence0_binding0,
      program3_sequence0_binding0_meme0,
      program3_sequence1,
      program3_sequence1_binding0,
      program3_sequence1_binding0_meme0,
      program15,
      program15_voiceBass,
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
   Integration tests use shared scenario fixtures as much as possible https://www.pivotaltracker.com/story/show/165954673
   <p>
   memes bound to sequence-pattern because sequence-binding is not considered for beat sequences, beat sequence patterns do not have memes. https://www.pivotaltracker.com/story/show/163158036
   <p>
   Choice is either by sequence-pattern (macro- or main-type sequences) or by sequence (beat- and detail-type sequences) https://www.pivotaltracker.com/story/show/165954619
   <p>
   Artist wants Pattern to have type *Macro* or *Main* (for Macro- or Main-type sequences), or *Intro*, *Loop*, or *Outro* (for Beat or Detail-type Sequence) in order to of a composition that is dynamic when chosen to fill a Segment. https://www.pivotaltracker.com/story/show/153976073
   + For this test, there's an Intro Pattern with all BLEEPS, multiple Loop Patterns with KICK and SNARE (2x each), and an Outro Pattern with all TOOTS.
   <p>
   Artist wants to of multiple Patterns with the same offset in the same Sequence, in order that XJ randomly select one of the patterns at that offset. https://www.pivotaltracker.com/story/show/150279647
   */
  public Collection<Object> setupFixtureB3() {
    // A basic beat
    program9 = buildProgram(library2, ProgramType.Beat, ProgramState.Published, "Basic Beat", "C", 121, 0.6f);
    program9_meme0 = buildMeme(program9, "Basic");
    //
    program9_voice0 = buildVoice(program9, InstrumentType.Drum, "Drums");
    program9_voice0_track0 = buildTrack(program9_voice0, "BLEEP");
    program9_voice0_track1 = buildTrack(program9_voice0, "BLEIP");
    program9_voice0_track2 = buildTrack(program9_voice0, "BLEAP");
    program9_voice0_track3 = buildTrack(program9_voice0, "BLEEEP");
    program9_voice0_track4 = buildTrack(program9_voice0, "CLOCK");
    program9_voice0_track5 = buildTrack(program9_voice0, "SNORT");
    program9_voice0_track6 = buildTrack(program9_voice0, "KICK");
    program9_voice0_track7 = buildTrack(program9_voice0, "SNARL");
    program9_voice0_track8 = buildTrack(program9_voice0, "KIICK");
    program9_voice0_track9 = buildTrack(program9_voice0, "SNARR");
    program9_voice0_track10 = buildTrack(program9_voice0, "KEICK");
    program9_voice0_track11 = buildTrack(program9_voice0, "SNAER");
    program9_voice0_track12 = buildTrack(program9_voice0, "TOOT");
    program9_voice0_track13 = buildTrack(program9_voice0, "TOOOT");
    program9_voice0_track14 = buildTrack(program9_voice0, "TOOTE");
    program9_voice0_track15 = buildTrack(program9_voice0, "TOUT");
    //
    program9_sequence0 = buildSequence(program9, 16, "Base", 0.5f, "C");
    //
    program9_sequence0_pattern0 = buildPattern(program9_sequence0, program9_voice0, 4, "Intro");
    program9_sequence0_pattern0_event0 = buildEvent(program9_sequence0_pattern0, program9_voice0_track0, 0, 1, "C2", 1.0f);
    program9_sequence0_pattern0_event1 = buildEvent(program9_sequence0_pattern0, program9_voice0_track1, 1, 1, "G5", 0.8f);
    program9_sequence0_pattern0_event2 = buildEvent(program9_sequence0_pattern0, program9_voice0_track2, 2.5f, 1, "C2", 0.6f);
    program9_sequence0_pattern0_event3 = buildEvent(program9_sequence0_pattern0, program9_voice0_track3, 3, 1, "G5", 0.9f);
    //
    program9_sequence0_pattern1 = buildPattern(program9_sequence0, program9_voice0, 4, "Loop A");
    program9_sequence0_pattern1_event0 = buildEvent(program9_sequence0_pattern1, program9_voice0_track4, 0, 1, "C2", 1.0f);
    program9_sequence0_pattern1_event1 = buildEvent(program9_sequence0_pattern1, program9_voice0_track5, 1, 1, "G5", 0.8f);
    program9_sequence0_pattern1_event2 = buildEvent(program9_sequence0_pattern1, program9_voice0_track6, 2.5f, 1, "C2", 0.6f);
    program9_sequence0_pattern1_event3 = buildEvent(program9_sequence0_pattern1, program9_voice0_track7, 3, 1, "G5", 0.9f);
    //
    program9_sequence0_pattern2 = buildPattern(program9_sequence0, program9_voice0, 4, "Loop B");
    program9_sequence0_pattern2_event0 = buildEvent(program9_sequence0_pattern2, program9_voice0_track8, 0, 1, "B5", 0.9f);
    program9_sequence0_pattern2_event1 = buildEvent(program9_sequence0_pattern2, program9_voice0_track9, 1, 1, "D2", 1.0f);
    program9_sequence0_pattern2_event2 = buildEvent(program9_sequence0_pattern2, program9_voice0_track10, 2.5f, 1, "E4", 0.7f);
    program9_sequence0_pattern2_event3 = buildEvent(program9_sequence0_pattern2, program9_voice0_track11, 3, 1, "C3", 0.5f);
    //
    program9_sequence0_pattern3 = buildPattern(program9_sequence0, program9_voice0, 4, "Outro");
    program9_sequence0_pattern3_event0 = buildEvent(program9_sequence0_pattern3, program9_voice0_track12, 0, 1, "C2", 1.0f);
    program9_sequence0_pattern3_event1 = buildEvent(program9_sequence0_pattern3, program9_voice0_track13, 1, 1, "G5", 0.8f);
    program9_sequence0_pattern3_event2 = buildEvent(program9_sequence0_pattern3, program9_voice0_track14, 2.5f, 1, "C2", 0.6f);
    program9_sequence0_pattern3_event3 = buildEvent(program9_sequence0_pattern3, program9_voice0_track15, 3, 1, "G5", 0.9f);

    // Instrument "808"
    instrument8 = buildInstrument(library2, InstrumentType.Drum, InstrumentMode.Event, InstrumentState.Published, "808 Drums");
    instrument8.setVolume(0.76f); // For testing: Instrument has overall volume parameter https://www.pivotaltracker.com/story/show/179215413
    instrument8_meme0 = buildMeme(instrument8, "heavy");
    instrument8_audio8kick = buildAudio(instrument8, "Kick", "19801735098q47895897895782138975898.wav", 0.01f, 2.123f, 120.0f, 0.62f, "KICK", "Eb", 1.0f);
    instrument8_audio8snare = buildAudio(instrument8, "Snare", "975898198017350afghjkjhaskjdfjhk.wav", 0.01f, 1.5f, 120.0f, 0.62f, "SNARE", "Ab", 0.8f);
    instrument8_audio8bleep = buildAudio(instrument8, "Bleep", "17350afghjkjhaskjdfjhk9758981980.wav", 0.01f, 1.5f, 120.0f, 0.62f, "BLEEP", "Ab", 0.8f);
    instrument8_audio8toot = buildAudio(instrument8, "Toot", "askjdfjhk975898198017350afghjkjh.wav", 0.01f, 1.5f, 120.0f, 0.62f, "TOOT", "Ab", 0.8f);

    // return them all
    return List.of(
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
   Detail Craft v1 https://www.pivotaltracker.com/story/show/154464276
   */
  public Collection<Object> setupFixtureB4_DetailBass() {
    // A basic bass pattern
    program10 = buildProgram(library2, ProgramType.Detail, ProgramState.Published, "Earth Bass Detail Pattern", "C", 121, 0.6f);
    program10_meme0 = buildMeme(program10, "EARTH");
    //
    program10_voice0 = buildVoice(program10, InstrumentType.Bass, "Dirty Bass");
    program10_voice0_track0 = buildTrack(program10_voice0, "BUM");
    //
    program10_sequence0 = buildSequence(program10, 16, "Simple Walk", 0.5f, "C");
    //
    program10_sequence0_pattern0 = buildPattern(program10_sequence0, program10_voice0, 4, "Intro");
    program10_sequence0_pattern0_event0 = buildEvent(program10_sequence0_pattern0, program10_voice0_track0, 0, 1, "C2", 1.0f);
    program10_sequence0_pattern0_event1 = buildEvent(program10_sequence0_pattern0, program10_voice0_track0, 1, 1, "G5", 0.8f);
    program10_sequence0_pattern0_event2 = buildEvent(program10_sequence0_pattern0, program10_voice0_track0, 2, 1, "C2", 0.6f);
    program10_sequence0_pattern0_event3 = buildEvent(program10_sequence0_pattern0, program10_voice0_track0, 3, 1, "G5", 0.9f);
    //
    program10_sequence0_pattern1 = buildPattern(program10_sequence0, program10_voice0, 4, "Loop A");
    program10_sequence0_pattern1_event0 = buildEvent(program10_sequence0_pattern1, program10_voice0_track0, 0, 1, "C2", 1.0f);
    program10_sequence0_pattern1_event1 = buildEvent(program10_sequence0_pattern1, program10_voice0_track0, 1, 1, "G5", 0.8f);
    program10_sequence0_pattern1_event2 = buildEvent(program10_sequence0_pattern1, program10_voice0_track0, 2, 1, "C2", 0.6f);
    program10_sequence0_pattern1_event3 = buildEvent(program10_sequence0_pattern1, program10_voice0_track0, 3, 1, "G5", 0.9f);
    //
    program10_sequence0_pattern2 = buildPattern(program10_sequence0, program10_voice0, 4, "Loop B");
    program10_sequence0_pattern2_event0 = buildEvent(program10_sequence0_pattern2, program10_voice0_track0, 0, 1, "B5", 0.9f);
    program10_sequence0_pattern2_event1 = buildEvent(program10_sequence0_pattern2, program10_voice0_track0, 1, 1, "D2", 1.0f);
    program10_sequence0_pattern2_event2 = buildEvent(program10_sequence0_pattern2, program10_voice0_track0, 2, 1, "E4", 0.7f);
    program10_sequence0_pattern2_event3 = buildEvent(program10_sequence0_pattern2, program10_voice0_track0, 3, 1, "C3", 0.5f);
    //
    program10_sequence0_pattern3 = buildPattern(program10_sequence0, program10_voice0, 4, "Outro");
    program10_sequence0_pattern3_event0 = buildEvent(program10_sequence0_pattern3, program10_voice0_track0, 0, 1, "C2", 1.0f);
    program10_sequence0_pattern3_event1 = buildEvent(program10_sequence0_pattern3, program10_voice0_track0, 1, 1, "G5", 0.8f);
    program10_sequence0_pattern3_event2 = buildEvent(program10_sequence0_pattern3, program10_voice0_track0, 2, 1, "C2", 0.6f);
    program10_sequence0_pattern3_event3 = buildEvent(program10_sequence0_pattern3, program10_voice0_track0, 3, 1, "G5", 0.9f);

    // Instrument "Bass"
    instrument9 = buildInstrument(library2, InstrumentType.Bass, InstrumentMode.Event, InstrumentState.Published, "Bass");
    instrument9_meme0 = buildMeme(instrument9, "heavy");
    instrument9_audio8 = buildAudio(instrument9, "bass", "19801735098q47895897895782138975898.wav", 0.01f, 2.123f, 120.0f, 0.62f, "BLOOP", "Eb", 1.0f);

    // return them all
    return List.of(
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
    Collection<Object> entities = new ArrayList<>();

    account1 = add(entities, buildAccount("Generated"));
    user1 = add(entities, buildUser("generated", "generated@email.com", "https://pictures.com/generated.gif", "Admin"));
    library1 = add(entities, buildLibrary(account1, "generated"));

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
      Instrument instrument = add(entities, buildInstrument(library1, InstrumentType.Drum, InstrumentMode.Event, InstrumentState.Published, String.format("%s Drums", majorMemeName)));
      add(entities, buildInstrumentMeme(instrument, majorMemeName));
      add(entities, buildInstrumentMeme(instrument, minorMemeName));
      // audios of instrument
      for (int k = 0; k < N; k++)
        add(entities, buildAudio(instrument, StringUtils.toProper(percussiveNames[k]), String.format("%s.wav", StringUtils.toLowerSlug(percussiveNames[k])), random(0, 0.05), random(0.25, 2), random(80, 120), 0.62f, percussiveNames[k], "X", random(0.8, 1)));
      //
      LOG.debug("Generated Drum-type Instrument id={}, minorMeme={}, majorMeme={}", instrument.getId(), minorMemeName, majorMemeName);
    }

    // Generate Perc Loop Instruments
    for (int i = 0; i < N; i++) {
      Instrument instrument = add(entities, buildInstrument(library1, InstrumentType.Percussion, InstrumentMode.Loop, InstrumentState.Published, "Perc Loop"));
      LOG.debug("Generated PercLoop-type Instrument id={}", instrument.getId());
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
      Program program = add(entities, buildProgram(library1, ProgramType.Macro, ProgramState.Published, String.format("%s, create %s to %s", minorMemeName, majorMemeFromName, majorMemeToName), keyFrom, tempoFrom, 0.6f));
      add(entities, buildProgramMeme(program, minorMemeName));
      // of offset 0
      var sequence0 = add(entities, buildSequence(program, 0, String.format("Start %s", majorMemeFromName), densityFrom, keyFrom));
      var binding0 = add(entities, buildProgramSequenceBinding(sequence0, 0));
      add(entities, buildProgramSequenceBindingMeme(binding0, majorMemeFromName));
      // to offset 1
      float densityTo = random(0.3, 0.9);
      var sequence1 = add(entities, buildSequence(program, 0, String.format("Finish %s", majorMemeToName), densityTo, keyTo));
      var binding1 = add(entities, buildProgramSequenceBinding(sequence1, 1));
      add(entities, buildProgramSequenceBindingMeme(binding1, majorMemeToName));
      //
      LOG.debug("Generated Macro-type Program id={}, minorMeme={}, majorMemeFrom={}, majorMemeTo={}", program.getId(), minorMemeName, majorMemeFromName, majorMemeToName);
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
      Program program = add(entities, buildProgram(library1, ProgramType.Main, ProgramState.Published, String.format("%s: %s", majorMemeName, String.join(",", sequenceNames)), subKeys[0], tempo, 0.6f));
      add(entities, buildProgramMeme(program, majorMemeName));
      // sequences of program
      for (int iP = 0; iP < N; iP++) {
        Integer total = random(LoremIpsum.SEQUENCE_TOTALS);
        sequences[iP] = add(entities, buildSequence(program, total, String.format("%s in %s", majorMemeName, sequenceNames[iP]), subDensities[iP], subKeys[iP]));
        for (int iPC = 0; iPC < N << 2; iPC++) {
          // always use first chord, then use more chords with more density
          if (0 == iPC || StrictMath.random() < subDensities[iP]) {
            add(entities, buildChord(sequences[iP], StrictMath.floor((float) iPC * total * 4 / N), random(LoremIpsum.MUSICAL_CHORDS)));
          }
        }
      }
      // sequence sequence binding
      for (int offset = 0; offset < N << 2; offset++) {
        int num = (int) StrictMath.floor(StrictMath.random() * N);
        var binding = add(entities, buildProgramSequenceBinding(sequences[num], offset));
        add(entities, buildMeme(binding, random(minorMemeNames)));
      }
      LOG.debug("Generated Main-type Program id={}, majorMeme={} with {} sequences bound {} times", program.getId(), majorMemeName, N, N << 2);
    }

    // Generate N total Beat-type Sequences, each having N voices, and N*2 patterns comprised of N*8 events
    ProgramVoice[] voices = new ProgramVoice[N];
    Map<String, ProgramVoiceTrack> trackMap = new HashMap<>();
    for (int i = 0; i < N; i++) {
      String majorMemeName = majorMemeNames[i];
      float tempo = random(80, 120);
      String key = random(LoremIpsum.MUSICAL_KEYS);
      float density = random(0.4, 0.9);
      //
      Program program = add(entities, buildProgram(library1, ProgramType.Beat, ProgramState.Published, String.format("%s Beat", majorMemeName), key, tempo, 0.6f));
      trackMap.clear();
      add(entities, buildProgramMeme(program, majorMemeName));
      // voices of program
      for (int iV = 0; iV < N; iV++) {
        voices[iV] = add(entities, buildVoice(program, InstrumentType.Drum, String.format("%s %s", majorMemeName, percussiveNames[iV])));
      }
      var sequenceBase = add(entities, buildSequence(program, random(LoremIpsum.SEQUENCE_TOTALS), "Base", density, key));
      // patterns of program
      for (int iP = 0; iP < N << 1; iP++) {
        Integer total = random(LoremIpsum.PATTERN_TOTALS);
        int num = (int) StrictMath.floor(StrictMath.random() * N);

        // first pattern is always a Loop (because that's required) then the rest at random
        var pattern = add(entities, buildPattern(sequenceBase, voices[num], total, String.format("%s %s %s", majorMemeName, majorMemeName + " pattern", random(LoremIpsum.ELEMENTS))));
        for (int iPE = 0; iPE < N << 2; iPE++) {
          // always use first chord, then use more chords with more density
          if (0 == iPE || StrictMath.random() < density) {
            String name = percussiveNames[num];
            if (!trackMap.containsKey(name))
              trackMap.put(name, add(entities, buildTrack(voices[num], name)));
            add(entities, buildEvent(pattern, trackMap.get(name), (float) StrictMath.floor((double) iPE * total * 4 / N), random(0.25, 1.0), "X", random(0.4, 0.9)));
          }
        }
      }
      LOG.debug("Generated Beat-type Program id={}, majorMeme={} with {} patterns", program.getId(), majorMemeName, N);
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
  <N> N add(Collection<Object> to, N entity) {
    to.add(entity);
    return entity;
  }

}
