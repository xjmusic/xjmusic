import { moduleFor, test } from 'ember-qunit';

moduleFor('route:accounts/one/libraries/one/instruments/one/audios/one/events/one', 'Unit | Route | accounts/one/libraries/one/instruments/one/audios/one/events/one', {
  // Specify the other units that are required for this test.
  needs: ['service:display']
});

test('it exists', function(assert) {
  let route = this.subject();
  assert.ok(route);
});
