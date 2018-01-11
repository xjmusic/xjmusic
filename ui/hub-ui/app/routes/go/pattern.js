import Route from '@ember/routing/route';
import { later } from '@ember/runloop';
import {get} from '@ember/object';

export default Route.extend({

  /**
   * Route Model
   Redirect to target pattern
   * @param params
   */
  model(params) {
    let self = this;
    self.store.findRecord('pattern', params.pattern_id)
      .then((pattern) => {
          pattern.get('library').then((library) => {
            library.get('account').then((account) => {
              later(() => {
                self.transitionTo('accounts.one.libraries.one.patterns.one', account, library, pattern);
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
