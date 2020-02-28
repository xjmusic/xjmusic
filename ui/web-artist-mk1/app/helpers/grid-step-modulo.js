/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {helper} from '@ember/component/helper';

// Possible return values
const MEASURE = 'measure';
const BEAT = 'beat';
const STEP = 'step';

/**
 * Grid step modulo type, based on the current step, and the meter super/sub values.
 *
 * Returns one of these three values, based on where the current step is:
 *   `measure`  at the top of a measure
 *      `beat`  at the top of a beat
 *      `step`  at the smallest unit
 *
 * Usage:
 *   {{grid-step-modulo <step> <meterSuper> <meterSub>}}
 * i.e.:
 *   <div class="step-{{grid-step-modulo <step> <meterSuper> <meterSub>}}">...</div>
 *
 * @param {[number,number,number]} params
 * @returns {*}
 */
export function gridStepModulo(params/*, hash*/) {
  let step = params[0];
  let meterSuper = params[1];
  let meterSub = params[2];
  let onBeat = 0 === step % meterSub;
  let onMeasure = onBeat ? 0 === (step / meterSub) % meterSuper : false;
  if (onMeasure) {
    return MEASURE;
  } else if (onBeat) {
    return BEAT;
  } else {
    return STEP;
  }
}

export default helper(gridStepModulo);
