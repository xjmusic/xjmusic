// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import { get } from '@ember/object';
import Service, { inject as service } from '@ember/service';

export default Service.extend({

  flashMessages: service(),

  error(error) {
    if (error instanceof String) {
      get(this, 'flashMessages').danger('Error: ' + error);
    } else if ('errors' in error && error.errors.length >= 1) {
      get(this, 'flashMessages').danger('Error: ' + error.errors[0].detail);
    } else if ('message' in error) {
      get(this, 'flashMessages').danger('Error: ' + error.message);
    } else {
      get(this, 'flashMessages').danger('Error: ' + error.toString());
    }
  },

  success(message) {
    get(this, 'flashMessages').success(message);
  },

  warning(message) {
    get(this, 'flashMessages').warning(message);
  }

});
