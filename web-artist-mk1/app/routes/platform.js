/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import Route from '@ember/routing/route';

import {inject as service} from '@ember/service';

export default Route.extend({

  auth: service(),

  config: service(),

  activate() {
    let self = this;
    this.auth.promise.then(
      (auth) => {
        if (!(auth.get('isAdmin') || auth.get('isEngineer'))) {
          console.error('Not authorized to access platform route');
          self.transitionTo('');
        } else {
          console.debug('Administrator Authorized OK');
        }
      },

      (error) => {
        console.error(error);
        self.transitionTo('');
      }
    );
  }
});
