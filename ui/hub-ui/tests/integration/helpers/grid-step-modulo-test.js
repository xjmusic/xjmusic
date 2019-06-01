import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('Integration | Helper | grid-step-modulo', function(hooks) {
  setupRenderingTest(hooks);

  // Replace this with your real tests.
  test('it renders', async function(assert) {
    this.set('inputValue', '1234');

    await render(hbs`{{grid-step-modulo inputValue}}`);

    assert.equal(this.element.textContent.trim(), '1234');
  });

  test('it returns "measure" at the top of a measure', async function(assert) {
    this.set('valueA', '16');
    this.set('valueB','4');
    this.set('valueC','4');

    await render(hbs`{{grid-step-modulo valueA valueB valueC}}`);

    assert.equal(this.element.textContent.trim(), 'measure');
  });

  test('it returns "beat" at the top of a beat', async function(assert) {
    this.set('valueA', '12');
    this.set('valueB','4');
    this.set('valueC','4');

    await render(hbs`{{grid-step-modulo valueA valueB valueC}}`);

    assert.equal(this.element.textContent.trim(), 'beat');
  });

  test('it returns "step" at the top of a step', async function(assert) {
    this.set('valueA', '11');
    this.set('valueB','4');
    this.set('valueC','4');

    await render(hbs`{{grid-step-modulo valueA valueB valueC}}`);

    assert.equal(this.element.textContent.trim(), 'step');
  });

});
