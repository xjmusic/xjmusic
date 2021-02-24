package io.xj.service.nexus.work;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import datadog.trace.api.Trace;
import io.xj.Chain;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.dao.ChainDAO;
import io.xj.service.nexus.dao.SegmentDAO;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;

/**
 Medic Worker implementation
 */
public class MedicWorkerImpl extends WorkerImpl implements MedicWorker {
  private static final String NAME = "Medic";
  private final Logger log = LoggerFactory.getLogger(MedicWorker.class);
  private final HubClientAccess access = HubClientAccess.internal();
  private final ChainDAO chainDAO;
  private final int reviveChainProductionStartedBeforeSeconds;
  private final TelemetryProvider telemetryProvider;
  private final SegmentDAO segmentDAO;
  private final int reviveChainSegmentsDubbedPastSeconds;
  private final boolean medicEnabled;

  @Inject
  public MedicWorkerImpl(
    Config config,
    ChainDAO chainDAO,
    SegmentDAO segmentDAO,
    TelemetryProvider telemetryProvider
  ) {
    this.chainDAO = chainDAO;
    this.segmentDAO = segmentDAO;
    this.telemetryProvider = telemetryProvider;

    medicEnabled = config.getBoolean("fabrication.medicEnabled");
    reviveChainSegmentsDubbedPastSeconds = config.getInt("fabrication.reviveChainSegmentsDubbedPastSeconds");
    reviveChainProductionStartedBeforeSeconds = config.getInt("fabrication.reviveChainProductionStartedBeforeSeconds");

    log.debug("Instantiated OK");
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
  @Trace(resourceName = "nexus/medic", operationName = "doWork")
  protected void doWork() throws DAOFatalException, DAOPrivilegeException, DAOValidationException, DAOExistenceException {
    if (medicEnabled) {
      long t = Instant.now().toEpochMilli();
      checkAndReviveAll();
      log.debug("Did run in {}ms OK", Instant.now().toEpochMilli() - t);
    }
  }

  /**
   [#158897383] Engineer wants platform heartbeat to check for any stale production chains in fabricate state,
   and if found, *revive* it in order to ensure the Chain remains in an operable state.

   @throws DAOFatalException      on failure
   @throws DAOPrivilegeException  on failure
   @throws DAOValidationException on failure
   @throws DAOExistenceException  on failure
   */
  @Trace(resourceName = "nexus/medic", operationName = "checkAndReviveAll")
  public void checkAndReviveAll() throws DAOFatalException, DAOPrivilegeException, DAOValidationException, DAOExistenceException {
    Instant thresholdChainProductionStartedBefore = Instant.now().minusSeconds(reviveChainProductionStartedBeforeSeconds);
    Instant thresholdChainSegmentsDubbedPast = Instant.now().plusSeconds(reviveChainSegmentsDubbedPastSeconds);

    Map<String, String> stalledChainIds = Maps.newHashMap();
    chainDAO.readManyInState(access, Chain.State.Fabricate)
      .stream()
      .filter((chain) ->
        Chain.Type.Production.equals(chain.getType()) &&
          Instant.parse(chain.getStartAt()).isBefore(thresholdChainProductionStartedBefore))
      .forEach(chain -> {
        try {
          var lastDubbedSegment = segmentDAO.readLastDubbedSegment(access, chain.getId());
          Instant chainDubbedUntil = lastDubbedSegment.isPresent() ?
            Instant.parse(lastDubbedSegment.get().getEndAt()) :
            Instant.parse(chain.getStartAt());
          log.debug("Chain[{}] dubbed until {} -- required until {}",
            chain.getId(), chainDubbedUntil, thresholdChainSegmentsDubbedPast);
          if (chainDubbedUntil.isBefore(thresholdChainSegmentsDubbedPast)) {
            log.warn("Chain {} is stalled!", chain.getId());
            stalledChainIds.put(chain.getId(),
              String.format("Segments dubbed until %s but are required to be dubbed until %s in production Chain started before %s",
                chainDubbedUntil, thresholdChainSegmentsDubbedPast, thresholdChainProductionStartedBefore));
          }
        } catch (DAOFatalException | DAOPrivilegeException | DAOExistenceException e) {

          log.warn("Failure while checking for Chains to revive!", e);
          e.printStackTrace();
        }
      });

    // revive all stalled chains
    for (String stalledChainId : stalledChainIds.keySet()) {
      chainDAO.revive(access, stalledChainId, stalledChainIds.get(stalledChainId));
      // [#173968355] Nexus deletes entire chain when no current segments are left.
      chainDAO.destroy(access, stalledChainId);
    }

    telemetryProvider.getStatsDClient().incrementCounter("chain.revived", stalledChainIds.size());
  }
}
