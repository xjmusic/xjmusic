// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.craft.impl;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.craft.Basis;
import io.outright.xj.core.craft.VoiceCraft;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 Voice craft for the current link includes events, arrangements, instruments, and audio
 */
public class VoiceCraftImpl implements VoiceCraft {
//  private final Logger log = LoggerFactory.getLogger(VoiceCraftImpl.class);
  private final Basis basis;

  @Inject
  public VoiceCraftImpl(
    @Assisted("basis") Basis basis
  /*-*/) throws BusinessException {
    this.basis = basis;
  }

  @Override
  public void craft() throws BusinessException {
    try {
      craftEvents();
      craftArrangements();
      craftInstruments();
      craftAudio();
      report();

    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException(
        String.format("Failed to do Voice %s-type craft for link #%s",
          basis.type(), basis.linkId().toString()), e);
    }
  }

  /**
   craft link events
   */
  private void craftEvents() throws Exception {
    // TODO craft events
  }

  /**
   craft link arrangements
   */
  private void craftArrangements() throws Exception {
    // TODO craft arrangements
  }

  /**
   craft link instruments
   */
  private void craftInstruments() throws Exception {
    // TODO craft instruments
  }

  /**
   craft link audio
   */
  private void craftAudio() throws Exception {
    // TODO craft audio
  }

  /**
   Report
   */
  private void report() {
    // TODO basis.report() anything else interesting from the craft operation
  }

}
