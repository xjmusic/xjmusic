// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.work;

import io.xj.hub.HubConfiguration;
import io.xj.hub.HubContent;
import io.xj.hub.tables.pojos.Program;
import io.xj.nexus.hub_client.HubClientAccess;
import io.xj.nexus.persistence.SegmentManager;

import java.util.Optional;
import java.util.function.Consumer;

public interface WorkManager {
  /**
   Start work.
   <p>
   This assigns the work configuration, hub configuration, and hub access.
   */
  void start(
    WorkConfiguration workConfig,
    HubConfiguration hubConfig,
    HubClientAccess hubAccess
  );

  /**
   Stop work
   */
  void finish(boolean cancelled);

  /**
   Get the segment manager
   */
  SegmentManager getSegmentManager();

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
  WorkState getWorkState();

  /**
   @return true if the current work is healthy
   */
  boolean isHealthy();

  /**
   @return true if the current work is finished
   */
  boolean isFinished();

  /**
   Set the on progress callback

   @param onProgress callback
   */
  void setOnProgress(Consumer<Float> onProgress);

  /**
   Set the on status callback

   @param onStateChange callback
   */
  void setOnStateChange(Consumer<WorkState> onStateChange);

  /**
   Set the on finish callback

   @param afterFinished callback
   */
  void setAfterFinished(Runnable afterFinished);

  /**
   Go to the given macro program right away

   @param macroProgram to go to
   */
  void gotoMacroProgram(Program macroProgram);

  /**
   Out of all the source material main programs, get the minimum sequence duration (in microseconds)

   @return the minimum sequence duration (in microseconds)
   */
  double getMinSequenceDurationMicros();
}
