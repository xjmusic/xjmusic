// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft;

import io.xj.craft.macro.MacroMainCraft;
import io.xj.craft.harmonic.HarmonicDetailCraft;
import io.xj.craft.rhythm.RhythmCraft;
import io.xj.craft.macro.impl.MacroMainCraftImpl;
import io.xj.craft.harmonic.impl.HarmonicDetailCraftImpl;
import io.xj.craft.rhythm.impl.RhythmCraftImpl;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class CraftModule extends AbstractModule {

  protected void configure() {
    installCraftFactory();
  }

  private void installCraftFactory() {
    install(new FactoryModuleBuilder()
      .implement(MacroMainCraft.class, MacroMainCraftImpl.class)
      .implement(RhythmCraft.class, RhythmCraftImpl.class)
      .implement(HarmonicDetailCraft.class, HarmonicDetailCraftImpl.class)
      .build(CraftFactory.class));
  }

}
