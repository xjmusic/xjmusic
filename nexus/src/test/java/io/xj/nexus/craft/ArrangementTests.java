// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangementPick;
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
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.app.AppException;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import io.xj.nexus.NexusException;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.work.NexusWorkModule;
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
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoice;
import static org.junit.Assert.assertEquals;

/**
 https://www.pivotaltracker.com/story/show/176696738 XJ has a serviceable voicing algorithm
 */
@RunWith(MockitoJUnitRunner.class)
public class ArrangementTests extends YamlTest {
  private static final String TEST_PATH_PREFIX = "/arrangements/";
  private static final int REPEAT_EACH_TEST_TIMES = 7;
  private static final Set<InstrumentType> INSTRUMENT_TYPES = ImmutableSet.of(
    InstrumentType.Bass,
    InstrumentType.Pad,
    InstrumentType.Stab,
    InstrumentType.Stripe
  );
  private final Logger LOG = LoggerFactory.getLogger(YamlTest.class);
  // this is how we provide content for fabrication
  @Mock
  public HubClient hubClient;
  private FabricatorFactory fabrication;
  private NexusEntityStore store;
  private Fabricator fabricator;
  // list of all entities to return from Hub
  private List<Object> content;
  // maps with specific entities that will reference each other
  private Map<InstrumentType, Instrument> instruments;
  private Map<InstrumentType, Program> detailPrograms;
  private Map<InstrumentType, ProgramVoice> detailProgramVoices;
  private Map<InstrumentType, ProgramSequence> detailProgramSequences;
  private Chain chain;
  private Segment segment;
  private Map<InstrumentType, SegmentChoice> segmentChoices;
  private Injector injector;
  private Program mainProgram1;

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
    Environment env = Environment.getDefault();
    injector = Guice.createInjector(Modules.override(new NexusWorkModule())
      .with(new AbstractModule() {
        @Override
        protected void configure() {
          bind(Environment.class).toInstance(env);
        }
      }));
  }

  /**
   Load the specified test YAML file and run it repeatedly.

   @param filename of test YAML file
   */
  private void loadAndRunTest(String filename) {
    for (int i = 0; i < REPEAT_EACH_TEST_TIMES; i++)
      try {
        reset();

        // Load YAML and parse
        var data = loadYaml(TEST_PATH_PREFIX, filename);

        // Read Instruments and Detail Programs from the test YAML
        for (var instrumentType : INSTRUMENT_TYPES) {
          loadInstrument(data, instrumentType);
          loadDetailProgram(data, instrumentType);
        }

        // Read Segment and make choices of instruments and programs
        loadSegment(data);

        // Fabricate: Craft Arrangements for Choices
        var sourceMaterial = new HubContent(content);
        fabricator = fabrication.fabricate(sourceMaterial, segment);
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
   Reset the resources before each repetition of each test
   */
  private void reset() throws Exception {
    fabrication = injector.getInstance(FabricatorFactory.class);
    var entityFactory = injector.getInstance(EntityFactory.class);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store = injector.getInstance(NexusEntityStore.class);
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
    segmentChoices = Maps.newHashMap();
  }

  /**
   Load the instrument section of the test YAML file, for one type of Instrument@param data YAML file wrapper

   @param type of instrument to read
   */
  private void loadInstrument(Map<?, ?> data, InstrumentType type) {
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
   Load the detail program section of the test YAML file, for one type of Instrument

   @param data YAML file wrapper
   @param type of instrument to read
   */
  private void loadDetailProgram(Map<?, ?> data, InstrumentType type) {
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

    //noinspection unchecked
    for (Map<?, ?> pObj : (List<Map<?, ?>>) sObj.get("patterns")) {
      var pattern = IntegrationTestingFixtures.buildPattern(sequence, voice,
        Objects.requireNonNull(getInt(pObj, "total")));
      content.add(pattern);
      //noinspection unchecked
      for (Map<?, ?> eObj : (List<Map<?, ?>>) pObj.get("events")) {
        content.add(IntegrationTestingFixtures.buildEvent(pattern, track,
          Objects.requireNonNull(getFloat(eObj, "position")),
          Objects.requireNonNull(getFloat(eObj, "duration")),
          getStr(eObj, "tones")));
      }
    }
  }

  /**
   Load the segment section of the test YAML file

   @param data YAML file wrapper
   */
  private void loadSegment(Map<?, ?> data) throws NexusException {
    Map<?, ?> obj = (Map<?, ?>) data.get("segment");

    segment = store.put(NexusIntegrationTestingFixtures.buildSegment(chain,
      Objects.requireNonNull(getStr(obj, "key")),
      Objects.requireNonNull(getInt(obj, "total")),
      Objects.requireNonNull(getFloat(obj, "density")),
      60)); // 60 BPM such that 1 beat = 1 second

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
   Load the assertions of picks section after a test has run
   Load the instrument section of the test YAML file, for one type of Instrument@param data YAML file wrapper
   */
  private void loadAndPerformAssertions(Map<?, ?> data) {
    @Nullable
    Map<?, ?> obj = (Map<?, ?>) data.get("assertPicks");
    if (Objects.isNull(obj)) return;
    for (var type : INSTRUMENT_TYPES) loadAndPerformAssertions(obj, type);
  }

  private void loadAndPerformAssertions(Map<?, ?> data, InstrumentType type) {
    @Nullable
    @SuppressWarnings("unchecked")
    List<Map<?, ?>> objs = (List<Map<?, ?>>) data.get(type.toString().toLowerCase(Locale.ROOT));
    if (Objects.isNull(objs)) return;

    LOG.info("Picks: {}", fabricator.getPicks().stream()
      .sorted(Comparator.comparing(SegmentChoiceArrangementPick::getStart))
      .map(pick -> String.format("%s@%f", pick.getTones(), pick.getStart()))
      .toList());

    for (var obj : objs) {
      Float start = getFloat(obj, "start");
      Float length = getFloat(obj, "length");
      Integer count = getInt(obj, "count");
      String notes = getStr(obj, "notes");

      var assertionName = String.format("%s-type picks", type) +
        (Objects.nonNull(start) ? String.format(" starting at %fs", start) : "") +
        (Objects.nonNull(length) ? String.format(" with length %fs", length) : "");

      var picks = fabricator.getPicks().stream()
        .filter(pick -> pick.getEvent().equals(type.toString()) &&
          (Objects.isNull(start) || start.equals(pick.getStart().floatValue())) &&
          (Objects.isNull(length) || length.equals(pick.getLength().floatValue())))
        .map(SegmentChoiceArrangementPick::getTones).toList();

      if (Objects.nonNull(count))
        assertSame(String.format("Count %d %s", count, assertionName),
          count, picks.size());

      if (Objects.nonNull(notes))
        assertSame(String.format("Notes of %s", assertionName),
          new HashSet<>(CSV.split(notes)), new HashSet<>(picks));
    }
  }
}
