import { moduleFor, test } from 'ember-qunit';

moduleFor('route:accounts/new', 'Unit | Route | accounts/new', {
  // Specify the other units that are required for this test.
  needs: ['service:display']
});

test('it exists', function(assert) {
  let route = this.subject();
  assert.ok(route);
});
