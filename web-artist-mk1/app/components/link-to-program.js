/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import Component from '@ember/component';
import {computed} from '@ember/object';

const LinkToProgramComponent = Component.extend({
  program: computed('programs', 'programId', function () {
    let program = '';
    this.programs.forEach(search => {
      if (search.get('id') === this.programId) {
        program = search;
      }
    });
    return program;
  }),
});

/**
 * Usage (e.g, in Handlebars):
 *
 *   {{link-to-program <programId>}}
 */
LinkToProgramComponent.reopenClass({
  positionalParams: ['programs', 'programId']
});

export default LinkToProgramComponent;

