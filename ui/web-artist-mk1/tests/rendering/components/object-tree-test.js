/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('ObjectTreeComponent', function (hooks) {
  setupRenderingTest(hooks);

  test('it renders inline', async function (assert) {
    await render(hbs`{{object-tree}}`);

    assert.equals(this.textContent, '');
  });

  test('it renders block', async function (assert) {
    await render(hbs`{{#object-tree}}dummy content{{/object-tree}}`);

    assert.equals(this.textContent, 'dummy content');
  });

});

