import { moduleFor, test } from 'ember-qunit';

moduleFor('route:accounts/one/libraries/one/patterns/one/phases/one/events/one/destroy', 'Unit | Route | accounts/one/libraries/one/patterns/one/phases/one/events/one/destroy', {
  // Specify the other units that are required for this test.
  needs: ['service:display','service:auth']
});

test('it exists', function(assert) {
  let route = this.subject();
  assert.ok(route);
});
