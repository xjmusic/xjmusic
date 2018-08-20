//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import {expect} from 'chai';
import {describe, it} from 'mocha';
import {setupRenderingTest} from 'ember-mocha';
import {render} from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

describe('ChordProgressionUsagesComponent', function () {
  setupRenderingTest();

  it('renders inline', async function () {
    await render(hbs`{{chord-progression-usages}}`);
    expect(this.$().text().trim()).to.contain('Sequence #');
    expect(this.$().text().trim()).to.contain('Pattern #');
  });

  it('renders block', async function () {
    await render(hbs`{{#chord-progression-usages}}dummy content{{/chord-progression-usages}}`);
    expect(this.$().text().trim()).to.contain('dummy content');
  });

});

