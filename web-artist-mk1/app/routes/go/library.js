/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import Route from '@ember/routing/route';
import {get} from '@ember/object';
import {later} from '@ember/runloop';

export default Route.extend({

  /**
   * Route Model
   Redirect to target library
   * @param params
   */
  model(params) {
    let self = this;
    self.store.findRecord('library', params['library_id'])
      .then((library) => {
        library.get('account').then((account) => {
          later(() => {
            self.transitionTo('accounts.one.libraries.one', account, library);
          }, 200); // just enough time to hit back twice if you need to actually go backwards through this flow
        });
      })
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('');
      });
  },

});
