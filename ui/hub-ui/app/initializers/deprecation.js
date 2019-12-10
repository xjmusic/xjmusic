// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

import {registerDeprecationHandler} from '@ember/debug';

export function initialize() {
  registerDeprecationHandler((message, options /* next */) => {
    console.warn(message);
    console.debug(options);
    // next(message, options);
  });
}

export default {initialize};
