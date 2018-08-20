//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { expect } from 'chai';
import { describe, it } from 'mocha';
import { setupRenderingTest } from 'ember-mocha';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

describe('ActionIconHelper', function() {
  setupRenderingTest();

  it('renders', async function() {
    await render(hbs`{{action-icon "clone"}}`);
    expect(this.$().text().trim()).to.equal('files-o');
  });
});
