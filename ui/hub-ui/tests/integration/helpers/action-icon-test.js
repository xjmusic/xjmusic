
import { moduleForComponent, test } from 'ember-qunit';
import hbs from 'htmlbars-inline-precompile';

moduleForComponent('action-icon', 'helper:action-icon', {
  integration: true
});

// Replace this with your real tests.
test('it renders', function(assert) {
  this.set('inputValue', 'clone');

  this.render(hbs`{{action-icon inputValue}}`);

  assert.equal(this.$().text().trim(), 'files-o');
});

