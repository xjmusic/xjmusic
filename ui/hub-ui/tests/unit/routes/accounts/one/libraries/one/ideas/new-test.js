import {moduleFor, test} from 'ember-qunit';

moduleFor('route:accounts/one/libraries/one/ideas/new', 'Unit | Route | accounts/one/libraries/one/ideas/new', {
  // Specify the other units that are required for this test.
  needs: ['service:config', 'service:auth', 'service:display']
});

test('it exists', function (assert) {
  let route = this.subject();
  assert.ok(route);
});
