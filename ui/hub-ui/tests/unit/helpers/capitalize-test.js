
import { capitalize } from 'hub-ui/helpers/capitalize';
import { module, test } from 'qunit';

module('Unit | Helper | capitalize');

// Replace this with your real tests.
test('it works', function(assert) {
  let result = capitalize(["junk"]);
  assert.ok("Junk"===result);
});

