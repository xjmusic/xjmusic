// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.ship.broadcast;

import io.xj.nexus.ship.ShipException;

import java.util.List;
import java.util.Optional;

/**
 This process is run directly in the hard loop (not in a Fork/Join pool)
 <p>
 Ship broadcast via HTTP Live Streaming https://www.pivotaltracker.com/story/show/179453189
 <p>
 MPEG-DASH Media Presentation Description
 <p>
 References
 <p>
 ISO/IEC 23009-1:2019: Information technology — Dynamic adaptive streaming over HTTP (DASH) — Part 1: Media presentation description and segment formats, 2019-12,
 International Organization for Standardization, Geneva, Switzerland.
 - https://www.iso.org/standard/79329.html
 - https://bitmovin.com/dynamic-adaptive-streaming-http-mpeg-dash/
 - https://en.wikipedia.org/wiki/Dynamic_Adaptive_Streaming_over_HTTP
 - https://mpeg.chiariglione.org/standards/mpeg-dash/media-presentation-description-and-segment-formats
 - http://rdmedia.bbc.co.uk/dash/ondemand/bbb/
 */
public interface PlaylistPublisher {

  /**
   Attempt to rehydrate ship from the last .m3u8 playlist that was uploaded for this ship key
   <p>
   Ship rehydrates from last shipped .m3u8 playlist file https://www.pivotaltracker.com/story/show/180723357

   @param streamBaseUrl of the stream
   @param initialSeqNum threshold .m3u8 playlist must be ahead of, else it will be considered stale
   @return max sequence number from rehydrated playlist, if found
   */
  Optional<Long> rehydrate(String streamBaseUrl, long initialSeqNum);

  /**
   Get the m3u8 playlist item for a given media sequence number

   @param mediaSequence number for which to get playlist item
   @return playlist item if found
   */
  Optional<Chunk> get(long mediaSequence);

  /**
   Store a m3u8 playlist item, only if it is after the threshold,
   and only if it has a sequence number of exactly the largest known sequence number + 1

   @param chunk to put
   @return true if this is a new media sequence number (playlist item not yet seen)
   */
  boolean putNext(Chunk chunk) throws ShipException;

  /**
   Publish the playlist after a new file is pushed

   @throws ShipException on failure
   */
  void publish() throws ShipException;

  /**
   Delete playlist before the threshold seconds before the given media sequence number

   @param mediaSequence number before the threshold of which to collect garbage
   */
  void collectGarbage(long mediaSequence);

  /**
   Get the whole .m3u8 playlist content, including headers, as a string

   @param mediaSequence at which to get playlist content
   @return playlist content
   */
  String getPlaylistContent(long mediaSequence);

  /**
   Parse and load all items from an .m3u8 file content

   @param m3u8Content to parse and load
   @return list of playlist items that were not previously in the store
   */
  List<Chunk> parseItems(String m3u8Content) throws ShipException;

  /**
   Check if the ship encoder process is healthy
   <p>
   Ship health check tests playlist length and ffmpeg liveness https://www.pivotaltracker.com/story/show/180746583

   @return true if healthy
   */
  boolean isHealthy();

  /**
   Get the maximum sequence number currently in the playlist

   @return max sequence number
   */
  long getMaxSequenceNumber();

  /**
   Whether the playlist is empty

   @return true if empty
   */
  boolean isEmpty();

  /**
   Get the end at chain microseconds for the chunk in the playlist with the maximum sequence number

   @return end at chain microseconds
   */
  Long getMaxToChainMicros();

  /**
   Send telemetry about current hls playlist
   <p>
   Ship should not enter permanent failure state unable to load segments https://www.pivotaltracker.com/story/show/180756082
   */
  void sendTelemetry();

  /**
   Start the playlist publisher

   @param streamBaseUrl of the stream
   @param initialSeqNum initial sequence number
   @return max sequence number from rehydrated playlist, if found
   */
  Optional<Long> start(String streamBaseUrl, long initialSeqNum);

  /**
   Set the target time in chain micros

   @param atChainMicros time in chain micros
   */
  void setAtChainMicros(long atChainMicros);
}
