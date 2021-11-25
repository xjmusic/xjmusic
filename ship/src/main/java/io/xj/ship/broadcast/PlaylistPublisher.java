// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.broadcast;

import io.xj.lib.util.ValueException;
import io.xj.nexus.persistence.ManagerExistenceException;
import io.xj.nexus.persistence.ManagerFatalException;
import io.xj.nexus.persistence.ManagerPrivilegeException;
import io.xj.ship.ShipException;

import java.io.IOException;

/**
 This process is run directly in the hard loop (not in a Fork/Join pool)
 <p>
 Ship broadcast via HTTP Live Streaming #179453189
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
 - https://ottverse.com/free-mpeg-dash-mpd-manifest-example-test-urls/
 - https://mpeg.chiariglione.org/standards/mpeg-dash/media-presentation-description-and-segment-formats
 - http://rdmedia.bbc.co.uk/dash/ondemand/bbb/
 */
public interface PlaylistPublisher {
  /**
   @param nowMillis at which to publish
   */
  void publish(long nowMillis);

  /**
   Compute content of the DASH .mpd (Media Presentation Description) file

   @return XML content
   @throws IOException    on failure
   @throws ShipException  on failure
   @throws ValueException on failure
   @param nowMillis  of stream
   */
  String computeMPD(long nowMillis) throws IOException, ShipException, ValueException, ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException;

  /**
   Compute content of the HLS .m3u8 file

   @return content
   @param nowMillis of stream
   */
  String computeM3U8(long nowMillis);

}
