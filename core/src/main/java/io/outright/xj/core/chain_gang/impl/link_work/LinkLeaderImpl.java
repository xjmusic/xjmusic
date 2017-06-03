// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.chain_gang.impl.link_work;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.dao.ChainDAO;
import io.outright.xj.core.dao.LinkDAO;
import io.outright.xj.core.tables.records.ChainRecord;
import io.outright.xj.core.transport.JSON;
import io.outright.xj.core.util.timestamp.TimestampUTC;
import io.outright.xj.core.chain_gang.Leader;

import org.jooq.Result;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 The link leader creates template entities of new Links that need to be readMany
 */
public class LinkLeaderImpl implements Leader {
  private final static Logger log = LoggerFactory.getLogger(LinkLeaderImpl.class);
  private final LinkDAO linkDAO;
  private ChainDAO chainDAO;
  private String fromState;
  private int bufferSeconds = 0;
  private int batchSize = 0; // TODO implement batch size in link leader

  @AssistedInject
  public LinkLeaderImpl(
    ChainDAO chainDAO,
    LinkDAO linkDAO,
    @Assisted("bufferSeconds") int bufferSeconds,
    @Assisted("batchSize") int batchSize
  ) {
    this.chainDAO = chainDAO;
    this.linkDAO = linkDAO;
    this.bufferSeconds = bufferSeconds;
    this.batchSize = batchSize;
  }

  @AssistedInject
  public LinkLeaderImpl(
    ChainDAO chainDAO,
    LinkDAO linkDAO,
    @Assisted("fromState") String fromState,
    @Assisted("bufferSeconds") int bufferSeconds,
    @Assisted("batchSize") int batchSize
  ) {
    this.chainDAO = chainDAO;
    this.linkDAO = linkDAO;
    this.fromState = fromState;
    this.bufferSeconds = bufferSeconds;
    this.batchSize = batchSize;
  }

  @Override
  public JSONArray getTasks() {
    JSONArray tasks = new JSONArray();
    try {
      Result<ChainRecord> chains = chainDAO.readAllInStateFabricating(Access.internal(), TimestampUTC.nowPlusSeconds(bufferSeconds));
      if (chains != null && chains.size() > 0) {
        for (ChainRecord chain : chains) {
          JSONObject link = readLinkFor(chain, fromState);
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

  private JSONObject readLinkFor(ChainRecord chain, String linkState) throws Exception {
    return JSON.objectFromRecord(linkDAO.readOneInState(
      Access.internal(),
      chain.getId(),
      linkState,
      TimestampUTC.nowPlusSeconds(bufferSeconds)));
  }

}


