// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.chain;

import io.outright.xj.core.model.link.Link;

public interface Chain {
  /**
   * One Chain has Many Links
   * @return Link[]
   */
  Link[] Links();
}
