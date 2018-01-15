import { moduleForComponent, test } from 'ember-qunit';
import hbs from 'htmlbars-inline-precompile';

moduleForComponent('chord-sequence-descriptor', 'Integration | Component | evaluation chord descriptor', {
  integration: true
});

test('it renders', function(assert) {
  // Set any properties with this.set('myProperty', 'value');
  // Handle any actions with this.on('myAction', function(val) { ... });

  this.render(hbs`{{chord-sequence-descriptor}}`);

  assert.equal(this.$().text().trim(), '');

  // Template block usage:
  this.render(hbs`
    {{#chord-sequence-descriptor}}
      template block text
    {{/chord-sequence-descriptor}}
  `);

  assert.equal(this.$().text().trim(), 'template block text');
});
