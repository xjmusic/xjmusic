//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('NodeWeightFontScaleHelper', function (hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function (assert) {
    await render(hbs`{{node-weight-font-scale '1234'}}`);

    assert.equals(this.element.textContent, 'weight-1234');
  });
});
