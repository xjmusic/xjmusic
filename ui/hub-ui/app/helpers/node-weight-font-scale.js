// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import {helper} from '@ember/component/helper';

const CSS_CLASS_PREFIX = 'weight-';

export function nodeWeightFontScale(params/*, hash*/) {
  return CSS_CLASS_PREFIX + params[0];
}

export default helper(nodeWeightFontScale);
