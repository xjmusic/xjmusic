// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.eraseworker.erase.impl;

import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.dao.ChainDAO;
import io.xj.core.dao.LinkDAO;
import io.xj.core.model.chain.ChainState;
import io.xj.core.tables.records.ChainRecord;
import io.xj.core.tables.records.LinkRecord;
import io.xj.eraseworker.erase.ChainEraseWorker;

import org.jooq.Result;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChainEraseWorkerImpl implements ChainEraseWorker {
  private final static Logger log = LoggerFactory.getLogger(ChainEraseWorkerImpl.class);
  private final Integer batchSize;
  private final ChainDAO chainDAO;
  private final LinkDAO linkDAO;

  @Inject
  public ChainEraseWorkerImpl(
    @Assisted("batchSize") Integer batchSize,
    ChainDAO chainDAO,
    LinkDAO linkDAO
  ) {
    this.batchSize = batchSize;
    this.chainDAO = chainDAO;
    this.linkDAO = linkDAO;
  }

  @Override
  public Runnable getTaskRunnable() throws Exception {
    return new ChainEraseWorkerTaskRunner(chainDAO, linkDAO);
  }

  /**
   This runnable is executed in a thread pool
   */
  public class ChainEraseWorkerTaskRunner implements Runnable {
    private final ChainDAO chainDAO;
    private final LinkDAO linkDAO;

    ChainEraseWorkerTaskRunner(
      ChainDAO chainDAO,
      LinkDAO linkDAO
    ) throws BusinessException {
      this.chainDAO = chainDAO;
      this.linkDAO = linkDAO;
    }

    @Override
    public void run() {
      try {
        for (ChainRecord chain : getChainsToErase())
          eraseChain(chain);


      } catch (Exception e) {
        log.error("{}:{} failed with exception {}",
          this.getClass().getSimpleName(), Thread.currentThread().getName(), e);
      }
    }

    /**
     Do Chain Erase Work
     If the Chain is empty, Eraseworker deletes the chain

     @param chain to erase
     @throws Exception on failure
     */
    private void eraseChain(ChainRecord chain) throws Exception {
      Result<LinkRecord> links = linkDAO.readAll(Access.internal(), chain.getId());
      if (links.size() == 0)
        chainDAO.delete(Access.internal(), chain.getId());
      else
        eraseLinks(links);
    }

    /**
     Erase many links
     Eraseworker iterates on each link in the chain, reading in batches of a limited size

     @param links to erase
     @throws Exception on failure
     */
    private void eraseLinks(Result<LinkRecord> links) throws Exception {
      for (LinkRecord link : links)
        eraseLink(link);
    }

    /**
     Erase a link
     Eraseworker removes all child entities for the Link
     Eraseworker deletes all S3 objects for the Link
     If the Link is empty and the S3 object is confirmed deleted, Eraseworker deletes the Link

     @param link to erase
     @throws Exception on failure
     */
    private void eraseLink(LinkRecord link) throws Exception {
      linkDAO.destroy(Access.internal(), link.getId());
      log.info("Erased Link #{}, destroyed child entities, and deleted s3 object {}", link.getId(), link.getWaveformKey());
    }

    /**
     Get chains to erase

     @return chains
     @throws Exception on failure
     */
    private Result<ChainRecord> getChainsToErase() throws Exception {
      return chainDAO.readAllInState(Access.internal(), ChainState.Erase, batchSize);
    }

  }

}
