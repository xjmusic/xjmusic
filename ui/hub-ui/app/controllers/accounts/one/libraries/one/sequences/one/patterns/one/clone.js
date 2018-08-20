//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import { get } from '@ember/object';
import { inject as service } from '@ember/service';
import Controller from '@ember/controller';

export default Controller.extend({
  config: service(),

  actions: {

    setPatternSequence(sequence) {
      let model = get(this, 'model.pattern');
      model.set('sequence', sequence);
      let sequenceType = sequence.get('type');
      switch (sequenceType) {
        case 'Macro':
        case 'Main':
          this.send('selectPatternType', sequenceType);
          break;

        default:
          this.send('selectPatternType', 'Loop');
          break;
      }
    },

    selectPatternType(type) {
      get(this, 'model.pattern').set('type', type);
    },

  }

});
