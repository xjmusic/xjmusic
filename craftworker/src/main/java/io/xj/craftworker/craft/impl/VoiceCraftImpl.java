// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.craftworker.craft.impl;

import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.basis.Basis;
import io.xj.core.dao.ArrangementDAO;
import io.xj.core.dao.InstrumentDAO;
import io.xj.core.dao.LinkMemeDAO;
import io.xj.core.dao.PickDAO;
import io.xj.core.isometry.MemeIsometry;
import io.xj.core.model.EventEntity;
import io.xj.core.model.MemeEntity;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.choice.Chance;
import io.xj.core.model.choice.Chooser;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.phase.Phase;
import io.xj.core.model.pick.Pick;
import io.xj.core.model.voice.Voice;
import io.xj.core.model.voice_event.VoiceEvent;
import io.xj.core.tables.records.ArrangementRecord;
import io.xj.core.tables.records.VoiceEventRecord;
import io.xj.craftworker.craft.VoiceCraft;
import io.xj.music.Chord;
import io.xj.music.Note;

import org.jooq.Record;
import org.jooq.Result;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.util.List;
import java.util.Objects;

/**
 Voice craft for the current link includes events, arrangements, instruments, and audio
 */
public class VoiceCraftImpl implements VoiceCraft {
  //  private final Logger log = LoggerFactory.getLogger(VoiceCraftImpl.class);
  private final static double SCORE_AVOID_CHOOSING_PREVIOUS = 10;
  private static final double PICK_INSTRUMENT_AUDIO_SCORE_MAX_DISTRIBUTION = 0.25;
  private final Basis basis;
  private final ArrangementDAO arrangementDAO;
  private final PickDAO pickDAO;
  private final LinkMemeDAO linkMemeDAO;
  private final InstrumentDAO instrumentDAO;

  @Inject
  public VoiceCraftImpl(
    @Assisted("basis") Basis basis,
    ArrangementDAO arrangementDAO,
    InstrumentDAO instrumentDAO,
    LinkMemeDAO linkMemeDAO,
    PickDAO pickDAO
    /*-*/) throws BusinessException {
    this.basis = basis;
    this.arrangementDAO = arrangementDAO;
    this.instrumentDAO = instrumentDAO;
    this.linkMemeDAO = linkMemeDAO;
    this.pickDAO = pickDAO;
  }

  @Override
  public void doWork() throws BusinessException {
    try {
      craftRhythmVoiceArrangements();
      report();

    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException(
        String.format("Failed to do %s-type VoiceCraft for link #%s",
          basis.type(), basis.linkId().toString()), e);
    }
  }

  /**
   craft link events for all rhythm voices
   */
  private void craftRhythmVoiceArrangements() throws Exception {
    for (Voice voice : rhythmPhaseVoices())
      craftArrangementForRhythmVoice(voice);
  }

  /**
   craft link events for one rhythm voice

   @param voice to craft events for
   @throws Exception on failure
   */
  private void craftArrangementForRhythmVoice(Voice voice) throws Exception {
    Instrument percussiveInstrument = choosePercussiveInstrument(voice);

    craftArrangement(
      rhythmPhase(),
      voice,
      basis.currentRhythmChoice().getTranspose(),
      percussiveInstrument,
      arrangementDAO.create(Access.internal(),
        new Arrangement()
          .setChoiceId(basis.currentRhythmChoice().getId().toBigInteger())
          .setVoiceId(voice.getId().toBigInteger())
          .setInstrumentId(percussiveInstrument.getId().toBigInteger())));
  }

  /**
   Choose percussive instrument
   [#325] Possible to choose multiple instruments for different voices in the same idea

   @param voice to choose instrument for
   @return percussive-type Instrument
   @throws Exception on failure
   */
  private Instrument choosePercussiveInstrument(Voice voice) throws Exception {
    Result<? extends Record> sourceRecords;
    Chooser<Instrument> chooser = new Chooser<>();

    // (1) retrieve memes of link, for use as a meme isometry comparison
    MemeIsometry memeIsometry = MemeIsometry.of(linkMemeDAO.readAll(Access.internal(), basis.linkId()));

    // (2a) retrieve instruments bound directly to chain
    sourceRecords = instrumentDAO.readAllBoundToChain(Access.internal(), basis.chainId(), Instrument.PERCUSSIVE);

    // (2b) only if none were found in the previous transpose, retrieve instruments bound to chain library
    if (sourceRecords.size() == 0)
      sourceRecords = instrumentDAO.readAllBoundToChainLibrary(Access.internal(), basis.chainId(), Instrument.PERCUSSIVE);

    // TODO [#258] Instrument selection is based on Text Isometry between the voice description and the instrument description

    // (3) score each source record based on meme isometry
    sourceRecords.forEach((record ->
      chooser.add(new Instrument().setFromRecord(record),
        Chance.normallyAround(
          memeIsometry.scoreCSV(String.valueOf(record.get(MemeEntity.KEY_MANY))),
          1))));

    /*
    DISABLED for [#324] Don't take into account which instruments were previously chosen, when choosing instruments for current link.
    // (3b) Avoid previous percussive instrument
    if (!basis.isInitialLink())
      basis.previousPercussiveArrangements().forEach(arrangement ->
        chooser.score(arrangement.getInstrumentId(), -SCORE_AVOID_CHOOSING_PREVIOUS));
        */

    // report
    basis.report("percussiveChoice", chooser.report());

    // (4) return the top choice
    Instrument instrument = chooser.getTop();
    if (Objects.nonNull(instrument))
      return instrument;
    else
      throw new BusinessException("Found no percussive-type instrument bound to Chain!");
  }

  /**
   Craft an arrangement based around a newly created arrangement record
   for each event in voice phase (repeat N times if needed to fill length of link)

   @param phase             that voice belongs to
   @param voice             that is being arranged
   @param transpose
   @param instrument        to arrange audio of
   @param arrangementRecord to create arrangement around
   @throws Exception on failure
   */
  private void craftArrangement(
    Phase phase,
    Voice voice,
    int transpose,
    Instrument instrument,
    ArrangementRecord arrangementRecord
  /*-*/) throws Exception {
    double repeat = basis.link().getTotal().doubleValue() / phase.getTotal().doubleValue();
    Result<VoiceEventRecord> voiceEvents = basis.voiceEvents(voice.getId());

    for (int i = 0; i < voiceEvents.size() * repeat; i++)
      pickInstrumentAudio(instrument, new Arrangement().setFromRecord(arrangementRecord), new VoiceEvent().setFromRecord(voiceEvents.get(i % voiceEvents.size())), transpose,
        Math.floor(i / voiceEvents.size()) * phase.getTotal().doubleValue());
  }

  /**
   create a pick of instrument-audio for each event, where events are conformed to chords/scales based on the master link chords
   pick instrument audio for one event, in a voice in a phase, belonging to an arrangement

   @param arrangement   to create pick record within
   @param voiceEvent    to pick audio for
   @param shiftPosition offset voice event zero within current link
   */
  private void pickInstrumentAudio(
    Instrument instrument,
    Arrangement arrangement,
    VoiceEvent voiceEvent,
    int transpose,
    Double shiftPosition
  /*-*/) throws Exception {
    Chooser<Audio> audioChooser = new Chooser<>();

    // add all audio to chooser
    audioChooser.addAll(basis.instrumentAudios(instrument.getId()));

    // score each audio against the current voice event, with some variability
    basis.instrumentAudioEvents(instrument.getId())
      .forEach(audioEvent ->
        audioChooser.score(audioEvent.getAudioId(),
          Chance.normallyAround(
            EventEntity.similarity(voiceEvent, audioEvent),
            PICK_INSTRUMENT_AUDIO_SCORE_MAX_DISTRIBUTION)));

    // final chosen audio event
    Audio audio = audioChooser.getTop();
    if (Objects.isNull(audio))
      throw new BusinessException("No acceptable Audio found!");

    // Morph & Point attributes are expressed in beats
    double position = voiceEvent.getPosition() + shiftPosition;
    double duration = voiceEvent.getDuration();
    Chord chord = basis.chordAt(position);

    // The final note is transformed based on instrument type
    Note note = pickNote(
      Note.of(voiceEvent.getNote()).transpose(transpose),
      chord, audio, instrument.getType());

    // Pick attributes are expressed "rendered" as actual seconds
    double startSeconds = basis.secondsAtPosition(position);
    double lengthSeconds = basis.secondsAtPosition(position + duration) - startSeconds;

    // create pick
    pickDAO.create(Access.internal(),
      new Pick()
        .setArrangementId(arrangement.getId().toBigInteger())
        .setAudioId(audio.getId().toBigInteger())
        .setStart(startSeconds)
        .setLength(lengthSeconds)
        .setAmplitude(voiceEvent.getVelocity())
        .setPitch(basis.pitch(note)));

  }

  /**
   Pick final note based on instrument type, voice event, transposition and current chord
   <p>
   [#295] Pitch of percussive-type instrument audio is altered the least # semitones possible to conform to the current chord

   @param fromNote       from voice event
   @param chord          current
   @param audio          that has been picked
   @param instrumentType of instrument
   @return final note
   */
  private Note pickNote(Note fromNote, Chord chord, Audio audio, String instrumentType) {
    switch (instrumentType) {

      case Instrument.PERCUSSIVE:
        return basis.note(audio.getPitch())
          .conformedTo(chord);

      default:
        return fromNote
          .conformedTo(chord);
    }
  }

  /**
   all voices in current phase of chosen rhythm-type idea

   @return voices
   @throws Exception on failure
   */
  private List<Voice> rhythmPhaseVoices() throws Exception {
    List<Voice> voices = Lists.newArrayList();
    basis.voices(rhythmPhase().getId()).forEach(record ->
      voices.add(new Voice().setFromRecord(record)));
    return voices;
  }

  /**
   get current rhythm phase
   (cache result)

   @return current rhythm phase
   @throws Exception on failure
   */
  private Phase rhythmPhase() throws Exception {
    return new Phase().setFromRecord(
      basis.phaseByOffset(
        basis.currentRhythmChoice().getIdeaId(),
        basis.currentRhythmChoice().getPhaseOffset()));
  }

  /**
   Report
   */
  private void report() {
    // TODO basis.report() anything else interesting from the craft operation
  }

}
