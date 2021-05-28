// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.craft.arrangement;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.Chain;
import io.xj.Instrument;
import io.xj.Program;
import io.xj.ProgramSequence;
import io.xj.ProgramSequencePattern;
import io.xj.ProgramVoice;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChoiceArrangementPick;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.AppException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.util.CSV;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.client.HubClient;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.client.HubContent;
import io.xj.service.nexus.NexusApp;
import io.xj.service.nexus.NexusException;
import io.xj.service.nexus.fabricator.Fabricator;
import io.xj.service.nexus.fabricator.FabricatorFactory;
import io.xj.service.nexus.persistence.NexusEntityStore;
import io.xj.service.nexus.testing.NexusTestConfiguration;
import io.xj.service.nexus.work.NexusWorkModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static io.xj.service.nexus.NexusIntegrationTestingFixtures.makeChain;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.makeChord;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.makeDetailProgram;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.makeEvent;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.makeInstrument;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.makeInstrumentWithEvents;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.makePattern;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.makeSegment;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.makeSegmentChoice;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.makeSequence;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.makeTrack;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.makeVoice;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.makeVoicing;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 [#176696738] XJ has a serviceable voicing algorithm
 */
@RunWith(MockitoJUnitRunner.class)
public class ArrangementCraftTests extends YamlTest {
  private static final int REPEAT_EACH_TEST_TIMES = 7;
  private static final Set<Instrument.Type> INSTRUMENT_TYPES = ImmutableSet.of(
    Instrument.Type.Bass,
    Instrument.Type.Pad,
    Instrument.Type.Stab,
    Instrument.Type.Stripe
  );
  private FabricatorFactory fabrication;
  private NexusEntityStore store;
  private Fabricator fabricator;

  // list of all entities to return from Hub
  private List<Object> content;

  // maps with specific entities that will reference each other
  private Map<Instrument.Type, Instrument> instruments;
  private Map<Instrument.Type, Program> detailPrograms;
  private Map<Instrument.Type, ProgramVoice> detailProgramVoices;
  private Map<Instrument.Type, ProgramSequence> detailProgramSequences;
  private Chain chain;
  private Segment segment;
  private Map<Instrument.Type, SegmentChoice> segmentChoices;
  private Injector injector;

  // this is how we provide content for fabrication
  @Mock
  public HubClient hubClient;

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

  @Before
  public void setUp() throws AppException {
    Config config = NexusTestConfiguration.getDefault();
    injector = AppConfiguration.inject(config,
      ImmutableSet.of(Modules.override(new NexusWorkModule())
        .with(new AbstractModule() {
          @Override
          public void configure() {
            bind(HubClient.class).toInstance(hubClient);
          }
        })));
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
        var data = loadYaml(filename);

        // Read Instruments and Detail Programs from the test YAML
        for (var instrumentType : INSTRUMENT_TYPES) {
          loadInstrument(data, instrumentType);
          loadDetailProgram(data, instrumentType);
        }

        // Read Segment and make choices of instruments and programs
        loadSegment(data);

        // Fabricate: Craft Arrangements for Choices
        when(hubClient.ingest(any(), any(), any(), any())).thenReturn(new HubContent(content));
        fabricator = fabrication.fabricate(HubClientAccess.internal(), segment);
        ArrangementCraftImpl subject = new ArrangementCraftImpl(fabricator);
        for (var choice : segmentChoices.values()) subject.craftArrangements(choice);

        // assert final picks
        loadAndPerformAssertions(data);

      } catch (Exception e) {
        failures.add(String.format("[%s] Exception: %s", filename, e.getMessage()));
      }
  }

  /**
   Reset the resources before each repetition of each test
   */
  private void reset() throws Exception {
    fabrication = injector.getInstance(FabricatorFactory.class);
    var entityFactory = injector.getInstance(EntityFactory.class);
    HubApp.buildApiTopology(entityFactory);
    NexusApp.buildApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store = injector.getInstance(NexusEntityStore.class);
    store.deleteAll();
    chain = store.put(makeChain());

    // prepare list of all entities to return from Hub
    content = Lists.newArrayList();

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
  private void loadInstrument(Map<?, ?> data, Instrument.Type type) {
    Map<?, ?> obj = (Map<?, ?>) data.get(String.format("%sInstrument", type.name().toLowerCase(Locale.ROOT)));
    if (Objects.isNull(obj)) return;

    var instrument = makeInstrument(
      type,
      getBool(obj, "isTonal"),
      getBool(obj, "isMultiphonic"));
    instruments.put(type, instrument);

    content.addAll(makeInstrumentWithEvents(
      instrument,
      getStr(obj, "notes")));
  }

  /**
   Load the detail program section of the test YAML file, for one type of Instrument

   @param data YAML file wrapper
   @param type of instrument to read
   */
  private void loadDetailProgram(Map<?, ?> data, Instrument.Type type) {
    Map<?, ?> obj = (Map<?, ?>) data.get(String.format("%sDetailProgram", type.name().toLowerCase(Locale.ROOT)));
    if (Objects.isNull(obj)) return;

    var program = makeDetailProgram(
      getStr(obj, "key"),
      getBool(obj, "doPatternRestartOnChord"),
      String.format("%s Test", type.name()));
    detailPrograms.put(type, program);
    content.add(program);

    var voice = makeVoice(program, type);
    detailProgramVoices.put(type, voice);
    content.add(voice);

    var track = makeTrack(voice);
    content.add(track);

    Map<?, ?> sObj = (Map<?, ?>) obj.get("sequence");
    var sequence = makeSequence(program, Objects.requireNonNull(getInt(sObj, "total")));
    detailProgramSequences.put(type, sequence);
    content.add(sequence);

    //noinspection unchecked
    for (Map<?, ?> pObj : (List<Map<?, ?>>) sObj.get("patterns")) {
      var pattern = makePattern(sequence, voice,
        ProgramSequencePattern.Type.valueOf(getStr(pObj, "type")),
        Objects.requireNonNull(getInt(pObj, "total")));
      content.add(pattern);
      //noinspection unchecked
      for (Map<?, ?> eObj : (List<Map<?, ?>>) pObj.get("events")) {
        content.add(makeEvent(pattern, track,
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

    segment = store.put(makeSegment(chain,
      Objects.requireNonNull(getStr(obj, "key")),
      Objects.requireNonNull(getInt(obj, "total")),
      Objects.requireNonNull(getDouble(obj, "density")),
      Objects.requireNonNull(getDouble(obj, "tempo"))));

    //noinspection unchecked
    for (Map<?, ?> cObj : (List<Map<?, ?>>) obj.get("chords")) {
      var chord = store.put(makeChord(segment,
        getDouble(cObj, "position"),
        getStr(cObj, "name")));
      Map<?, ?> vObj = (Map<?, ?>) cObj.get("voicings");
      for (var instrumentType : instruments.keySet()) {
        var notes = getStr(vObj, instrumentType.name().toLowerCase(Locale.ROOT));
        if (Objects.nonNull(notes))
          store.put(makeVoicing(chord, instrumentType, notes));
      }
    }

    for (var instrument : instruments.values())
      if (detailPrograms.containsKey(instrument.getType()) &&
        detailProgramSequences.containsKey(instrument.getType()) &&
        detailProgramVoices.containsKey(instrument.getType()))
        segmentChoices.put(instrument.getType(), store.put(makeSegmentChoice(segment,
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

  private void loadAndPerformAssertions(Map<?, ?> data, Instrument.Type type) {
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
          (Objects.isNull(start) || start == pick.getStart()) &&
          (Objects.isNull(length) || length == pick.getLength()))
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
