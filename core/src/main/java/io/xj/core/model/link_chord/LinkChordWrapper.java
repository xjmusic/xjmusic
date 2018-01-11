// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.link_chord;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.EntityWrapper;

import java.util.Objects;

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
    if (Objects.isNull(linkChord)) {
      throw new BusinessException("linkChord is required.");
    }
    linkChord.validate();
    return linkChord;
  }

}
