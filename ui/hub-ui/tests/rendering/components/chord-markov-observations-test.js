//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('ChordMarkovObservationsComponent', function (hooks) {
  setupRenderingTest(hooks);

  test('renders inline', async function (assert) {
    await render(hbs`{{chord-markov-observations}}`);

    assert.equals(this.textContent.trim()).to.equal('');
  });

  test('renders block', async function (assert) {
    await render(hbs`{{#chord-markov-observations}}dummy content{{/chord-markov-observations}}`);

    assert.equals(this.textContent.trim()).to.equal('dummy content');
  });

});

