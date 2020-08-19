package io.xj.service.nexus.work;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.lib.entity.Entity;
import io.xj.lib.entity.EntityStoreException;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.dao.SegmentDAO;
import io.xj.service.nexus.entity.Segment;
import io.xj.service.nexus.persistence.NexusEntityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Janitor Worker implementation
 */
public class JanitorWorkerImpl implements JanitorWorker {
  private final Logger log = LoggerFactory.getLogger(JanitorWorker.class);
  private final HubClientAccess access = HubClientAccess.internal();
  private final NexusEntityStore store;
  private final SegmentDAO segmentDAO;
  private final int eraseSegmentsOlderThanSeconds;

  @Inject
  public JanitorWorkerImpl(
    NexusEntityStore store,
    SegmentDAO segmentDAO,
    Config config
  ) {
    this.store = store;
    this.segmentDAO = segmentDAO;

    eraseSegmentsOlderThanSeconds = config.getInt("work.eraseSegmentsOlderThanSeconds");

    log.info("Instantiated OK");
  }

  public void run() {
    final Thread currentThread = Thread.currentThread();
    final String _ogThreadName = currentThread.getName();
    currentThread.setName(_ogThreadName + "-Janitor");
    try {
      long t = Instant.now().toEpochMilli();
      Collection<UUID> idsToErase = getSegmentIdsToErase();
      for (UUID segmentId : idsToErase)
        segmentDAO.destroy(access, segmentId);
      if (idsToErase.isEmpty())
        log.info("Found no segments to erase in {}ms OK", Instant.now().toEpochMilli() - t);
      else
        log.info("Did erase {} segments in {}ms OK", idsToErase.size(), Instant.now().toEpochMilli() - t);

    } catch (Throwable e) {
      log.error("Failed!", e);

    } finally {
      currentThread.setName(_ogThreadName);
    }
  }

  /**
   Get the IDs of all Segments that we ought to erase

   @return list of IDs of Segments we ought to erase
   */
  private Collection<UUID> getSegmentIdsToErase() throws EntityStoreException {
    Instant eraseBefore = Instant.now().minusSeconds(eraseSegmentsOlderThanSeconds);
    return store.getAll(Segment.class)
      .stream()
      .filter(segment -> segment.isBefore(eraseBefore))
      .map(Entity::getId)
      .collect(Collectors.toList());
  }
}
