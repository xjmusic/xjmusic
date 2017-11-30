import { moduleFor, test } from 'ember-qunit';

moduleFor('route:platform/messages/new', 'Unit | Route | platform/messages/new', {
  // Specify the other units that are required for this test.
  needs: ['service:display']
});

test('it exists', function(assert) {
  let route = this.subject();
  assert.ok(route);
});
