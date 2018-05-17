// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import Application from '@ember/application';
import { run } from '@ember/runloop';
import { initialize } from 'hub-ui/instance-initializers/auth';
import { module, test } from 'qunit';
import destroyApp from '../../helpers/destroy-app';

module('Unit | Instance Initializer | authorizer', {
  beforeEach: function() {
    run(() => {
      this.application = Application.create();
      this.appInstance = this.application.buildInstance();
    });
  },
  afterEach: function() {
    run(this.appInstance, 'destroy');
    destroyApp(this.application);
  }
});

// Replace this with your real tests.
test('it works', function(assert) {
  initialize(this.appInstance);

  // you would normally confirm the results of the initializer here
  assert.ok(true);
});
