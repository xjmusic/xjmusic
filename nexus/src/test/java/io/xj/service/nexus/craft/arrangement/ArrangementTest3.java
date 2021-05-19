// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.craft.arrangement;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.Instrument;
import io.xj.ProgramSequencePattern;
import io.xj.Segment;
import io.xj.SegmentChoiceArrangementPick;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.entity.EntityFactory;
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

import java.util.Comparator;
import java.util.List;
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
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 Based on note-picking test #3 by Mark Stewart
 https://docs.google.com/document/d/1kj2v6FaoZN2ztHd44wN4HvSULufleGXw5dd8lYefBig/edit
 <p>

 @see ArrangementTest1
 @see ArrangementTest2
 This test is similar to Tests #1 and #2 except the following:
  * The second chord occurs at 3.0
  * The starting chord is in first inversion
 <p>
 Choose correct instrument note based on detail + voicing
 https://www.pivotaltracker.com/story/show/176695166
 <p>
 [#176474164] If Sequence has no key/tempo/density inherit from Program
 <p>
 This pad is held for the duration of the C chord as dictated by the main program; it is not re-attacked with each new eighth note
 <p>
 [#176696738] XJ has a serviceable voicing algorithm
 */
@RunWith(MockitoJUnitRunner.class)
public class ArrangementTest3 {
  private FabricatorFactory fabrication;
  private NexusEntityStore store;
  private Fabricator fabricator;

  @Mock
  public HubClient hubClient;

  @Before
  public void setUp() throws Exception {
    Config config = NexusTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config,
      ImmutableSet.of(Modules.override(new NexusWorkModule())
        .with(new AbstractModule() {
          @Override
          public void configure() {
            bind(HubClient.class).toInstance(hubClient);
          }
        })));
    fabrication = injector.getInstance(FabricatorFactory.class);
    var entityFactory = injector.getInstance(EntityFactory.class);
    HubApp.buildApiTopology(entityFactory);
    NexusApp.buildApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store = injector.getInstance(NexusEntityStore.class);
    store.deleteAll();
  }

  @Test
  public void arrangement() throws Exception {
    var bass = makeInstrument(Instrument.Type.Bass, true, true);
    var bassProgram = makeDetailProgram("C", false, "Bass Test");
    var bassVoice = makeVoice(bassProgram, Instrument.Type.Bass);
    var bassTrack = makeTrack(bassVoice);
    var bassSequence = makeSequence(bassProgram, 4);
    var bassPattern = makePattern(bassSequence, bassVoice, ProgramSequencePattern.Type.Loop, 4);
    var pad = makeInstrument(Instrument.Type.Pad, true, true);
    var padProgram = makeDetailProgram("C", true, "Pad Test");
    var padVoice = makeVoice(padProgram, Instrument.Type.Pad);
    var padTrack = makeTrack(padVoice);
    var padSequence = makeSequence(padProgram, 4);
    var padPattern = makePattern(padSequence, padVoice, ProgramSequencePattern.Type.Loop, 4);
    var stab = makeInstrument(Instrument.Type.Stab, true, true);
    var stabProgram = makeDetailProgram("C", true, "Stab Test");
    var stabVoice = makeVoice(stabProgram, Instrument.Type.Stab);
    var stabTrack = makeTrack(stabVoice);
    var stabSequence = makeSequence(stabProgram, 4);
    var stabPattern = makePattern(stabSequence, stabVoice, ProgramSequencePattern.Type.Loop, 4);
    List<Object> content = Lists.newArrayList(
      bassProgram,
      bassVoice,
      bassTrack,
      bassSequence,
      bassPattern,
      makeEvent(bassPattern, bassTrack, 0.0, 0.5, "C1"),
      makeEvent(bassPattern, bassTrack, 0.5, 0.5, "C2"),
      makeEvent(bassPattern, bassTrack, 1.0, 0.5, "C1"),
      makeEvent(bassPattern, bassTrack, 1.5, 0.5, "C2"),
      makeEvent(bassPattern, bassTrack, 2.0, 0.5, "C1"),
      makeEvent(bassPattern, bassTrack, 2.5, 0.5, "C2"),
      makeEvent(bassPattern, bassTrack, 3.0, 0.5, "C1"),
      makeEvent(bassPattern, bassTrack, 3.5, 0.5, "C2"),
      padProgram,
      padVoice,
      padTrack,
      padSequence,
      padPattern,
      makeEvent(padPattern, padTrack, 0.0, 4.0, "E4, G4, C5"),
      stabProgram,
      stabVoice,
      stabTrack,
      stabSequence,
      stabPattern,
      makeEvent(stabPattern, stabTrack, 0.0, 0.5, "E4, G4, C5")
    );
    content.addAll(makeInstrumentWithEvents(bass, "C1, E1, F1, G1, A1, C2, E2, F2, G2, A2, C3, E3"));
    content.addAll(makeInstrumentWithEvents(pad, "E3, F3, G3, A3, C4, E4, F4, G4, A4, C5, E5, F5, G5, A5, C6"));
    content.addAll(makeInstrumentWithEvents(stab, "E3, F3, G3, A3, C4, E4, F4, G4, A4, C5, E5, F5, G5, A5, C6"));
    when(hubClient.ingest(any(), any(), any(), any())).thenReturn(new HubContent(content));
    // Chain and Segment
    var chain = store.put(makeChain());
    var segment = store.put(makeSegment(chain, "C", 4, 1, 120));
    var bassChoice = store.put(makeSegmentChoice(segment, bassProgram, bassSequence, bassVoice, bass));
    var padChoice = store.put(makeSegmentChoice(segment, padProgram, padSequence, padVoice, pad));
    var stabChoice = store.put(makeSegmentChoice(segment, stabProgram, stabSequence, stabVoice, stab));
    var chord0 = store.put(makeChord(segment, 0.0, "C"));
    store.put(makeVoicing(chord0, Instrument.Type.Bass, "C1, E1, G1, C2, E2, G2, C3, E3"));
    store.put(makeVoicing(chord0, Instrument.Type.Pad, "E3, G3, C4, E4, G4, C5, E5, G5, C6"));
    store.put(makeVoicing(chord0, Instrument.Type.Stab, "E3, G3, C4, E4, G4, C5, E5, G5, C6"));
    var chord1 = store.put(makeChord(segment, 3.0, "F"));
    store.put(makeVoicing(chord1, Instrument.Type.Bass, "F1, A1, C2, F2, A2, C3"));
    store.put(makeVoicing(chord1, Instrument.Type.Pad, "F3, A3, C4, F4, A4, C5, F5, A5, C6"));
    store.put(makeVoicing(chord1, Instrument.Type.Stab, "F3, A3, C4, F4, A4, C5, F5, A5, C6"));


    // Execute
    var subject = fabricate(segment);
    subject.craftArrangements(bassChoice);
    subject.craftArrangements(padChoice);
    subject.craftArrangements(stabChoice);


    // Assert
    var result = store.getSegment(segment.getId()).orElseThrow();
    assertEquals("C", result.getKey());
    assertEquals(20, fabricator.getPicks().size());

    // Assert Bass: list of notes in order they are played
    assertEquals(ImmutableList.of("C1", "C2", "C1", "C2", "C1", "C2", "F1", "F2"),
      fabricator.getPicks().stream()
        .filter(pick -> like(pick, "Bass", 0.25))
        .sorted(Comparator.comparing(SegmentChoiceArrangementPick::getStart))
        .map(SegmentChoiceArrangementPick::getNote)
        .collect(Collectors.toList()));

    // Assert Pad: Set of notes is in no particular order for each of the two sustained chords
    assertEquals(ImmutableSet.of("E4", "G4", "C5"),
      fabricator.getPicks().stream()
        .filter(pick -> like(pick, "Pad", 0.0, 1.5))
        .map(SegmentChoiceArrangementPick::getNote)
        .collect(Collectors.toSet()));
    assertEquals(ImmutableSet.of("F4", "A4", "C5"),
      fabricator.getPicks().stream()
        .filter(pick -> like(pick, "Pad", 1.5, 0.5))
        .map(SegmentChoiceArrangementPick::getNote)
        .collect(Collectors.toSet()));

    // Assert Pad: Set of notes is in no particular order for each of the two chord hits
    assertEquals(ImmutableSet.of("E4", "G4", "C5"),
      fabricator.getPicks().stream()
        .filter(pick -> like(pick, "Stab", 0.0, 0.25))
        .map(SegmentChoiceArrangementPick::getNote)
        .collect(Collectors.toSet()));
    assertEquals(ImmutableSet.of("F4", "A4", "C5"),
      fabricator.getPicks().stream()
        .filter(pick -> like(pick, "Stab", 1.5, 0.25))
        .map(SegmentChoiceArrangementPick::getNote)
        .collect(Collectors.toSet()));
  }

  @SuppressWarnings("SameParameterValue")
  private boolean like(SegmentChoiceArrangementPick pick, String name, double startAtSeconds, double lengthSeconds) {
    return pick.getName().equals(name)
      && pick.getStart() == startAtSeconds
      && pick.getLength() == lengthSeconds;
  }

  @SuppressWarnings("SameParameterValue")
  private boolean like(SegmentChoiceArrangementPick pick, String name, double length) {
    return pick.getName().equals(name)
      && pick.getLength() == length;
  }

  private ArrangementCraftImpl fabricate(Segment segment) throws NexusException {
    fabricator = fabrication.fabricate(HubClientAccess.internal(), segment);
    return new ArrangementCraftImpl(fabricator);
  }
}
