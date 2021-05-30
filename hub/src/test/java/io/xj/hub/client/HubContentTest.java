package io.xj.hub.client;

import com.google.api.client.util.Lists;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.xj.Instrument;
import io.xj.InstrumentAudioEvent;
import io.xj.hub.HubContentFixtures;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityModule;
import io.xj.lib.entity.common.Topology;
import io.xj.lib.jsonapi.JsonApiModule;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class HubContentTest {
  private HubContentFixtures fake;
  private HubContent subject;

  @Before
  public void setUp() throws Exception {
    Config config = ConfigFactory.parseResources("test.conf");
    var injector = Guice.createInjector(
      ImmutableSet.of(
        new HubClientModule(),
        new EntityModule(),
        new JsonApiModule(),
        new AbstractModule() {
          @Override
          protected void configure() {
            bind(Config.class).toInstance(config);
          }
        }));
    Topology.buildHubApiTopology(injector.getInstance(EntityFactory.class));

    fake = new HubContentFixtures();
    subject = new HubContent(Streams.concat(
      fake.setupFixtureB1(false).stream(),
      fake.setupFixtureB2().stream(),
      fake.setupFixtureB3().stream()
    ).collect(Collectors.toList()));
  }

  @Test
  public void getAvailableOffsets() {
    List<Long> result = Lists.newArrayList(subject.getAvailableOffsets(fake.program4_sequence1_binding0));
    Collections.sort(result);

    assertEquals(ImmutableList.of(0L, 1L, 2L), result);
  }

  @Test
  public void toStringOutput() {
    assertEquals("1 Instrument, 4 InstrumentAudio, 4 InstrumentAudioEvent, 1 InstrumentMeme, 8 Program, 6 ProgramMeme, 11 ProgramSequence, 9 ProgramSequenceBinding, 11 ProgramSequenceBindingMeme, 9 ProgramSequenceChord, 3 ProgramSequenceChordVoicing, 6 ProgramSequencePattern, 24 ProgramSequencePatternEvent, 2 ProgramVoice, 20 ProgramVoiceTrack", subject.toString());
  }

  /**
   [#177052278] Chain should always be able to determine main sequence binding offset
   */
  @Test
  public void getProgramSequenceBindingsAtOffset() {
    var result = subject.getProgramSequenceBindingsAtOffset(fake.program5, 0L);

    assertEquals(fake.program5_sequence0_binding0.getId(), result.iterator().next().getId());
  }

  /**
   [#177052278] Chain should always be able to determine main sequence binding offset
   */
  @Test
  public void getProgramSequenceBindingsAtOffset_second() {
    var result = subject.getProgramSequenceBindingsAtOffset(fake.program5, 1L);

    assertEquals(fake.program5_sequence1_binding1.getId(), result.iterator().next().getId());
  }

  /**
   [#177052278] Chain should always be able to determine main sequence binding offset
   */
  @Test
  public void getProgramSequenceBindingsAtOffset_nearestAvailableBeforeZero() {
    var result = subject.getProgramSequenceBindingsAtOffset(fake.program5, -1L);

    assertEquals(fake.program5_sequence0_binding0.getId(), result.iterator().next().getId());
  }

  /**
   [#177052278] Chain should always be able to determine main sequence binding offset
   */
  @Test
  public void getProgramSequenceBindingsAtOffset_nearestAvailablePastEnd() {
    var result = subject.getProgramSequenceBindingsAtOffset(fake.program5, 3L);

    assertEquals(fake.program5_sequence1_binding1.getId(), result.iterator().next().getId());
  }

  @Test
  public void getInstrumentsOfType() throws HubClientException {
    Collection<Instrument> result = subject.getInstrumentsOfType(Instrument.Type.Percussive);

    assertEquals(1, result.size());
  }

  @Test
  public void getFirstEventsOfAudiosOfInstrument() throws HubClientException {
    Collection<InstrumentAudioEvent> result = subject.getFirstEventsOfAudiosOfInstrument(fake.instrument8);

    Collection<String> resultNotes = result.stream().map(InstrumentAudioEvent::getNote).collect(Collectors.toList());
    assertEquals(4, resultNotes.size());
  }

  @Test
  public void size() {
    assertEquals(119, subject.size());
  }
}
