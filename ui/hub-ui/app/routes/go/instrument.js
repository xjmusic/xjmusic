//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import Route from '@ember/routing/route';
import { later } from '@ember/runloop';
import { get } from '@ember/object';

export default Route.extend({

  /**
   * Route Model
   Redirect to target instrument
   * @param params
   */
  model(params) {
    let self = this;
    self.store.findRecord('instrument', params.instrument_id)
      .then((instrument) => {
        instrument.get('library').then((library) => {
          library.get('account').then((account) => {
            later(() => {
              self.transitionTo('accounts.one.libraries.one.instruments.one', account, library, instrument);
            }, 250);
          });
        });
      })
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('');
      });
  },


});
