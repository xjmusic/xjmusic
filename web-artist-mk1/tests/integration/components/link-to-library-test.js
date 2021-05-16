/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import { module, test } from 'qunit';
import { setupRenderingTest } from 'ember-qunit';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

module('Integration | Component | link-to-library', function(hooks) {
  setupRenderingTest(hooks);

  test('it renders', async function(assert) {
    // Set any properties with this.set('myProperty', 'value');
    // Handle any actions with this.set('myAction', function(val) { ... });

    await render(hbs`<LinkToLibrary />`);

    assert.equal(this.element.textContent.trim(), '');

    // Template block usage:
    await render(hbs`
      <LinkToLibrary>
        template block text
      </LinkToLibrary>
    `);

    assert.equal(this.element.textContent.trim(), 'template block text');
  });
});
