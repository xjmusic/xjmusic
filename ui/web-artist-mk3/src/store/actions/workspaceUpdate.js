/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import {WORKSPACE_UPDATE} from "../reducers/workspaceReducer";

/**
 Action to patch attributes of any entity

 @param {String} key to update
 @param {Object} value to update with
 @returns {Object} action to dispatch
 */
export default (key, value) => {
  return {
    type: WORKSPACE_UPDATE,
    payload: {
      key,
      value,
    }
  }
};
