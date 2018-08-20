//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import { helper } from '@ember/component/helper';

let icons = {
  "edit": "edit",
  "clone": "files-o",
  "destroy": "times"
};

export function actionIcon(params/*, hash*/) {
  let key = params[0].trim().toLowerCase();
  if (key in icons) {
    return icons[key];
  }

  return null;
}

export default helper(actionIcon);
