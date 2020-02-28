/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('Integration | Helper | fraction', function (hooks) {
  setupRenderingTest(hooks);

  // Replace this with your real tests.
  test('it renders', async function (assert) {
    this.set('inputValue', '1234');

    await render(hbs`{{fraction inputValue}}`);

    assert.dom(this.element).hasText('1234');
  });

  test('it renders 1/4', async function (assert) {
    this.set('valueA', '1');
    this.set('valueB', '4');

    await render(hbs`{{fraction valueA valueB}}`);

    assert.contains(this.element.textContent.trim(), '1');
    assert.contains(this.element.textContent.trim(), '4');
  });

  test('it renders 1/666', async function (assert) {
    this.set('valueA', '1');
    this.set('valueB', '666');

    await render(hbs`{{fraction valueA valueB}}`);

    assert.contains(this.element.textContent.trim(), '1');
    assert.contains(this.element.textContent.trim(), '666');
  });

});
