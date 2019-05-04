//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.ingest.Ingest;
import io.xj.core.ingest.IngestFactory;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio_chord.AudioChord;
import io.xj.core.model.audio_event.AudioEvent;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.instrument_meme.InstrumentMeme;
import io.xj.core.model.library.Library;
import io.xj.core.model.meme.Meme;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern_chord.PatternChord;
import io.xj.core.model.pattern_event.PatternEvent;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.sequence_meme.SequenceMeme;
import io.xj.core.model.sequence_pattern.SequencePattern;
import io.xj.core.model.sequence_pattern_meme.SequencePatternMeme;
import io.xj.core.model.voice.Voice;
import io.xj.core.util.TimestampUTC;
import io.xj.music.PitchClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(MockitoJUnitRunner.class)
public class IngestIT extends BaseIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  private IngestFactory ingestFactory;

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();
    insertLibraryA();
    ingestFactory = injector.getInstance(IngestFactory.class);
  }

  @Test
  public void evaluate() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Ingest result = ingestFactory.evaluate(access, ImmutableList.of(new Library(10000001)));

    assertEquals(PitchClass.Cs, result.getKeyOfPattern(BigInteger.valueOf(901)).getRootPitchClass());
  }

  @Test
  public void getSequenceMap() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    Map<BigInteger, Sequence> result = ingest.getSequenceMap();

    assertEquals(3, result.size());
    assertEquals("leaves", result.get(BigInteger.valueOf(701)).getName());
    assertEquals("coconuts", result.get(BigInteger.valueOf(702)).getName());
    assertEquals("bananas", result.get(BigInteger.valueOf(703)).getName());
  }

  @Test
  public void getPatternMap() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    Map<BigInteger, Pattern> result = ingest.getPatternMap();

    assertEquals(2, result.size());
    assertEquals("growth", result.get(BigInteger.valueOf(901)).getName());
    assertEquals("decay", result.get(BigInteger.valueOf(902)).getName());
  }

  @Test
  public void getInstrumentMap() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    Map<BigInteger, Instrument> result = ingest.getInstrumentMap();

    assertEquals(2, result.size());
    assertEquals("808 Drums", result.get(BigInteger.valueOf(201)).getDescription());
    assertEquals("909 Drums", result.get(BigInteger.valueOf(202)).getDescription());
  }

  @Test
  public void getAllSequences() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));
    assertEquals(3, ingest.getAllSequences().size());
  }

  @Test
  public void getSequencesOfType() throws Exception {
    IntegrationTestEntity.insertSequence(711, 101, 10000001, SequenceType.Rhythm, SequenceState.Published, "cups", 0.342, "B", 120.4);
    IntegrationTestEntity.insertSequence(712, 101, 10000001, SequenceType.Main, SequenceState.Published, "plates", 0.342, "Bb", 120.4);
    IntegrationTestEntity.insertSequence(715, 101, 10000001, SequenceType.Rhythm, SequenceState.Published, "bowls", 0.342, "A", 120.4);
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals(3, ingest.getSequencesOfType(SequenceType.Main).size());
    assertEquals(2, ingest.getSequencesOfType(SequenceType.Rhythm).size());
    assertEquals(1, ingest.getSequencesOfType(SequenceType.Detail).size());
  }

  @Test
  public void access() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User,Artist",
      "accounts", "1"
    ));
    Ingest ingest = ingestFactory.evaluate(access, ImmutableList.of(new Library(10000001)));

    assertEquals(access, ingest.getAccess());
  }

  @Test
  public void getSequence() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals("leaves", ingest.getSequence(BigInteger.valueOf(701)).getName());
    assertEquals("coconuts", ingest.getSequence(BigInteger.valueOf(702)).getName());
    assertEquals("bananas", ingest.getSequence(BigInteger.valueOf(703)).getName());
  }

  @Test
  public void getSequence_exceptionOnMissing() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Cannot fetch entity");
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    ingest.getSequence(BigInteger.valueOf(79972));
  }

  @Test
  public void getAllSequenceMemes() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals(3, ingest.getAllSequenceMemes().size());
  }

  @Test
  public void getSequenceMemesOfSequence() throws Exception {
    IntegrationTestEntity.insertSequenceMeme(701, "More Ants", TimestampUTC.now());
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals(2, ingest.getSequenceMemesOfSequence(BigInteger.valueOf(701)).size());
    assertEquals(1, ingest.getSequenceMemesOfSequence(BigInteger.valueOf(702)).size());
    assertEquals(1, ingest.getSequenceMemesOfSequence(BigInteger.valueOf(703)).size());
  }

  @Test
  public void getMemesAtBeginningOfSequence() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    Collection<Meme> results = ingest.getMemesAtBeginningOfSequence(BigInteger.valueOf(701));

    assertEquals(2, results.size());
    Iterator<Meme> resultsIterator = results.iterator();
    Meme result0 = resultsIterator.next();
    assertSame(SequenceMeme.class, result0.getClass());
    assertEquals("Ants", result0.getName());
    Meme result1 = resultsIterator.next();
    assertSame(SequencePatternMeme.class, result1.getClass());
    assertEquals("Gravel", result1.getName());
  }

  @Test
  public void getAllPatterns() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals(2, ingest.getAllPatterns().size());
  }

  @Test
  public void getPatternsOfSequence() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals(2, ingest.getPatternsOfSequence(BigInteger.valueOf(701)).size());
  }

  @Test
  public void getAllSequencePatternMemes() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals(8, ingest.getAllSequencePatternMemes().size());
  }

  @Test
  public void getAllSequencePatternsOfSequence() throws Exception {
    Ingest ingest1 = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));
    assertEquals(6, ingest1.getSequencePatternsOfSequence(BigInteger.valueOf(701)).size());

    Ingest ingest2 = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000002)));
    assertEquals(2, ingest2.getSequencePatternsOfSequence(BigInteger.valueOf(751)).size());
  }

  @Test
  public void getSequencePatternsOfSequenceAtOffset() throws Exception {
    IntegrationTestEntity.insertSequencePattern(999, 701, 902, 5, TimestampUTC.now());
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals(1, ingest.getSequencePatternsOfSequenceAtOffset(BigInteger.valueOf(701), BigInteger.valueOf(2)).size());
    assertEquals(2, ingest.getSequencePatternsOfSequenceAtOffset(BigInteger.valueOf(701), BigInteger.valueOf(5)).size());
  }

  @Test
  public void getAllInstruments() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals(2, ingest.getAllInstruments().size());
  }

  @Test
  public void getInstrumentsOfType() throws Exception {
    IntegrationTestEntity.insertInstrument(1201, 10000001, 101, "Dreamy", InstrumentType.Harmonic, 0.9, TimestampUTC.now());
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals(2, ingest.getInstrumentsOfType(InstrumentType.Percussive).size());
    assertEquals(1, ingest.getInstrumentsOfType(InstrumentType.Harmonic).size());
  }

  @Test
  public void getInstrument() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals("808 Drums", ingest.getInstrument(BigInteger.valueOf(201)).getDescription());
  }

  @Test
  public void getFirstEventsOfAudiosOfInstrument() throws Exception {
    IntegrationTestEntity.insertAudioEvent(402, 0, 1, "PING", "G", 0.1, 0.8, TimestampUTC.now());
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals(2, ingest.getFirstEventsOfAudiosOfInstrument(BigInteger.valueOf(201)).size());
  }

  @Test
  public void getAllInstrumentMemes() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals(3, ingest.getAllInstrumentMemes().size());
  }

  @Test
  public void getMemesOfInstrument() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals(2, ingest.getMemesOfInstrument(BigInteger.valueOf(201)).size());
    assertEquals(1, ingest.getMemesOfInstrument(BigInteger.valueOf(202)).size());
  }

  @Test
  public void getAllLibraries() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals(1, ingest.getAllLibraries().size());
  }

  @Test
  public void getAllAudios() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals(2, ingest.getAllAudios().size());
  }

  @Test
  public void getAudiosOfInstrument() throws Exception {
    IntegrationTestEntity.insertAudio(403, 202, "Published", "Chords Cm to D", "instrument/percussion/909/kick1.wav", 0.01, 2.123, 120.0, 440, TimestampUTC.now());
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals(2, ingest.getAudiosOfInstrument(BigInteger.valueOf(201)).size());
    assertEquals(1, ingest.getAudiosOfInstrument(BigInteger.valueOf(202)).size());
  }

  @Test
  public void getAudiosEventsOfInstrument() throws Exception {
    IntegrationTestEntity.insertAudio(403, 202, "Published", "Chords Cm to D", "instrument/percussion/909/kick1.wav", 0.01, 2.123, 120.0, 440, TimestampUTC.now());
    IntegrationTestEntity.insertAudioEvent(403, 0, 1, "X", "C", 1, 1);
    IntegrationTestEntity.insertAudioEvent(403, 1, 1, "X", "C", 1, 1);
    IntegrationTestEntity.insertAudioEvent(403, 2, 1, "X", "C", 1, 1);
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals(4, ingest.getAudioEventsOfInstrument(BigInteger.valueOf(201)).size());
    assertEquals(3, ingest.getAudioEventsOfInstrument(BigInteger.valueOf(202)).size());
  }

  @Test
  public void getAudio() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals("Beat", ingest.getAudio(BigInteger.valueOf(401)).getName());
  }

  @Test
  public void getKeyOfPattern() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals("C#", ingest.getKeyOfPattern(BigInteger.valueOf(901)).toString());
    assertEquals("F#", ingest.getKeyOfPattern(BigInteger.valueOf(902)).toString());
  }

  @Test
  public void getAllAudioChords() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals(6, ingest.getAllAudioChords().size());
  }

  @Test
  public void getAllAudioEvents() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals(4, ingest.getAllAudioEvents().size());
  }

  @Test
  public void getAllPatternChords() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals(6, ingest.getAllPatternChords().size());
  }

  @Test
  public void getChordsOfPattern() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals(6, ingest.getChordsOfPattern(BigInteger.valueOf(902)).size());
    assertEquals(0, ingest.getChordsOfPattern(BigInteger.valueOf(901)).size());
  }

  @Test
  public void getAllVoices() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals(2, ingest.getAllVoices().size());
  }

  @Test
  public void getVoicesOfSequence() throws Exception {
    IntegrationTestEntity.insertVoice(1299, 702, InstrumentType.Harmonic, "Bass", TimestampUTC.now());
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals(1, ingest.getVoicesOfSequence(BigInteger.valueOf(701)).size());
    assertEquals(2, ingest.getVoicesOfSequence(BigInteger.valueOf(702)).size());
  }

  @Test
  public void getAllPatternEvents() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals(4, ingest.getAllPatternEvents().size());
  }

  @Test
  public void getEventsOfPatternByVoice() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals(4, ingest.getEventsOfPatternByVoice(BigInteger.valueOf(901), BigInteger.valueOf(1201)).size());
    assertEquals(0, ingest.getEventsOfPatternByVoice(BigInteger.valueOf(901), BigInteger.valueOf(1202)).size());
  }

  @Test
  public void getAudioMap() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    Map<BigInteger, Audio> result = ingest.getAudioMap();

    assertEquals(2, result.size());
    assertEquals("Beat", result.get(BigInteger.valueOf(401)).getName());
    assertEquals("Chords Cm to D", result.get(BigInteger.valueOf(402)).getName());
  }

  @Test
  public void getAllEntities() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    Collection<Entity> result = ingest.getAllEntities();

    assertEquals(52, result.size());
    Map<Class, Integer> classes = classTally(result);
    assertEquals(Integer.valueOf(1), classes.get(Library.class));
    assertEquals(Integer.valueOf(2), classes.get(Instrument.class));
    assertEquals(Integer.valueOf(3), classes.get(InstrumentMeme.class));
    assertEquals(Integer.valueOf(2), classes.get(Audio.class));
    assertEquals(Integer.valueOf(6), classes.get(AudioChord.class));
    assertEquals(Integer.valueOf(4), classes.get(AudioEvent.class));
    assertEquals(Integer.valueOf(3), classes.get(Sequence.class));
    assertEquals(Integer.valueOf(3), classes.get(SequenceMeme.class));
    assertEquals(Integer.valueOf(2), classes.get(Pattern.class));
    assertEquals(Integer.valueOf(6), classes.get(PatternChord.class));
    assertEquals(Integer.valueOf(2), classes.get(Voice.class));
    assertEquals(Integer.valueOf(4), classes.get(PatternEvent.class));
    assertEquals(Integer.valueOf(6), classes.get(SequencePattern.class));
    assertEquals(Integer.valueOf(8), classes.get(SequencePatternMeme.class));
  }

  @Test
  public void toStringOutput() throws Exception {
    Ingest ingest = ingestFactory.evaluate(Access.internal(), ImmutableList.of(new Library(10000001)));

    assertEquals("4 PatternEvent, 2 Pattern, 6 PatternChord, 8 SequencePatternMeme, 3 Sequence, 3 InstrumentMeme, 2 Instrument, 4 AudioEvent, 1 Library, 2 Audio, 6 AudioChord, 3 SequenceMeme, 6 SequencePattern, 2 Voice", ingest.toString());
  }

  private static Map<Class, Integer> classTally(Collection<Entity> allEntities) {
    Map<Class, Integer> out = Maps.newConcurrentMap();
    allEntities.forEach(entity -> {
      Class clazz = entity.getClass();
      out.put(clazz, out.containsKey(clazz) ? out.get(clazz) + 1 : 1);
    });
    return out;
  }

}
