import Route from '@ember/routing/route';
import { later } from '@ember/runloop';
import {get} from '@ember/object';

export default Route.extend({

  /**
   * Route Model
   Redirect to target audio
   * @param params
   */
  model(params) {
    let self = this;
    self.store.findRecord('audio', params.audio_id)
      .then((audio) => {
        audio.get('instrument').then((instrument) => {
          instrument.get('library').then((library) => {
            library.get('account').then((account) => {
              later(() => {
                self.transitionTo('accounts.one.libraries.one.instruments.one.audios.one', account, library, instrument, audio);
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
