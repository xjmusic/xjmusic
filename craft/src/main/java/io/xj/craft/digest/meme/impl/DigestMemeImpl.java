// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest.meme.impl;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.exception.CoreException;
import io.xj.core.ingest.Ingest;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument_meme.InstrumentMeme;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence_meme.SequenceMeme;
import io.xj.core.model.sequence_pattern.SequencePattern;
import io.xj.core.model.sequence_pattern_meme.SequencePatternMeme;
import io.xj.craft.digest.DigestType;
import io.xj.craft.digest.impl.DigestImpl;
import io.xj.craft.digest.meme.DigestMeme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 In-memory cache of ingest of all memes in a library
 <p>
 [#154234716] Architect wants ingest of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
public class DigestMemeImpl extends DigestImpl implements DigestMeme {
  private final Map<String, DigestMemesItem> memes = Maps.newConcurrentMap();
  private final Logger log = LoggerFactory.getLogger(DigestMemeImpl.class);

  /**
   Instantiate a new digest with a collection of target entities

   @param ingest to digest
   */
  @Inject
  public DigestMemeImpl(
    @Assisted("ingest") Ingest ingest
  ) {
    super(ingest, DigestType.DigestMeme);
    try {
      digest();
    } catch (Exception e) {
      log.error("Failed to digest memes of ingest {}", ingest, e);
    }
  }

  /**
   Digest entities from ingest
   */
  private void digest() {
    // for each sequence, stash collection of sequence memes and prepare map of sequence patterns
    for (Sequence sequence : ingest.getAllSequences()) {
      for (SequenceMeme sequenceMeme : ingest.getSequenceMemesOfSequence(sequence.getId())) {
        digestMemesItem(sequenceMeme.getName()).addSequenceId(sequence.getId());
      }
      // for each sequence pattern in sequence, stash collection of sequence pattern memes
      try {
        for (SequencePattern sequencePattern : ingest.getSequencePatternsOfSequence(sequence.getId())) {
          for (SequencePatternMeme sequencePatternMeme : ingest.getSequencePatternMemesOfSequencePattern(sequencePattern.getId())) {
            digestMemesItem(sequencePatternMeme.getName()).addSequencePattern(sequencePattern);
          }
        }
      } catch (CoreException e) {
        log.warn("Failed to get sequence patterns of sequenceId={}", sequence.getId());
      }
    }

    // for each instrument, stash collection of instrument memes
    for (Instrument instrument : ingest.getAllInstruments()) {
      for (InstrumentMeme instrumentMeme : ingest.getMemesOfInstrument(instrument.getId())) {
        digestMemesItem(instrumentMeme.getName()).addInstrumentId(instrument.getId());
      }
    }
  }

  /**
   Get all evaluated memes

   @return map of meme name to evaluated meme
   */
  public Map<String, DigestMemesItem> getMemes() {
    return Collections.unmodifiableMap(memes);
  }

  /**
   Get a meme digest item, and instantiate if it doesn't already exist

   @param name of meme to get digest item for
   */
  public DigestMemesItem digestMemesItem(String name) {
    if (!memes.containsKey(name))
      memes.put(name, new DigestMemesItem(name));

    return memes.get(name);
  }

  /*
  TODO: custom JSON serializer for DigestChordProgression


  public JSONObject toJSONObject() {
    JSONObject result = new JSONObject();
    JSONObject memeUsage = new JSONObject();
    memes.forEach((memeName, memeDigestItem) -> {
      JSONObject memeObj = new JSONObject();

      JSONArray memeInstrumentsArr = new JSONArray();
      memeDigestItem.getInstrumentIds().forEach(instrumentId -> {
        JSONObject instrumentObj = new JSONObject();
        instrumentObj.put(KEY_INSTRUMENT_ID, instrumentId);
        instrumentObj.put(KEY_INSTRUMENT_TYPE, getInstrument(instrumentId).getType());
        instrumentObj.put(KEY_INSTRUMENT_DESCRIPTION, getInstrument(instrumentId).getDescription());
        memeInstrumentsArr.put(instrumentObj);
      });
      memeObj.put(KEY_INSTRUMENTS, memeInstrumentsArr);

      JSONArray memeSequencesArr = new JSONArray();
      memeDigestItem.getSequenceIds().forEach(sequenceId -> {
        JSONObject sequenceObj = new JSONObject();
        JSONArray sequencePatternsArr = new JSONArray();
        memeDigestItem.getPatternIds(sequenceId).forEach(patternId -> {
          JSONObject patternObj = new JSONObject();
          patternObj.put(KEY_PATTERN_ID, patternId);
          patternObj.put(KEY_PATTERN_NAME, getPattern(patternId).getName());
          patternObj.put(KEY_PATTERN_TYPE, getPattern(patternId).getType());
          sequencePatternsArr.put(patternObj);
        });
        sequenceObj.put(KEY_SEQUENCE_ID, sequenceId);
        sequenceObj.put(KEY_SEQUENCE_TYPE, getSequence(sequenceId).getType());
        sequenceObj.put(KEY_SEQUENCE_NAME, getSequence(sequenceId).getName());
        sequenceObj.put(KEY_SEQUENCE_HAS_MEME, memeDigestItem.getSequenceIds().contains(sequenceId));
        sequenceObj.put(KEY_PATTERNS_WITH_MEME, sequencePatternsArr);
        memeSequencesArr.put(sequenceObj);
      });
      memeDigestItem.getSequenceIds().forEach(sequenceId -> {
        if (memeDigestItem.getSequenceIds().contains(sequenceId)) return;
        JSONObject sequenceObj = new JSONObject();
        sequenceObj.put(KEY_SEQUENCE_ID, sequenceId);
        sequenceObj.put(KEY_SEQUENCE_TYPE, getSequence(sequenceId).getType());
        sequenceObj.put(KEY_SEQUENCE_NAME, getSequence(sequenceId).getName());
        sequenceObj.put(KEY_SEQUENCE_HAS_MEME, true);
        memeSequencesArr.put(sequenceObj);
      });
      memeObj.put(KEY_SEQUENCES, memeSequencesArr);

      memeUsage.put(memeName, memeObj);
    });

    result.put(KEY_MEME_USAGE, memeUsage);
    return result;
  }

   */

}
