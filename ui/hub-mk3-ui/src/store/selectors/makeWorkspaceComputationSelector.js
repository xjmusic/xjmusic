// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

import {createSelector} from "reselect";

/**
 Selects workspace value by key

 @param key {String} key to get value of
 @param computeFn {Function} to execute on value for final computed value
 @returns {Object}
 */
export default (key, computeFn) => {
  return createSelector(
    [(state) => {
      return state.workspace[key];
    }],
    (value) => {
      return computeFn(value);
    });
};
