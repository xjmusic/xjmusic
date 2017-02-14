// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.link_chord;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

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
   * Validate data.
   * @throws BusinessException if invalid.
   */
  public LinkChord validate() throws BusinessException{
    if (this.linkChord == null) {
      throw new BusinessException("linkChord is required.");
    }
    this.linkChord.validate();
    return this.linkChord;
  }

  @Override
  public String toString() {
    return "{" +
      LinkChord.KEY_ONE + ":" + this.linkChord +
      "}";
  }
}
