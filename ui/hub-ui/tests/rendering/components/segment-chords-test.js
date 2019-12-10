// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('SegmentChordsComponent', function (hooks) {
  setupRenderingTest(hooks);

  test('it renders inline', async function (assert) {
    await render(hbs`{{segment-chords}}`);

    assert.equals(this.textContent, '');
  });

  test('it renders block', async function (assert) {
    await render(hbs`{{#segment-chords}}dummy content{{/segment-chords}}`);

    assert.equals(this.textContent, 'dummy content');
  });

});

