// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.chain;

import io.outright.xj.core.model.link.Link;

public class ChainImpl implements Chain {
  private Link[] links = new Link[0];

  public ChainImpl() {}

  @Override
  public Link[] Links() {
    return links;
  }
}
