//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

import {helper} from '@ember/component/helper';

export function capitalize(params/*, hash*/) {
  let string = params[0];
  if (string !== undefined && string !== null && string.length > 0) {
    return string.charAt(0).toUpperCase() + string.slice(1);
  } else {
    return '';
  }
}

export default helper(capitalize);
