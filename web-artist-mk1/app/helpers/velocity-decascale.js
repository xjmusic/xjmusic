/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import {helper} from '@ember/component/helper';

export function velocityDecascale(params/*, hash*/) {
  let value = params[0];
  if (value !== undefined && value !== null && value > 0) {
    if (value < 0.09) {
      return 1;
    } else if (value < 0.15) {
      return 2;
    } else {
      return Math.round(value * 10);
    }
  } else {
    return 0;
  }
}

export default helper(velocityDecascale);
