//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('MemeUsageComponent', function (hooks) {
  setupRenderingTest(hooks);

  test('it renders inline', async function (assert) {
    await render(hbs`{{meme-usage}}`);

    assert.equals(this.element.textContent, '');
  });

  test('it renders block', async function (assert) {
    await render(hbs`{{#meme-usage}}dummy content{{/meme-usage}}`);

    assert.equals(this.element.textContent, 'dummy content');
  });

});

