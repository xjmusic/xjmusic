// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.work;

import io.xj.hub.HubContent;
import io.xj.hub.TemplateConfig;
import io.xj.hub.pojos.Instrument;
import io.xj.hub.pojos.InstrumentAudio;
import io.xj.hub.pojos.Program;
import io.xj.engine.FabricationException;
import io.xj.engine.model.Chain;
import io.xj.engine.model.Segment;
import io.xj.engine.model.SegmentChoiceArrangementPick;

import java.util.Collection;
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
 */
public interface CraftWork extends Work {

  /**
   Run the work cycle
   */
  void runCycle(long shippedToChainMicros, long dubbedToChainMicros);

  /**
   Get the current chain, if loaded

   @return the current chain
   */
  Optional<Chain> getChain();

  /**
   Get the template config, if loaded

   @return the template config
   */
  TemplateConfig getTemplateConfig();

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
  List<SegmentChoiceArrangementPick> getPicks(List<Segment> segments) throws FabricationException;

  /**
   Get the instrument for the given pick

   @param audio the audio for which to get instrument
   @return the instrument for the given pick
   */
  Instrument getInstrument(InstrumentAudio audio);

  /**
   Get the audio for the given pick

   @param pick the pick for which to get audio
   @return the audio for the given pick
   */
  InstrumentAudio getInstrumentAudio(SegmentChoiceArrangementPick pick);

  /**
   Check whether the given pick is muted (by its choice)

   @param pick the pick for which to get audio
   @return true if the given pick is muted
   */
  boolean isMuted(SegmentChoiceArrangementPick pick);

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
   Get the source material
   */
  HubContent getSourceMaterial();

  /**
   Get the crafted-to chain micros

   @return the crafted-to chain micros
   */
  Optional<Long> getCraftedToChainMicros();

  /**
   Whether the current craft state is ready

   @return true if ready
   */
  boolean isReady();

  /**
   Go to the given macro program right now
   https://github.com/xjmusic/workstation/issues/197

   @param macroProgram the macro program to go to
   */
  void doOverrideMacro(Program macroProgram);

  /**
   Manually go to a specific taxonomy category meme, and force until reset
   https://github.com/xjmusic/workstation/issues/199

   @param memes specific (assumed allowably) set of taxonomy category memes
   */
  void doOverrideMemes(Collection<String> memes);

  /**
   Get whether an override happened, and reset its state after getting

   @return true if an override happened
   */
  boolean getAndResetDidOverride();
}
