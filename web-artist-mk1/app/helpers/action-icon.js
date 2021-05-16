/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import {helper} from '@ember/component/helper';

let icons = {
  "edit": "edit",
  "upload": "upload",
  "clone": "files-o",
  "move": "angle-double-right",
  "destroy": "times",
  "add": "plus-circle",
};

export function actionIcon(params/*, hash*/) {
  let key = params[0].trim().toLowerCase();
  if (key in icons) {
    return icons[key];
  }

  return null;
}

export default helper(actionIcon);
