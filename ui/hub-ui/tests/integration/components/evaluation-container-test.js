import {moduleForComponent, test} from 'ember-qunit';
import hbs from 'htmlbars-inline-precompile';

moduleForComponent('evaluation-container', 'Integration | Component | evaluation container', {
  integration: true,
});

test('it renders', function (assert) {
  // Set any properties with this.set('myProperty', 'value');
  // Handle any actions with this.on('myAction', function(val) { ... });

  this.render(hbs`{{evaluation-container}}`);

  assert.equal(this.$().text().trim(), '');

  // Template block usage:
  this.render(hbs`
    {{#evaluation-container}}
      template block text
    {{/evaluation-container}}
  `);

  assert.equal(this.$().text().trim(), 'template block text');
});
