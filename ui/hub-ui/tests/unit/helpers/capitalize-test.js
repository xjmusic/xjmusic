//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import { expect } from 'chai';
import { describe, it } from 'mocha';
import { setupTest } from 'ember-mocha';

describe('CapitalizeHelper', function () {
  setupTest();

  it('capitalizes a word', function () {
    let capitalize = this.owner.lookup('helper:capitalize');
    expect(capitalize.compute(['junk'])).to.equal('Junk');
  });

});
