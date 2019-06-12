//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('DigestTypeHelper', function (hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function (assert) {
    await render(hbs`{{digest-type "1234"}}`);

    assert.equals(this.element.textContent, 'Digest1234');
  });
});
