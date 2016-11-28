// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.credit;

import io.outright.xj.core.model.user.User;

public interface Credit {
  /**
   * Credit belongs to a User
   * @return User
   */
  User User();
}
