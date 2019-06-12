//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Route from '@ember/routing/route';
import {get} from '@ember/object';
import {inject as service} from '@ember/service';
import {Promise} from 'rsvp';

export default Route.extend({

  auth: service(),

  config: service(),

  display: service(),

  // for persisting the auto-refresh interval
  refreshInterval: null,

  // # of seconds between auto-refresh
  refreshSeconds: 5,

  /**
   * Model is a promise because it depends on promised configs
   * @returns {Ember.RSVP.Promise}
   */
  model() {
    let self = this;
    return new Promise((resolve, reject) => {
      this.auth.promise.then(
        () => {
          resolve(self.store.query('work', {})
            .catch((error) => {
              get(self, 'display').error(error);
            }));
        },

        err => {
          reject(err);
          console.error(err);
        });

    });
  },


  /**
   On route deactivation, clear the refresh interval
   */
  deactivate() {
    clearInterval(this.refreshInterval);
    console.log("...auto-refresh Stopped.");
  },

  /**
   On route activation, set the refresh interval
   */
  activate() {
    console.log("Started auto-refresh...");
    let self = this;
    self.set('refreshInterval', setInterval(function () {
      console.log("Auto-refresh now!");
      self.send("sessionChanged");
    }, self.refreshSeconds * 1000));
  }

});
