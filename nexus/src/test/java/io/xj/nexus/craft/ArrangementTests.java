// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.xj.hub.HubTopology;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.client.HubClient;
import io.xj.hub.client.HubContent;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.app.AppException;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.lib.music.StickyBun;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import io.xj.nexus.NexusException;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangementPick;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.NexusEntityStoreImpl;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.persistence.SegmentManagerImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static io.xj.hub.IntegrationTestingFixtures.buildProgram;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.lib.util.Values.MICROS_PER_SECOND;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoice;
import static org.junit.Assert.assertEquals;

/**
 * XJ has a serviceable voicing algorithm https://www.pivotaltracker.com/story/show/176696738
 */
@RunWith(MockitoJUnitRunner.class)
public class ArrangementTests extends YamlTest {
  static final String TEST_PATH_PREFIX = "/arrangements/";
  static final int REPEAT_EACH_TEST_TIMES = 7;
  static final Set<InstrumentType> INSTRUMENT_TYPES_TO_TEST = ImmutableSet.of(
    InstrumentType.Bass,
    InstrumentType.Pad,
    InstrumentType.Stab,
    InstrumentType.Stripe,
    InstrumentType.Sticky
  );
  final Logger LOG = LoggerFactory.getLogger(YamlTest.class);
  // this is how we provide content for fabrication
  @Mock
  public HubClient hubClient;
  @Mock
  public NotificationProvider notificationProvider;
  FabricatorFactory fabrication;
  NexusEntityStore store;
  Fabricator fabricator;
  // list of all entities to return from Hub
  List<Object> content;
  // maps with specific entities that will reference each other
  Map<InstrumentType, Instrument> instruments;
  Map<InstrumentType, Program> detailPrograms;
  Map<InstrumentType, ProgramVoice> detailProgramVoices;
  Map<InstrumentType, ProgramSequence> detailProgramSequences;
  Map<InstrumentType, List<ProgramSequencePatternEvent>> detailProgramSequencePatternEvents;
  List<StickyBun> stickyBuns;
  Chain chain;
  Segment segment;
  Map<InstrumentType, SegmentChoice> segmentChoices;
  Program mainProgram1;

  @Test
  public void arrangementBaseline() {
    var prg = new Program();
    prg.setId(UUID.randomUUID());
    prg.setName("Baseline");

    prg.setName("Modified");
    assertEquals("Modified", prg.getName());
  }

  @Test
  public void arrangement1() {
    loadAndRunTest("arrangement_1.yaml");
  }

  @Test
  public void arrangement2() {
    loadAndRunTest("arrangement_2.yaml");
  }

  @Test
  public void arrangement3() {
    loadAndRunTest("arrangement_3.yaml");
  }

  @Test
  public void arrangement4() {
    loadAndRunTest("arrangement_4.yaml");
  }

  @Test
  public void arrangement5() {
    loadAndRunTest("arrangement_5.yaml");
  }

  @Test
  public void arrangement6() {
    loadAndRunTest("arrangement_6.yaml");
  }

  @Test
  public void arrangement7() {
    loadAndRunTest("arrangement_7.yaml");
  }

  @Test
  public void arrangement8() {
    loadAndRunTest("arrangement_8.yaml");
  }

  @Test
  public void arrangement9() {
    loadAndRunTest("arrangement_9.yaml");
  }

  @Test
  public void arrangement10() {
    loadAndRunTest("arrangement_10.yaml");
  }

  @Test
  public void arrangement_12_sticky_bun_basic() {
    loadAndRunTest("arrangement_12_sticky_bun_basic.yaml");
  }

/*
FUTURE goal
  @Test
  public void arrangement11() {
    loadAndRunTest("arrangement_11.yaml");
  }
*/

  @Test
  public void arrangement0_NoChordSections() {
    loadAndRunTest("arrangement_0_no_chord_sections.yaml");
  }

  @Before
  public void setUp() throws AppException {
  }

  /**
   * Load the specified test YAML file and run it repeatedly.
   *
   * @param filename of test YAML file
   */
  void loadAndRunTest(String filename) {
    for (int i = 0; i < REPEAT_EACH_TEST_TIMES; i++)
      try {
        reset();

        // Load YAML and parse
        var data = loadYaml(TEST_PATH_PREFIX, filename);

        // Read Instruments and Detail Programs from the test YAML
        for (var instrumentType : INSTRUMENT_TYPES_TO_TEST) {
          loadInstrument(data, instrumentType);
          loadDetailProgram(data, instrumentType);
        }

        // Read Segment and make choices of instruments and programs
        loadSegment(data);

        // Fabricate: Craft Arrangements for Choices
        var sourceMaterial = new HubContent(content);
        fabricator = fabrication.fabricate(sourceMaterial, segment);
        for (StickyBun bun : stickyBuns) {
          fabricator.putStickyBun(bun);
        }
        fabricator.put(buildSegmentChoice(segment, mainProgram1));
        CraftImpl subject = new CraftImpl(fabricator);
        for (var choice : segmentChoices.values()) subject.craftNoteEventArrangements(choice, false);

        // assert final picks
        loadAndPerformAssertions(data);

      } catch (Exception e) {
        failures.add(String.format("[%s] Exception: %s", filename, Text.formatStackTrace(e)));
      }
  }

  /**
   * Reset the resources before each repetition of each test
   */
  void reset() throws Exception {
    JsonProvider jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    store = new NexusEntityStoreImpl(entityFactory);
    SegmentManager segmentManager = new SegmentManagerImpl(entityFactory, store);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    fabrication = new FabricatorFactoryImpl(
      segmentManager,
      jsonapiPayloadFactory,
      jsonProvider
    );
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store.deleteAll();

    var account1 = buildAccount("fish");
    Template template1 = buildTemplate(account1, "Test Template 1", "test1");
    var library1 = buildLibrary(account1, "palm tree");
    mainProgram1 = buildProgram(library1, ProgramType.Main, ProgramState.Published, "ANTS", "C#", 60.0f, 0.6f); // 60 BPM such that 1 beat = 1 second
    chain = store.put(NexusIntegrationTestingFixtures.buildChain(template1));

    // prepare list of all entities to return from Hub
    content = Lists.newArrayList(template1, library1, mainProgram1);

    // prepare maps with specific entities that will reference each other
    instruments = Maps.newHashMap();
    detailPrograms = Maps.newHashMap();
    detailProgramVoices = Maps.newHashMap();
    detailProgramSequences = Maps.newHashMap();
    detailProgramSequencePatternEvents = Maps.newHashMap();
    stickyBuns = Lists.newArrayList();
    segmentChoices = Maps.newHashMap();
  }

  /**
   * Load the instrument section of the test YAML file, for one type of Instrument@param data YAML file wrapper
   *
   * @param type of instrument to read
   */
  void loadInstrument(Map<?, ?> data, InstrumentType type) {
    Map<?, ?> obj = (Map<?, ?>) data.get(String.format("%sInstrument", type.toString().toLowerCase(Locale.ROOT)));
    if (Objects.isNull(obj)) return;

    var instrument = IntegrationTestingFixtures.buildInstrument(
      type,
      InstrumentMode.Event,
      getBool(obj, "isTonal"),
      getBool(obj, "isMultiphonic"));
    instruments.put(type, instrument);

    content.addAll(IntegrationTestingFixtures.buildInstrumentWithAudios(
      instrument,
      getStr(obj, "notes")));
  }

  /**
   * Load the detail program section of the test YAML file, for one type of Instrument
   *
   * @param data YAML file wrapper
   * @param type of instrument to read
   */
  void loadDetailProgram(Map<?, ?> data, InstrumentType type) {
    Map<?, ?> obj = (Map<?, ?>) data.get(String.format("%sDetailProgram", type.toString().toLowerCase(Locale.ROOT)));
    if (Objects.isNull(obj)) return;

    var program = IntegrationTestingFixtures.buildDetailProgram(
      getStr(obj, "key"),
      getBool(obj, "doPatternRestartOnChord"),
      String.format("%s Test", type));
    detailPrograms.put(type, program);
    content.add(program);

    var voice = IntegrationTestingFixtures.buildVoice(program, type);
    detailProgramVoices.put(type, voice);
    content.add(voice);

    var track = IntegrationTestingFixtures.buildTrack(voice);
    content.add(track);

    Map<?, ?> sObj = (Map<?, ?>) obj.get("sequence");
    var sequence = IntegrationTestingFixtures.buildSequence(program, Objects.requireNonNull(getInt(sObj, "total")));
    detailProgramSequences.put(type, sequence);
    content.add(sequence);

    Map<?, ?> pObj = (Map<?, ?>) sObj.get("pattern");
    var pattern = IntegrationTestingFixtures.buildPattern(sequence, voice,
      Objects.requireNonNull(getInt(pObj, "total")));
    content.add(pattern);
    //noinspection unchecked
    for (Map<?, ?> eObj : (List<Map<?, ?>>) pObj.get("events")) {
      var event = IntegrationTestingFixtures.buildEvent(pattern, track,
        Objects.requireNonNull(getFloat(eObj, "position")),
        Objects.requireNonNull(getFloat(eObj, "duration")),
        getStr(eObj, "tones"));
      content.add(event);
      if (!detailProgramSequencePatternEvents.containsKey(type)) {
        detailProgramSequencePatternEvents.put(type, Lists.newArrayList());
      }
      detailProgramSequencePatternEvents.get(type).add(event);
    }
  }

  /**
   * Load the segment section of the test YAML file
   *
   * @param data YAML file wrapper
   */
  void loadSegment(Map<?, ?> data) throws NexusException {
    Map<?, ?> obj = (Map<?, ?>) data.get("segment");

    segment = store.put(NexusIntegrationTestingFixtures.buildSegment(chain,
      Objects.requireNonNull(getStr(obj, "key")),
      Objects.requireNonNull(getInt(obj, "total")),
      Objects.requireNonNull(getFloat(obj, "density")),
      60)); // 60 BPM such that 1 beat = 1 second

    if (obj.containsKey("stickyBuns")) {
      //noinspection unchecked
      for (Map<?, ?> sbObj : (List<Map<?, ?>>) obj.get("stickyBuns")) {
        var sbType = InstrumentType.valueOf(getStr(sbObj, "type"));
        var sbPosition = getFloat(sbObj, "position");
        var sbSeed = getInt(sbObj, "seed");
        var event = detailProgramSequencePatternEvents.get(sbType).stream()
          .filter(e -> e.getPosition().equals(sbPosition))
          .findAny()
          .orElseThrow(() -> new NexusException(String.format("Failed to locate event type %s position %f", sbType, sbPosition)));
        stickyBuns.add(new StickyBun(event.getId(), List.of(Objects.requireNonNull(sbSeed))));
      }
    }

    //noinspection unchecked
    for (Map<?, ?> cObj : (List<Map<?, ?>>) obj.get("chords")) {
      var chord = store.put(NexusIntegrationTestingFixtures.buildSegmentChord(segment,
        getDouble(cObj, "position"),
        getStr(cObj, "name")));
      Map<?, ?> vObj = (Map<?, ?>) cObj.get("voicings");
      for (var instrumentType : instruments.keySet()) {
        var notes = getStr(vObj, instrumentType.toString().toLowerCase(Locale.ROOT));
        if (Objects.nonNull(notes))
          store.put(NexusIntegrationTestingFixtures.buildSegmentChordVoicing(chord, instrumentType, notes));
      }
    }

    for (var instrument : instruments.values())
      if (detailPrograms.containsKey(instrument.getType()) &&
        detailProgramSequences.containsKey(instrument.getType()) &&
        detailProgramVoices.containsKey(instrument.getType()))
        segmentChoices.put(instrument.getType(),
          store.put(buildSegmentChoice(segment,
            detailPrograms.get(instrument.getType()),
            detailProgramSequences.get(instrument.getType()),
            detailProgramVoices.get(instrument.getType()),
            instrument)));
  }

  /**
   * Load the assertions of picks section after a test has run
   * Load the instrument section of the test YAML file, for one type of Instrument@param data YAML file wrapper
   */
  void loadAndPerformAssertions(Map<?, ?> data) {
    @Nullable
    Map<?, ?> obj = (Map<?, ?>) data.get("assertPicks");
    if (Objects.isNull(obj)) return;
    for (var type : INSTRUMENT_TYPES_TO_TEST) loadAndPerformAssertions(obj, type);
  }

  void loadAndPerformAssertions(Map<?, ?> data, InstrumentType type) {
    @Nullable
    @SuppressWarnings("unchecked")
    List<Map<?, ?>> objs = (List<Map<?, ?>>) data.get(type.toString().toLowerCase(Locale.ROOT));
    if (Objects.isNull(objs)) return;

    LOG.info("Picks: {}", fabricator.getPicks().stream()
      .sorted(Comparator.comparing(SegmentChoiceArrangementPick::getStartAtSegmentMicros))
      .map(pick -> String.format("%s@%.1f", pick.getTones(), ((float) pick.getStartAtSegmentMicros() / MICROS_PER_SECOND)))
      .toList());

    for (var obj : objs) {
      @Nullable Float startAtSeconds = getFloat(obj, "start");
      @Nullable Float lengthSeconds = getFloat(obj, "length");
      @Nullable Long startAtMicros = Objects.nonNull(startAtSeconds) ? (long) (startAtSeconds * MICROS_PER_SECOND) : null;
      @Nullable Long lengthMicros = Objects.nonNull(lengthSeconds) ? (long) (lengthSeconds * MICROS_PER_SECOND) : null;
      Integer count = getInt(obj, "count");
      String notes = getStr(obj, "notes");

      var assertionName = String.format("%s-type picks", type) +
        (Objects.nonNull(startAtMicros) ? String.format(" starting at %fs", startAtSeconds) : "") +
        (Objects.nonNull(lengthMicros) ? String.format(" with length %fs", lengthSeconds) : "");

      var picks = fabricator.getPicks().stream()
        .filter(pick -> pick.getEvent().equals(type.toString()) &&
          (Objects.isNull(startAtMicros) || startAtMicros.equals(pick.getStartAtSegmentMicros())) &&
          (Objects.isNull(lengthMicros) || lengthMicros.equals(pick.getLengthMicros())))
        .map(SegmentChoiceArrangementPick::getTones).toList();

      if (Objects.nonNull(count))
        assertSame(String.format("Count %d %s", count, assertionName),
          count, picks.size());

      if (Objects.nonNull(notes))
        assertSameNotes(String.format("Notes of %s", assertionName),
          new HashSet<>(CSV.split(notes)), new HashSet<>(picks));
    }
  }
}
