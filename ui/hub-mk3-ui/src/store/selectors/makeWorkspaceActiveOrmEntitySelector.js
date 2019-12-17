// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

// vendor
import {createSelector} from "reselect";
// app
import orm from "../orm";
import {toEntityName} from "../../util";
import {activationKey} from "../reducers/workspaceReducer";

/**
 Selects an ORM model for the active child of a given type and parentId

 * @param parentId of parent to get active child of
 * @param childType to get ORM Model of the entity record that's currently active for the parent id
 */
export default (parentId, childType) => {
  const key = activationKey(parentId, childType);
  const entityName = toEntityName(childType);

  return createSelector(
    [
      (state) => {
        return key in state.workspace.active ?
          state.workspace.active[key] :
          false;
      },
      (state) => {
        return orm.session(state.orm)[entityName];
      }
    ],
    (id, model) => {
      return !!id ? model.withId(id) : null;
    })
};
