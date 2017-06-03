// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.chain_gang.impl.link_pilot_work;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.dao.ChainDAO;
import io.outright.xj.core.dao.LinkDAO;
import io.outright.xj.core.tables.records.ChainRecord;
import io.outright.xj.core.util.timestamp.TimestampUTC;
import io.outright.xj.core.chain_gang.Leader;

import org.jooq.Result;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 The pilot leader creates template entities of new Links that need to be readMany
 */
public class LinkPilotLeaderImpl implements Leader {
  private final static Logger log = LoggerFactory.getLogger(LinkPilotLeaderImpl.class);
  private final LinkDAO linkDAO;
  private final int bufferSeconds;
  private final int batchSize; // TODO implement batch size in pilot leader getTasks
  private ChainDAO chainDAO;

  @Inject
  public LinkPilotLeaderImpl(
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

  @Override
  public JSONArray getTasks() {
    try {
      return buildNextLinksOrComplete(
        chainDAO.readAllInStateFabricating(
          Access.internal(),
          TimestampUTC.nowPlusSeconds(bufferSeconds)));

    } catch (Exception e) {
      log.error("PilotLeader get chains", e);
      return new JSONArray();
    }
  }

  private JSONArray buildNextLinksOrComplete(Result<ChainRecord> chains) {
    JSONArray tasks = new JSONArray();

    if (chains != null && chains.size() > 0) {
      for (ChainRecord chain : chains) {
        try {
          JSONObject createLinkTask = chainDAO.buildNextLinkOrComplete(
            Access.internal(),
            chain,
            TimestampUTC.nowPlusSeconds(bufferSeconds),
            TimestampUTC.nowMinusSeconds(bufferSeconds));

          if (createLinkTask != null) {
            tasks.put(createLinkTask);
          }

        } catch (Exception e) {
          log.error("PilotLeader get tasks", e);
        }
      }
    }

    return tasks;
  }

}


