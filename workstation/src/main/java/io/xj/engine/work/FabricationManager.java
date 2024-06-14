// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.engine.work;

import io.xj.model.HubContent;
import io.xj.model.meme.MemeTaxonomy;
import io.xj.model.pojos.Program;
import io.xj.engine.fabricator.SegmentEntityStore;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface FabricationManager {
  /**
   Start work.
   * @param content for fabrication
   * @param config for fabrication
   */
  void start(
    HubContent content,
    FabricationSettings config
  );

  /**
   Stop work
   */
  void finish(boolean cancelled);

  /**
   Get the entity store
   */
  SegmentEntityStore getEntityStore();

  /**
   Reset the factory including the segment manager and its store
   */
  void reset();

  /**
   Get the Hub content source material

   @return source material
   */
  HubContent getSourceMaterial();

  /**
   Return the current shipped-to chain micros

   @return chain micros, else empty
   */
  Optional<Long> getShippedToChainMicros();

  /**
   Return the current dubbed-to sync chain micros

   @return chain micros, else empty
   */
  Optional<Long> getDubbedToChainMicros();

  /**
   Return the current crafted-to chain micros

   @return chain micros, else empty
   */
  Optional<Long> getCraftedToChainMicros();

  /**
   @return the current work state
   */
  FabricationState getWorkState();

  /**
   Set the on progress callback

   @param onProgress callback
   */
  void setOnProgress(@Nullable Consumer<Float> onProgress);

  /**
   Set the on progress label callback

   @param onProgressLabel callback
   */
  void setOnProgressLabel(@Nullable Consumer<String> onProgressLabel);

  /**
   Set the on status callback

   @param onStateChange callback
   */
  void setOnStateChange(Consumer<FabricationState> onStateChange);

  /**
   Go to the given macro program right away
   https://github.com/xjmusic/xjmusic/issues/197

   @param macroProgram to go to
   */
  void doOverrideMacro(Program macroProgram);

  /**
   @return the meme taxonomy from the current template configuration
   */
  Optional<MemeTaxonomy> getMemeTaxonomy();

  /**
   * @return all macro programs in alphabetical order
   */
  List<Program> getAllMacroPrograms();

  /**
   Manually go to a specific taxonomy category meme, and force until reset
   https://github.com/xjmusic/xjmusic/issues/199

   @param memes specific (assumed allowably) set of taxonomy category memes
   */
  void doOverrideMemes(Collection<String> memes);

  /**
   Get whether an override happened, and reset its state after getting

   @return true if an override happened
   */
  boolean getAndResetDidOverride();

  /**
   Set the intensity override to a value between 0 and 1, or null if no override

   @param intensity the intensity to set
   */
  void setIntensityOverride(@Nullable Double intensity);
}
