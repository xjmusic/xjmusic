import { moduleFor, test } from 'ember-qunit';

moduleFor('route:accounts/one', 'Unit | Route | accounts/one', {
  // Specify the other units that are required for this test.
  needs: ['service:display','service:auth']
});

test('it exists', function(assert) {
  let route = this.subject();
  assert.ok(route);
});
