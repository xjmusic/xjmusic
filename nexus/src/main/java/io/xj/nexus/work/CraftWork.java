// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.work;

import io.xj.hub.HubContent;
import io.xj.hub.TemplateConfig;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.Program;
import io.xj.nexus.NexusException;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoiceArrangementPick;

import java.util.List;
import java.util.Optional;

/**
 THERE IS NO SPOON
 <p>
 Ground-up rewrite of the XJ work logic. First we instantiate the dub cycle, which depends on the craft cycle
 operating on a separate thread. All Segment craft is persisted in memory, and the dub cycle is responsible for
 requesting crafted segments and specifically the picked audio, and then dubbing each output audio chunk.
 <p>
 Output audio chunks are dynamically sized. There is a default size, but the size is also determined by the
 duration of the segment. E.g., during gapless album output, the chunk will cut short if necessary to begin the next
 chunk at exactly the top of the following segment.
 <p>
 https://www.nurkiewicz.com/2014/11/executorservice-10-tips-and-tricks.html
 */
public interface CraftWork {

  /**
   This blocks for as long as the work is alive
   */
  void start();

  /**
   Stop work
   */
  void finish();

  /**
   Test whether all expected chains are healthy, depending on chain manager mode
   Whether the next cycle nanos is above threshold, compared to System.nanoTime();

   @return next cycle nanos
   */
  boolean isHealthy();

  /**
   Set the target plan to the given chain micros

   @param chainMicros the target plan
   */
  void setAtChainMicros(long chainMicros);

  /**
   Get the current chain, if loaded

   @return the current chain
   */
  Optional<Chain> getChain();

  /**
   Get the template config, if loaded

   @return the template config
   */
  Optional<TemplateConfig> getTemplateConfig();

  /**
   Get all ready segments - if none are ready, return an empty list

   @return all ready segments
   */
  List<Segment> getAllSegments();

  /**
   Get the segments spanning the given time range, if they are ready- if not, return an empty list

   @param planFromChainMicros the start time
   @param planToChainMicros   the end time
   @return the segments spanning the given time range, or an empty list if the segment span is not ready
   */
  List<Segment> getSegmentsIfReady(Long planFromChainMicros, Long planToChainMicros);

  /**
   Get the segment at the given chain microseconds, if it is ready
   segment beginning <= chain microseconds < end

   @param chainMicros the microseconds since beginning of chain for which to get the segment
   @return the segment at the given chain microseconds, or an empty optional if the segment is not ready
   */
  Optional<Segment> getSegmentAtChainMicros(long chainMicros);

  /**
   Get the segment at the given offset, if it is ready

   @param offset of segment
   @return the segment at the given offset
   */
  Optional<Segment> getSegmentAtOffset(int offset);

  /**
   Get the segments spanning the given time range, if they are ready- if not, return an empty list

   @param segments the segments for which to get picks
   @return the picks for the given segments
   */
  List<SegmentChoiceArrangementPick> getPicks(List<Segment> segments) throws NexusException;

  /**
   Get the instrument for the given pick

   @param audio the audio for which to get instrument
   @return the instrument for the given pick
   */
  Optional<Instrument> getInstrument(InstrumentAudio audio);

  /**
   Get the audio for the given pick

   @param pick the pick for which to get audio
   @return the audio for the given pick
   */
  Optional<InstrumentAudio> getInstrumentAudio(SegmentChoiceArrangementPick pick);

  /**
   @return the input template key
   */
  String getTemplateKey();

  /**
   Check whether the given pick is muted (by its choice)

   @param pick the pick for which to get audio
   @return true if the given pick is muted
   */
  boolean isMuted(SegmentChoiceArrangementPick pick);

  /**
   Check whether the craft work is running

   @return true if running
   */
  boolean isRunning();

  /**
   Get the main program for the given segment

   @param segment for which to get main program
   @return the main program for the given segment, or empty if not chosen
   */
  Optional<Program> getMainProgram(Segment segment);

  /**
   Get the macro program for the given segment

   @param segment for which to get macro program
   @return the macro program for the given segment, or empty if not chosen
   */
  Optional<Program> getMacroProgram(Segment segment);

  /**
   Whether craft work has failed

   @return true if failed
   */
  boolean isFailed();

  /**
   Get the source material
   */
  HubContent getSourceMaterial();

  /**
   Get the crafted-to chain micros

   @return the crafted-to chain micros
   */
  Optional<Long> getCraftedToChainMicros();
}
