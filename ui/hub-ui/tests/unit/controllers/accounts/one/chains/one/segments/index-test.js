import { expect } from 'chai';
import { describe, it } from 'mocha';
import { setupTest } from 'ember-mocha';

describe('Unit | Controller | accounts/one/chains/one/segments/index', function() {
  setupTest('controller:accounts/one/chains/one/segments/index', {
    needs: ['service:config','service:player']
  });

  // Replace this with your real tests.
  it('exists', function() {
    let controller = this.subject();
    expect(controller).to.be.ok;
  });
});
