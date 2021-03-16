package io.xj.service.nexus.craft.arrangement;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import datadog.trace.api.Trace;
import io.xj.Instrument;
import io.xj.InstrumentAudio;
import io.xj.InstrumentAudioEvent;
import io.xj.ProgramSequence;
import io.xj.ProgramSequencePattern;
import io.xj.ProgramSequencePatternEvent;
import io.xj.ProgramVoice;
import io.xj.SegmentChoiceArrangement;
import io.xj.SegmentChoiceArrangementPick;
import io.xj.SegmentChord;
import io.xj.lib.music.AdjSymbol;
import io.xj.lib.music.Chord;
import io.xj.lib.music.Note;
import io.xj.lib.music.NoteRange;
import io.xj.lib.music.PitchClass;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Chance;
import io.xj.lib.util.Value;
import io.xj.service.nexus.NexusException;
import io.xj.service.nexus.fabricator.EntityScorePicker;
import io.xj.service.nexus.fabricator.FabricationWrapperImpl;
import io.xj.service.nexus.fabricator.NameIsometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Arrangement of Segment Events is a common foundation for both Detail and Rhythm craft
 */
public class ArrangementCraftImpl extends FabricationWrapperImpl {
  private static final double SCORE_ARRANGEMENT_ENTROPY = 0.5;
  private static final Logger log = LoggerFactory.getLogger(ArrangementCraftImpl.class);
  private final SecureRandom random = new SecureRandom();

  /**
   Craft events for a section of one detail voice

   @param chord       (optional) to use for fabrication
   @param sequence    from which to craft events
   @param arrangement of instrument
   @param voice       within program
   @param fromPos     position (in beats)
   @param maxPos      position (in beats)
   @throws NexusException on failure
   */
  @Trace(resourceName = "nexus/craft/arrangement", operationName = "craftArrangementForVoiceSection")
  protected void craftArrangementForVoiceSection(
    @Nullable SegmentChord chord,
    ProgramSequence sequence,
    SegmentChoiceArrangement arrangement,
    ProgramVoice voice,
    double fromPos,
    double maxPos
  ) throws NexusException {
    // choose intro pattern (if available)
    Optional<ProgramSequencePattern> introPattern = fabricator.randomlySelectPatternOfSequenceByVoiceAndType(sequence, voice, ProgramSequencePattern.Type.Intro);

    // choose outro pattern (if available)
    Optional<ProgramSequencePattern> outroPattern = fabricator.randomlySelectPatternOfSequenceByVoiceAndType(sequence, voice, ProgramSequencePattern.Type.Outro);

    // compute in and out points, and length # beats for which loop patterns will be required
    long loopOutPos = (int) maxPos - (outroPattern.map(ProgramSequencePattern::getTotal).orElse(0));

    // begin at the beginning and fabricate events for the segment of beginning to end
    double curPos = fromPos;

    // if intro pattern, fabricate those voice event first
    if (introPattern.isPresent())
      curPos += craftPatternEvents(chord, arrangement, introPattern.get(), curPos, loopOutPos);

    // choose loop patterns until arrive at the out point or end of segment
    while (curPos < loopOutPos) {
      Optional<ProgramSequencePattern> loopPattern = fabricator.randomlySelectPatternOfSequenceByVoiceAndType(sequence, voice, ProgramSequencePattern.Type.Loop);
      if (loopPattern.isPresent())
        curPos += craftPatternEvents(chord, arrangement, loopPattern.get(), curPos, loopOutPos);
      else
        curPos = loopOutPos;
    }

    // if outro pattern, fabricate those voice event last
    // [#161466708] compute how much to go for it in the outro
    if (outroPattern.isPresent())
      craftPatternEvents(chord, arrangement, outroPattern.get(), curPos, loopOutPos);
  }

  /**
   Craft the voice events of a single pattern.
   [#161601279] Artist during craft audio selection wants randomness of outro audio selection to gently ramp of zero to N over the course of the outro.

   @param chord       (optional) to use for fabrication
   @param arrangement to craft pattern events for
   @param pattern     to source events
   @param fromPos     to write events to segment
   @param maxPos      to write events to segment
   @return deltaPos of start, after crafting this batch of pattern events
   */
  @Trace(resourceName = "nexus/craft/arrangement", operationName = "craftPatternEvents")
  protected double craftPatternEvents(
    @Nullable SegmentChord chord,
    SegmentChoiceArrangement arrangement,
    ProgramSequencePattern pattern,
    double fromPos,
    double maxPos
  ) throws NexusException {
    if (Objects.isNull(pattern)) throw new NexusException("Cannot craft create null pattern");
    double totalPos = maxPos - fromPos;
    Collection<ProgramSequencePatternEvent> events = fabricator.getSourceMaterial().getEvents(pattern);
    var instrument = fabricator.getSourceMaterial().getInstrument(arrangement.getInstrumentId())
      .orElseThrow(() -> new NexusException("Failed to retrieve instrument"));
    for (ProgramSequencePatternEvent event : events)
      pickInstrumentAudios(chord, instrument, arrangement, event, fromPos);
    return Math.min(totalPos, pattern.getTotal());
  }

  /**
   of a pick of instrument-audio for each event, where events are conformed to entities/scales based on the master segment entities
   pick instrument audio for one event, in a voice in a pattern, belonging to an arrangement

   @param chord         (optional) to use for fabrication
   @param event         to pick audio for
   @param shiftPosition offset voice event zero within current segment
   */
  @Trace(resourceName = "nexus/craft/arrangement", operationName = "pickInstrumentAudio")
  protected void pickInstrumentAudios(
    @Nullable SegmentChord chord,
    Instrument instrument,
    SegmentChoiceArrangement segmentChoiceArrangement,
    ProgramSequencePatternEvent event,
    Double shiftPosition
  ) throws NexusException {
    // Morph & Point attributes are expressed in beats
    double position = event.getPosition() + shiftPosition;
    double duration = event.getDuration();
    SegmentChord realChord = Value.isNonNull(chord) ? chord :
      fabricator.getChordAt((int) Math.floor(position))
        .orElseThrow(() -> new NexusException("No Segment Chord found!"));
    assert realChord != null;

    // The final note is voiced from the chord voicing (if found) or else the default is used
    Collection<Note> voicingNotes = fabricator.getVoicingNotes(realChord, instrument.getType());
    List<Note> notes = 0 < voicingNotes.size() ?
      getNotes(instrument, segmentChoiceArrangement, event, Chord.of(realChord.getName()), voicingNotes) :
      ImmutableList.of(Note.of(event.getNote()));
    if (notes.isEmpty()) return;

    // Pick attributes are expressed "rendered" as actual seconds
    double startSeconds = fabricator.computeSecondsAtPosition(position);
    double lengthSeconds = fabricator.computeSecondsAtPosition(position + duration) - startSeconds;

    // pick an audio for each note
    for (var note : notes)
      pickInstrumentAudio(note, instrument, event, segmentChoiceArrangement, startSeconds, lengthSeconds);
  }

  /**
   [#176696738] XJ has a serviceable voicing algorithm
   <p>
   [#176474113] Artist can edit comma-separated notes into detail program events
   <p>
   of a pick of instrument-audio for each event, where events are conformed to entities/scales based on the master segment entities
   pick instrument audio for one event, in a voice in a pattern, belonging to an arrangement

   @param note                     to pick audio for
   @param instrument               from which to pick audio
   @param event                    to pick audio for
   @param segmentChoiceArrangement arranging this instrument for a program
   @param startSeconds             of audio
   @param lengthSeconds            of audio
   @throws NexusException on failure
   */
  private void pickInstrumentAudio(
    Note note,
    Instrument instrument,
    ProgramSequencePatternEvent event,
    SegmentChoiceArrangement segmentChoiceArrangement,
    double startSeconds,
    double lengthSeconds
  ) throws NexusException {
    var audio =
      fabricator.getInstrumentConfig(instrument).isMultiphonic() ?
        selectMultiphonicInstrumentAudio(instrument, event, note) :
        selectInstrumentAudio(instrument, event);

    // [#176373977] Should gracefully skip audio in unfulfilled by instrument
    if (audio.isEmpty()) return;

    // Audio pitch is not modified for atonal instruments
    double pitch = fabricator.getInstrumentConfig(instrument).isTonal() ?
      fabricator.getPitch(note) : audio.get().getPitch();

    // of pick
    fabricator.add(SegmentChoiceArrangementPick.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segmentChoiceArrangement.getSegmentId())
      .setSegmentChoiceArrangementId(segmentChoiceArrangement.getId())
      .setInstrumentAudioId(audio.get().getId())
      .setProgramSequencePatternEventId(event.getId())
      .setName(fabricator.getTrackName(event))
      .setStart(startSeconds)
      .setLength(lengthSeconds)
      .setAmplitude(event.getVelocity())
      .setPitch(pitch)
      .build());
  }

  /**
   Pick final note based on instrument type, voice event, transposition and current chord
   <p>
   [#176695166] XJ should choose correct instrument note based on detail program note

   @param instrument               comprising audios
   @param segmentChoiceArrangement chosen program for reference
   @param sourceEvent              of program to pick instrument note for
   @param voicingChord             to use for interpreting the voicing
   @param voicingNotes             to choose a note from
   @return note picked from the available voicing
   */
  private List<Note> getNotes(
    Instrument instrument, SegmentChoiceArrangement segmentChoiceArrangement,
    ProgramSequencePatternEvent sourceEvent,
    Chord voicingChord,
    Collection<Note> voicingNotes
  ) {
    List<Note> notes = Lists.newArrayList();

    for (var note : CSV.split(sourceEvent.getNote()).stream().map(Note::of).collect(Collectors.toList()))
      if (PitchClass.None.equals(note.getPitchClass()))
        pickRandomInstrumentNote(voicingNotes).ifPresent(notes::add);
      else
        try {
          var sourceKey = fabricator.getKeyForArrangement(segmentChoiceArrangement);
          var sourceRange = fabricator.getRangeForArrangement(segmentChoiceArrangement);
          var targetShiftSemitones = sourceKey.getRootPitchClass().delta(voicingChord.getRootPitchClass());
          var targetRange = fabricator.getVoicingNoteRange(instrument.getType());
          var targetRangeShiftOctaves = computeRangeShiftOctaves(instrument.getType(), sourceRange, targetRange);
          var targetNote = note.shift(targetShiftSemitones + 12 * targetRangeShiftOctaves);

          notes.add(voicingNotes
            .stream()
            .map(n -> new RankedNote(n,
              Math.abs(n.delta(targetNote))))
            .min(Comparator.comparing(RankedNote::getDelta))
            .map(RankedNote::getNote)
            .orElseThrow(() -> new NexusException("Note not found")));

        } catch (NexusException e) {
          log.warn("Failed to pick note", e);
        }

    return notes;
  }

  /**
   [#176696738] Detail craft shifts source program events into the target range
   <p>
   via average of delta from source low to target low, and from source high to target high, rounded to octave

   @param type        of instrument
   @param sourceRange to compute from
   @param targetRange to compute required # of octaves to shift into
   @return +/- octaves required to shift from source to target range
   */
  public int computeRangeShiftOctaves(Instrument.Type type, NoteRange sourceRange, NoteRange targetRange) {
    switch (type) {

      case Bass:
        var shiftOctave = 0; // search for optimal value
        var baselineDelta = 100; // optimal is lowest possible integer zero or above
        for (var o = -10; o <= 10; o++) {
          int d = sourceRange.getLow()
            .shiftOctave(o)
            .delta(targetRange.getLow());
          if (0 <= d && d < baselineDelta) {
            baselineDelta = d;
            shiftOctave = o;
          }
        }
        return shiftOctave;

      case Percussive:
      case Pad:
      case Sticky:
      case Stripe:
      case Stab:
      default:
        int dLow = sourceRange.getLow().delta(targetRange.getLow());
        int dHigh = sourceRange.getHigh().delta(targetRange.getHigh());
        return (int) Math.round((dLow + dHigh) / 2.0);
    }
  }

  /**
   Pick a random instrument note from the available notes in the voicing
   <p>
   [#175947230] Artist writing detail program expects 'X' note value to result in random selection from available Voicings

   @param voicingNotes to pick from
   @return a random note from the voicing
   */
  private Optional<Note> pickRandomInstrumentNote(Collection<Note> voicingNotes) {
    return voicingNotes
      .stream()
      .sorted(Comparator.comparing((s) -> random.nextFloat()))
      .findAny();
  }

  /**
   Select audio from a multiphonic instrument
   <p>
   [#176649593] Sampler obeys isMultiphonic from Instrument config

   @param instrument of which to score available audios, and make a selection
   @param event      for caching reference
   @param note       to match
   selection)
   @return matched new audio
   @throws NexusException on failure
   */
  @Trace(resourceName = "nexus/craft/arrangement", operationName = "selectMultiphonicInstrumentAudio")
  protected Optional<InstrumentAudio> selectMultiphonicInstrumentAudio(
    Instrument instrument,
    ProgramSequencePatternEvent event,
    Note note
  ) throws NexusException {
    String key = fabricator.keyByTrackNote(event.getProgramVoiceTrackId(), note);

    if (!fabricator.getPreviousInstrumentAudio().containsKey(key)) {
      var audio = selectNewMultiphonicInstrumentAudio(instrument, note);
      if (audio.isPresent()) fabricator.getPreviousInstrumentAudio().put(key, audio.get());
    }

    return fabricator.getPreviousInstrumentAudio().containsKey(key) ?
      Optional.of(fabricator.getPreviousInstrumentAudio().get(key)) : Optional.empty();
  }

  /**
   Select the cached (already selected for this voice + track name)
   instrument audio based on a pattern event.
   <p>
   If never encountered, default to new selection and cache that.

   @param instrument of which to score available audios, and make a selection
   @param event      to match
   selection)
   @return matched new audio
   @throws NexusException on failure
   */
  @Trace(resourceName = "nexus/craft/arrangement", operationName = "selectInstrumentAudio")
  protected Optional<InstrumentAudio> selectInstrumentAudio(
    Instrument instrument,
    ProgramSequencePatternEvent event
  ) throws NexusException {
    String key = fabricator.keyByVoiceTrack(event);

    if (!fabricator.getPreviousInstrumentAudio().containsKey(key)) {
      var audio = selectNewInstrumentAudio(instrument, event);
      if (audio.isPresent()) fabricator.getPreviousInstrumentAudio().put(key, audio.get());
    }

    return fabricator.getPreviousInstrumentAudio().containsKey(key) ?
      Optional.of(fabricator.getPreviousInstrumentAudio().get(key)) : Optional.empty();
  }

  /**
   Select a new random instrument audio based on a pattern event

   @param instrument of which to score available audios, and make a selection
   @param event      to match
   @return matched new audio
   */
  @Trace(resourceName = "nexus/craft/arrangement", operationName = "selectNewInstrumentAudio")
  protected Optional<InstrumentAudio> selectNewInstrumentAudio(
    Instrument instrument,
    ProgramSequencePatternEvent event
  ) throws NexusException {
    EntityScorePicker<InstrumentAudio> audioEntityScorePicker = new EntityScorePicker<>();

    // add all audio to chooser
    audioEntityScorePicker.addAll(fabricator.getSourceMaterial().getAudios(instrument));

    // score each audio against the current voice event, with some variability
    for (InstrumentAudioEvent audioEvent : fabricator.getSourceMaterial().getFirstEventsOfAudiosOfInstrument(instrument))
      audioEntityScorePicker.score(audioEvent.getInstrumentAudioId(),
        Chance.normallyAround(
          NameIsometry.similarity(fabricator.getTrackName(event), audioEvent.getName()),
          SCORE_ARRANGEMENT_ENTROPY));

    // final chosen audio event
    return audioEntityScorePicker.getTop();
  }

  /**
   Select a new random instrument audio based on a pattern event
   <p>
   [#176649593] Sampler obeys isMultiphonic from Instrument config

   @param instrument of which to score available audios, and make a selection
   @param note       to match
   @return matched new audio
   */
  @Trace(resourceName = "nexus/craft/arrangement", operationName = "selectNewMultiphonicInstrumentAudio")
  protected Optional<InstrumentAudio> selectNewMultiphonicInstrumentAudio(
    Instrument instrument,
    Note note
  ) throws NexusException {
    var audioEvent = fabricator.getFirstEventsOfAudiosOfInstrument(instrument)
      .stream()
      .filter(instrumentAudioEvent ->
        Note.of(instrumentAudioEvent.getNote()).sameAs(note))
      .findAny();

    if (audioEvent.isEmpty()) {
      reportMissing(InstrumentAudio.class, "attempting to pickInstrumentAudio", ImmutableMap.of(
        "instrumentId", instrument.getId(),
        "searchForNote", note.toString(AdjSymbol.Sharp),
        "availableNotes", CSV.from(fabricator.getFirstEventsOfAudiosOfInstrument(instrument)
          .stream()
          .map(InstrumentAudioEvent::getNote)
          .map(Note::of)
          .sorted(Note::compareTo)
          .map(n -> n.toString(AdjSymbol.Sharp))
          .collect(Collectors.toList()))
      ));
      return Optional.empty();
    }

    return fabricator.getSourceMaterial().getInstrumentAudio(audioEvent.get().getInstrumentAudioId());

  }
}
