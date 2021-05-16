/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('EntityActionsComponent', function (hooks) {
  setupRenderingTest(hooks);

  test('it renders inline', async function (assert) {
    await render(hbs`{{entity-actions}}`);

    assert.equals(this.textContent.trim(), '');
  });

  test('it renders block', async function (assert) {
    await render(hbs`{{#entity-actions}}dummy content{{/entity-actions}}`);

    assert.equals(this.textContent.trim(), 'dummy content');
  });

});

