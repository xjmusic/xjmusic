// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.ChainDAO;
import io.xj.core.dao.LinkDAO;
import io.xj.core.model.link.Link;
import io.xj.core.transport.CSV;
import io.xj.core.work.WorkManager;
import io.xj.worker.job.ChainEraseJob;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

public class ChainEraseJobImpl implements ChainEraseJob {
  private static final Logger log = LoggerFactory.getLogger(ChainEraseJobImpl.class);
  private final BigInteger entityId;
  private final ChainDAO chainDAO;
  private final LinkDAO linkDAO;
  private final WorkManager workManager;

  @Inject
  public ChainEraseJobImpl(
    @Assisted("entityId") BigInteger entityId,
    ChainDAO chainDAO,
    LinkDAO linkDAO,
    WorkManager workManager
  ) {
    this.entityId = entityId;
    this.chainDAO = chainDAO;
    this.linkDAO = linkDAO;
    this.workManager = workManager;
  }

  @Override
  public void run() {
    try {
      eraseChain();

    } catch (Exception e) {
      log.error("{}:{} failed ({})",
        getClass().getSimpleName(), Thread.currentThread().getName(), e);
    }
  }

  /**
   Do Chain Erase Work
   If the Chain is empty, Eraseworker deletes the chain

   @throws Exception on failure
   */
  private void eraseChain() throws Exception {
    Collection<Link> links = linkDAO.readAll(Access.internal(), entityId);
    if (links.isEmpty())
      try {
        log.info("Found ZERO links in chainId={}; attempting to delete...", entityId);
        chainDAO.delete(Access.internal(), entityId);
      } catch (Exception e) {
        log.warn("Failed to delete chainId={}", entityId, e);
        workManager.stopChainErase(entityId);
      }
    else {
      List<String> linkIds = Lists.newArrayList();
      for (Link link : links) {
        linkIds.add(link.getId().toString());
      }
      log.info("Found {} links in chainId={}; linksIds={}; attempting to erase...", links.size(), entityId, CSV.join(linkIds));
      eraseLinks(links);
    }
  }

  /**
   Erase many links
   Eraseworker iterates on each link in the chain, reading in batches of a limited size

   @param links to erase
   @throws Exception on failure
   */
  private void eraseLinks(Iterable<Link> links) throws Exception {
    for (Link link : links)
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
  private void eraseLink(Link link) throws Exception {
    linkDAO.destroy(Access.internal(), link.getId());
    log.info("Erased Link #{}, destroyed child entities, and deleted s3 object {}", link.getId(), link.getWaveformKey());
  }

}
