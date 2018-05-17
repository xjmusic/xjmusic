// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Component from '@ember/component';

/**
 * Displays a Chain state-change U.I.
 */
const EntityActionsComponent = Component.extend({
  willRender() {
    if (typeof this.get("show") === "string") {
      this.set("show", this.get("show").split("|"));
    }

/*
    console.log("EntityActionsComponent", {
      base: this.get("base"),
      show: this.get("show"),
      model0: this.get("modelA"),
      model1: this.get("modelB"),
      model2: this.get("modelC"),
      model3: this.get("modelD"),
      model4: this.get("modelE"),
      model5: this.get("modelF")
    });
*/
  },

  actions: {

    do(act) {
      console.log("gonna do", act);
    }

  }

});

/**
 * Usage (e.g, in Handlebars, where chain model is "myChainModel"):
 *
 *   {{chain-state-change myChainModel}}
 */
EntityActionsComponent.reopenClass({
  positionalParams: ['base', 'show', 'modelA', 'modelB', 'modelC', 'modelD', 'modelE', 'modelF']
});

export default EntityActionsComponent;
