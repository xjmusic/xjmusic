// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Route from '@ember/routing/route';
import {get} from '@ember/object';
import {inject as service} from '@ember/service';
import {Promise} from 'rsvp';

export default Route.extend({

  auth: service(),

  config: service(),

  display: service(),

  /**
   * Model is a promise because it depends on promised configs
   * @returns {Ember.RSVP.Promise}
   */
  model() {
    let self = this;
    return new Promise((resolve, reject) => {
      this.get('auth').promise.then(
        () => {
          resolve(self.store.query('platformMessage', {})
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

  breadCrumb: null,

});
