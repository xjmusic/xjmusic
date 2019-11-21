//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

import { expect } from 'chai';
import { describe, it } from 'mocha';
import { setupTest } from 'ember-mocha';

describe('OnlyIfHelper', function () {
  setupTest();

  it('returns value if condition is met', function () {
    let onlyIf = this.owner.lookup('helper:only-if');
    expect(onlyIf.compute([true, 'stinky'])).to.equal('stinky');
  });

  it('returns empty string if condition is not met', function () {
    let onlyIf = this.owner.lookup('helper:only-if');
    expect(onlyIf.compute([false, 'stinky'])).to.equal('');
  });

});
