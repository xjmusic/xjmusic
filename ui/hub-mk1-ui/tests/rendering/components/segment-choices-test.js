// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('EditProgramAttributesComponent', function (hooks) {
  setupRenderingTest(hooks);

  test('it renders inline', async function (assert) {
    await render(hbs`{{segment-choices}}`);

    assert.equals(this.element.textContent, '');
  });

  test('it renders block', async function (assert) {
    await render(hbs`{{#segment-choices}}dummy content{{/segment-choices}}`);

    assert.equals(this.element.textContent, 'dummy content');
  });

});

