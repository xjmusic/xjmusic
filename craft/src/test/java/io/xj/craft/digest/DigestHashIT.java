// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.VoiceDAO;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.library.Library;
import io.xj.core.model.voice.Voice;
import io.xj.craft.BaseIT;
import io.xj.craft.CraftModule;
import io.xj.craft.digest.hash.DigestHash;
import io.xj.core.ingest.IngestFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class DigestHashIT extends BaseIT {
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  private IngestFactory ingestFactory;
  private DigestFactory digestFactory;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();
    insertLibraryA();

    ingestFactory = injector.getInstance(IngestFactory.class);
    digestFactory = injector.getInstance(DigestFactory.class);
  }

  @Test
  public void readHash_ofLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User,Artist",
      "accounts", "1"
    ));
    Collection<Entity> entities = ImmutableList.of(new Library(10000001));

    DigestHash result = digestFactory.hashOf(ingestFactory.evaluate(access, entities));

    assertNotNull(result);
    assertEquals("Audio-401=1407871023,Audio-402=1407871023,AudioChord-402000=1407871023,AudioChord-402001=1407871023,AudioChord-402002=1407871023,AudioChord-402003=1407871023,AudioChord-402004=1407871023,AudioChord-402005=1407871023,AudioEvent-401000=1407871023,AudioEvent-401001=1407871023,AudioEvent-401002=1407871023,AudioEvent-401003=1407871023,Instrument-201=1407871023,Instrument-202=1407871023,InstrumentMeme-201000=1407871023,InstrumentMeme-201001=1407871023,InstrumentMeme-202002=1407871023,Library-10000001=1407871023,Pattern-901=1407871023,Pattern-902=1407871023,PatternChord-902000=1407871023,PatternChord-902001=1407871023,PatternChord-902002=1407871023,PatternChord-902003=1407871023,PatternChord-902004=1407871023,PatternChord-902005=1407871023,PatternEvent-9011201000=1407871023,PatternEvent-9011201001=1407871023,PatternEvent-9011201002=1407871023,PatternEvent-9011201003=1407871023,Sequence-701=1407871023,Sequence-702=1407871023,Sequence-703=1407871023,SequenceMeme-701000=1407871023,SequenceMeme-702001=1407871023,SequenceMeme-703002=1407871023,SequencePattern-7900=1407871023,SequencePattern-7901=1407871023,SequencePattern-7902=1407871023,SequencePattern-7903=1407871023,SequencePattern-7904=1407871023,SequencePattern-7905=1407871023,SequencePatternMeme-7900000=1407871023,SequencePatternMeme-7901001=1407871023,SequencePatternMeme-7901004=1407871023,SequencePatternMeme-7902002=1407871023,SequencePatternMeme-7902005=1407871023,SequencePatternMeme-7903003=1407871023,SequencePatternMeme-7903006=1407871023,SequencePatternMeme-7904007=1407871023,Voice-1201=1407871023,Voice-1202=1407871023", result.toString());
    assertEquals("51d22af16662dbe14d5cf04a01754c6cc5910e6ae33dcffd47313264834f197a", result.sha256());
  }

  @Test
  public void readHash_ofLibrary_afterUpdateEntity() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User,Artist",
      "accounts", "1"
    ));
    Collection<Entity> entities = ImmutableList.of(new Library(10000001));
    injector.getInstance(VoiceDAO.class).update(Access.internal(), BigInteger.valueOf(1201),
      new Voice()
        .setDescription("new description")
        .setSequenceId(BigInteger.valueOf(701))
        .setType("Percussive"));

    DigestHash result = digestFactory.hashOf(ingestFactory.evaluate(access, entities));

    assertNotNull(result);
    Voice updatedVoice = injector.getInstance(VoiceDAO.class).readOne(Access.internal(), BigInteger.valueOf(1201));
    assertNotNull(updatedVoice);
    // NOTE the following injects new updatedEventSeconds value at %d via String.format(..)
    assertEquals(String.format("Audio-401=1407871023,Audio-402=1407871023,AudioChord-402000=1407871023,AudioChord-402001=1407871023,AudioChord-402002=1407871023,AudioChord-402003=1407871023,AudioChord-402004=1407871023,AudioChord-402005=1407871023,AudioEvent-401000=1407871023,AudioEvent-401001=1407871023,AudioEvent-401002=1407871023,AudioEvent-401003=1407871023,Instrument-201=1407871023,Instrument-202=1407871023,InstrumentMeme-201000=1407871023,InstrumentMeme-201001=1407871023,InstrumentMeme-202002=1407871023,Library-10000001=1407871023,Pattern-901=1407871023,Pattern-902=1407871023,PatternChord-902000=1407871023,PatternChord-902001=1407871023,PatternChord-902002=1407871023,PatternChord-902003=1407871023,PatternChord-902004=1407871023,PatternChord-902005=1407871023,PatternEvent-9011201000=1407871023,PatternEvent-9011201001=1407871023,PatternEvent-9011201002=1407871023,PatternEvent-9011201003=1407871023,Sequence-701=1407871023,Sequence-702=1407871023,Sequence-703=1407871023,SequenceMeme-701000=1407871023,SequenceMeme-702001=1407871023,SequenceMeme-703002=1407871023,SequencePattern-7900=1407871023,SequencePattern-7901=1407871023,SequencePattern-7902=1407871023,SequencePattern-7903=1407871023,SequencePattern-7904=1407871023,SequencePattern-7905=1407871023,SequencePatternMeme-7900000=1407871023,SequencePatternMeme-7901001=1407871023,SequencePatternMeme-7901004=1407871023,SequencePatternMeme-7902002=1407871023,SequencePatternMeme-7902005=1407871023,SequencePatternMeme-7903003=1407871023,SequencePatternMeme-7903006=1407871023,SequencePatternMeme-7904007=1407871023,Voice-1201=%d,Voice-1202=1407871023", updatedVoice.getUpdatedAt().toInstant().getEpochSecond()), result.toString());
  }

  @Test
  public void readHash_ofLibrary_afterDestroyEntity() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User,Artist",
      "accounts", "1"
    ));
    Collection<Entity> entities = ImmutableList.of(new Library(10000001));
    injector.getInstance(VoiceDAO.class).destroy(Access.internal(), BigInteger.valueOf(1202));

    DigestHash result = digestFactory.hashOf(ingestFactory.evaluate(access, entities));

    assertNotNull(result);
    assertEquals("Audio-401=1407871023,Audio-402=1407871023,AudioChord-402000=1407871023,AudioChord-402001=1407871023,AudioChord-402002=1407871023,AudioChord-402003=1407871023,AudioChord-402004=1407871023,AudioChord-402005=1407871023,AudioEvent-401000=1407871023,AudioEvent-401001=1407871023,AudioEvent-401002=1407871023,AudioEvent-401003=1407871023,Instrument-201=1407871023,Instrument-202=1407871023,InstrumentMeme-201000=1407871023,InstrumentMeme-201001=1407871023,InstrumentMeme-202002=1407871023,Library-10000001=1407871023,Pattern-901=1407871023,Pattern-902=1407871023,PatternChord-902000=1407871023,PatternChord-902001=1407871023,PatternChord-902002=1407871023,PatternChord-902003=1407871023,PatternChord-902004=1407871023,PatternChord-902005=1407871023,PatternEvent-9011201000=1407871023,PatternEvent-9011201001=1407871023,PatternEvent-9011201002=1407871023,PatternEvent-9011201003=1407871023,Sequence-701=1407871023,Sequence-702=1407871023,Sequence-703=1407871023,SequenceMeme-701000=1407871023,SequenceMeme-702001=1407871023,SequenceMeme-703002=1407871023,SequencePattern-7900=1407871023,SequencePattern-7901=1407871023,SequencePattern-7902=1407871023,SequencePattern-7903=1407871023,SequencePattern-7904=1407871023,SequencePattern-7905=1407871023,SequencePatternMeme-7900000=1407871023,SequencePatternMeme-7901001=1407871023,SequencePatternMeme-7901004=1407871023,SequencePatternMeme-7902002=1407871023,SequencePatternMeme-7902005=1407871023,SequencePatternMeme-7903003=1407871023,SequencePatternMeme-7903006=1407871023,SequencePatternMeme-7904007=1407871023,Voice-1201=1407871023", result.toString());
    assertEquals("62325a7bf39c66567b2d1532c333c00fa09df1bc2c99ddde15ecda70991f5367", result.sha256());
  }

}
