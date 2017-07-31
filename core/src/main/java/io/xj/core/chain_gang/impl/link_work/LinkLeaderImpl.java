// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.chain_gang.impl.link_work;

import io.xj.core.app.access.impl.Access;
import io.xj.core.chain_gang.Leader;
import io.xj.core.dao.ChainDAO;
import io.xj.core.dao.LinkDAO;
import io.xj.core.model.link.LinkState;
import io.xj.core.tables.records.ChainRecord;
import io.xj.core.transport.JSON;
import io.xj.core.util.timestamp.TimestampUTC;

import org.jooq.Result;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 The link leader creates template entities of new Links that need to be readMany
 */
public class LinkLeaderImpl implements Leader {
  private static final Logger log = LoggerFactory.getLogger(LinkLeaderImpl.class);
  private final LinkDAO linkDAO;
  private ChainDAO chainDAO;
  private LinkState fromState;
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
    @Assisted("fromState") LinkState fromState,
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
      if (Objects.nonNull(chains )&& !chains.isEmpty()) {
        for (ChainRecord chain : chains) {
          JSONObject link = readLinkFor(chain, fromState);
          if (Objects.nonNull(link )) {
            tasks.put(link);
          }
        }
      }

    } catch (Exception e) {
      log.error("LinkLeader failed to get tasks", e);
    }

    return tasks;
  }

  private JSONObject readLinkFor(ChainRecord chain, LinkState linkState) throws Exception {
    return JSON.objectFromRecord(linkDAO.readOneInState(
      Access.internal(),
      chain.getId(),
      linkState,
      TimestampUTC.nowPlusSeconds(bufferSeconds)));
  }

}


