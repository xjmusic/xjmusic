// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import { moduleFor, test } from 'ember-qunit';

moduleFor('route:platform/messages', 'Unit | Route | platform/messages', {
  // Specify the other units that are required for this test.
  needs: ['service:auth','service:config','service:display']
});

test('it exists', function(assert) {
  let route = this.subject();
  assert.ok(route);
});
