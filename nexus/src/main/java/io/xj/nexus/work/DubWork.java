// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.work;

import io.xj.hub.tables.pojos.Program;
import io.xj.lib.mixer.BytePipeline;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.Segment;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
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
public interface DubWork {

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
   Test whether the next cycle is planned ahead

   @return true if planned ahead
   */
  boolean isPlannedAhead();

  /**
   Check whether the work is active

   @return true if active
   */
  boolean isRunning();

  /**
   Whether the dub work has failed

   @return true if faile
   */
  boolean isFailed();

  /**
   Get the mixer audio buffer to read the mix output

   @return the mixer audio buffer, or empty if not yet available
   */
  Optional<BytePipeline> getMixerBuffer();

  /**
   Get the number of bytes available from the mixer

   @return the number of bytes available
   */
  int getMixerBufferAvailableBytesCount() throws IOException;

  /**
   Get the audio format of the mixer

   @return the audio format, or empty if not yet available
   */
  Optional<AudioFormat> getAudioFormat();

  /**
   Get the number of bytes per microsecond of the mixer

   @return the number of bytes per microsecond, or empty if not yet available
   */
  Optional<Float> getMixerOutputMicrosPerByte();

  /**
   Get the chain from craft work

   @return chain or empty if not yet available
   */
  Optional<Chain> getChain();

  /**
   Get the input template key

   @return the input template key
   */
  String getInputTemplateKey();

  /**
   Get the segment at the given chain micros

   @param atChainMicros the chain micros
   @return the segment, or empty if not yet available
   */
  Optional<Segment> getSegmentAtChainMicros(long atChainMicros);

  /**
   Get the segment at the given offset

   @param offset the offset
   @return the segment, or empty if not yet available
   */
  Optional<Segment> getSegmentAtOffset(int offset);

  /**
   Get the main program for the given segment

   @param segment for which to get program
   @return main program or empty if not yet available
   */
  Optional<Program> getMainProgram(Segment segment);

  /**
   Get the macro program for the given segment

   @param segment for which to get program
   @return macro program or empty if not yet available
   */
  Optional<Program> getMacroProgram(Segment segment);
}
