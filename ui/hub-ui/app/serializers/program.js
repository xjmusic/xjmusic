// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

import JSONAPISerializer from './application';

export default JSONAPISerializer.extend({
  attrs: {
    patterns: {
      serialize: true
    },
    patternEvents: {
      serialize: true
    },
    programMemes: {
      serialize: true
    },
    sequences: {
      serialize: true
    },
    sequenceBindings: {
      serialize: true
    },
    sequenceBindingMemes: {
      serialize: true
    },
    sequenceChords: {
      serialize: true
    },
    voices: {
      serialize: true
    },
  }
});
