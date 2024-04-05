// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.work;

import io.xj.hub.HubContent;
import io.xj.hub.meme.MemeTaxonomy;
import io.xj.hub.pojos.Program;
import io.xj.nexus.persistence.NexusEntityStore;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

public interface FabricationManager {
  /**
   Start work.
   <p>
   This assigns the work configuration, hub configuration, and hub access.
   */
  void start(
    FabricationSettings workConfig
  );

  /**
   Stop work
   */
  void finish(boolean cancelled);

  /**
   Get the entity store
   */
  NexusEntityStore getEntityStore();

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
   @return true if the current work is healthy
   */
  boolean isHealthy();

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
   Set the on finish callback

   @param afterFinished callback
   */
  void setAfterFinished(Runnable afterFinished);

  /**
   Go to the given macro program right away
   https://www.pivotaltracker.com/story/show/186003440

   @param macroProgram to go to
   */
  void doOverrideMacro(Program macroProgram);

  /**
   Reset the macro program override
   https://www.pivotaltracker.com/story/show/186003440
   */
  void resetOverrideMacro();

  /**
   @return the meme taxonomy from the current template configuration
   */
  Optional<MemeTaxonomy> getMemeTaxonomy();

  /**
   Manually go to a specific taxonomy category meme, and force until reset
   https://www.pivotaltracker.com/story/show/186714075

   @param memes specific (assumed allowably) set of taxonomy category memes
   */
  void doOverrideMemes(Collection<String> memes);

  /**
   Reset the taxonomy category memes
   https://www.pivotaltracker.com/story/show/186714075
   */
  void resetOverrideMemes();

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
