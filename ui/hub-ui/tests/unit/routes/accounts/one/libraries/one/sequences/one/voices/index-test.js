// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import { moduleFor, test } from 'ember-qunit';

moduleFor('route:accounts/one/libraries/one/sequences/one/voices/index', 'Unit | Route | accounts/one/libraries/one/sequences/one/voices/index', {
  // Specify the other units that are required for this test.
  needs: ['service:display']
});

test('it exists', function(assert) {
  let route = this.subject();
  assert.ok(route);
});
