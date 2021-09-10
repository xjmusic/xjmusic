//  Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.xj.api.Account;
import io.xj.api.AccountUser;
import io.xj.api.Chain;
import io.xj.api.ChainState;
import io.xj.api.ContentBindingType;
import io.xj.api.Instrument;
import io.xj.api.InstrumentAudio;
import io.xj.api.InstrumentMeme;
import io.xj.api.InstrumentState;
import io.xj.api.InstrumentType;
import io.xj.api.Library;
import io.xj.api.Program;
import io.xj.api.ProgramMeme;
import io.xj.api.ProgramSequence;
import io.xj.api.ProgramSequenceBinding;
import io.xj.api.ProgramSequenceBindingMeme;
import io.xj.api.ProgramSequenceChord;
import io.xj.api.ProgramSequenceChordVoicing;
import io.xj.api.ProgramSequencePattern;
import io.xj.api.ProgramSequencePatternEvent;
import io.xj.api.ProgramSequencePatternType;
import io.xj.api.ProgramState;
import io.xj.api.ProgramType;
import io.xj.api.ProgramVoice;
import io.xj.api.ProgramVoiceTrack;
import io.xj.api.Segment;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentChoiceArrangement;
import io.xj.api.SegmentChoiceArrangementPick;
import io.xj.api.SegmentChord;
import io.xj.api.SegmentChordVoicing;
import io.xj.api.SegmentMeme;
import io.xj.api.SegmentState;
import io.xj.api.SegmentType;
import io.xj.api.Template;
import io.xj.api.TemplateBinding;
import io.xj.api.TemplateType;
import io.xj.api.User;
import io.xj.api.UserAuth;
import io.xj.api.UserRole;
import io.xj.api.UserRoleType;
import io.xj.hub.LoremIpsum;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.common.Users;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import io.xj.lib.util.Value;
import io.xj.nexus.dao.Segments;
import io.xj.nexus.hub_client.client.HubClientAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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
  public UserRole userRole2a;
  public UserRole userRole3a;

  /**
   Random type of rhythm pattern

   @return randomly selected rhythm pattern type
   */
  protected static ProgramSequencePatternType randomRhythmPatternType() {
    return new ProgramSequencePatternType[]{
      ProgramSequencePatternType.INTRO,
      ProgramSequencePatternType.LOOP,
      ProgramSequencePatternType.OUTRO
    }[(int) StrictMath.floor(StrictMath.random() * 3)];
  }

  /**
   List of N random values

   @param N number of values
   @return list of values
   */
  protected static Double[] listOfRandomValues(int N) {
    Double[] result = new Double[N];
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
  protected static Double random(double A, double B) {
    return A + StrictMath.random() * (B - A);
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

  public static SegmentChoice buildSegmentChoice(Segment segment, ProgramType programType, ProgramSequenceBinding programSequenceBinding) {
    return new SegmentChoice()
      .id(UUID.randomUUID())
      .segmentId(segment.getId())
      .deltaIn(Segments.DELTA_UNLIMITED)
      .deltaOut(Segments.DELTA_UNLIMITED)
      .programId(programSequenceBinding.getProgramId())
      .programSequenceBindingId(programSequenceBinding.getId())
      .programType(programType);
  }

  public static SegmentChoice buildSegmentChoice(Segment segment, Program program) {
    return new SegmentChoice()
      .id(UUID.randomUUID())
      .segmentId(segment.getId())
      .deltaIn(Segments.DELTA_UNLIMITED)
      .deltaOut(Segments.DELTA_UNLIMITED)
      .programId(program.getId())
      .programType(program.getType());
  }

  public static SegmentChoice buildSegmentChoice(Segment segment, Program program, ProgramSequence programSequence, ProgramVoice voice, Instrument instrument) {
    return new SegmentChoice()
      .id(UUID.randomUUID())
      .programVoiceId(voice.getId())
      .instrumentId(instrument.getId())
      .instrumentType(instrument.getType())
      .deltaIn(Segments.DELTA_UNLIMITED)
      .deltaOut(Segments.DELTA_UNLIMITED)
      .segmentId(segment.getId())
      .programId(program.getId())
      .programSequenceId(programSequence.getId())
      .programType(program.getType());
  }

  public static SegmentMeme buildMeme(Segment segment, String name) {
    return new SegmentMeme()
      .id(UUID.randomUUID())
      .segmentId(segment.getId())
      .name(name);
  }

  public static SegmentChord buildChord(Segment segment, Double position, String name) {
    return new SegmentChord()
      .id(UUID.randomUUID())
      .segmentId(segment.getId())
      .position(position)
      .name(name);
  }

  public static SegmentChordVoicing buildVoicing(SegmentChord chord, InstrumentType type, String notes) {
    return new SegmentChordVoicing()
      .id(UUID.randomUUID())
      .segmentId(chord.getSegmentId())
      .segmentChordId(chord.getId())
      .type(type)
      .notes(notes);
  }

  public static SegmentChoiceArrangement buildArrangement(SegmentChoice segmentChoice) {
    return new SegmentChoiceArrangement()
      .id(UUID.randomUUID())
      .segmentId(segmentChoice.getSegmentId())
      .segmentChoiceId(segmentChoice.getId());
  }

  public static SegmentChoiceArrangementPick buildPick(SegmentChoiceArrangement segmentChoiceArrangement, ProgramSequencePatternEvent programSequencePatternEvent, InstrumentAudio instrumentAudio, double position, double duration, double velocity, String note, String name) {
    return new SegmentChoiceArrangementPick()
      .id(UUID.randomUUID())
      .segmentId(segmentChoiceArrangement.getSegmentId())
      .segmentChoiceArrangementId(segmentChoiceArrangement.getId())
      .programSequencePatternEventId(programSequencePatternEvent.getId())
      .instrumentAudioId(instrumentAudio.getId())
      .start(position)
      .length(duration)
      .amplitude(velocity)
      .note(note)
      .name(name);
  }

  public static Collection<Object> buildInstrumentWithAudios(Instrument instrument, String notes) {
    List<Object> result = Lists.newArrayList(instrument);
    for (String note : CSV.split(notes)) {
      var audio = buildAudio(instrument, String.format("%s-%s", instrument.getType().name(), note), note);
      result.add(audio);
    }
    return result;
  }

  public static InstrumentAudio buildAudio(Instrument instrument, String name, String waveformKey, double start, double length, double tempo, double density, String event, String note, double volume) {
    return new InstrumentAudio()
      .id(UUID.randomUUID())
      .instrumentId(instrument.getId())
      .name(name)
      .waveformKey(waveformKey)
      .start(start)
      .length(length)
      .tempo(tempo)
      .density(density)
      .volume(volume)
      .note(note)
      .event(event);
  }

  public static InstrumentAudio buildAudio(Instrument instrument, String name, String note) {
    return new InstrumentAudio()
      .id(UUID.randomUUID())
      .instrumentId(instrument.getId())
      .name(name)
      .waveformKey("test123")
      .start(0.0)
      .length(1.0)
      .tempo(120.0)
      .density(1.0)
      .volume(1.0)
      .event("X")
      .note(note);
  }

  public static User buildUser(String name, String email, String avatarUrl) {
    return new User()
      .id(UUID.randomUUID())
      .email(email)
      .avatarUrl(avatarUrl)
      .name(name);
  }

  public static Library buildLibrary(Account account, String name) {
    return new Library()
      .id(UUID.randomUUID())
      .accountId(account.getId())
      .name(name);
  }

  public static Account buildAccount() {
    return new Account()
      .id(UUID.randomUUID());
  }

  public static Account buildAccount(String name) {
    return buildAccount()
      .name(name);
  }

  public static UserRole buildUserRole(User user, UserRoleType type) {
    return new UserRole()
      .id(UUID.randomUUID())
      .userId(user.getId())
      .type(type);
  }

  public static AccountUser buildAccountUser(Account account, User user) {
    return new AccountUser()
      .id(UUID.randomUUID())
      .accountId(account.getId())
      .userId(user.getId());
  }

  public static Program buildProgram(Library library, ProgramType type, ProgramState state, String name, String key, double tempo, double density) {
    return new Program()
      .id(UUID.randomUUID())
      .libraryId(library.getId())
      .type(type)
      .state(state)
      .name(name)
      .key(key)
      .tempo(tempo)
      .density(density);
  }

  public static Program buildProgram(ProgramType type, String key, double tempo, double density) {
    return new Program()
      .id(UUID.randomUUID())
      .libraryId(UUID.randomUUID())
      .type(type)
      .state(ProgramState.PUBLISHED)
      .name(String.format("Test %s-Program", type.name()))
      .key(key)
      .tempo(tempo)
      .density(density);
  }

  public static Program buildDetailProgram(String key, Boolean doPatternRestartOnChord, String name) {
    return new Program()
      .id(UUID.randomUUID())
      .libraryId(UUID.randomUUID())
      .type(ProgramType.DETAIL)
      .state(ProgramState.PUBLISHED)
      .name(name)
      .key(key)
      .config(String.format("doPatternRestartOnChord=%b", doPatternRestartOnChord));
  }

  public static ProgramMeme buildMeme(Program program, String name) {
    return new ProgramMeme()
      .id(UUID.randomUUID())
      .programId(program.getId())
      .name(name);
  }

  public static ProgramSequence buildSequence(Program program, int total, String name, double density, String key, double tempo) {
    return new ProgramSequence()
      .id(UUID.randomUUID())
      .programId(program.getId())
      .total(total)
      .name(name)
      .key(key)
      .tempo(tempo)
      .density(density);
  }

  public static ProgramSequence buildSequence(Program program, int total) {
    return new ProgramSequence()
      .id(UUID.randomUUID())
      .programId(program.getId())
      .total(total)
      .name(String.format("Test %d-beat Sequence", total));
  }

  public static ProgramSequenceBinding buildBinding(ProgramSequence programSequence, int offset) {
    return new ProgramSequenceBinding()
      .id(UUID.randomUUID())
      .programId(programSequence.getProgramId())
      .programSequenceId(programSequence.getId())
      .offset(offset);
  }

  public static ProgramSequenceBindingMeme buildMeme(ProgramSequenceBinding programSequenceBinding, String name) {
    return new ProgramSequenceBindingMeme()
      .id(UUID.randomUUID())
      .programId(programSequenceBinding.getProgramId())
      .programSequenceBindingId(programSequenceBinding.getId())
      .name(name);
  }

  public static ProgramSequenceChord buildChord(ProgramSequence programSequence, double position, String name) {
    return new ProgramSequenceChord()
      .id(UUID.randomUUID())
      .programSequenceId(programSequence.getId())
      .programId(programSequence.getProgramId())
      .position(position)
      .name(name);
  }

  public static ProgramSequenceChordVoicing buildVoicing(InstrumentType type, ProgramSequenceChord programSequenceChord, String notes) {
    return new ProgramSequenceChordVoicing()
      .id(UUID.randomUUID())
      .programId(programSequenceChord.getProgramId())
      .programSequenceChordId(programSequenceChord.getId())
      .type(type)
      .notes(notes);
  }

  public static ProgramVoice buildVoice(Program program, InstrumentType type, String name) {
    return new ProgramVoice()
      .id(UUID.randomUUID())
      .programId(program.getId())
      .type(type)
      .name(name);
  }

  public static ProgramVoice buildVoice(Program program, InstrumentType type) {
    return buildVoice(program, type, type.name());
  }

  public static ProgramVoiceTrack buildTrack(ProgramVoice programVoice, String name) {
    return new ProgramVoiceTrack()
      .id(UUID.randomUUID())
      .programId(programVoice.getProgramId())
      .programVoiceId(programVoice.getId())
      .name(name);
  }

  public static ProgramVoiceTrack buildTrack(ProgramVoice programVoice) {
    return buildTrack(programVoice, programVoice.getType().name());
  }

  public static ProgramSequencePattern buildPattern(ProgramSequence programSequence, ProgramVoice programVoice, ProgramSequencePatternType type, int total, String name) {
    return new ProgramSequencePattern()
      .id(UUID.randomUUID())
      .programId(programSequence.getProgramId())
      .programSequenceId(programSequence.getId())
      .programVoiceId(programVoice.getId())
      .type(type)
      .total(total)
      .name(name);
  }

  public static ProgramSequencePattern buildPattern(ProgramSequence sequence, ProgramVoice voice, ProgramSequencePatternType type, int total) {
    return buildPattern(sequence, voice, type, total, type.name());
  }

  public static ProgramSequencePatternEvent buildEvent(ProgramSequencePattern pattern, ProgramVoiceTrack track, double position, double duration, String note, double velocity) {
    return new ProgramSequencePatternEvent()
      .id(UUID.randomUUID())
      .programId(pattern.getProgramId())
      .programSequencePatternId(pattern.getId())
      .programVoiceTrackId(track.getId())
      .position(position)
      .duration(duration)
      .note(note)
      .velocity(velocity);
  }

  public static ProgramSequencePatternEvent buildEvent(ProgramSequencePattern pattern, ProgramVoiceTrack track, double position, double duration, String note) {
    return buildEvent(pattern, track, position, duration, note, 1.0);
  }

  public static Instrument buildInstrument(Library library, InstrumentType type, InstrumentState state, String name) {
    return new Instrument()
      .id(UUID.randomUUID())
      .libraryId(library.getId())
      .type(type)
      .state(state)
      .name(name);
  }

  public static Instrument buildInstrument(InstrumentType type, Boolean isTonal, Boolean isMultiphonic) {
    return new Instrument()
      .id(UUID.randomUUID())
      .libraryId(UUID.randomUUID())
      .type(type)
      .state(InstrumentState.PUBLISHED)
      .config(String.format("isTonal=%b\nisMultiphonic=%b", isTonal, isMultiphonic))
      .name(String.format("Test %s-Instrument", type.name()));
  }

  public static InstrumentMeme buildMeme(Instrument instrument, String name) {
    return new InstrumentMeme()
      .id(UUID.randomUUID())
      .instrumentId(instrument.getId())
      .name(name);
  }

  public static Chain buildChain(Template template) {
    return new Chain()
      .id(UUID.randomUUID())
      .accountId(UUID.randomUUID())
      .templateId(template.getId())
      .name("Test Chain")
      .type(TemplateType.PRODUCTION)
      .state(ChainState.FABRICATE)
      .startAt(Value.formatIso8601UTC(Instant.now()));
  }

  public static Chain buildChain(Account account, String name, TemplateType type, ChainState state, Template template, Instant startAt, @Nullable Instant stopAt, @Nullable String embedKey) {
    Chain builder = new Chain()
      .id(UUID.randomUUID())
      .templateId(template.getId())
      .accountId(account.getId())
      .name(name)
      .type(type)
      .state(state)
      .startAt(Value.formatIso8601UTC(startAt));
    if (Objects.nonNull(stopAt))
      builder.stopAt(Value.formatIso8601UTC(stopAt));
    if (Objects.nonNull(embedKey))
      builder.embedKey(embedKey);
    return builder;
  }

  public static TemplateBinding buildBinding(Template template, Program program) {
    return new TemplateBinding()
      .id(UUID.randomUUID())
      .templateId(template.getId())
      .targetId(program.getId())
      .type(ContentBindingType.PROGRAM);
  }

  public static TemplateBinding buildBinding(Template template, Instrument instrument) {
    return new TemplateBinding()
      .id(UUID.randomUUID())
      .templateId(template.getId())
      .targetId(instrument.getId())
      .type(ContentBindingType.INSTRUMENT);
  }

  public static TemplateBinding buildBinding(Template template, Library library) {
    return new TemplateBinding()
      .id(UUID.randomUUID())
      .templateId(template.getId())
      .targetId(library.getId())
      .type(ContentBindingType.LIBRARY);
  }

  public static Segment buildSegment(Chain chain, int offset, SegmentState state, Instant beginAt, @Nullable Instant endAt, String key, int total, double density, double tempo, String storageKey, String outputEncoder) {
    Segment builder = new Segment()
      .id(UUID.randomUUID())
      .chainId(chain.getId())
      .type(0 < offset ? SegmentType.CONTINUE : SegmentType.INITIAL)
      .outputEncoder(outputEncoder)
      .offset((long) offset)
      .delta(0)
      .state(state)
      .beginAt(Value.formatIso8601UTC(beginAt))
      .key(key)
      .total(total)
      .density(density)
      .tempo(tempo)
      .storageKey(storageKey);

    if (Objects.nonNull(endAt))
      builder.endAt(Value.formatIso8601UTC(endAt));

    return builder;
  }

  public static Segment buildSegment(Chain chain, String key, int total, double density, double tempo) {
    return buildSegment(
      chain,
      0,
      SegmentState.CRAFTING,
      Instant.parse(chain.getStartAt()),
      Instant.parse(chain.getStartAt()).plusNanos((long) (NANOS_PER_SECOND * total * (60 / tempo))), key, total, density, tempo, "segment123", "wav");
  }

  public static Segment buildSegment(Chain chain, int offset, Instant beginAt, String key, int total, double density, double tempo) {
    return buildSegment(
      chain,
      offset,
      SegmentState.CRAFTING,
      beginAt,
      beginAt.plusNanos((long) (NANOS_PER_SECOND * total * (60 / tempo))), key, total, density, tempo, "segment123", "wav");
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
    account1 = buildAccount("bananas");

    // Library "house"
    library2 = buildLibrary(account1, "house");

    // Template Binding to library 2
    template1 = buildTemplate(account1, "Test Template 1", "test1");
    templateBinding1 = buildTemplateBinding(template1, library2);

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    user2 = buildUser("john", "john@email.com", "http://pictures.com/john.gif");
    userRole2a = buildUserRole(user2, UserRoleType.ADMIN);

    // Jenny has a "user" role and belongs to account "bananas"
    user3 = buildUser("jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    userRole3a = buildUserRole(user3, UserRoleType.USER);
    accountUser1a = buildAccountUser(account1, user3);

    // "Tropical, Wild to Cozy" macro-program in house library
    program4 = buildProgram(library2, ProgramType.MACRO, ProgramState.PUBLISHED, "Tropical, Wild to Cozy", "C", 120.0, 0.6);
    program4_meme0 = buildMeme(program4, "Tropical");
    //
    program4_sequence0 = buildSequence(program4, 0, "Start Wild", 0.6, "C", 125.0);
    program4_sequence0_binding0 = buildBinding(program4_sequence0, 0);
    program4_sequence0_binding0_meme0 = buildMeme(program4_sequence0_binding0, "Wild");
    //
    program4_sequence1 = buildSequence(program4, 0, "Intermediate", 0.4, "Bb minor", 115.0);
    program4_sequence1_binding0 = buildBinding(program4_sequence1, 1);
    program4_sequence1_binding0_meme0 = buildMeme(program4_sequence1_binding0, "Cozy");
    program4_sequence1_binding0_meme1 = buildMeme(program4_sequence1_binding0, "Wild");
    //
    program4_sequence2 = buildSequence(program4, 0, "Finish Cozy", 0.4, "Ab minor", 125.0);
    program4_sequence2_binding0 = buildBinding(program4_sequence2, 2);
    program4_sequence2_binding0_meme0 = buildMeme(program4_sequence2_binding0, "Cozy");

    // Main program
    program5 = buildProgram(library2, ProgramType.MAIN, ProgramState.PUBLISHED, "Main Jam", "C minor", 140, 0.6);
    program5_meme0 = buildMeme(program5, "Outlook");
    //
    program5_sequence0 = buildSequence(program5, 16, "Intro", 0.5, "G major", 135.0);
    program5_sequence0_chord0 = buildChord(program5_sequence0, 0.0, "G major");

    program5_sequence0_chord0_voicing = buildVoicing(InstrumentType.BASS, program5_sequence0_chord0, "G3, B3, D4");
    program5_sequence0_chord1 = buildChord(program5_sequence0, 8.0, "Ab minor");

    program5_sequence0_chord1_voicing = buildVoicing(InstrumentType.BASS, program5_sequence0_chord1, "Ab3, Db3, F4");
    program5_sequence0_chord2 = buildChord(program5_sequence0, 75.0, "G-9"); // [#154090557] this ChordEntity should be ignored, because it's past the end of the main-pattern total

    program5_sequence0_chord2_voicing = buildVoicing(InstrumentType.BASS, program5_sequence0_chord2, "G3, Bb3, D4, A4");
    program5_sequence0_binding0 = buildBinding(program5_sequence0, 0);
    program5_sequence0_binding0_meme0 = buildMeme(program5_sequence0_binding0, "Optimism");
    //
    program5_sequence1 = buildSequence(program5, 32, "Drop", 0.5, "G minor", 135.0);
    program5_sequence1_chord0 = buildChord(program5_sequence1, 0.0, "C major");

    program5_sequence1_chord0_voicing = buildVoicing(InstrumentType.BASS, program5_sequence1_chord0, "Ab3, Db3, F4");
    program5_sequence1_chord1 = buildChord(program5_sequence1, 8.0, "Bb minor");

    program5_sequence1_chord1_voicing = buildVoicing(InstrumentType.BASS, program5_sequence1_chord1, "Ab3, Db3, F4");
    program5_sequence1_binding0 = buildBinding(program5_sequence1, 1);
    program5_sequence1_binding0_meme0 = buildMeme(program5_sequence1_binding0, "Pessimism");

    // A basic beat
    program35 = buildProgram(library2, ProgramType.RHYTHM, ProgramState.PUBLISHED, "Basic Beat", "C", 121, 0.6);
    program35_meme0 = buildMeme(program35, "Basic");
    program35_voice0 = buildVoice(program35, InstrumentType.DRUM, "Drums");
    program35_voice0_track0 = buildTrack(program35_voice0, "CLOCK");
    program35_voice0_track1 = buildTrack(program35_voice0, "SNORT");
    program35_voice0_track2 = buildTrack(program35_voice0, "KICK");
    program35_voice0_track3 = buildTrack(program35_voice0, "SNARL");
    //
    program35_sequence0 = buildSequence(program35, 16, "Base", 0.5, "C", 110.3);
    program35_sequence0_pattern0 = buildPattern(program35_sequence0, program35_voice0, ProgramSequencePatternType.LOOP, 4, "Drop");
    program35_sequence0_pattern0_event0 = buildEvent(program35_sequence0_pattern0, program35_voice0_track0, 0.0, 1.0, "C2", 1.0);
    program35_sequence0_pattern0_event1 = buildEvent(program35_sequence0_pattern0, program35_voice0_track1, 1.0, 1.0, "G5", 0.8);
    program35_sequence0_pattern0_event2 = buildEvent(program35_sequence0_pattern0, program35_voice0_track2, 2.5, 1.0, "C2", 0.6);
    program35_sequence0_pattern0_event3 = buildEvent(program35_sequence0_pattern0, program35_voice0_track3, 3.0, 1.0, "G5", 0.9);
    //
    program35_sequence0_pattern1 = buildPattern(program35_sequence0, program35_voice0, ProgramSequencePatternType.LOOP, 4, "Drop Alt");
    program35_sequence0_pattern1_event0 = buildEvent(program35_sequence0_pattern1, program35_voice0_track0, 0.0, 1.0, "B5", 0.9);
    program35_sequence0_pattern1_event1 = buildEvent(program35_sequence0_pattern1, program35_voice0_track1, 1.0, 1.0, "D2", 1.0);
    program35_sequence0_pattern1_event2 = buildEvent(program35_sequence0_pattern1, program35_voice0_track2, 2.5, 1.0, "E4", 0.7);
    program35_sequence0_pattern1_event3 = buildEvent(program35_sequence0_pattern1, program35_voice0_track3, 3.0, 1.0, "c3", 0.5);

    // List of all parent entities including the library
    // ORDER IS IMPORTANT because this list will be used for real database entities, so ordered from parent -> child
    return ImmutableList.of(
      account1,
      library2,
      user2,
      userRole2a,
      user3,
      userRole3a,
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
    program3 = buildProgram(library2, ProgramType.MACRO, ProgramState.PUBLISHED, "Tangy, Chunky to Smooth", "G minor", 120.0, 0.6);
    program3_meme0 = buildMeme(program3, "Tangy");
    //
    program3_sequence0 = buildSequence(program3, 0, "Start Chunky", 0.4, "G minor", 115.0);
    program3_sequence0_binding0 = buildBinding(program3_sequence0, 0);
    program3_sequence0_binding0_meme0 = buildMeme(program3_sequence0_binding0, "Chunky");
    //
    program3_sequence1 = buildSequence(program3, 0, "Finish Smooth", 0.6, "C", 125.0);
    program3_sequence1_binding0 = buildBinding(program3_sequence1, 1);
    program3_sequence1_binding0_meme0 = buildMeme(program3_sequence1_binding0, "Smooth");

    // Main program
    program15 = buildProgram(library2, ProgramType.MAIN, ProgramState.PUBLISHED, "Next Jam", "Db minor", 140, 0.6);
    program15_meme0 = buildMeme(program15, "Hindsight");
    //
    program15_sequence0 = buildSequence(program15, 16, "Intro", 0.5, "G minor", 135.0);
    program15_sequence0_chord0 = buildChord(program15_sequence0, 0.0, "G minor");
    program15_sequence0_chord0_voicing = buildVoicing(InstrumentType.BASS, program15_sequence0_chord0, "G3, Bb3, D4");
    program15_sequence0_chord1 = buildChord(program15_sequence0, 8.0, "Ab minor");
    program15_sequence0_chord1_voicing = buildVoicing(InstrumentType.BASS, program15_sequence0_chord1, "Ab3, C3, Eb4");
    program15_sequence0_binding0 = buildBinding(program15_sequence0, 0);
    program15_sequence0_binding0_meme0 = buildMeme(program15_sequence0_binding0, "Regret");
    //
    program15_sequence1 = buildSequence(program15, 32, "Outro", 0.5, "A major", 135.0);
    program15_sequence1_chord0 = buildChord(program15_sequence1, 0.0, "C major");
    program15_sequence1_chord0_voicing = buildVoicing(InstrumentType.BASS, program15_sequence0_chord0, "E3, G3, C4");
    program15_sequence1_chord1 = buildChord(program15_sequence1, 8.0, "Bb major");
    program15_sequence1_chord1_voicing = buildVoicing(InstrumentType.BASS, program15_sequence0_chord1, "F3, Bb3, D4");
    program15_sequence1_binding0 = buildBinding(program15_sequence1, 1);
    program15_sequence1_binding0_meme0 = buildMeme(program15_sequence1_binding0, "Pride");
    program15_sequence1_binding0_meme1 = buildMeme(program15_sequence1_binding0, "Shame");

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
    program9 = buildProgram(library2, ProgramType.RHYTHM, ProgramState.PUBLISHED, "Basic Beat", "C", 121, 0.6);
    program9_meme0 = buildMeme(program9, "Basic");
    //
    program9_voice0 = buildVoice(program9, InstrumentType.DRUM, "Drums");
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
    program9_sequence0 = buildSequence(program9, 16, "Base", 0.5, "C", 110.3);
    //
    program9_sequence0_pattern0 = buildPattern(program9_sequence0, program9_voice0, ProgramSequencePatternType.INTRO, 4, "Intro");
    program9_sequence0_pattern0_event0 = buildEvent(program9_sequence0_pattern0, program9_voice0_track0, 0, 1, "C2", 1.0);
    program9_sequence0_pattern0_event1 = buildEvent(program9_sequence0_pattern0, program9_voice0_track1, 1, 1, "G5", 0.8);
    program9_sequence0_pattern0_event2 = buildEvent(program9_sequence0_pattern0, program9_voice0_track2, 2.5, 1, "C2", 0.6);
    program9_sequence0_pattern0_event3 = buildEvent(program9_sequence0_pattern0, program9_voice0_track3, 3, 1, "G5", 0.9);
    //
    program9_sequence0_pattern1 = buildPattern(program9_sequence0, program9_voice0, ProgramSequencePatternType.LOOP, 4, "Loop A");
    program9_sequence0_pattern1_event0 = buildEvent(program9_sequence0_pattern1, program9_voice0_track4, 0, 1, "C2", 1.0);
    program9_sequence0_pattern1_event1 = buildEvent(program9_sequence0_pattern1, program9_voice0_track5, 1, 1, "G5", 0.8);
    program9_sequence0_pattern1_event2 = buildEvent(program9_sequence0_pattern1, program9_voice0_track6, 2.5, 1, "C2", 0.6);
    program9_sequence0_pattern1_event3 = buildEvent(program9_sequence0_pattern1, program9_voice0_track7, 3, 1, "G5", 0.9);
    //
    program9_sequence0_pattern2 = buildPattern(program9_sequence0, program9_voice0, ProgramSequencePatternType.LOOP, 4, "Loop B");
    program9_sequence0_pattern2_event0 = buildEvent(program9_sequence0_pattern2, program9_voice0_track8, 0, 1, "B5", 0.9);
    program9_sequence0_pattern2_event1 = buildEvent(program9_sequence0_pattern2, program9_voice0_track9, 1, 1, "D2", 1.0);
    program9_sequence0_pattern2_event2 = buildEvent(program9_sequence0_pattern2, program9_voice0_track10, 2.5, 1, "E4", 0.7);
    program9_sequence0_pattern2_event3 = buildEvent(program9_sequence0_pattern2, program9_voice0_track11, 3, 1, "C3", 0.5);
    //
    program9_sequence0_pattern3 = buildPattern(program9_sequence0, program9_voice0, ProgramSequencePatternType.OUTRO, 4, "Outro");
    program9_sequence0_pattern3_event0 = buildEvent(program9_sequence0_pattern3, program9_voice0_track12, 0, 1, "C2", 1.0);
    program9_sequence0_pattern3_event1 = buildEvent(program9_sequence0_pattern3, program9_voice0_track13, 1, 1, "G5", 0.8);
    program9_sequence0_pattern3_event2 = buildEvent(program9_sequence0_pattern3, program9_voice0_track14, 2.5, 1, "C2", 0.6);
    program9_sequence0_pattern3_event3 = buildEvent(program9_sequence0_pattern3, program9_voice0_track15, 3, 1, "G5", 0.9);

    // Instrument "808"
    instrument8 = buildInstrument(library2, InstrumentType.DRUM, InstrumentState.PUBLISHED, "808 Drums");
    instrument8_meme0 = buildMeme(instrument8, "heavy");
    instrument8_audio8kick = buildAudio(instrument8, "Kick", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 0.62, "KICK", "Eb", 1.0);
    instrument8_audio8snare = buildAudio(instrument8, "Snare", "975898198017350afghjkjhaskjdfjhk.wav", 0.01, 1.5, 120.0, 0.62, "SNARE", "Ab", 0.8);
    instrument8_audio8bleep = buildAudio(instrument8, "Bleep", "17350afghjkjhaskjdfjhk9758981980.wav", 0.01, 1.5, 120.0, 0.62, "BLEEP", "Ab", 0.8);
    instrument8_audio8toot = buildAudio(instrument8, "Toot", "askjdfjhk975898198017350afghjkjh.wav", 0.01, 1.5, 120.0, 0.62, "TOOT", "Ab", 0.8);

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
    program10 = buildProgram(library2, ProgramType.DETAIL, ProgramState.PUBLISHED, "Earth Bass Detail Pattern", "C", 121, 0.6);
    program10_meme0 = buildMeme(program10, "EARTH");
    //
    program10_voice0 = buildVoice(program10, InstrumentType.BASS, "Dirty Bass");
    program10_voice0_track0 = buildTrack(program10_voice0, "BUM");
    //
    program10_sequence0 = buildSequence(program10, 16, "Simple Walk", 0.5, "C", 110.3);
    //
    program10_sequence0_pattern0 = buildPattern(program10_sequence0, program10_voice0, ProgramSequencePatternType.INTRO, 4, "Intro");
    program10_sequence0_pattern0_event0 = buildEvent(program10_sequence0_pattern0, program10_voice0_track0, 0, 1, "C2", 1.0);
    program10_sequence0_pattern0_event1 = buildEvent(program10_sequence0_pattern0, program10_voice0_track0, 1, 1, "G5", 0.8);
    program10_sequence0_pattern0_event2 = buildEvent(program10_sequence0_pattern0, program10_voice0_track0, 2, 1, "C2", 0.6);
    program10_sequence0_pattern0_event3 = buildEvent(program10_sequence0_pattern0, program10_voice0_track0, 3, 1, "G5", 0.9);
    //
    program10_sequence0_pattern1 = buildPattern(program10_sequence0, program10_voice0, ProgramSequencePatternType.LOOP, 4, "Loop A");
    program10_sequence0_pattern1_event0 = buildEvent(program10_sequence0_pattern1, program10_voice0_track0, 0, 1, "C2", 1.0);
    program10_sequence0_pattern1_event1 = buildEvent(program10_sequence0_pattern1, program10_voice0_track0, 1, 1, "G5", 0.8);
    program10_sequence0_pattern1_event2 = buildEvent(program10_sequence0_pattern1, program10_voice0_track0, 2, 1, "C2", 0.6);
    program10_sequence0_pattern1_event3 = buildEvent(program10_sequence0_pattern1, program10_voice0_track0, 3, 1, "G5", 0.9);
    //
    program10_sequence0_pattern2 = buildPattern(program10_sequence0, program10_voice0, ProgramSequencePatternType.LOOP, 4, "Loop B");
    program10_sequence0_pattern2_event0 = buildEvent(program10_sequence0_pattern2, program10_voice0_track0, 0, 1, "B5", 0.9);
    program10_sequence0_pattern2_event1 = buildEvent(program10_sequence0_pattern2, program10_voice0_track0, 1, 1, "D2", 1.0);
    program10_sequence0_pattern2_event2 = buildEvent(program10_sequence0_pattern2, program10_voice0_track0, 2, 1, "E4", 0.7);
    program10_sequence0_pattern2_event3 = buildEvent(program10_sequence0_pattern2, program10_voice0_track0, 3, 1, "C3", 0.5);
    //
    program10_sequence0_pattern3 = buildPattern(program10_sequence0, program10_voice0, ProgramSequencePatternType.OUTRO, 4, "Outro");
    program10_sequence0_pattern3_event0 = buildEvent(program10_sequence0_pattern3, program10_voice0_track0, 0, 1, "C2", 1.0);
    program10_sequence0_pattern3_event1 = buildEvent(program10_sequence0_pattern3, program10_voice0_track0, 1, 1, "G5", 0.8);
    program10_sequence0_pattern3_event2 = buildEvent(program10_sequence0_pattern3, program10_voice0_track0, 2, 1, "C2", 0.6);
    program10_sequence0_pattern3_event3 = buildEvent(program10_sequence0_pattern3, program10_voice0_track0, 3, 1, "G5", 0.9);

    // Instrument "Bass"
    instrument9 = buildInstrument(library2, InstrumentType.BASS, InstrumentState.PUBLISHED, "Bass");
    instrument9_meme0 = buildMeme(instrument9, "heavy");
    instrument9_audio8 = buildAudio(instrument9, "bass", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 0.62, "BLOOP", "Eb", 1.0);

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

    account1 = add(entities, new Account()
      .id(UUID.randomUUID())
      .name("Generated"));
    user1 = add(entities, new User()
      .id(UUID.randomUUID())
      .name("generated")
      .email("generated@email.com")
      .avatarUrl("http://pictures.com/generated.gif"));
    add(entities, new UserRole()
      .id(UUID.randomUUID())
      .userId(user1.getId())
      .type(UserRoleType.ADMIN));
    library1 = add(entities, new Library()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("generated"));

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
      Instrument instrument = add(entities, buildInstrument(library1, InstrumentType.DRUM, InstrumentState.PUBLISHED, String.format("%s Drums", majorMemeName)));
      add(entities, new InstrumentMeme()
        .id(UUID.randomUUID())
        .instrumentId(instrument.getId())
        .name(majorMemeName)
      );
      add(entities, new InstrumentMeme()
        .id(UUID.randomUUID())
        .instrumentId(instrument.getId())
        .name(minorMemeName)
      );
      // audios of instrument
      for (int k = 0; k < N; k++)
        add(entities, buildAudio(instrument, Text.toProper(percussiveNames[k]), String.format("%s.wav", Text.toLowerSlug(percussiveNames[k])), random(0, 0.05), random(0.25, 2), random(80, 120), 0.62, percussiveNames[k], "X", random(0.8, 1)));
      //
      log.debug("Generated Drum-type Instrument id={}, minorMeme={}, majorMeme={}", instrument.getId(), minorMemeName, majorMemeName);
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
      double densityFrom = random(0.3, 0.9);
      double tempoFrom = random(80, 120);
      //
      Program program = add(entities, buildProgram(library1, ProgramType.MACRO, ProgramState.PUBLISHED, String.format("%s, create %s to %s", minorMemeName, majorMemeFromName, majorMemeToName), keyFrom, tempoFrom, 0.6));
      add(entities, new ProgramMeme()
        .id(UUID.randomUUID())
        .programId(program.getId())
        .name(minorMemeName)
      );
      // of offset 0
      var sequence0 = add(entities, buildSequence(program, 0, String.format("Start %s", majorMemeFromName), densityFrom, keyFrom, tempoFrom));
      var binding0 = add(entities, new ProgramSequenceBinding()
        .id(UUID.randomUUID())
        .programSequenceId(sequence0.getId())
        .programId(sequence0.getProgramId())
        .offset(0)
      );
      add(entities, new ProgramSequenceBindingMeme()
        .id(UUID.randomUUID())
        .programId(binding0.getProgramId())
        .programSequenceBindingId(binding0.getId())
        .name(majorMemeFromName)
      );
      // to offset 1
      double densityTo = random(0.3, 0.9);
      double tempoTo = random(803, 120);
      var sequence1 = add(entities, buildSequence(program, 0, String.format("Finish %s", majorMemeToName), densityTo, keyTo, tempoTo));
      var binding1 = add(entities, new ProgramSequenceBinding()
        .id(UUID.randomUUID())
        .programSequenceId(sequence1.getId())
        .programId(sequence1.getProgramId())
        .offset(1)
      );
      add(entities, new ProgramSequenceBindingMeme()
        .id(UUID.randomUUID())
        .programSequenceBindingId(binding1.getId())
        .programId(binding1.getProgramId())
        .name(majorMemeToName)
      );
      //
      log.debug("Generated Macro-type Program id={}, minorMeme={}, majorMemeFrom={}, majorMemeTo={}", program.getId(), minorMemeName, majorMemeFromName, majorMemeToName);
    }

    // Generate N*4 total Main-type Programs, each having N patterns comprised of ~N*2 chords, bound to N*4 sequence patterns
    ProgramSequence[] sequences = new ProgramSequence[N];
    for (int i = 0; i < N << 2; i++) {
      String majorMemeName = random(majorMemeNames);
      String[] sequenceNames = listOfUniqueRandom(N, LoremIpsum.ELEMENTS);
      String[] subKeys = listOfUniqueRandom(N, LoremIpsum.MUSICAL_KEYS);
      Double[] subDensities = listOfRandomValues(N);
      double tempo = random(80, 120);
      //
      Program program = add(entities, buildProgram(library1, ProgramType.MAIN, ProgramState.PUBLISHED, String.format("%s: %s", majorMemeName, String.join(",", sequenceNames)), subKeys[0], tempo, 0.6));
      add(entities, new ProgramMeme()
        .id(UUID.randomUUID())
        .programId(program.getId())
        .name(majorMemeName)
      );
      // sequences of program
      for (int iP = 0; iP < N; iP++) {
        Integer total = random(LoremIpsum.SEQUENCE_TOTALS);
        sequences[iP] = add(entities, buildSequence(program, total, String.format("%s in %s", majorMemeName, sequenceNames[iP]), subDensities[iP], subKeys[iP], tempo));
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
        var binding = add(entities, new ProgramSequenceBinding()
          .id(UUID.randomUUID())
          .programSequenceId(sequences[num].getId())
          .programId(sequences[num].getProgramId())
          .offset(offset)
        );
        add(entities, buildMeme(binding, random(minorMemeNames)));
      }
      log.debug("Generated Main-type Program id={}, majorMeme={} with {} sequences bound {} times", program.getId(), majorMemeName, N, N << 2);
    }

    // Generate N total Rhythm-type Sequences, each having N voices, and N*2 patterns comprised of N*8 events
    ProgramVoice[] voices = new ProgramVoice[N];
    Map<String, ProgramVoiceTrack> trackMap = Maps.newHashMap();
    for (int i = 0; i < N; i++) {
      String majorMemeName = majorMemeNames[i];
      double tempo = random(80, 120);
      String key = random(LoremIpsum.MUSICAL_KEYS);
      double density = random(0.4, 0.9);
      //
      Program program = add(entities, buildProgram(library1, ProgramType.RHYTHM, ProgramState.PUBLISHED, String.format("%s Beat", majorMemeName), key, tempo, 0.6));
      trackMap.clear();
      add(entities, new ProgramMeme()
        .id(UUID.randomUUID())
        .programId(program.getId())
        .name(majorMemeName)
      );
      // voices of program
      for (int iV = 0; iV < N; iV++) {
        voices[iV] = add(entities, buildVoice(program, InstrumentType.DRUM, String.format("%s %s", majorMemeName, percussiveNames[iV])));
      }
      var sequenceBase = add(entities, buildSequence(program, random(LoremIpsum.SEQUENCE_TOTALS), "Base", density, key, tempo));
      // patterns of program
      for (int iP = 0; iP < N << 1; iP++) {
        Integer total = random(LoremIpsum.PATTERN_TOTALS);
        int num = (int) StrictMath.floor(StrictMath.random() * N);

        // first pattern is always a Loop (because that's required) then the rest at random
        ProgramSequencePatternType type = 0 == iP ? ProgramSequencePatternType.LOOP : randomRhythmPatternType();
        var pattern = add(entities, buildPattern(sequenceBase, voices[num], type, total, String.format("%s %s %s", majorMemeName, type.toString(), random(LoremIpsum.ELEMENTS))));
        for (int iPE = 0; iPE < N << 2; iPE++) {
          // always use first chord, then use more chords with more density
          if (0 == iPE || StrictMath.random() < density) {
            String name = percussiveNames[num];
            if (!trackMap.containsKey(name))
              trackMap.put(name, add(entities, buildTrack(voices[num], name)));
            add(entities, buildEvent(pattern, trackMap.get(name), StrictMath.floor((float) iPE * total * 4 / N), random(0.25, 1.0), "X", random(0.4, 0.9)));
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
