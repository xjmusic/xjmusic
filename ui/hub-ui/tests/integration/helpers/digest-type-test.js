
import { moduleForComponent, test } from 'ember-qunit';
import hbs from 'htmlbars-inline-precompile';

moduleForComponent('digest-type', 'helper:digest-type', {
  integration: true
});

// Replace this with your real tests.
test('it renders', function(assert) {
  this.set('inputValue', '1234');

  this.render(hbs`{{digest-type inputValue}}`);

  assert.equal(this.$().text().trim(), 'Digest1234');
});

