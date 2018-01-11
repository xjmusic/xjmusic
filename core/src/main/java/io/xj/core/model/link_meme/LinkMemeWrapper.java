// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.link_meme;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class LinkMemeWrapper extends EntityWrapper {

  // Link
  private LinkMeme linkMeme;

  public LinkMeme getLinkMeme() {
    return linkMeme;
  }

  public LinkMemeWrapper setLinkMeme(LinkMeme linkMeme) {
    this.linkMeme = linkMeme;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  public LinkMeme validate() throws BusinessException {
    if (this.linkMeme == null) {
      throw new BusinessException("Link is required.");
    }
    this.linkMeme.validate();
    return this.linkMeme;
  }

}
