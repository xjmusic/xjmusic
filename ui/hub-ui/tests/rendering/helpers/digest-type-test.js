//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { expect } from 'chai';
import { describe, it } from 'mocha';
import { setupRenderingTest } from 'ember-mocha';
import { render } from '@ember/test-helpers';
import hbs from 'htmlbars-inline-precompile';

describe('DigestTypeHelper', function() {
  setupRenderingTest();

  it('renders', async function() {
    await render(hbs`{{digest-type "1234"}}`);
    expect(this.$().text().trim()).to.equal('Digest1234');
  });
});
