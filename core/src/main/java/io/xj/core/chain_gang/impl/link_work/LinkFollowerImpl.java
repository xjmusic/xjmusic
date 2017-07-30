// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.chain_gang.impl.link_work;

import io.xj.core.chain_gang.ChainGangOperation;
import io.xj.core.chain_gang.Follower;
import io.xj.core.dao.ChainDAO;
import io.xj.core.dao.LinkDAO;
import io.xj.core.dao.LinkMessageDAO;
import io.xj.core.model.link.Link;
import io.xj.core.model.link.LinkState;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.json.JSONObject;

public class LinkFollowerImpl implements Follower {
  private final LinkDAO linkDAO;
  private final ChainDAO chainDAO;
  private final LinkMessageDAO linkMessageDAO;
  private LinkState workingState;
  private final LinkState finishedState;
  private ChainGangOperation chainGangOperation;

  @AssistedInject
  public LinkFollowerImpl(
    LinkDAO linkDAO,
    ChainDAO chainDAO, LinkMessageDAO linkMessageDAO, @Assisted("workingState") LinkState workingState,
    @Assisted("finishedState") LinkState finishedState,
    @Assisted("operation") ChainGangOperation chainGangOperation
  ) {
    this.chainDAO = chainDAO;
    this.linkMessageDAO = linkMessageDAO;
    this.workingState = workingState;
    this.finishedState = finishedState;
    this.linkDAO = linkDAO;
    this.chainGangOperation = chainGangOperation;
  }

  @AssistedInject
  public LinkFollowerImpl(
    LinkDAO linkDAO,
    ChainDAO chainDAO, LinkMessageDAO linkMessageDAO, @Assisted("finishedState") LinkState finishedState
  ) {
    this.chainDAO = chainDAO;
    this.linkMessageDAO = linkMessageDAO;
    this.finishedState = finishedState;
    this.linkDAO = linkDAO;
  }

  @Override
  public Runnable getTaskRunnable(JSONObject task) throws Exception {
    Link link = new Link().setFromJSON(task);
    return new LinkWorkerTaskRunner(chainDAO, linkDAO, linkMessageDAO, chainGangOperation, link, workingState, finishedState);
  }

}

