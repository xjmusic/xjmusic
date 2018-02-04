// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import io.xj.craft.basis.Basis;
import io.xj.craft.basis.BasisFactory;
import io.xj.craft.basis.impl.BasisImpl;
import io.xj.craft.cache.digest.DigestCacheProvider;
import io.xj.craft.cache.digest.impl.DigestCacheProviderImpl;
import io.xj.craft.cache.ingest.IngestCacheProvider;
import io.xj.craft.cache.ingest.impl.IngestCacheProviderImpl;
import io.xj.craft.digest.DigestFactory;
import io.xj.craft.digest.chord_markov.DigestChordMarkov;
import io.xj.craft.digest.chord_markov.impl.DigestChordMarkovImpl;
import io.xj.craft.digest.chord_progression.DigestChordProgression;
import io.xj.craft.digest.chord_progression.impl.DigestChordProgressionImpl;
import io.xj.craft.digest.hash.DigestHash;
import io.xj.craft.digest.hash.impl.DigestHashImpl;
import io.xj.craft.digest.meme.DigestMeme;
import io.xj.craft.digest.meme.impl.DigestMemeImpl;
import io.xj.craft.digest.pattern_style.DigestPatternStyle;
import io.xj.craft.digest.pattern_style.impl.DigestPatternStyleImpl;
import io.xj.craft.ingest.Ingest;
import io.xj.craft.ingest.IngestFactory;
import io.xj.craft.ingest.impl.IngestImpl;
import io.xj.craft.generation.Generation;
import io.xj.craft.generation.GenerationFactory;
import io.xj.craft.generation.superpattern.LibrarySuperpatternGeneration;
import io.xj.craft.generation.superpattern.impl.LibrarySuperpatternGenerationImpl;
import io.xj.craft.harmonic.HarmonicDetailCraft;
import io.xj.craft.harmonic.impl.HarmonicDetailCraftImpl;
import io.xj.craft.macro.MacroMainCraft;
import io.xj.craft.macro.impl.MacroMainCraftImpl;
import io.xj.craft.rhythm.RhythmCraft;
import io.xj.craft.rhythm.impl.RhythmCraftImpl;

public class CraftModule extends AbstractModule {

  protected void configure() {
    cfgCraft();
    cfgBasis();
    cfgDigest();
    cfgIngest();
    cfgGeneration();
  }

  private void cfgCraft() {
    install(new FactoryModuleBuilder()
      .implement(MacroMainCraft.class, MacroMainCraftImpl.class)
      .implement(RhythmCraft.class, RhythmCraftImpl.class)
      .implement(HarmonicDetailCraft.class, HarmonicDetailCraftImpl.class)
      .build(CraftFactory.class));
  }


  private void cfgBasis() {
    install(new FactoryModuleBuilder()
      .implement(Basis.class, BasisImpl.class)
      .build(BasisFactory.class));
  }

  private void cfgGeneration() {
    install(new FactoryModuleBuilder()
      .implement(Generation.class, LibrarySuperpatternGenerationImpl.class)
      .implement(LibrarySuperpatternGeneration.class, LibrarySuperpatternGenerationImpl.class)
      .build(GenerationFactory.class));
  }

  private void cfgIngest() {
    bind(IngestCacheProvider.class).to(IngestCacheProviderImpl.class);
    install(new FactoryModuleBuilder()
      .implement(Ingest.class, IngestImpl.class)
      .build(IngestFactory.class));
  }

  private void cfgDigest() {
    bind(DigestCacheProvider.class).to(DigestCacheProviderImpl.class);
    install(new FactoryModuleBuilder()
      .implement(DigestChordProgression.class, DigestChordProgressionImpl.class)
      .implement(DigestChordMarkov.class, DigestChordMarkovImpl.class)
      .implement(DigestHash.class, DigestHashImpl.class)
      .implement(DigestMeme.class, DigestMemeImpl.class)
      .implement(DigestPatternStyle.class, DigestPatternStyleImpl.class)
      .build(DigestFactory.class));
  }
}
