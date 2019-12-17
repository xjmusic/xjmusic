// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

// app
import apiOrmReducer from "./apiOrmReducer";
import apiAuthReducer from "./apiAuthReducer";
import workspaceReducer from "./workspaceReducer";

/**
 * Configure all reducers for the redux store
 */
export default {
  orm: apiOrmReducer,
  auth: apiAuthReducer,
  workspace: workspaceReducer,
}
