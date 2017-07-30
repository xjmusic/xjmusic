// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.chain_gang.impl.link_pilot_work;

import io.xj.core.chain_gang.Follower;
import io.xj.core.dao.LinkDAO;
import io.xj.core.model.link.Link;
import io.xj.core.model.link.LinkState;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.json.JSONObject;

import java.sql.Timestamp;

public class LinkPilotFollowerImpl implements Follower {
  private final LinkDAO linkDAO;
  private final LinkState finishedState;

  @Inject
  public LinkPilotFollowerImpl(
    LinkDAO linkDAO,
    @Assisted("finishedState") LinkState finishedState
  ) {
    this.finishedState = finishedState;
    this.linkDAO = linkDAO;
  }

  @Override
  public Runnable getTaskRunnable(JSONObject task) throws Exception {
    return new LinkPilotWorkerTaskRunner(
      task.getBigInteger(Link.KEY_CHAIN_ID),
      task.getBigInteger(Link.KEY_OFFSET),
      Timestamp.valueOf(task.get(Link.KEY_BEGIN_AT).toString()),
      finishedState,
      linkDAO
    );
  }

}

