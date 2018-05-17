// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest.meme.impl;

import io.xj.craft.ingest.Ingest;
import io.xj.craft.digest.DigestType;
import io.xj.craft.digest.meme.DigestMeme;
import io.xj.craft.digest.impl.DigestImpl;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.meme.Meme;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.pattern.Pattern;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 In-memory cache of ingest of all memes in a library
 <p>
 [#154234716] Architect wants ingest of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
public class DigestMemeImpl extends DigestImpl implements DigestMeme {
  protected final Map<String, DigestMemesItem> memes = Maps.newConcurrentMap();
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
  private void digest() throws Exception {
    // in-memory caches of original objects
    Map<BigInteger, Collection<? extends Meme>> instrumentMemes = Maps.newConcurrentMap();
    Map<BigInteger, Collection<? extends Meme>> sequenceMemes = Maps.newConcurrentMap();
    Map<BigInteger, Map<BigInteger, Collection<? extends Meme>>> sequencePatternMemes = Maps.newConcurrentMap();

    // for each sequence, stash collection of sequence memes and prepare map of patterns
    for (Sequence sequence : ingest.sequences()) {
      sequenceMemes.put(sequence.getId(), ingest.sequenceMemes(sequence.getId()));
      sequencePatternMemes.put(sequence.getId(), Maps.newConcurrentMap());

      // for each pattern in sequence, stash collection of pattern memes
      for (Pattern pattern : ingest.patterns(sequence.getId())) {
        sequencePatternMemes.get(sequence.getId()).put(pattern.getId(), ingest.patternMemes(pattern.getId()));
      }
    }

    // for each instrument, stash collection of instrument memes
    for (Instrument instrument : ingest.instruments()) {
      instrumentMemes.put(instrument.getId(), ingest.instrumentMemes(instrument.getId()));
    }

    // reverse-match everything and store it
    sequenceMemes.forEach((sequenceId, memesInSequence) -> memesInSequence.forEach(meme -> {
      digestMemesItem(meme.getName()).addSequenceId(sequenceId);
    }));
    sequencePatternMemes.forEach((sequenceId, sequencePatterns) ->
      sequencePatterns.forEach((patternId, patternMemes) -> {
      patternMemes.forEach(meme -> digestMemesItem(meme.getName()).addSequencePatternId(sequenceId, patternId));
    }));
    instrumentMemes.forEach((instrumentId, memesInInstrument) -> memesInInstrument.forEach(meme -> {
      digestMemesItem(meme.getName()).addInstrumentId(instrumentId);
    }));
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

  @Override
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
      memeDigestItem.getPatternSequenceIds().forEach(sequenceId -> {
        JSONObject sequenceObj = new JSONObject();
        JSONArray sequencePatternsArr = new JSONArray();
        memeDigestItem.getPatternIds(sequenceId).forEach(patternId -> {
          JSONObject patternObj = new JSONObject();
          patternObj.put(KEY_PATTERN_ID, patternId);
          patternObj.put(KEY_PATTERN_NAME, getPattern(patternId).getName());
          patternObj.put(KEY_PATTERN_TYPE, getPattern(patternId).getType());
          patternObj.put(KEY_PATTERN_OFFSET, getPattern(patternId).getOffset());
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
        if (memeDigestItem.getPatternSequenceIds().contains(sequenceId)) return;
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

}
