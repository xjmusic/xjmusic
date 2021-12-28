// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import java.util.List;
import java.util.Optional;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 */
public interface PlaylistManager {

  /**
   Get the m3u8 playlist item for a given media sequence number

   @param mediaSequence number for which to get playlist item
   @return playlist item if found
   */
  Optional<Chunk> get(long mediaSequence);

  /**
   Store a m3u8 playlist item, only if it is after the threshold,
   and only if it has a sequence number of exactly the largest known sequence number + 1

   @param m3U8Playlist to put
   @return true if this is a new media sequence number (playlist item not yet seen)
   */
  boolean putNext(Chunk m3U8Playlist);

  /**
   Delete playlist items with media sequence numbers before the threshold

   @param mediaSequence number before which to collect garbage
   */
  void collectGarbageBefore(long mediaSequence);

  /**
   Get the media sequence number of a given time in milliseconds

   @param epochMillis for which to get media sequence number
   @return media sequence number
   */
  int computeMediaSequence(long epochMillis);

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
  List<Chunk> parseAndLoadItems(String m3u8Content);

  /**
   Check if the ship encoder process is healthy
   <p>
   Ship health check tests playlist length and ffmpeg liveness #180746583

   @return true if healthy
   */
  boolean isHealthy();

  /**
   Get the maximum sequence number currently in the playlist

   @return max sequence number
   */
  long getMaxSequenceNumber();
}
