package io.xj.ship.persistence;

import io.xj.api.Segment;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.mixer.FormatException;
import io.xj.ship.ShipException;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Ship broadcast via HTTP Live Streaming #179453189
 */
public interface SegmentAudioManager {

  /**
   * Get the segment audio for a given segment id
   *
   * @param segmentId for which to get audio
   * @return segment audio if found
   */
  Optional<SegmentAudio> get(UUID segmentId);

  /**
   * Update a segment audio to failed state
   *
   * @param id to update
   */
  void updateStateFailed(UUID id);

  /**
   * Update the audio stream loader for a given segment id
   *
   * @param shipKey of segment for which to update audio loader
   * @param segment audio data
   */
  void createAndLoadAudio(String shipKey, Segment segment) throws ShipException, IOException, FormatException, FileStoreException;

  /**
   * Put a Segment Audio in the store
   *
   * @param segmentAudio to put
   */
  void put(SegmentAudio segmentAudio);

  /**
   * Destroy the audio for a segment
   *
   * @param segmentId for which to destroy audio
   */
  void collectGarbage(UUID segmentId);

  /**
   * Get all segments intersecting the specified time frame
   *
   * @param shipKey     of segments
   * @param fromInstant time frame
   * @param toInstant   time frame
   * @return all segments intersecting the time frame
   */
  Collection<SegmentAudio> getAllIntersecting(String shipKey, Instant fromInstant, Instant toInstant);

}
