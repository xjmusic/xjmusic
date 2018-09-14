import { expect } from 'chai';
import { describe, it } from 'mocha';
import { setupComponentTest } from 'ember-mocha';
import hbs from 'htmlbars-inline-precompile';

describe('Integration | Helper | fraction', function() {
  setupComponentTest('fraction', {
    integration: true
  });

  it('renders 1/4', function() {
    this.render(hbs`{{fraction 1 4}}`);
    expect(this.$().text().trim()).to.contain('1');
    expect(this.$().text().trim()).to.contain('4');
  });

  it('renders 1/666', function() {
    this.render(hbs`{{fraction 1 666}}`);
    expect(this.$().text().trim()).to.contain('1');
    expect(this.$().text().trim()).to.contain('666');
  });

});

