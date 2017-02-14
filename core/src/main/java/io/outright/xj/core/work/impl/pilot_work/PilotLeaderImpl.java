// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.work.impl.pilot_work;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.dao.ChainDAO;
import io.outright.xj.core.dao.LinkDAO;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.model.chain.Chain;
import io.outright.xj.core.work.Leader;

import org.jooq.types.ULong;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;

/**
 * The pilot leader creates template entities of new Links that need to be created
 */
public class PilotLeaderImpl implements Leader {
  private final static Logger log = LoggerFactory.getLogger(PilotLeaderImpl.class);

  private ChainDAO chainDAO;
  private final LinkDAO linkDAO;
  private final int aheadSeconds;
  private final int batchSize;

  @Inject
  public PilotLeaderImpl(
    ChainDAO chainDAO,
    LinkDAO linkDAO,
    @Assisted("aheadSeconds") int aheadSeconds,
    @Assisted("batchSize") int batchSize
  ) {
    this.chainDAO = chainDAO;
    this.linkDAO = linkDAO;
    this.aheadSeconds = aheadSeconds;
    this.batchSize = batchSize; // TODO implement batch size in pilot leader getTasks
  }

  @Override
  public JSONArray getTasks() {
    JSONArray tasks = new JSONArray();
    try {
      JSONArray chains = chainDAO.readAllInProduction(AccessControl.forInternalWorker());
      if (chains != null && chains.length() > 0) {
        for (int i = 0; i < chains.length(); i++) {
          JSONObject pilotLink = readPilotTemplateFor((JSONObject) chains.get(i));
          if (pilotLink != null) {
            tasks.put(pilotLink);
          }
        }
      }

    } catch (Exception e) {
      log.error("PilotLeader get tasks", e);
    }

    return tasks;
  }

  private JSONObject readPilotTemplateFor(JSONObject chain) throws Exception {
    Timestamp chainBeginAt = Timestamp.valueOf(chain.get(Chain.KEY_START_AT).toString());
    ULong chainId = ULong.valueOf(chain.getBigInteger(Entity.KEY_ID));
    JSONObject link = linkDAO.readPilotTemplateFor(
      AccessControl.forInternalWorker(),
      chainId,
      chainBeginAt,
      aheadSeconds);
    return link;
  }

}


