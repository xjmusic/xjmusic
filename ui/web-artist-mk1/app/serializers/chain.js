/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import JSONAPISerializer from './application';

export default JSONAPISerializer.extend({
  attrs: {
    chainConfigs: {
      serialize: true
    },
    chainBindings: {
      serialize: true
    },
    segments: {
      serialize: false
    },
  }
});
