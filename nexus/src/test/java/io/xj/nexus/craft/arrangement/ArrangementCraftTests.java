// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.arrangement;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.api.Chain;
import io.xj.api.Instrument;
import io.xj.api.InstrumentType;
import io.xj.api.Program;
import io.xj.api.ProgramSequence;
import io.xj.api.ProgramSequencePatternType;
import io.xj.api.ProgramVoice;
import io.xj.api.Segment;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentChoiceArrangementPick;
import io.xj.api.Template;
import io.xj.lib.app.AppException;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.Topology;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import io.xj.nexus.NexusException;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTestConfiguration;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.hub_client.client.HubClient;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.hub_client.client.HubContent;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.work.NexusWorkModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildAccount;
import static org.junit.Assert.assertEquals;

/**
 [#176696738] XJ has a serviceable voicing algorithm
 */
@RunWith(MockitoJUnitRunner.class)
public class ArrangementCraftTests extends YamlTest {
  private static final int REPEAT_EACH_TEST_TIMES = 7;
  private static final Set<InstrumentType> INSTRUMENT_TYPES = ImmutableSet.of(
    InstrumentType.BASS,
    InstrumentType.PAD,
    InstrumentType.STAB,
    InstrumentType.STRIPE
  );
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

  @Test
  public void arrangementBaseline() {
    var baseline = new Program()
      .id(UUID.randomUUID())
      .name("Baseline");

    assertEquals("Modified", baseline.name("Modified").getName());
  }

  @Test
  public void arrangementCraft1() {
    loadAndRunTest("arrangement_craft_1.yaml");
  }

  @Test
  public void arrangementCraft2() {
    loadAndRunTest("arrangement_craft_2.yaml");
  }

  @Test
  public void arrangementCraft3() {
    loadAndRunTest("arrangement_craft_3.yaml");
  }

  @Test
  public void arrangementCraft4() {
    loadAndRunTest("arrangement_craft_4.yaml");
  }

  @Test
  public void arrangementCraft5() {
    loadAndRunTest("arrangement_craft_5.yaml");
  }

  @Test
  public void arrangementCraft6() {
    loadAndRunTest("arrangement_craft_6.yaml");
  }

  @Test
  public void arrangementCraft7NoChordSections() {
    loadAndRunTest("arrangement_craft_7_no_chord_sections.yaml");
  }

  @Before
  public void setUp() throws AppException {
    Config config = NexusTestConfiguration.getDefault();
    Environment env = Environment.getDefault();
    injector = Guice.createInjector(Modules.override(new NexusWorkModule())
      .with(new AbstractModule() {
        @Override
        protected void configure() {
          bind(Config.class).toInstance(config);
          bind(Environment.class).toInstance(env);
        }
      }));
  }

  /**
   Load the specified test YAML file and run it repeatedly.@param filename of test YAML file

   */
  private void loadAndRunTest(String filename) {
    for (int i = 0; i < REPEAT_EACH_TEST_TIMES; i++)
      try {
        reset();

        // Load YAML and parse
        var data = loadYaml(filename);

        // Read Instruments and Detail Programs from the test YAML
        for (var instrumentType : INSTRUMENT_TYPES) {
          loadInstrument(data, instrumentType);
          loadDetailProgram(data, instrumentType);
        }

        // Read Segment and make choices of instruments and programs
        loadSegment(data);

        // Fabricate: Craft Arrangements for Choices
        var sourceMaterial = new HubContent(content);
        fabricator = fabrication.fabricate(HubClientAccess.internal(), sourceMaterial, segment);
        ArrangementCraftImpl subject = new ArrangementCraftImpl(fabricator);
        for (var choice : segmentChoices.values()) subject.craftArrangements(choice, false);

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
    Topology.buildHubApiTopology(entityFactory);
    Topology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store = injector.getInstance(NexusEntityStore.class);
    store.deleteAll();

    var account1 = buildAccount("fish");
    Template template1 = buildTemplate(account1, "Test Template 1", "test1");
    chain = store.put(NexusIntegrationTestingFixtures.buildChain(template1));

    // prepare list of all entities to return from Hub
    content = Lists.newArrayList(template1);

    // prepare maps with specific entities that will reference each other
    instruments = Maps.newHashMap();
    detailPrograms = Maps.newHashMap();
    detailProgramVoices = Maps.newHashMap();
    detailProgramSequences = Maps.newHashMap();
    segmentChoices = Maps.newHashMap();
  }

  /**
   Load the instruments section of the test YAML file, for one type of Instrument

   @param data YAML file wrapper
   @param type of instrument to read
   */
  private void loadInstrument(Map<?, ?> data, InstrumentType type) {
    Map<?, ?> obj = (Map<?, ?>) data.get(String.format("%sInstrument", type.name().toLowerCase(Locale.ROOT)));
    if (Objects.isNull(obj)) return;

    var instrument = NexusIntegrationTestingFixtures.buildInstrument(
      type,
      getBool(obj, "isTonal"),
      getBool(obj, "isMultiphonic"));
    instruments.put(type, instrument);

    content.addAll(NexusIntegrationTestingFixtures.buildInstrumentWithAudios(
      instrument,
      getStr(obj, "notes")));
  }

  /**
   Load the detail program section of the test YAML file, for one type of Instrument

   @param data YAML file wrapper
   @param type of instrument to read
   */
  private void loadDetailProgram(Map<?, ?> data, InstrumentType type) {
    Map<?, ?> obj = (Map<?, ?>) data.get(String.format("%sDetailProgram", type.name().toLowerCase(Locale.ROOT)));
    if (Objects.isNull(obj)) return;

    var program = NexusIntegrationTestingFixtures.buildDetailProgram(
      getStr(obj, "key"),
      getBool(obj, "doPatternRestartOnChord"),
      String.format("%s Test", type.name()));
    detailPrograms.put(type, program);
    content.add(program);

    var voice = NexusIntegrationTestingFixtures.buildVoice(program, type);
    detailProgramVoices.put(type, voice);
    content.add(voice);

    var track = NexusIntegrationTestingFixtures.buildTrack(voice);
    content.add(track);

    Map<?, ?> sObj = (Map<?, ?>) obj.get("sequence");
    var sequence = NexusIntegrationTestingFixtures.buildSequence(program, Objects.requireNonNull(getInt(sObj, "total")));
    detailProgramSequences.put(type, sequence);
    content.add(sequence);

    //noinspection unchecked
    for (Map<?, ?> pObj : (List<Map<?, ?>>) sObj.get("patterns")) {
      var pattern = NexusIntegrationTestingFixtures.buildPattern(sequence, voice,
        ProgramSequencePatternType.fromValue(getStr(pObj, "type")),
        Objects.requireNonNull(getInt(pObj, "total")));
      content.add(pattern);
      //noinspection unchecked
      for (Map<?, ?> eObj : (List<Map<?, ?>>) pObj.get("events")) {
        content.add(NexusIntegrationTestingFixtures.buildEvent(pattern, track,
          Objects.requireNonNull(getDouble(eObj, "position")),
          Objects.requireNonNull(getDouble(eObj, "duration")),
          getStr(eObj, "note")));
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
      Objects.requireNonNull(getDouble(obj, "density")),
      Objects.requireNonNull(getDouble(obj, "tempo"))));

    //noinspection unchecked
    for (Map<?, ?> cObj : (List<Map<?, ?>>) obj.get("chords")) {
      var chord = store.put(NexusIntegrationTestingFixtures.buildChord(segment,
        getDouble(cObj, "position"),
        getStr(cObj, "name")));
      Map<?, ?> vObj = (Map<?, ?>) cObj.get("voicings");
      for (var instrumentType : instruments.keySet()) {
        var notes = getStr(vObj, instrumentType.name().toLowerCase(Locale.ROOT));
        if (Objects.nonNull(notes))
          store.put(NexusIntegrationTestingFixtures.buildVoicing(chord, instrumentType, notes));
      }
    }

    for (var instrument : instruments.values())
      if (detailPrograms.containsKey(instrument.getType()) &&
        detailProgramSequences.containsKey(instrument.getType()) &&
        detailProgramVoices.containsKey(instrument.getType()))
        segmentChoices.put(instrument.getType(), store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(segment,
          detailPrograms.get(instrument.getType()),
          detailProgramSequences.get(instrument.getType()),
          detailProgramVoices.get(instrument.getType()),
          instrument)));
  }

  /**
   Load the assertions of picks section after a test has run
   Load the instruments section of the test YAML file, for one type of Instrument@param data YAML file wrapper
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
    List<Map<?, ?>> objs = (List<Map<?, ?>>) data.get(type.name().toLowerCase(Locale.ROOT));
    if (Objects.isNull(objs)) return;

    for (var obj : objs) {
      Double start = getDouble(obj, "start");
      Double length = getDouble(obj, "length");
      Integer count = getInt(obj, "count");
      String notes = getStr(obj, "notes");

      var assertionName = String.format("%s-type picks", type) +
        (Objects.nonNull(start) ? String.format(" starting at %fs", start) : "") +
        (Objects.nonNull(length) ? String.format(" with length %fs", length) : "");

      var picks = fabricator.getPicks().stream()
        .filter(pick -> pick.getName().equals(type.name()) &&
          (Objects.isNull(start) || start.equals(pick.getStart())) &&
          (Objects.isNull(length) || length.equals(pick.getLength())))
        .map(SegmentChoiceArrangementPick::getNote)
        .collect(Collectors.toList());

      if (Objects.nonNull(count))
        assertSame(String.format("Count %d %s", count, assertionName),
          count, picks.size());

      if (Objects.nonNull(notes))
        assertSame(String.format("Notes of %s", assertionName),
          new HashSet<>(CSV.split(notes)), new HashSet<>(picks));
    }
  }
}
