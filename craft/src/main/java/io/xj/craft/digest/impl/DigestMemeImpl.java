// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.ingest.Ingest;
import io.xj.core.model.Instrument;
import io.xj.core.model.InstrumentMeme;
import io.xj.core.model.Program;
import io.xj.core.model.ProgramMeme;
import io.xj.core.model.ProgramSequenceBinding;
import io.xj.core.model.ProgramSequenceBindingMeme;
import io.xj.craft.digest.DigestMeme;
import io.xj.craft.digest.DigestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

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
      log.error("Failed to digest memes create ingest {}", ingest, e);
    }
  }

  /**
   Digest entities of ingest
   */
  private void digest() {
    // for each program, stash collection of program memes and prepare map of program patterns
    for (Program program : ingest.getAllPrograms()) {
      for (ProgramMeme programMeme : ingest.getMemes(program)) {
        digestMemesItem(programMeme.getName()).addProgramId(program.getId());
      }
      // for each program pattern in program, stash collection of program pattern memes
      for (ProgramSequenceBinding sequenceBinding : ingest.getSequenceBindings(program)) {
        for (ProgramSequenceBindingMeme sequenceBindingMeme : ingest.getMemes(sequenceBinding)) {
          digestMemesItem(sequenceBindingMeme.getName()).addSequenceBinding(sequenceBinding);
        }
      }
    }

    // for each instrument, stash collection of instrument memes
    for (Instrument instrument : ingest.getAllInstruments()) {
      for (InstrumentMeme instrumentMeme : ingest.getMemes(instrument)) {
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
  public void validate() {

  }

  /**
   In-memory cache of ingest of a meme usage in a library
   <p>
   [#154234716] Artist wants to run a library ingest in order to understand all of the existing contents within the programs in a library.
   */
  public class DigestMemesItem {
    private final String name;
    private final Collection<UUID> instrumentIds = Lists.newArrayList();
    private final Collection<UUID> programIds = Lists.newArrayList();
    private final Map<UUID, Collection<UUID>> sequenceIds = Maps.newHashMap(); // programId: collection of sequenceIds mapped to it

    /**
     of instance

     @param name of meme
     */
    public DigestMemesItem(String name) {
      this.name = name;
    }

    /**
     Get name of meme

     @return meme name
     */
    public String getName() {
      return name;
    }

    /**
     Add a program id, if it isn't already in the list

     @param id of program to add
     */
    public void addProgramId(UUID id) {
      if (!programIds.contains(id))
        programIds.add(id);
    }

    /**
     Add a program id, if it isn't already in the list

     @param sequenceBinding to add
     */
    public void addSequenceBinding(ProgramSequenceBinding sequenceBinding) {
      addProgramId(sequenceBinding.getProgramId());

      UUID programId = sequenceBinding.getProgramId();
      if (!sequenceIds.containsKey(programId)) {
        sequenceIds.put(programId, Lists.newArrayList());
      }

      UUID sequenceId = sequenceBinding.getProgramSequenceId();
      if (!sequenceIds.get(programId).contains(sequenceId)) {
        sequenceIds.get(programId).add(sequenceId);
      }
    }

    /**
     Add a instrument id, if it isn't already in the list

     @param id of instrument to add
     */
    public void addInstrumentId(UUID id) {
      if (!instrumentIds.contains(id))
        instrumentIds.add(id);
    }

    /**
     Get the instrument ids in which this meme is used

     @return collection of instrument id
     */
    public Collection<UUID> getInstrumentIds() {
      return Collections.unmodifiableCollection(instrumentIds);
    }

    /**
     Get the program ids in which this meme is used

     @return collection of program id
     */
    public Collection<UUID> getProgramIds() {
      return Collections.unmodifiableCollection(programIds);
    }

    /**
     Get the sequence ids is used in sequences in a particular program

     @return collection of program id
     */
    public Collection<UUID> getSequenceIds(UUID programId) {
      return sequenceIds.getOrDefault(programId, Lists.newArrayList());
    }

  }

}
