//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import {module, test} from 'qunit';
import {setupRenderingTest} from 'ember-qunit';
import {render} from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('ActionIconHelper', function (hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function (assert) {
    await render(hbs`{{action-icon "clone"}}`);

    assert.equals(this.element.textContent, 'files-o');
  });
});
