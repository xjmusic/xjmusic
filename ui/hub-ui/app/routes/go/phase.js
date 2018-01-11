import Route from '@ember/routing/route';
import {later} from '@ember/runloop';
import {get} from '@ember/object';

export default Route.extend({

  /**
   * Route Model
   Redirect to target phase
   * @param params
   */
  model(params) {
    let self = this;
    self.store.findRecord('phase', params.phase_id)
      .then((phase) => {
        phase.get('pattern').then((pattern) => {
          pattern.get('library').then((library) => {
            library.get('account').then((account) => {
              later(() => {
                self.transitionTo('accounts.one.libraries.one.patterns.one.phases.one', account, library, pattern, phase);
              }, 250);
            });
          });
        });
      })
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('');
      });
  },


});
