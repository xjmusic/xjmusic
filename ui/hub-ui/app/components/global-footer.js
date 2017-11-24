// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import { inject as service } from '@ember/service';

import Component from '@ember/component';

export default Component.extend({
  auth: service(),

  needs: ['application'],

  year: new Date().getUTCFullYear(),

  actions: {

  }
});


