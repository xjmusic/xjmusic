// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.credit;

import io.outright.xj.core.model.user.User;

public class CreditImpl implements Credit {
  private User user = null;

  public CreditImpl(
    User _user
  ) {
    user = _user;
  }

  @Override
  public User User() {
    return user;
  }
}
