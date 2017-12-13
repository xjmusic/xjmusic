// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.link;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class LinkWrapper extends EntityWrapper {

  // Link
  private Link link;

  public Link getLink() {
    return link;
  }

  public LinkWrapper setLink(Link link) {
    this.link = link;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  @Override
  public Link validate() throws BusinessException {
    if (this.link == null) {
      throw new BusinessException("Link is required.");
    }
    this.link.validate();
    return this.link;
  }

}
