package io.xj.service.hub.client;

import com.google.api.client.util.Lists;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.xj.Instrument;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.HubContentFixtures;
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
    Injector injector = Guice.createInjector(
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
    HubApp.buildApiTopology(injector.getInstance(EntityFactory.class));

    fake = new HubContentFixtures();
    subject = new HubContent(Streams.concat(
      fake.setupFixtureB1(false).stream(),
      fake.setupFixtureB2().stream(),
      fake.setupFixtureB3().stream()
    ).collect(Collectors.toList()));
  }

  @Test
  public void getAvailableOffsets() throws HubClientException {
    List<Long> result = Lists.newArrayList(subject.getAvailableOffsets(fake.program4_sequence1_binding0));
    Collections.sort(result);

    assertEquals(ImmutableList.of(0L, 1L, 2L), result);
  }

  @Test
  public void toStringOutput() {
    assertEquals("1 Instrument, 4 InstrumentAudio, 4 InstrumentAudioEvent, 1 InstrumentMeme, 8 Program, 6 ProgramMeme, 11 ProgramSequence, 9 ProgramSequenceBinding, 11 ProgramSequenceBindingMeme, 9 ProgramSequenceChord, 3 ProgramSequenceChordVoicing, 6 ProgramSequencePattern, 24 ProgramSequencePatternEvent, 2 ProgramVoice, 20 ProgramVoiceTrack", subject.toString());
  }

  @Test
  public void getInstrumentAudioChord() {
    // FUTURE Collection<Object> result = subject.getInstrumentAudioChord();
  }

  @Test
  public void getInstrumentAudio() {
    // FUTURE Collection<Object> result = subject.getInstrumentAudio();
  }

  @Test
  public void getInstrumentAudioEvent() {
    // FUTURE Collection<Object> result = subject.getInstrumentAudioEvent();
  }

  @Test
  public void getInstrument() {
    // FUTURE Collection<Object> result = subject.getInstrument();
  }

  @Test
  public void getInstrumentMeme() {
    // FUTURE Collection<Object> result = subject.getInstrumentMeme();
  }

  @Test
  public void getProgram() {
    // FUTURE Collection<Object> result = subject.getProgram();
  }

  @Test
  public void getProgramEvent() {
    // FUTURE Collection<Object> result = subject.getProgramEvent();
  }

  @Test
  public void getProgramMeme() {
    // FUTURE Collection<Object> result = subject.getProgramMeme();
  }

  @Test
  public void getProgramPattern() {
    // FUTURE Collection<Object> result = subject.getProgramPattern();
  }

  @Test
  public void getProgramSequenceBinding() {
    // FUTURE Collection<Object> result = subject.getProgramSequenceBinding();
  }

  @Test
  public void getProgramSequenceBindingMeme() {
    // FUTURE Collection<Object> result = subject.getProgramSequenceBindingMeme();
  }

  @Test
  public void getProgramSequenceChord() {
    // FUTURE Collection<Object> result = subject.getProgramSequenceChord();
  }

  @Test
  public void getProgramSequence() {
    // FUTURE Collection<Object> result = subject.getProgramSequence();
  }

  @Test
  public void getProgramTrack() {
    // FUTURE Collection<Object> result = subject.getProgramTrack();
  }

  @Test
  public void getProgramVoice() {
    // FUTURE Collection<Object> result = subject.getProgramVoice();
  }

  @Test
  public void getAllPrograms() {
    // FUTURE Collection<Object> result = subject.getAllPrograms();
  }

  @Test
  public void getAllInstrumentAudios() {
    // FUTURE Collection<Object> result = subject.getAllInstrumentAudios();
  }

  @Test
  public void getAllInstrumentAudioChords() {
    // FUTURE Collection<Object> result = subject.getAllInstrumentAudioChords();
  }

  @Test
  public void getAllInstrumentAudioEvents() {
    // FUTURE Collection<Object> result = subject.getAllInstrumentAudioEvents();
  }

  @Test
  public void getAllInstrumentMemes() {
    // FUTURE Collection<Object> result = subject.getAllInstrumentMemes();
  }

  @Test
  public void getAllProgramSequencePatternEvents() {
    // FUTURE Collection<Object> result = subject.getAllProgramSequencePatternEvents();
  }

  @Test
  public void getAllProgramMemes() {
    // FUTURE Collection<Object> result = subject.getAllProgramMemes();
  }

  @Test
  public void getAllProgramSequencePatterns() {
    // FUTURE Collection<Object> result = subject.getAllProgramSequencePatterns();
  }

  @Test
  public void getAllProgramSequences() {
    // FUTURE Collection<Object> result = subject.getAllProgramSequences();
  }

  @Test
  public void getAllProgramSequenceBindings() {
    // FUTURE Collection<Object> result = subject.getAllProgramSequenceBindings();
  }

  @Test
  public void getAllProgramSequenceBindingMemes() {
    // FUTURE Collection<Object> result = subject.getAllProgramSequenceBindingMemes();
  }

  @Test
  public void getAllProgramSequenceChords() {
    // FUTURE Collection<Object> result = subject.getAllProgramSequenceChords();
  }

  @Test
  public void getAllProgramTracks() {
    // FUTURE Collection<Object> result = subject.getAllProgramTracks();
  }

  @Test
  public void getAllProgramVoices() {
    // FUTURE Collection<Object> result = subject.getAllProgramVoices();
  }

  @Test
  public void getAllInstruments() {
    // FUTURE Collection<Object> result = subject.getAllInstruments();
  }

  @Test
  public void getProgramsOfType() {
    // FUTURE Collection<Object> result = subject.getProgramsOfType();
  }

  @Test
  public void getInstrumentsOfType() throws HubClientException {
    Collection<Instrument> result = subject.getInstrumentsOfType(Instrument.Type.Percussive);

    assertEquals(1, result.size());
  }

  @Test
  public void getAllEntities() {
    // FUTURE Collection<Object> result = subject.getAllEntities();
  }

  @Test
  public void getMemes() {
    // FUTURE Collection<Object> result = subject.getMemes();
  }

  @Test
  public void getEvents() {
    // FUTURE Collection<Object> result = subject.getEvents();
  }

  @Test
  public void getAudiosForInstrumentId() {
    // FUTURE Collection<Object> result = subject.getAudiosForInstrumentId();
  }

  @Test
  public void getAudios() {
    // FUTURE Collection<Object> result = subject.getAudios();
  }

  @Test
  public void getChords() {
    // FUTURE Collection<Object> result = subject.getChords();
  }

  @Test
  public void getSequenceBindings() {
    // FUTURE Collection<Object> result = subject.getSequenceBindings();
  }

  @Test
  public void getSequences() {
    // FUTURE Collection<Object> result = subject.getSequences();
  }

  @Test
  public void getSequence() {
    // FUTURE Collection<Object> result = subject.getSequence();
  }

  @Test
  public void getFirstEventsOfAudiosOfInstrument() {
    // FUTURE Collection<Object> result = subject.getFirstEventsOfAudiosOfInstrument();
  }

  @Test
  public void getMemesAtBeginning() {
    // FUTURE Collection<Object> result = subject.getMemesAtBeginning();
  }

  @Test
  public void getProgramSequenceBindingsAtOffset() {
    // FUTURE Collection<Object> result = subject.getProgramSequenceBindingsAtOffset();
  }

  @Test
  public void getVoice() {
    // FUTURE Collection<Object> result = subject.getVoice();
  }

  @Test
  public void getTrack() {
    // FUTURE Collection<Object> result = subject.getTrack();
  }

  @Test
  public void getVoices() {
    // FUTURE Collection<Object> result = subject.getVoices();
  }

  @Test
  public void size() {
    assertEquals(119, subject.size());
  }
}
