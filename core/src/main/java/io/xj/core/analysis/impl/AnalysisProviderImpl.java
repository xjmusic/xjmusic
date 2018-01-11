// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.analysis.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.analysis.AnalysisProvider;
import io.xj.core.dao.AudioChordDAO;
import io.xj.core.dao.AudioDAO;
import io.xj.core.dao.InstrumentDAO;
import io.xj.core.dao.InstrumentMemeDAO;
import io.xj.core.dao.LibraryDAO;
import io.xj.core.dao.PatternDAO;
import io.xj.core.dao.PatternMemeDAO;
import io.xj.core.dao.PhaseChordDAO;
import io.xj.core.dao.PhaseDAO;
import io.xj.core.dao.PhaseMemeDAO;
import io.xj.core.model.analysis.AnalysisType;
import io.xj.core.model.analysis.library_chord.LibraryChordAnalysis;
import io.xj.core.model.analysis.library_meme.LibraryMemeAnalysis;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.chord.ChordSequence;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.meme.Meme;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.phase.Phase;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

/**
 [#154234716] Architect wants analysis of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
public class AnalysisProviderImpl implements AnalysisProvider {
  private static final Logger log = LoggerFactory.getLogger(AnalysisProviderImpl.class);
  private final InstrumentDAO instrumentDAO;
  private final InstrumentMemeDAO instrumentMemeDAO;
  private final LibraryDAO libraryDAO;
  private final PatternDAO patternDAO;
  private final PatternMemeDAO patternMemeDAO;
  private final PhaseDAO phaseDAO;
  private final PhaseMemeDAO phaseMemeDAO;
  private final PhaseChordDAO phaseChordDAO;
  private final AudioDAO audioDAO;
  private final AudioChordDAO audioChordDAO;

  @Inject
  public AnalysisProviderImpl(
    AudioChordDAO audioChordDAO,
    AudioDAO audioDAO,
    InstrumentDAO instrumentDAO,
    InstrumentMemeDAO instrumentMemeDAO,
    LibraryDAO libraryDAO,
    PatternDAO patternDAO,
    PatternMemeDAO patternMemeDAO,
    PhaseChordDAO phaseChordDAO,
    PhaseDAO phaseDAO,
    PhaseMemeDAO phaseMemeDAO
  ) {
    this.audioChordDAO = audioChordDAO;
    this.audioDAO = audioDAO;
    this.instrumentDAO = instrumentDAO;
    this.instrumentMemeDAO = instrumentMemeDAO;
    this.libraryDAO = libraryDAO;
    this.patternDAO = patternDAO;
    this.patternMemeDAO = patternMemeDAO;
    this.phaseChordDAO = phaseChordDAO;
    this.phaseDAO = phaseDAO;
    this.phaseMemeDAO = phaseMemeDAO;
  }

  @Override
  public LibraryMemeAnalysis analyzeLibraryMemes(Access access, BigInteger libraryId) throws Exception {
    LibraryMemeAnalysis result = new LibraryMemeAnalysis();
    result.setTargetId(libraryId);
    result.setType(AnalysisType.LibraryMeme);

    // in-memory caches of original objects
    Map<BigInteger, Collection<? extends Meme>> instrumentMemes = Maps.newConcurrentMap();
    Map<BigInteger, Collection<? extends Meme>> patternMemes = Maps.newConcurrentMap();
    Map<BigInteger, Map<BigInteger, Collection<? extends Meme>>> patternPhaseMemes = Maps.newConcurrentMap();

    // for each pattern, stash collection of pattern memes and prepare map of phases
    for (Pattern pattern : patternDAO.readAllInLibrary(access, libraryId)) {
      result.putPattern(pattern);
      patternMemes.put(pattern.getId(), patternMemeDAO.readAll(access, pattern.getId()));
      patternPhaseMemes.put(pattern.getId(), Maps.newConcurrentMap());

      // for each phase in pattern, stash collection of phase memes
      for (Phase phase : phaseDAO.readAll(access, pattern.getId())) {
        result.putPhase(phase);
        patternPhaseMemes.get(pattern.getId()).put(phase.getId(), phaseMemeDAO.readAll(access, phase.getId()));
      }
    }

    // for each instrument, stash collection of instrument memes
    for (Instrument instrument : instrumentDAO.readAllInLibrary(access, libraryId)) {
      result.putInstrument(instrument);
      instrumentMemes.put(instrument.getId(), instrumentMemeDAO.readAll(access, instrument.getId()));
    }

    // reverse-match everything and store it
    instrumentMemes.forEach((instrumentId, memesInInstrument) -> {
      memesInInstrument.forEach(meme -> {
        result.addInstrumentId(meme.getName(), instrumentId);
      });
    });
    patternMemes.forEach((patternId, memesInPattern) -> {
      memesInPattern.forEach(meme -> {
        result.addPatternId(meme.getName(), patternId);
      });
    });
    patternPhaseMemes.forEach((patternId, phases) -> {
      phases.forEach((phaseId, phaseMemes) -> {
        phaseMemes.forEach(meme -> {
          result.addPatternPhaseId(meme.getName(), patternId, phaseId);
        });
      });
    });

    return result;
  }

  @Override
  public LibraryChordAnalysis analyzeLibraryChords(Access access, BigInteger libraryId) throws Exception {
    LibraryChordAnalysis result = new LibraryChordAnalysis();
    result.setTargetId(libraryId);
    result.setType(AnalysisType.LibraryChord);

    // for each pattern, stash collection of phase chords
    for (Pattern pattern : patternDAO.readAllInLibrary(access, libraryId)) {
      result.putPattern(pattern);
      for (Phase phase : phaseDAO.readAll(access, pattern.getId())) {
        result.putPhase(phase);
        for (ChordSequence chordSequence : phaseChordDAO.readAllSequences(access, phase.getId())) {
          result.putSequence(chordSequence);
        }
      }
    }

    // for each instrument, stash collection of audio chords
    for (Instrument instrument : instrumentDAO.readAllInLibrary(access, libraryId)) {
      result.putInstrument(instrument);
      for (Audio audio : audioDAO.readAll(access, instrument.getId())) {
        result.putAudio(audio);
        for (ChordSequence chordSequence : audioChordDAO.readAllSequences(access, audio.getId())) {
          result.putSequence(chordSequence);
        }
      }
    }

    // eliminate all chord sequences that are redundant subsets of longer sequences
    result.prune();

    return result;
  }

}
