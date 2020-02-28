/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('ChordProgressionUsagesComponent', function (hooks) {
  setupRenderingTest(hooks);

  test('renders inline', async function (assert) {
    await render(hbs`{{chord-progression-usages}}`);

    assert.contains(this.element.textContent, 'Sequence #');
    assert.contains(this.element.textContent, 'Pattern #');
  });

  test('renders block', async function (assert) {
    await render(hbs`{{#chord-progression-usages}}dummy content{{/chord-progression-usages}}`);

    assert.contains(this.element.textContent, 'dummy content');
  });

});

