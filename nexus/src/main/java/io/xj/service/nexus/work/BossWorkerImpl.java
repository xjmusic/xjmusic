package io.xj.service.nexus.work;

import com.google.inject.Inject;
import datadog.trace.api.Trace;
import io.xj.Chain;
import io.xj.lib.entity.Entities;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.dao.ChainDAO;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 Boss Worker implementation
 */
public class BossWorkerImpl extends WorkerImpl implements BossWorker {
  private static final String NAME = "Boss";
  private final Logger log = LoggerFactory.getLogger(BossWorkerImpl.class);
  private final HubClientAccess access = HubClientAccess.internal();
  private final NexusWork work;
  private final ChainDAO chainDAO;
  private final TelemetryProvider telemetryProvider;

  @Inject
  public BossWorkerImpl(
    NexusWork work,
    ChainDAO chainDAO,
    TelemetryProvider telemetryProvider
  ) {
    this.work = work;
    this.chainDAO = chainDAO;
    this.telemetryProvider = telemetryProvider;

    log.info("Instantiated OK");
  }

  /**
   Do the work-- this is called by the underlying WorkerImpl run() hook

   @throws DAOPrivilegeException on access failure
   @throws DAOFatalException     on internal failure
   */
  @Trace(resourceName = "nexus/boss", operationName = "doWork")
  protected void doWork() throws DAOPrivilegeException, DAOFatalException {
    Collection<String> activeIds = getActiveChainIds();
    startActiveChains(activeIds);
    cancelInactiveChains(activeIds);
  }

  @Override
  protected String getName() {
    return NAME;
  }

  /**
   Get the IDs of all Chains in the store whose state is currently in Fabricate

   @return active Chain IDS
   @throws DAOPrivilegeException on access control failure
   @throws DAOFatalException     on internal failure
   */
  private Collection<String> getActiveChainIds() throws DAOPrivilegeException, DAOFatalException {
    return chainDAO.readManyInState(access, Chain.State.Fabricate)
      .stream()
      .flatMap(Entities::flatMapIds)
      .collect(Collectors.toList());
  }

  /**
   Cancel all Chains that are not in the list of active Chain IDs

   @param activeIds to avoid cancellation
   */
  private void cancelInactiveChains(Collection<String> activeIds) {
    int chainsCanceled = 0;
    for (String id : work.getChainWorkingIds())
      if (!activeIds.contains(id)) {
        work.cancelChainWork(id);
        log.info("Did cancel work on Chain[{}]", id);
        chainsCanceled++;
      }
    telemetryProvider.getStatsDClient().incrementCounter("chain.cancelled", chainsCanceled);
  }

  /**
   Start work on all chains from this list of active Chain IDs

   @param activeIds to start if not already active
   */
  private void startActiveChains(Collection<String> activeIds) {
    int chainsStarted = 0;
    for (String id : activeIds)
      if (!work.isWorkingOnChain(id)) {
        work.beginChainWork(id);
        log.info("Did start work on Chain[{}]", id);
        chainsStarted++;
      }
    telemetryProvider.getStatsDClient().incrementCounter("chain.started", chainsStarted);
  }
}
