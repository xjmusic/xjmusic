package io.xj.service.nexus.work;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.dao.ChainDAO;
import io.xj.service.nexus.dao.SegmentDAO;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;
import io.xj.service.nexus.entity.ChainState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 Medic Worker implementation
 */
public class MedicWorkerImpl extends WorkerImpl implements MedicWorker {
  private static final String NAME = "Medic";
  private static final String CHAINS_REVIVED = "CHAINS_REVIVED";
  private final Logger log = LoggerFactory.getLogger(MedicWorker.class);
  private final HubClientAccess access = HubClientAccess.internal();
  private final ChainDAO chainDAO;
  private final int chainReviveThresholdStartSeconds;
  private final SegmentDAO segmentDAO;
  private final int chainReviveThresholdHeadSeconds;

  @Inject
  public MedicWorkerImpl(
    Config config,
    ChainDAO chainDAO,
    SegmentDAO segmentDAO,
    TelemetryProvider telemetryProvider
  ) {
    super(telemetryProvider);
    this.chainDAO = chainDAO;
    this.segmentDAO = segmentDAO;

    chainReviveThresholdHeadSeconds = config.getInt("chain.reviveThresholdHeadSeconds");
    chainReviveThresholdStartSeconds = config.getInt("chain.reviveThresholdStartSeconds");

    log.info("Instantiated OK");
  }

  @Override
  protected String getName() {
    return NAME;
  }

  /**
   Do the work-- this is called by the underlying WorkerImpl run() hook

   @throws DAOFatalException      on failure
   @throws DAOPrivilegeException  on failure
   @throws DAOValidationException on failure
   @throws DAOExistenceException  on failure
   */
  protected void doWork() throws DAOFatalException, DAOPrivilegeException, DAOValidationException, DAOExistenceException {
    long t = Instant.now().toEpochMilli();
    checkAndReviveAll();
    log.info("Did run in {}ms OK", Instant.now().toEpochMilli() - t);
  }

  /**
   [#158897383] Engineer wants platform heartbeat to check for any stale production chains in fabricate state,
   and if found, *revive* it in order to ensure the Chain remains in an operable state.

   @throws DAOFatalException      on failure
   @throws DAOPrivilegeException  on failure
   @throws DAOValidationException on failure
   @throws DAOExistenceException  on failure
   */
  public void checkAndReviveAll() throws DAOFatalException, DAOPrivilegeException, DAOValidationException, DAOExistenceException {
    Instant thresholdChainStartAt = Instant.now().minusSeconds(chainReviveThresholdStartSeconds);
    Instant thresholdChainHeadAt = Instant.now().minusSeconds(chainReviveThresholdHeadSeconds);

    Map<UUID, String> stalledChainIds = Maps.newHashMap();
    chainDAO.readManyInState(access, ChainState.Fabricate)
      .stream()
      .filter((chain) -> chain.isProductionStartedBefore(thresholdChainStartAt))
      .forEach(chain -> {
        try {
          if (!segmentDAO.existsAnyDubbedEndingAfter(chain.getId(), thresholdChainHeadAt)) {
            log.warn("Found stalled Chain {} with no Segments Dubbed ending after {}", chain.getId(), thresholdChainHeadAt);
            stalledChainIds.put(chain.getId(), String.format("found no Segments Dubbed ending after %s in production Chain started before %s", thresholdChainHeadAt, thresholdChainStartAt));
          }
        } catch (DAOFatalException e) {
          log.warn("Failure while checking for Chains to revive!", e);
          e.printStackTrace();
        }
      });

    // revive all stalled chains
    for (UUID stalledChainId : stalledChainIds.keySet()) {
      chainDAO.revive(access, stalledChainId, stalledChainIds.get(stalledChainId));
      // [#173968355] Nexus deletes entire chain when no current segments are left.
      chainDAO.destroy(access, stalledChainId);
    }

    observeCount(CHAINS_REVIVED, stalledChainIds.size());
  }
}
