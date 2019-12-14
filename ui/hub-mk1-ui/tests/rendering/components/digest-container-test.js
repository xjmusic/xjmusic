// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('DigestContainerComponent', function (hooks) {
  setupRenderingTest(hooks);

  test('renders inline', async function (assert) {
    await render(hbs`{{digest-container}}`);

    assert.equals(this.element.textContent).to.equal('');
  });

  test('renders block', async function (assert) {
    await render(hbs`{{#digest-container}}dummy content{{/digest-container}}`);

    assert.equals(this.element.textContent).to.equal('dummy content');
  });

});

