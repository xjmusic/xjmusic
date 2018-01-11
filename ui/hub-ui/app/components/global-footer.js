// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { inject as service } from '@ember/service';

import Component from '@ember/component';

export default Component.extend({
  auth: service(),

  needs: ['application'],

  year: new Date().getUTCFullYear(),

  actions: {

  }
});


