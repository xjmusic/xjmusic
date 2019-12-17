// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

import {activationKey, WORKSPACE_DEACTIVATE} from "../reducers/workspaceReducer";

/**
 Action to set no child active for a given parent in the workspace

 @param parentId that will have its child deactivated
 @param childType e.g. "libraries" or "program-sequences"
 @returns {Object} action to dispatch
 */
export default (parentId, childType) => {
  return {
    type: WORKSPACE_DEACTIVATE,
    payload: {
      key: activationKey(parentId, childType),
    }
  }
};
