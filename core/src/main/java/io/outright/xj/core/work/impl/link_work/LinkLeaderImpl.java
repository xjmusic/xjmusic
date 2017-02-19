// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.work.impl.link_work;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.dao.ChainDAO;
import io.outright.xj.core.dao.LinkDAO;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.util.timestamp.TimestampUTC;
import io.outright.xj.core.work.Leader;

import org.jooq.types.ULong;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The link leader creates template entities of new Links that need to be created
 */
public class LinkLeaderImpl implements Leader {
  private final static Logger log = LoggerFactory.getLogger(LinkLeaderImpl.class);

  private ChainDAO chainDAO;
  private final LinkDAO linkDAO;
  private String fromState;
  private int aheadSeconds = 0;
  private int batchSize = 0; // TODO implement batch size in link leader

  @AssistedInject
  public LinkLeaderImpl(
    ChainDAO chainDAO,
    LinkDAO linkDAO,
    @Assisted("aheadSeconds") int aheadSeconds,
    @Assisted("batchSize") int batchSize
  ) {
    this.chainDAO = chainDAO;
    this.linkDAO = linkDAO;
    this.aheadSeconds = aheadSeconds;
    this.batchSize = batchSize;
  }

  @AssistedInject
  public LinkLeaderImpl(
    ChainDAO chainDAO,
    LinkDAO linkDAO,
    @Assisted("fromState") String fromState,
    @Assisted("aheadSeconds") int aheadSeconds,
    @Assisted("batchSize") int batchSize
  ) {
    this.chainDAO = chainDAO;
    this.linkDAO = linkDAO;
    this.fromState = fromState;
    this.aheadSeconds = aheadSeconds;
    this.batchSize = batchSize;
  }

  @Override
  public JSONArray getTasks() {
    JSONArray tasks = new JSONArray();
    try {
      JSONArray chains = chainDAO.readAllIdBoundsInProduction(AccessControl.forInternalWorker(), TimestampUTC.now(), aheadSeconds);
      if (chains != null && chains.length() > 0) {
        for (int i = 0; i < chains.length(); i++) {
          JSONObject link = readLinkFor((JSONObject) chains.get(i), fromState);
          if (link != null) {
            tasks.put(link);
          }
        }
      }

    } catch (Exception e) {
      log.error("LinkLeader failed to get tasks", e);
    }

    return tasks;
  }

  private JSONObject readLinkFor(JSONObject chain, String linkState) throws Exception {
    ULong chainId = ULong.valueOf(chain.getBigInteger(Entity.KEY_ID));
    return linkDAO.readOneInState(
      AccessControl.forInternalWorker(),
      chainId,
      linkState,
      TimestampUTC.nowPlusSeconds(aheadSeconds));
  }

}


