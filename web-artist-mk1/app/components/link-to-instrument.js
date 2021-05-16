/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import Component from '@ember/component';
import {computed} from '@ember/object';

const LinkToInstrumentComponent = Component.extend({
  instrument: computed('instruments', 'instrumentId', function () {
    let instrument = '';
    this.instruments.forEach(search => {
      if (search.get('id') === this.instrumentId) {
        instrument = search;
      }
    });
    return instrument;
  }),
});

/**
 * Usage (e.g, in Handlebars):
 *
 *   {{link-to-instrument <instrumentId>}}
 */
LinkToInstrumentComponent.reopenClass({
  positionalParams: ['instruments', 'instrumentId']
});

export default LinkToInstrumentComponent;

