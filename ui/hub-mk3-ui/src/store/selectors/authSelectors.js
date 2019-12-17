// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

// vendor
import {createSelector} from "reselect";
// app
import orm from "../orm";

export const
  selectUserAuthId = createSelector(
    state => state.auth,
    auth => auth.userAuthId
  ),
  selectRoles = createSelector(
    state => state.auth,
    auth => auth.roles.split(",")
  ),
  selectAccounts = createSelector(
    state => state.auth,
    auth => auth.accounts
  ),
  selectUserId = createSelector(
    state => state.auth,
    auth => auth.userId
  ),
  selectUser = createSelector(
    state => state.orm,
    selectUserId,
    (ormState, userId) => {
      const sess = orm.session(ormState);
      return sess.User.withId(userId);
    }
  );
