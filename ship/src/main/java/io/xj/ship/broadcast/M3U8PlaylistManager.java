// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import java.util.List;
import java.util.Optional;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 */
public interface M3U8PlaylistManager {

  /**
   Get the m3u8 playlist item for a given media sequence number

   @param mediaSequence number for which to get playlist item
   @return playlist item if found
   */
  Optional<M3U8PlaylistItem> get(long mediaSequence);

  /**
   Store a m3u8 playlist item, only if it is after the threshold

   @param m3U8Playlist to put
   @return true if this is a new media sequence number (playlist item not yet seen)
   */
  boolean put(M3U8PlaylistItem m3U8Playlist);

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

   @return playlist content
   @param mediaSequence at which to get playlist content
   */
  String getPlaylistContent(long mediaSequence);

  /**
   Parse and load all items from an .m3u8 file content

   @param m3u8Content to parse and load
   @return list of playlist items that were not previously in the store
   */
  List<M3U8PlaylistItem> parseAndLoadItems(String m3u8Content);
}
