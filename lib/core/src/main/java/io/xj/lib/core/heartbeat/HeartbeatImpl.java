// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.heartbeat;

import com.google.inject.Inject;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.dao.ChainDAO;
import io.xj.lib.core.payload.Payload;
import io.xj.lib.core.work.WorkManager;

/**
 Implementation of application heartbeat pulse
 */
public class HeartbeatImpl implements Heartbeat {
  private final WorkManager workManager;
  private final ChainDAO chainDAO;

  @Inject
  public HeartbeatImpl(
    WorkManager workManager,
    ChainDAO chainDAO
  ) {
    this.workManager = workManager;
    this.chainDAO = chainDAO;
  }

  @Override
  public Payload pulse() throws Exception {
    Payload payload = new Payload();
    workManager.reinstateAllWork().forEach(work -> payload.addData(work.toPayloadObject()));
    chainDAO.checkAndReviveAll(Access.internal()).forEach(chain -> payload.addData(chain.toPayloadObject()));
    return payload;
  }
}
