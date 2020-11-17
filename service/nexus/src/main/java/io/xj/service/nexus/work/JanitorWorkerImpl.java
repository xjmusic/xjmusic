package io.xj.service.nexus.work;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.Segment;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityStoreException;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.lib.util.Value;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.dao.SegmentDAO;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.persistence.NexusEntityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 Janitor Worker implementation
 */
public class JanitorWorkerImpl extends WorkerImpl implements JanitorWorker {
  private static final String NAME = "Janitor";
  private static final String SEGMENT_ERASED = "SegmentErased";
  private final Logger log = LoggerFactory.getLogger(JanitorWorker.class);
  private final HubClientAccess access = HubClientAccess.internal();
  private final NexusEntityStore store;
  private final SegmentDAO segmentDAO;
  private final int eraseSegmentsOlderThanSeconds;

  @Inject
  public JanitorWorkerImpl(
    Config config,
    NexusEntityStore store,
    SegmentDAO segmentDAO,
    TelemetryProvider telemetryProvider
  ) {
    super(telemetryProvider);
    this.store = store;
    this.segmentDAO = segmentDAO;

    eraseSegmentsOlderThanSeconds = config.getInt("work.eraseSegmentsOlderThanSeconds");

    log.info("Instantiated OK");
  }

  /**
   Do the work-- this is called by the underlying WorkerImpl run() hook

   @throws Exception on failure
   */
  protected void doWork() throws Exception {
    long t = Instant.now().toEpochMilli();
    Collection<String> segmentIdsToErase = getSegmentIdsToErase();
    for (String segmentId : segmentIdsToErase)
      try {
        segmentDAO.destroy(access, segmentId);
      } catch (DAOExistenceException e) {
        log.warn("Entity nonexistent while destroying Segment[{}]", segmentId, e);
      }

    if (segmentIdsToErase.isEmpty())
      log.info("Found no segments to erase in {}ms OK", Instant.now().toEpochMilli() - t);
    else
      log.info("Did erase {} segments in {}ms OK", segmentIdsToErase.size(), Instant.now().toEpochMilli() - t);

    observeCount(SEGMENT_ERASED, segmentIdsToErase.size());
  }

  /**
   Whether this Segment is before a given threshold, first by end-at if available, else begin-at

   @param eraseBefore threshold to filter before
   @return true if segment is before threshold
   */
  protected boolean isBefore(Segment segment, Instant eraseBefore) {
    return Value.isSet(segment.getEndAt()) ?
      Instant.parse(segment.getEndAt()).isBefore(eraseBefore) :
      Instant.parse(segment.getBeginAt()).isBefore(eraseBefore);
  }

  @Override
  protected String getName() {
    return NAME;
  }

  /**
   Get the IDs of all Segments that we ought to erase

   @return list of IDs of Segments we ought to erase
   */
  private Collection<String> getSegmentIdsToErase() throws EntityStoreException {
    Instant eraseBefore = Instant.now().minusSeconds(eraseSegmentsOlderThanSeconds);
    return store.getAll(Segment.class)
      .stream()
      .filter(segment -> isBefore(segment, eraseBefore))
      .flatMap(Entities::flatMapIds)
      .collect(Collectors.toList());
  }
}
