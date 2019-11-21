//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

import JSONAPISerializer from './application';

export default JSONAPISerializer.extend({
  attrs: {
    instrumentMemes: {
      serialize: true
    },
    audios: {
      serialize: true
    },
    audioChords: {
      serialize: true
    },
    audioEvents: {
      serialize: true
    },
  }
});
