//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.app.impl;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.xj.core.access.impl.Access;
import io.xj.core.app.Heartbeat;
import io.xj.core.dao.ChainDAO;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.work.Work;
import io.xj.core.work.WorkManager;

import java.util.Collection;
import java.util.Map;

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
  public Map<String, Collection> pulse() throws Exception {
    Map<String, Collection> pulse = Maps.newConcurrentMap();
    pulse.put(Work.KEY_MANY, workManager.reinstateAllWork());
    pulse.put(Chain.KEY_MANY, chainDAO.checkAndReviveAll(Access.internal()));
    return pulse;
  }
}
