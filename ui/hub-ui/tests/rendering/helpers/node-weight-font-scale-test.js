//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { expect } from 'chai';
import { describe, it } from 'mocha';
import { setupRenderingTest } from 'ember-mocha';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

describe('NodeWeightFontScaleHelper', function() {
  setupRenderingTest();

  it('renders', async function() {
    await render(hbs`{{node-weight-font-scale '1234'}}`);
    expect(this.$().text().trim()).to.equal('weight-1234');
  });
});
