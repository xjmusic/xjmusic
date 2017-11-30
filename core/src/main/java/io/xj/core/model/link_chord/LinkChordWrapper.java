// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.link_chord;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class LinkChordWrapper extends EntityWrapper {

  // LinkChord
  private LinkChord linkChord;

  public LinkChord getLinkChord() {
    return linkChord;
  }

  public LinkChordWrapper setLinkChord(LinkChord linkChord) {
    this.linkChord = linkChord;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  public LinkChord validate() throws BusinessException {
    if (this.linkChord == null) {
      throw new BusinessException("linkChord is required.");
    }
    this.linkChord.validate();
    return this.linkChord;
  }

}
