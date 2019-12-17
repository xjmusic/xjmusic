// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

// vendor
import {createSelector} from "reselect";
// app
import {activationKey} from "../reducers/workspaceReducer";

const VALUE_IF_NOT_PRESENT = "";

/**
 Selects the active child of a given type and parentId

 * @param parentId of parent to get active child of
 * @param childType to get currently active in parent
 */
export default (parentId, childType) => {
  const key = activationKey(parentId, childType);

  return createSelector(
    [(state) => {
      return key in state.workspace.active ?
        state.workspace.active[key] :
        VALUE_IF_NOT_PRESENT;
    }],
    (value) => {
      return value;
    });
};
