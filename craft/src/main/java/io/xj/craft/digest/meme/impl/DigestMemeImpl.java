// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest.meme.impl;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.ingest.Ingest;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.sub.InstrumentMeme;
import io.xj.core.model.program.Program;
import io.xj.core.model.program.sub.ProgramMeme;
import io.xj.core.model.program.sub.SequenceBinding;
import io.xj.core.model.program.sub.SequenceBindingMeme;
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
  private final Map<String, DigestMemesItem> memes = Maps.newHashMap();

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
      Logger log = LoggerFactory.getLogger(DigestMemeImpl.class);
      log.error("Failed to digest memes of ingest {}", ingest, e);
    }
  }

  /**
   Digest entities from ingest
   */
  private void digest() {
    // for each program, stash collection of program memes and prepare map of program patterns
    for (Program program : ingest.getAllPrograms()) {
      for (ProgramMeme programMeme : program.getMemes()) {
        digestMemesItem(programMeme.getName()).addProgramId(program.getId());
      }
      // for each program pattern in program, stash collection of program pattern memes
      for (SequenceBinding sequenceBinding : program.getSequenceBindings()) {
        for (SequenceBindingMeme sequenceBindingMeme : program.getMemes(sequenceBinding)) {
          digestMemesItem(sequenceBindingMeme.getName()).addSequenceBinding(sequenceBinding);
        }
      }
    }

    // for each instrument, stash collection of instrument memes
    for (Instrument instrument : ingest.getAllInstruments()) {
      for (InstrumentMeme instrumentMeme : instrument.getMemes()) {
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

  @Override
  public DigestMeme validate() {
    return this;
  }

  /*
  FUTURE: custom JSON serializer for DigestChordProgression


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
        instrumentObj.put(KEY_INSTRUMENT_NAME, getInstrument(instrumentId).getName());
        memeInstrumentsArr.put(instrumentObj);
      });
      memeObj.put(KEY_INSTRUMENTS, memeInstrumentsArr);

      JSONArray memeProgramsArr = new JSONArray();
      memeDigestItem.getProgramIds().forEach(programId -> {
        JSONObject programObj = new JSONObject();
        JSONArray sequenceBindingsArr = new JSONArray();
        memeDigestItem.getPatternIds(programId).forEach(patternId -> {
          JSONObject patternObj = new JSONObject();
          patternObj.put(KEY_PATTERN_ID, patternId);
          patternObj.put(KEY_PATTERN_NAME, getPattern(patternId).getName());
          patternObj.put(KEY_PATTERN_TYPE, getPattern(patternId).getType());
          sequenceBindingsArr.put(patternObj);
        });
        programObj.put(KEY_PROGRAM_ID, programId);
        programObj.put(KEY_PROGRAM_TYPE, getProgram(programId).getType());
        programObj.put(KEY_PROGRAM_NAME, getProgram(programId).getName());
        programObj.put(KEY_PROGRAM_HAS_MEME, memeDigestItem.getProgramIds().contains(programId));
        programObj.put(KEY_PATTERNS_WITH_MEME, sequenceBindingsArr);
        memeProgramsArr.put(programObj);
      });
      memeDigestItem.getProgramIds().forEach(programId -> {
        if (memeDigestItem.getProgramIds().contains(programId)) return;
        JSONObject programObj = new JSONObject();
        programObj.put(KEY_PROGRAM_ID, programId);
        programObj.put(KEY_PROGRAM_TYPE, getProgram(programId).getType());
        programObj.put(KEY_PROGRAM_NAME, getProgram(programId).getName());
        programObj.put(KEY_PROGRAM_HAS_MEME, true);
        memeProgramsArr.put(programObj);
      });
      memeObj.put(KEY_PROGRAMS, memeProgramsArr);

      memeUsage.put(memeName, memeObj);
    });

    result.put(KEY_MEME_USAGE, memeUsage);
    return result;
  }

   */

}
