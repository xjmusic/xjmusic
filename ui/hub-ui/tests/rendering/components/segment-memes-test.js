//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import {expect} from 'chai';
import {describe, it} from 'mocha';
import {setupRenderingTest} from 'ember-mocha';
import {render} from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

describe('SegmentMemesComponent', function () {
  setupRenderingTest();

  it('renders inline', async function () {
    await render(hbs`{{segment-memes}}`);
    expect(this.$().text().trim()).to.equal('');
  });

  it('renders block', async function () {
    await render(hbs`{{#segment-memes}}dummy content{{/segment-memes}}`);
    expect(this.$().text().trim()).to.equal('dummy content');
  });

});

