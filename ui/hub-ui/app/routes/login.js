//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Route from '@ember/routing/route';

import { get } from '@ember/object';

import { Promise as EmberPromise } from 'rsvp';
import { inject as service } from '@ember/service';

export default Route.extend({

  auth: service(),

  config: service(),

  /**
   * Model is a promise because it depends on promised configs
   * @returns {Ember.RSVP.Promise}
   */
  model() {
    return new EmberPromise((resolve, reject) => {
      let self = this;
      get(this, 'config').promises.config.then(
        () => {
          resolve(self.resolvedModel());
        },
        (error) => {
          reject('Could not get login route', error);
        }
      );
    });
  },

  /**
   * Resolved (with configs) model
   * @returns {*}
   */
  resolvedModel() {
    if (this.get('auth.isUser')) {
      this.transitionTo('');
    }
  },

  breadCrumb: null
});
