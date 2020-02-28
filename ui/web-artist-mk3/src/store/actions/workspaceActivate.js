/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import {activationKey, WORKSPACE_ACTIVATE} from "../reducers/workspaceReducer";

/**
 Action to set the child of a parent as active in the workspace

 @param parentId that will have its child activated
 @param childType e.g. "libraries" or "program-sequences"
 @param childId to activate
 @returns {Object} action to dispatch
 */
export default (parentId, childType, childId) => {
  return {
    type: WORKSPACE_ACTIVATE,
    payload: {
      key: activationKey(parentId, childType),
      value: childId,
    }
  }
};
