package io.xj.ship.broadcast;

import io.xj.hub.tables.pojos.Template;
import io.xj.lib.util.ValueException;
import io.xj.ship.ShipException;

import java.io.IOException;

/**
 * MPEG-DASH Media Presentation Description
 * <p>
 * References
 * <p>
 * ISO/IEC 23009-1:2019: Information technology — Dynamic adaptive streaming over HTTP (DASH) — Part 1: Media presentation description and segment formats, 2019-12,
 * International Organization for Standardization, Geneva, Switzerland.
 * - https://www.iso.org/standard/79329.html
 * - https://bitmovin.com/dynamic-adaptive-streaming-http-mpeg-dash/
 * - https://en.wikipedia.org/wiki/Dynamic_Adaptive_Streaming_over_HTTP
 * - https://ottverse.com/free-mpeg-dash-mpd-manifest-example-test-urls/
 * - https://mpeg.chiariglione.org/standards/mpeg-dash/media-presentation-description-and-segment-formats
 * - http://rdmedia.bbc.co.uk/dash/ondemand/bbb/
 */
public interface PlaylistProvider {
  String computeMpdXML(String shipKey, String shipTitle, String shipSource, long nowMillis) throws IOException, ShipException, ValueException;
}
