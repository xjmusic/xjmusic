// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

/**
 M3U8 playlist item to support advanced parsing of M3U8
 <p>
 Ship rehydrates from last shipped .m3u8 playlist file #180723357
 */
public class M3U8PlaylistItem {
  private final DecimalFormat df;
  private final long mediaSequence;
  private final double seconds;
  private final String filename;

  public M3U8PlaylistItem(long mediaSequence, double seconds, String filename) {
    this.mediaSequence = mediaSequence;
    this.seconds = seconds;
    this.filename = filename;
    df = new DecimalFormat("#.######");
    df.setRoundingMode(RoundingMode.FLOOR);
    df.setMinimumFractionDigits(6);
    df.setMaximumFractionDigits(6);
  }

  /**
   @return media sequence number
   */
  public long getMediaSequence() {
    return mediaSequence;
  }

  /**
   @return number of seconds length of audio
   */
  public double getSeconds() {
    return seconds;
  }

  /**
   @return # of seconds formatted to 6 decimal places
   */
  public String getSecondsFormatted() {
    return String.format("#EXTINF:%s,", df.format(seconds));
  }

  /**
   @return filename
   */
  public String getFilename() {
    return filename;
  }

  /**
   Format the .m3u8 playlist items as a sequence of two lines, as it would appear in the playlist

   @return playlist item lines
   */
  public List<String> toLines() {
    return List.of(
      getSecondsFormatted(),
      getFilename()
    );
  }
}
