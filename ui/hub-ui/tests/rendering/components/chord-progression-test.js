//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('ChordProgressionComponent', function (hooks) {
  setupRenderingTest(hooks);

  test('it renders inline', async function (assert) {
    await render(hbs`{{chord-progression}}`);

    assert.equals(this.textContent.trim()).to.equal('');
  });

  test('it renders block', async function (assert) {
    await render(hbs`{{#chord-progression}}dummy content{{/chord-progression}}`);

    assert.equals(this.textContent.trim()).to.equal('dummy content');
  });

});

