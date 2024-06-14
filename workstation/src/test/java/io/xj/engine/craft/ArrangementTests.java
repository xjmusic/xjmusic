// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.craft;

import io.xj.engine.ContentFixtures;
import io.xj.engine.SegmentFixtures;
import io.xj.engine.FabricationException;
import io.xj.engine.FabricationTopology;
import io.xj.engine.fabricator.SegmentEntityStore;
import io.xj.engine.fabricator.SegmentEntityStoreImpl;
import io.xj.engine.fabricator.Fabricator;
import io.xj.engine.fabricator.FabricatorFactory;
import io.xj.engine.fabricator.FabricatorFactoryImpl;
import io.xj.model.pojos.Chain;
import io.xj.model.pojos.Segment;
import io.xj.model.pojos.SegmentChoice;
import io.xj.model.pojos.SegmentChoiceArrangementPick;
import io.xj.model.HubContent;
import io.xj.model.HubTopology;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.enums.InstrumentMode;
import io.xj.model.enums.InstrumentType;
import io.xj.model.enums.ProgramState;
import io.xj.model.enums.ProgramType;
import io.xj.model.json.JsonProvider;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.model.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.model.music.StickyBun;
import io.xj.model.pojos.Instrument;
import io.xj.model.pojos.Program;
import io.xj.model.pojos.ProgramSequence;
import io.xj.model.pojos.ProgramSequencePatternEvent;
import io.xj.model.pojos.ProgramVoice;
import io.xj.model.pojos.Template;
import io.xj.model.util.CsvUtils;
import io.xj.model.util.StringUtils;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static io.xj.model.util.ValueUtils.MICROS_PER_SECOND;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 XJ has a serviceable voicing algorithm https://github.com/xjmusic/xjmusic/issues/221
 */
@ExtendWith(MockitoExtension.class)
public class ArrangementTests extends YamlTest {
  static final String TEST_PATH_PREFIX = "/arrangements/";
  static final int REPEAT_EACH_TEST_TIMES = 7;

  static final int TEMPO = 60; // 60 BPM such that 1 beat = 1 second
  static final Set<InstrumentType> INSTRUMENT_TYPES_TO_TEST = Set.of(
    InstrumentType.Bass,
    InstrumentType.Pad,
    InstrumentType.Stab,
    InstrumentType.Stripe,
    InstrumentType.Sticky
  );
  final Logger LOG = LoggerFactory.getLogger(YamlTest.class);
  // this is how we provide content for fabrication
  FabricatorFactory fabrication;
  SegmentEntityStore store;
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

  @BeforeEach
  public void setUp() {
  }

  /**
   Load the specified test YAML file and run it repeatedly.

   @param filename of test YAML file
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
        fabricator = fabrication.fabricate(sourceMaterial, segment.getId(), 48000.0f, 2, null);
        for (StickyBun bun : stickyBuns) {
          fabricator.putStickyBun(bun);
        }
        fabricator.put(SegmentFixtures.buildSegmentChoice(segment, mainProgram1), false);
        CraftImpl subject = new CraftImpl(fabricator);
        for (var choice : segmentChoices.values())
          subject.craftNoteEventArrangements(TEMPO, choice, false);

        // assert final picks
        loadAndPerformAssertions(data);

      } catch (Exception e) {
        failures.add(String.format("[%s] Exception: %s", filename, StringUtils.formatStackTrace(e)));
      }
  }

  /**
   Reset the resources before each repetition of each test
   */
  void reset() throws Exception {
    JsonProvider jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    store = new SegmentEntityStoreImpl(entityFactory);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    fabrication = new FabricatorFactoryImpl(
      store,
      jsonapiPayloadFactory,
      jsonProvider
    );
    HubTopology.buildHubApiTopology(entityFactory);
    FabricationTopology.buildFabricationTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store.clear();

    var project1 = ContentFixtures.buildProject("fish");
    Template template1 = ContentFixtures.buildTemplate(project1, "Test Template 1", "test1");
    var library1 = ContentFixtures.buildLibrary(project1, "palm tree");
    mainProgram1 = ContentFixtures.buildProgram(library1, ProgramType.Main, ProgramState.Published, "ANTS", "C#", 60.0f); // 60 BPM such that 1 beat = 1 second
    chain = store.put(SegmentFixtures.buildChain(template1));

    // prepare list of all entities to return from Hub
    content = new ArrayList<>(List.of(template1, library1, mainProgram1));

    // prepare maps with specific entities that will reference each other
    instruments = new HashMap<>();
    detailPrograms = new HashMap<>();
    detailProgramVoices = new HashMap<>();
    detailProgramSequences = new HashMap<>();
    detailProgramSequencePatternEvents = new HashMap<>();
    stickyBuns = new ArrayList<>();
    segmentChoices = new HashMap<>();
  }

  /**
   Load the instrument section of the test YAML file, for one type of Instrument@param data YAML file wrapper

   @param type of instrument to read
   */
  void loadInstrument(Map<?, ?> data, InstrumentType type) {
    Map<?, ?> obj = (Map<?, ?>) data.get(String.format("%sInstrument", type.toString().toLowerCase(Locale.ROOT)));
    if (Objects.isNull(obj)) return;

    var instrument = ContentFixtures.buildInstrument(
      type,
      InstrumentMode.Event,
      getBool(obj, "isTonal"),
      getBool(obj, "isMultiphonic"));
    instruments.put(type, instrument);

    content.addAll(ContentFixtures.buildInstrumentWithAudios(
      instrument,
      getStr(obj, "notes")));
  }

  /**
   Load the detail program section of the test YAML file, for one type of Instrument

   @param data YAML file wrapper
   @param type of instrument to read
   */
  @SuppressWarnings("unchecked")
  void loadDetailProgram(Map<?, ?> data, InstrumentType type) {
    Map<?, ?> obj = (Map<?, ?>) data.get(String.format("%sDetailProgram", type.toString().toLowerCase(Locale.ROOT)));
    if (Objects.isNull(obj)) return;

    var program = ContentFixtures.buildDetailProgram(
      getStr(obj, "key"),
      getBool(obj, "doPatternRestartOnChord"),
      String.format("%s Test", type));
    detailPrograms.put(type, program);
    content.add(program);

    var voice = ContentFixtures.buildVoice(program, type);
    detailProgramVoices.put(type, voice);
    content.add(voice);

    var track = ContentFixtures.buildTrack(voice);
    content.add(track);

    Map<?, ?> sObj = (Map<?, ?>) obj.get("sequence");
    var sequence = ContentFixtures.buildSequence(program, Objects.requireNonNull(getInt(sObj, "total")));
    detailProgramSequences.put(type, sequence);
    content.add(sequence);

    Map<?, ?> pObj = (Map<?, ?>) sObj.get("pattern");
    var pattern = ContentFixtures.buildPattern(sequence, voice,
      Objects.requireNonNull(getInt(pObj, "total")));
    content.add(pattern);
    for (Map<?, ?> eObj : (List<Map<?, ?>>) pObj.get("events")) {
      var event = ContentFixtures.buildEvent(pattern, track,
        Objects.requireNonNull(getFloat(eObj, "position")),
        Objects.requireNonNull(getFloat(eObj, "duration")),
        getStr(eObj, "tones"));
      content.add(event);
      if (!detailProgramSequencePatternEvents.containsKey(type)) {
        detailProgramSequencePatternEvents.put(type, new ArrayList<>());
      }
      detailProgramSequencePatternEvents.get(type).add(event);
    }
  }

  /**
   Load the segment section of the test YAML file

   @param data YAML file wrapper
   */
  @SuppressWarnings("unchecked")
  void loadSegment(Map<?, ?> data) throws FabricationException {
    Map<?, ?> obj = (Map<?, ?>) data.get("segment");

    segment = store.put(SegmentFixtures.buildSegment(chain,
      Objects.requireNonNull(getStr(obj, "key")),
      Objects.requireNonNull(getInt(obj, "total")),
      Objects.requireNonNull(getFloat(obj, "intensity")),
      TEMPO));

    if (obj.containsKey("stickyBuns")) {
      for (Map<?, ?> sbObj : (List<Map<?, ?>>) obj.get("stickyBuns")) {
        var sbType = InstrumentType.valueOf(getStr(sbObj, "type"));
        var sbPosition = getFloat(sbObj, "position");
        var sbSeed = getInt(sbObj, "seed");
        var event = detailProgramSequencePatternEvents.get(sbType).stream()
          .filter(e -> e.getPosition().equals(sbPosition))
          .findAny()
          .orElseThrow(() -> new FabricationException(String.format("Failed to locate event type %s position %f", sbType, sbPosition)));
        stickyBuns.add(new StickyBun(event.getId(), List.of(Objects.requireNonNull(sbSeed))));
      }
    }

    for (Map<?, ?> cObj : (List<Map<?, ?>>) obj.get("chords")) {
      var chord = store.put(SegmentFixtures.buildSegmentChord(segment,
        Objects.requireNonNull(getFloat(cObj)),
        getStr(cObj, "name")));
      Map<?, ?> vObj = (Map<?, ?>) cObj.get("voicings");
      for (var instrumentType : instruments.keySet()) {
        var notes = getStr(vObj, instrumentType.toString().toLowerCase(Locale.ROOT));
        if (Objects.nonNull(notes))
          store.put(SegmentFixtures.buildSegmentChordVoicing(chord, instrumentType, notes));
      }
    }

    for (var instrument : instruments.values())
      if (detailPrograms.containsKey(instrument.getType()) &&
        detailProgramSequences.containsKey(instrument.getType()) &&
        detailProgramVoices.containsKey(instrument.getType()))
        segmentChoices.put(instrument.getType(),
          store.put(SegmentFixtures.buildSegmentChoice(segment,
            detailPrograms.get(instrument.getType()),
            detailProgramSequences.get(instrument.getType()),
            detailProgramVoices.get(instrument.getType()),
            instrument)));
  }

  /**
   Load the assertions of picks section after a test has run
   Load the instrument section of the test YAML file, for one type of Instrument@param data YAML file wrapper
   */
  void loadAndPerformAssertions(Map<?, ?> data) {
    @Nullable
    Map<?, ?> obj = (Map<?, ?>) data.get("assertPicks");
    if (Objects.isNull(obj)) return;
    for (var type : INSTRUMENT_TYPES_TO_TEST) loadAndPerformAssertions(obj, type);
  }

  @SuppressWarnings("unchecked")
  void loadAndPerformAssertions(Map<?, ?> data, InstrumentType type) {
    @Nullable
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
          new HashSet<>(CsvUtils.split(notes)), new HashSet<>(picks));
    }
  }
}
