// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.link;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

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
