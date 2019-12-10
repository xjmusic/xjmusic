// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.xj.craft.digest.DigestFactory;
import io.xj.craft.digest.DigestCacheProvider;
import io.xj.craft.digest.DigestCacheProviderImpl;
import io.xj.craft.digest.DigestChordMarkov;
import io.xj.craft.digest.DigestChordMarkovImpl;
import io.xj.craft.digest.DigestChordProgression;
import io.xj.craft.digest.DigestChordProgressionImpl;
import io.xj.craft.digest.DigestHash;
import io.xj.craft.digest.DigestHashImpl;
import io.xj.craft.digest.DigestMeme;
import io.xj.craft.digest.DigestMemeImpl;
import io.xj.craft.digest.DigestProgramStyle;
import io.xj.craft.digest.DigestProgramStyleImpl;
import io.xj.craft.generation.Generation;
import io.xj.craft.generation.GenerationFactory;
import io.xj.craft.generation.LibrarySupersequenceGeneration;
import io.xj.craft.generation.LibrarySupersequenceGenerationImpl;
import io.xj.craft.harmonic.HarmonicDetailCraft;
import io.xj.craft.harmonic.HarmonicDetailCraftImpl;
import io.xj.craft.macro.MacroMainCraft;
import io.xj.craft.macro.MacroMainCraftImpl;
import io.xj.craft.rhythm.RhythmCraft;
import io.xj.craft.rhythm.RhythmCraftImpl;

public class CraftModule extends AbstractModule {

  protected void configure() {
    bindCraft();
    bindDigest();
    bindGeneration();
  }

  private void bindCraft() {
    install(new FactoryModuleBuilder()
      .implement(MacroMainCraft.class, MacroMainCraftImpl.class)
      .implement(RhythmCraft.class, RhythmCraftImpl.class)
      .implement(HarmonicDetailCraft.class, HarmonicDetailCraftImpl.class)
      .build(CraftFactory.class));
  }


  private void bindDigest() {
    bind(DigestCacheProvider.class).to(DigestCacheProviderImpl.class);
    install(new FactoryModuleBuilder()
      .implement(DigestChordProgression.class, DigestChordProgressionImpl.class)
      .implement(DigestChordMarkov.class, DigestChordMarkovImpl.class)
      .implement(DigestHash.class, DigestHashImpl.class)
      .implement(DigestMeme.class, DigestMemeImpl.class)
      .implement(DigestProgramStyle.class, DigestProgramStyleImpl.class)
      .build(DigestFactory.class));
  }

  private void bindGeneration() {
    install(new FactoryModuleBuilder()
      .implement(Generation.class, LibrarySupersequenceGenerationImpl.class)
      .implement(LibrarySupersequenceGeneration.class, LibrarySupersequenceGenerationImpl.class)
      .build(GenerationFactory.class));
  }
}
