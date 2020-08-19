package io.xj.service.hub.client;

import com.google.api.client.util.Lists;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import io.xj.lib.entity.Entity;
import io.xj.service.hub.HubContentFixtures;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.entity.Instrument;
import io.xj.service.hub.entity.InstrumentType;
import io.xj.service.hub.ingest.HubIngest;
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
  public void toStringOutput() throws Exception {
    assertEquals("1 Instrument, 4 InstrumentAudio, 4 InstrumentAudioEvent, 1 InstrumentMeme, 8 Program, 6 ProgramMeme, 11 ProgramSequence, 9 ProgramSequenceBinding, 11 ProgramSequenceBindingMeme, 9 ProgramSequenceChord, 6 ProgramSequencePattern, 24 ProgramSequencePatternEvent, 2 ProgramVoice, 20 ProgramVoiceTrack", subject.toString());
  }

  @Test
  public void getInstrumentAudioChord() {
    // FUTURE Collection<Entity> result = subject.getInstrumentAudioChord();
  }

  @Test
  public void getInstrumentAudio() {
    // FUTURE Collection<Entity> result = subject.getInstrumentAudio();
  }

  @Test
  public void getInstrumentAudioEvent() {
    // FUTURE Collection<Entity> result = subject.getInstrumentAudioEvent();
  }

  @Test
  public void getInstrument() {
    // FUTURE Collection<Entity> result = subject.getInstrument();
  }

  @Test
  public void getInstrumentMeme() {
    // FUTURE Collection<Entity> result = subject.getInstrumentMeme();
  }

  @Test
  public void getProgram() {
    // FUTURE Collection<Entity> result = subject.getProgram();
  }

  @Test
  public void getProgramEvent() {
    // FUTURE Collection<Entity> result = subject.getProgramEvent();
  }

  @Test
  public void getProgramMeme() {
    // FUTURE Collection<Entity> result = subject.getProgramMeme();
  }

  @Test
  public void getProgramPattern() {
    // FUTURE Collection<Entity> result = subject.getProgramPattern();
  }

  @Test
  public void getProgramSequenceBinding() {
    // FUTURE Collection<Entity> result = subject.getProgramSequenceBinding();
  }

  @Test
  public void getProgramSequenceBindingMeme() {
    // FUTURE Collection<Entity> result = subject.getProgramSequenceBindingMeme();
  }

  @Test
  public void getProgramSequenceChord() {
    // FUTURE Collection<Entity> result = subject.getProgramSequenceChord();
  }

  @Test
  public void getProgramSequence() {
    // FUTURE Collection<Entity> result = subject.getProgramSequence();
  }

  @Test
  public void getProgramTrack() {
    // FUTURE Collection<Entity> result = subject.getProgramTrack();
  }

  @Test
  public void getProgramVoice() {
    // FUTURE Collection<Entity> result = subject.getProgramVoice();
  }

  @Test
  public void getAllPrograms() {
    // FUTURE Collection<Entity> result = subject.getAllPrograms();
  }

  @Test
  public void getAllInstrumentAudios() {
    // FUTURE Collection<Entity> result = subject.getAllInstrumentAudios();
  }

  @Test
  public void getAllInstrumentAudioChords() {
    // FUTURE Collection<Entity> result = subject.getAllInstrumentAudioChords();
  }

  @Test
  public void getAllInstrumentAudioEvents() {
    // FUTURE Collection<Entity> result = subject.getAllInstrumentAudioEvents();
  }

  @Test
  public void getAllInstrumentMemes() {
    // FUTURE Collection<Entity> result = subject.getAllInstrumentMemes();
  }

  @Test
  public void getAllProgramSequencePatternEvents() {
    // FUTURE Collection<Entity> result = subject.getAllProgramSequencePatternEvents();
  }

  @Test
  public void getAllProgramMemes() {
    // FUTURE Collection<Entity> result = subject.getAllProgramMemes();
  }

  @Test
  public void getAllProgramSequencePatterns() {
    // FUTURE Collection<Entity> result = subject.getAllProgramSequencePatterns();
  }

  @Test
  public void getAllProgramSequences() {
    // FUTURE Collection<Entity> result = subject.getAllProgramSequences();
  }

  @Test
  public void getAllProgramSequenceBindings() {
    // FUTURE Collection<Entity> result = subject.getAllProgramSequenceBindings();
  }

  @Test
  public void getAllProgramSequenceBindingMemes() {
    // FUTURE Collection<Entity> result = subject.getAllProgramSequenceBindingMemes();
  }

  @Test
  public void getAllProgramSequenceChords() {
    // FUTURE Collection<Entity> result = subject.getAllProgramSequenceChords();
  }

  @Test
  public void getAllProgramTracks() {
    // FUTURE Collection<Entity> result = subject.getAllProgramTracks();
  }

  @Test
  public void getAllProgramVoices() {
    // FUTURE Collection<Entity> result = subject.getAllProgramVoices();
  }

  @Test
  public void getAllInstruments() {
    // FUTURE Collection<Entity> result = subject.getAllInstruments();
  }

  @Test
  public void getProgramsOfType() {
    // FUTURE Collection<Entity> result = subject.getProgramsOfType();
  }

  @Test
  public void getInstrumentsOfType() {
    Collection<Instrument> result = subject.getInstrumentsOfType(InstrumentType.Percussive);

    assertEquals(1, result.size());
  }

  @Test
  public void getAllEntities() {
    // FUTURE Collection<Entity> result = subject.getAllEntities();
  }

  @Test
  public void getMemes() {
    // FUTURE Collection<Entity> result = subject.getMemes();
  }

  @Test
  public void getEvents() {
    // FUTURE Collection<Entity> result = subject.getEvents();
  }

  @Test
  public void getAudiosForInstrumentId() {
    // FUTURE Collection<Entity> result = subject.getAudiosForInstrumentId();
  }

  @Test
  public void getAudios() {
    // FUTURE Collection<Entity> result = subject.getAudios();
  }

  @Test
  public void getChords() {
    // FUTURE Collection<Entity> result = subject.getChords();
  }

  @Test
  public void getSequenceBindings() {
    // FUTURE Collection<Entity> result = subject.getSequenceBindings();
  }

  @Test
  public void getSequences() {
    // FUTURE Collection<Entity> result = subject.getSequences();
  }

  @Test
  public void getSequence() {
    // FUTURE Collection<Entity> result = subject.getSequence();
  }

  @Test
  public void getFirstEventsOfAudiosOfInstrument() {
    // FUTURE Collection<Entity> result = subject.getFirstEventsOfAudiosOfInstrument();
  }

  @Test
  public void getMemesAtBeginning() {
    // FUTURE Collection<Entity> result = subject.getMemesAtBeginning();
  }

  @Test
  public void getProgramSequenceBindingsAtOffset() {
    // FUTURE Collection<Entity> result = subject.getProgramSequenceBindingsAtOffset();
  }

  @Test
  public void getVoice() {
    // FUTURE Collection<Entity> result = subject.getVoice();
  }

  @Test
  public void getTrack() {
    // FUTURE Collection<Entity> result = subject.getTrack();
  }

  @Test
  public void getVoices() {
    // FUTURE Collection<Entity> result = subject.getVoices();
  }

  @Test
  public void size() {
    assertEquals(116, subject.size());
  }
}
