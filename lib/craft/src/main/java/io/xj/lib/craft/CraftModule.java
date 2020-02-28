// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.craft;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.xj.lib.craft.digest.DigestFactory;
import io.xj.lib.craft.digest.DigestCacheProvider;
import io.xj.lib.craft.digest.DigestCacheProviderImpl;
import io.xj.lib.craft.digest.DigestChordMarkov;
import io.xj.lib.craft.digest.DigestChordMarkovImpl;
import io.xj.lib.craft.digest.DigestChordProgression;
import io.xj.lib.craft.digest.DigestChordProgressionImpl;
import io.xj.lib.craft.digest.DigestHash;
import io.xj.lib.craft.digest.DigestHashImpl;
import io.xj.lib.craft.digest.DigestMeme;
import io.xj.lib.craft.digest.DigestMemeImpl;
import io.xj.lib.craft.digest.DigestProgramStyle;
import io.xj.lib.craft.digest.DigestProgramStyleImpl;
import io.xj.lib.craft.generation.Generation;
import io.xj.lib.craft.generation.GenerationFactory;
import io.xj.lib.craft.generation.LibrarySupersequenceGeneration;
import io.xj.lib.craft.generation.LibrarySupersequenceGenerationImpl;
import io.xj.lib.craft.harmonic.HarmonicDetailCraft;
import io.xj.lib.craft.harmonic.HarmonicDetailCraftImpl;
import io.xj.lib.craft.macro.MacroMainCraft;
import io.xj.lib.craft.macro.MacroMainCraftImpl;
import io.xj.lib.craft.rhythm.RhythmCraft;
import io.xj.lib.craft.rhythm.RhythmCraftImpl;

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
