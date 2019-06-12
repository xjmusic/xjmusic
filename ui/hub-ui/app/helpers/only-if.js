//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import {helper} from '@ember/component/helper';

/**
 * Usage: {{only-if <isConditionMet> <string>}}
 *
 * returns <string> only if <isConditionMet> true, else empty string
 *
 * @param params
 * @returns {string}
 */
export function onlyIf(params/*, hash*/) {
  let conditionIsMet = params[0];
  let conditionalValue = params[1];
  if (conditionIsMet) {
    return conditionalValue;
  } else {
    return '';
  }
}

export default helper(onlyIf);
