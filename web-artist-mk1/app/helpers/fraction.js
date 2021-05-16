/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {helper} from '@ember/component/helper';
import {htmlSafe} from '@ember/string';

export function fraction(params/*, hash*/) {
  let numerator = params[0];
  let denominator = params[1];
  return htmlSafe(`<sup>${numerator}</sup>&frasl;<sub>${denominator}</sub>`);
}

export default helper(fraction);
